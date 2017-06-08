package com.ilya.ivanov.controller;

import com.google.common.collect.Lists;
import com.ilya.ivanov.Updater;
import com.ilya.ivanov.data.model.file.FileEntity;
import com.ilya.ivanov.data.model.user.UserEntity;
import com.ilya.ivanov.security.session.NewSessionEvent;
import com.ilya.ivanov.security.session.Session;
import com.ilya.ivanov.security.session.SessionManager;
import com.ilya.ivanov.service.file.DownloadContext;
import com.ilya.ivanov.service.file.FileService;
import com.ilya.ivanov.service.file.SimpleDownloadContext;
import com.ilya.ivanov.service.search.SearchService;
import com.ilya.ivanov.view.CSSDriver;
import com.ilya.ivanov.view.ViewManager;
import impl.org.controlsfx.skin.DecorationPane;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.apache.commons.lang3.ArrayUtils;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.dialog.ExceptionDialog;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javafx.concurrent.Worker.State;

/**
 * Main stage controller
 * Created by ilya on 5/20/17.
 */
public class MainController implements ApplicationListener<NewSessionEvent> {
    @FXML private VBox root;
    @FXML private Button addButton;
    @FXML private Button removeButton;
    @FXML private Text userNameLetter;
    @FXML private StackPane searchFieldPane;
    @FXML private TextField searchField;
    @FXML private VBox workingTreeLayout;
    @FXML private TreeTableView<FileEntity> workingTreeTable;
    @FXML private StatusBar workingStatusBar;
    @FXML private VBox searchLayout;
    @FXML private TableView<FileEntity> searchTable;
    @FXML private Text searchResultText;
    @FXML private Pagination searchPagination;

    private Action removeFilesAction;
    private Action addFilesAction;
    private DialogAction<String, NamingDialog> addDirectoryAction;
    private DialogAction<String, NamingDialog> renameFileAction;
    private Action openFileAction;
    private DialogAction<? extends DownloadContext, DownloadingDialog> downloadFileAction;

    private Alert removeConfirmation;
    private ContextMenu mainTableMenu;

    private SessionManager sessionManager;
    private ViewManager viewManager;
    private FileService fileService;
    private SearchService searchService;
    private CSSDriver cssDriver;

    /** JavaFX initialize */
    public void initialize() {
        root.addEventHandler(KeyEvent.KEY_TYPED, event -> {
            if (event.getCode().equals(KeyCode.F5)) {
                event.consume();
                refresh();
            }
        });
        Platform.runLater(this::initializeWorkingLayout);
    }

    @PostConstruct
    private void initializePostConstruct() {
        this.initializeActions();
    }

    private void initializeActions() {
        this.assignActions();
        this.initializeContextMenus();
        this.initializeButtons();
        this.initializeActionsLogic();
    }

    private void assignActions() {
        this.removeFilesAction = removeFilesAction();
        this.addFilesAction = addFilesAction();
        this.addDirectoryAction = addDirectoryAction();
        this.renameFileAction = renameFileAction();
        this.openFileAction = openFileAction();
        this.downloadFileAction = downloadFileAction();
    }

    public void initAfterDI() {
        this.checkDependencies();
        this.initializeSearchLayout();
        this.initializeDialogs();
    }

    private void checkDependencies() {
        Objects.requireNonNull(viewManager, "View manager cannot be null");
        Objects.requireNonNull(sessionManager, "Session manager cannot be null");
        Objects.requireNonNull(fileService, "File repository cannot be null");
    }

    private void initializeWorkingLayout() {
        this.initializeWorkingTreeTable();
        this.initializeWorkingStatusBar();
    }

    private void initializeWorkingStatusBar() {
        workingStatusBar = new StatusBar();
        workingTreeLayout.getChildren().add(workingStatusBar);
    }

    private void initializeWorkingTreeTable() {
        workingTreeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        workingTreeTable.setRowFactory(param -> {
            final TreeTableRow<FileEntity> row = new TreeTableRow<>();
            this.tuneTableRow(row, () -> param.getSelectionModel().clearSelection());
            row.setContextMenu(mainTableMenu);
            return row;
        });
    }

    private void initializeSearchLayout() {
        searchLayout.visibleProperty().bind(workingTreeLayout.visibleProperty().not());
        this.initializeSearchField();
        this.initializeSearchingTable();
        this.initializeSearchPagination();
    }

    private void initializeSearchField() {
        searchField = TextFields.createClearableTextField();
        TextFields.bindAutoCompletion(searchField, param -> searchService.getQueries());
        searchField.setPadding(new Insets(7., 5., 5., 7.));
        HBox.setMargin(searchField, new Insets(10., 20., 20., 10.));
        searchField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                searchField.setText("");
                searchTable.getItems().clear();
                workingTreeLayout.setVisible(true);
            }
        });
        searchField.setOnAction(this::handleSearch);
        searchFieldPane.getChildren().add(searchField);
    }

    private void initializeSearchingTable() {
        searchTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        searchTable.setRowFactory(param -> {
            final TableRow<FileEntity> row = new TableRow<>();
            this.tuneTableRow(row, () -> param.getSelectionModel().clearSelection());
            return row;
        });
    }

    @SuppressWarnings("unchecked")
    private void tuneTableRow(IndexedCell<FileEntity> row, Runnable onEmpty) {
        row.setOnMouseClicked(event -> {
            final IndexedCell<FileEntity> source = (IndexedCell<FileEntity>) event.getSource();
            if (event.getClickCount() == 1) {
                if (event.getButton().equals(MouseButton.SECONDARY))
                    source.updateSelected(true);
                if (row.isEmpty() && onEmpty != null)
                    onEmpty.run();
            } else if (event.getClickCount() == 2 && !row.isEmpty() && row.getItem().isFile()) {
                openFileAction.handle(new ActionEvent());
            }
        });
    }

    private void initializeSearchPagination() {
        searchResultText.textProperty().bind(Bindings.createStringBinding(
                        () -> searchService.getCurrentNumberOfElements().toString(),
                        searchService.currentNumberOfElementsProperty()));
        searchPagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            final List<FileEntity> files = searchService.getSlice(newValue.intValue());
            searchTable.getItems().setAll(files);
        });
    }

    private void initializeDialogs() {
        removeConfirmation = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?") {
            {
                setTitle("Remove");
                setHeaderText("Confirm operation!");
                initModality(Modality.APPLICATION_MODAL);
                initOwner(getWindow());
                setResultConverter(param -> param.equals(ButtonType.OK) ? param : null);
            }
        };
        NamingDialog addDirectoryDialog = new NamingDialog(getWindow(), "Add new directory", "Directory name");
        addDirectoryDialog.setInitialText("New directory");
        addDirectoryAction.setDialog(addDirectoryDialog);

        final NamingDialog renameFileDialog = new NamingDialog(getWindow(), "Rename", "New name");
        renameFileAction.setDialog(renameFileDialog);

        final DownloadingDialog downloadingDialog = new DownloadingDialog(getWindow(), "Download", "Path");
        downloadFileAction.setDialog(downloadingDialog);
    }

    private void initializeContextMenus() {
        mainTableMenu =
                ActionUtils.createContextMenu(Lists.newArrayList(
                        openFileAction,
                        downloadFileAction,
                        addDirectoryAction,
                        addFilesAction,
                        renameFileAction,
                        removeFilesAction
                ));
    }

    private void initializeActionsLogic() {
        this.initializeRemoveFilesActionLogic();
        this.initializeAddFilesActionLogic();
        this.initializeOpenActionLogic();
        this.initializeAddDirectoryActionLogic();
        this.initializeRenameFileActionLogic();
    }

    private void initializeRemoveFilesActionLogic() {
        Predicate<ObservableList<?>> emptyPredicate = (l) -> l.size() == 0;
        final BooleanBinding removeActionLogic = or2Tables(emptyPredicate);
        removeFilesAction.disabledProperty().bind(removeActionLogic);
    }

    private void initializeAddFilesActionLogic() {
        final BooleanBinding addActionLogic = getPaneVisibleBinding(searchLayout);
        addFilesAction.disabledProperty().bind(addActionLogic);
    }

    @SuppressWarnings("unchecked")
    private void initializeOpenActionLogic() {
        Predicate<ObservableList<?>> singleSelectionPredicate = (l) -> l.size() == 1;
        final BooleanBinding openActionLogic = or2Tables(singleSelectionPredicate).not()
                .or(getWorkingTableSelectionBinding((l) -> {
                    final ObservableList<TreeItem<FileEntity>> items = (ObservableList<TreeItem<FileEntity>>) l;
                    return items.isEmpty() || items.get(0).getValue().isDirectory();
                }))
                .or(getSearchTableSelectionBinding((l) -> {
                    final ObservableList<FileEntity> items = (ObservableList<FileEntity>) l;
                    return items.isEmpty() || items.get(0).isDirectory();
                }));
        openFileAction.disabledProperty().bind(openActionLogic);
    }

    private void initializeAddDirectoryActionLogic() {
        addDirectoryAction.disabledProperty().bind(searchLayout.visibleProperty());
    }

    private void initializeRenameFileActionLogic() {
        Predicate<ObservableList<?>> singleSelectionPredicate = (l) -> l.size() == 1;
        final BooleanBinding renameFileActionLogic = or2Tables(singleSelectionPredicate);
        renameFileAction.disabledProperty().bind(renameFileActionLogic.not());
    }

    private <T> BooleanBinding or2Tables(Predicate<ObservableList<? extends T>> predicate) {
        final BooleanBinding WorkingTableSelection = getWorkingTableSelectionBinding(predicate);
        final BooleanBinding SearchTableSelection = getSearchTableSelectionBinding(predicate);
        return SearchTableSelection.or(WorkingTableSelection);
    }

    private <T> BooleanBinding getWorkingTableSelectionBinding(Predicate<ObservableList<? extends T>> predicate) {
        return getTableSelectionBinding(workingTreeTable.getSelectionModel(), predicate)
                .and(getPaneVisibleBinding(workingTreeLayout));
    }

    private <T> BooleanBinding getSearchTableSelectionBinding(Predicate<ObservableList<? extends T>> predicate) {
        return getTableSelectionBinding(searchTable.getSelectionModel(), predicate)
                .and(getPaneVisibleBinding(searchLayout));
    }

    @SuppressWarnings("unchecked")
    private <T> BooleanBinding getTableSelectionBinding(TableSelectionModel model, Predicate<ObservableList<? extends T>> predicate) {
        return Bindings.createBooleanBinding(() -> predicate.test(model.getSelectedItems()), model.getSelectedItems());
    }

    private BooleanBinding getPaneVisibleBinding(Pane pane) {
        return Bindings.createBooleanBinding(pane::isVisible, pane.visibleProperty());
    }

    private void initializeButtons() {
        ActionUtils.configureButton(addFilesAction(), addButton);
        ActionUtils.configureButton(removeFilesAction, removeButton);
    }

    private List<FileEntity> determineSourceItems() {
        List<FileEntity> item;
        if (workingTreeLayout.isVisible()) {
            item = workingTreeTable
                    .getSelectionModel()
                    .getSelectedItems()
                    .stream()
                    .map(TreeItem::getValue)
                    .collect(Collectors.toList());
            if (item == null)
                item = Lists.newArrayList(workingTreeTable.getRoot().getValue());
        } else {
            item = searchTable.getSelectionModel().getSelectedItems();
        }
        return item;
    }

    private TreeItem<FileEntity> determineParentItem() {
        TreeItem<FileEntity> parent = this.determineSourceItem();
        if (parent.getValue().isFile())
            parent = parent.getParent();
        return parent;
    }

    private TreeItem<FileEntity> determineSourceItem() {
        TreeItem<FileEntity> selectedItem = workingTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            selectedItem = workingTreeTable.getRoot();
        return selectedItem;
    }

    private void deselect() {
        if (workingTreeLayout.isVisible()) {
            workingTreeTable.getSelectionModel().clearSelection();
        } else {
            searchTable.getSelectionModel().clearSelection();
        }
    }

    @SuppressWarnings("unused")
    private void handleSearch(ActionEvent actionEvent) {
        final String query = searchField.getText();
        final List<FileEntity> files = searchService.startNewSearch(query);
        final int pageCount = searchService.getPageCount();
        searchPagination.setPageCount(pageCount);
        searchPagination.setVisible(pageCount > 1);
        searchTable.getItems().setAll(files);
        workingTreeLayout.setVisible(false);
    }

    public void handleSignOut() {
        sessionManager.invalidateSession();
        viewManager.hideAllAndShow("loginView");
    }

    public void handleChangeTheme() {
        cssDriver.setNext(getWindow().getScene());
    }

    @Bean("addFilesAction")
    private Action addFilesAction() {
        final MainController controller = this;
        return new CustomAction("Add files", workingStatusBar) {
            {
                setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN));
                setEventHandler((e) -> {
                    List<File> selectedFiles = selectFiles();
                    final TreeItem<FileEntity> parent = controller.determineParentItem();
                    parent.getChildren(); // fetching files from db to avoid duplicates
                    final Task<Collection<FileEntity>> task = fileService.addFiles(parent.getValue(), selectedFiles);
                    task.setOnSucceeded(event -> {
                        final Collection<FileEntity> files = (Collection<FileEntity>) event.getSource().getValue();
                        parent.getChildren().addAll(files.stream().map(controller::createNode).collect(Collectors.toSet()));
                    });
                    this.handleTask(task);
                });
            }

            private List<File> selectFiles() {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select files");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All", "*.*"));
                final List<File> files = fileChooser.showOpenMultipleDialog(getWindow());
                return files == null ? Lists.newArrayList() : files;
            }
        };
    }

    @Bean("removeFilesAction")
    public CustomAction removeFilesAction() {
        return new CustomAction("Remove", workingStatusBar) {
            {
                setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
                setEventHandler(e -> {
                    final Optional<ButtonType> confirmation = removeConfirmation.showAndWait();
                    confirmation.ifPresent(
                            buttonType -> {
                                final List<FileEntity> files = determineSourceItems();
                                MainController.this.deselect();
                                final Map<FileEntity, Collection<FileEntity>> parentToFilesMap = getParentToFilesMap(files);
                                final Task<Collection<FileEntity>> task = fileService.removeFiles(Updater.flat(parentToFilesMap.values()));
                                task.setOnSucceeded(ev -> {
                                    parentToFilesMap.forEach(FileEntity::removeChildren);
                                    this.removeAndUpdate(mapToTable(parentToFilesMap));
                                });
                                this.handleTask(task);
                    });
                });
            }

            private Map<FileEntity, Collection<FileEntity>> getParentToFilesMap(List<FileEntity> files) {
                final Map<FileEntity, Collection<FileEntity>> entireParentToFilesMap = getEntireParentToFilesMap(files);
                return getFilteredParentToFilesMap(entireParentToFilesMap, files);
            }

            private Map<FileEntity, Collection<FileEntity>> getEntireParentToFilesMap(List<FileEntity> files) {
                Map<FileEntity, Collection<FileEntity>> parentToFiles = new HashMap<>();
                files.forEach(f -> {
                    final FileEntity parent = f.getParent();
                    if (parentToFiles.containsKey(parent)) {
                        parentToFiles.get(parent).add(f);
                    } else {
                        parentToFiles.put(parent, Lists.newArrayList(f));
                    }
                });
                return parentToFiles;
            }

            private Map<FileEntity, Collection<FileEntity>> getFilteredParentToFilesMap(Map<FileEntity, Collection<FileEntity>> entireParentToFilesMap, List<FileEntity> files) {
                final Iterator<FileEntity> iterator = entireParentToFilesMap.keySet().iterator();
                while (iterator.hasNext()) {
                    final FileEntity next = iterator.next();
                    if (files.contains(next)) {
                        files.removeAll(next.getChildren());
                        iterator.remove();
                    }
                }
                return entireParentToFilesMap;
            }

            private Map<TreeItem<FileEntity>, Collection<TreeItem<FileEntity>>> mapToTable(Map<FileEntity, Collection<FileEntity>> map) {
                Map<TreeItem<FileEntity>, Collection<TreeItem<FileEntity>>> tableMap = new HashMap<>();
                map.forEach((key, value) -> {
                    final TreeItem<FileEntity> newKey = Updater.findItem(workingTreeTable.getRoot(), key);
                    final Set<TreeItem<FileEntity>> newValue = Updater.findItems(newKey, value.toArray(new FileEntity[0]));
                    tableMap.put(newKey, newValue);
                });
                return tableMap;
            }

            private void removeAndUpdate(Map<TreeItem<FileEntity>, Collection<TreeItem<FileEntity>>> parentToFilesMap) {
                parentToFilesMap.forEach((k, v) -> k.getChildren().removeAll(v));
                MainController.this.refresh();
            }
        };
    }

    @Bean("addDirectoryAction")
    private NamingAction addDirectoryAction() {
        return new NamingAction("Add directory", workingStatusBar) {
            {
                setEventHandler((e) -> {
                    final TreeItem<FileEntity> parent = determineParentItem();
                    this.addNameCrossingValidator(parent);
                    final Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        final Task<Collection<FileEntity>> task = fileService.addDirectory(parent.getValue(), result.get());
                        task.setOnSucceeded(event -> {
                            final Collection<FileEntity> files = (Collection<FileEntity>) event.getSource().getValue();
                            parent.getChildren().addAll(files.stream().map(MainController.this::createNode).collect(Collectors.toList()));
                        });
                        this.handleTask(task);
                    }
                });
                setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
            }
        };
    }

    @Bean("renameFileAction")
    private NamingAction renameFileAction() {
        return new NamingAction("Rename", workingStatusBar) {
            {
                setEventHandler((e) -> {
                    final TreeItem<FileEntity> source = determineSourceItem();
                    final FileEntity value = source.getValue();
                    dialog.setInitialText(value.getFilename());
                    this.addNameCrossingValidator(source.getParent());
                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(s -> {
                        final Task<FileEntity> task = fileService.renameFile(value, s);
                        // TODO parallel execution issue
                        refresh();
                        this.handleTask(task);
                    });
                });
                setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
            }
        };
    }

    @Bean("openFileAction")
    private Action openFileAction() {
        return new CustomAction("Open...", workingStatusBar) {
            {
                setEventHandler((e) -> {
                    final TreeItem<FileEntity> source = determineSourceItem();
                    final Task<Void> task = fileService.openFile(source.getValue());
                    this.handleTask(task);
                });
                setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
            }
        };
    }

    @Bean("downloadFileAction")
    private DialogAction<? extends DownloadContext, DownloadingDialog> downloadFileAction() {
        return new DialogAction<SimpleDownloadContext, DownloadingDialog>("Download", workingStatusBar) {
            {
                setEventHandler((e) -> {
                    Optional<SimpleDownloadContext> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        final SimpleDownloadContext context = result.get();
                        final List<FileEntity> fileEntities = determineSourceItems();
                        final Task<Collection<File>> task = fileService.downloadFiles(context, fileEntities);
                        this.handleTask(task);
                    }
                });
                setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
            }
        };
    }

    private Window getWindow() {
        return viewManager.getView("mainView").getView().getScene().getWindow();
    }

    private void refresh() {
        workingTreeTable.refresh();
        this.updateSearch();
    }

    private void updateSearch() {
        if (searchLayout.isVisible())
            handleSearch(new ActionEvent());
    }

    @SuppressWarnings({"unchecked", "unused", "SameParameterValue"})
    private static class CustomDialog<T> extends Dialog<T> {
        private ValidationSupport vs = new ValidationSupport();
        private TextField textField;
        private ButtonType okButton;
        List<Validator<?>> registeredValidators = new ArrayList<>();
        final Validator<Object> emptyValidator = Validator.createEmptyValidator("Cannot be empty");

        CustomDialog(Window owner, String title, String labelText) {
            initModality(Modality.APPLICATION_MODAL);
            initOwner(owner);
            setTitle(title);

            final GridPane grid = createGridPane();
            addValidatedInputTextRow(grid, labelText, 0, 0);
            addStuff(grid);
            final DecorationPane decorationPane = decorateContent(grid);
            getDialogPane().setContent(decorationPane);

            okButton = addValidatedButtons();
            Platform.runLater(() -> textField.requestFocus());
        }

        GridPane createGridPane() {
            GridPane grid = new GridPane();
            grid.setVgap(10);
            grid.setHgap(10);
            return grid;
        }

        void addValidatedInputTextRow(GridPane grid, String labelText, int leftCol, int row) {
            Label label = new Label(labelText);
            grid.add(label, leftCol, row);
            textField = createTextField("textField");
            registeredValidators.add(emptyValidator);
            this.resetValidators();
            grid.add(textField, ++leftCol, row);
        }

        void addStuff(GridPane gridPane) {}

        ButtonType addValidatedButtons() {
            final ButtonType ok = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ok);
            getDialogPane().lookupButton(ok).disableProperty().bind(vs.invalidProperty());
            return ok;
        }

        private DecorationPane decorateContent(Pane pane) {
            DecorationPane decorationPane = new DecorationPane();
            decorationPane.getChildren().add(pane);
            return decorationPane;
        }

        TextField getTextField() {
            return textField;
        }

        ButtonType getOkButton() {
            return okButton;
        }

        void setInitialText(String initialText) {
            setOnShowing((e) -> textField.setText(initialText));
        }

        void resetValidators() {
            final Validator<Object>[] validators = registeredValidators.toArray(new Validator[0]);
            final Validator<Object> combined = Validator.combine(validators);
            vs.registerValidator(textField, combined);
        }

        void reattachValidator(Validator<Object> validator) {
            final Validator<Object>[] validators = ArrayUtils.add(registeredValidators.toArray(new Validator[0]), validator);
            final Validator<Object> combined = Validator.combine(validators);
            vs.registerValidator(textField, combined);
        }

        void addValidator(Validator<Object> validator) {
            registeredValidators.add(validator);
            final Validator<Object>[] validators = registeredValidators.toArray(new Validator[0]);
            final Validator<Object> combined = Validator.combine(validators);
            vs.registerValidator(textField, combined);
        }

        private TextField createTextField(String id) {
            TextField textField = new TextField();
            textField.setId(id);
            GridPane.setHgrow(textField, Priority.ALWAYS);
            return textField;
        }
    }

    private static class NamingDialog extends CustomDialog<String> {
        final Validator<String> symbolValidator =
                Validator.createRegexValidator("Allowed symbols: a-zA-Z0-9_.", Pattern.compile(".*[a-zA-Z0-9_.].*"), null);
        NamingDialog(Window owner, String title, String labelText) {
            super(owner, title, labelText);
            registeredValidators.add(symbolValidator);
            this.resetValidators();
            setResultConverter(dialogButton -> dialogButton == getOkButton() ? getTextField().getText() : null);
        }
    }

    private class DownloadingDialog extends CustomDialog<SimpleDownloadContext> {
        private CheckBox checkBox;
        private final Window window;
        private File file = new File("/");

        DownloadingDialog(Window owner, String title, String labelText) {
            super(owner, title, labelText);
            this.window = owner;
            setOnShowing(e -> getTextField().setText(file.getAbsolutePath()));
            setResultConverter(dialogButton -> {
                if (dialogButton == getOkButton()) {
                    boolean createParentIfNotExists = false;
                    file = new File(getTextField().getText());
                    if (!file.exists()) {
                        createParentIfNotExists = showCreateIfNotExistsDialog(file);
                    }
                    return SimpleDownloadContext.create(file, checkBox.isSelected(), createParentIfNotExists);
                } else
                    return null;
            });
        }

        private boolean showCreateIfNotExistsDialog(File file) {
            Alert dlg = new Alert(Alert.AlertType.CONFIRMATION, file.getAbsolutePath() + " doesn't exists. Create?");
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.initOwner(getWindow());
            dlg.setTitle("Confirmation");
            final Optional<ButtonType> result = dlg.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
        }

        @Override
        void setInitialText(String initialText) {}

        @Override
        void addStuff(GridPane gridPane) {
            ImageView graphics = new ImageView(new Image("static/pics/folder-icon.png"));
            graphics.setFitWidth(20.);
            graphics.setFitHeight(20.);
            Button button = new Button("", graphics);
            button.setOnAction(event -> {
                File file = selectDirectory();
                this.file = file == null ? this.file : file;
                getTextField().setText(this.file.getAbsolutePath());
            });
            gridPane.add(button, 2, 0);
            checkBox = new CheckBox("Replace existing");
            gridPane.add(checkBox,0, 1);
        }

        private File selectDirectory() {
            final DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(file);
            directoryChooser.setTitle("Download");
            return directoryChooser.showDialog(window);
        }
    }

    private static class CustomAction extends Action {
        private final StatusBar statusBar;

        private String previousStatus;

        private Consumer<Throwable> onFail = (ex) -> Platform.runLater(() -> showExceptionDialog(ex));

        CustomAction(String text, StatusBar statusBar) {
            super(text);
            Objects.requireNonNull(statusBar);
            this.statusBar = statusBar;
        }

        CustomAction(Consumer<ActionEvent> eventHandler, StatusBar statusBar) {
            super(eventHandler);
            Objects.requireNonNull(statusBar);
            this.statusBar = statusBar;
        }

        CustomAction(String text, Consumer<ActionEvent> eventHandler, StatusBar statusBar) {
            super(text, eventHandler);
            Objects.requireNonNull(statusBar);
            this.statusBar = statusBar;
        }

        Consumer<Throwable> getOnFail() {
            return onFail;
        }

        public void setOnFail(Consumer<Throwable> onFail) {
            this.onFail = onFail;
        }

        private void showExceptionDialog(Throwable e) {
            ExceptionDialog dlg;
            dlg = new ExceptionDialog(e);
            dlg.setTitle("IOException");
            dlg.setContentText("Cannot open file");
            dlg.show();
        }

        <T> void handleTask(Task<T> task) {
            this.bindToStatusBar(task);
            this.setUnbindFinally(task);
            task.setOnFailed(ev -> {
                getOnFail().accept(ev.getSource().getException());
                ev.getSource().cancel();
            });
        }

        private <T> void setUnbindFinally(Task<T> task) {
            task.addEventHandler(WorkerStateEvent.ANY, event -> {
                final Worker source = event.getSource();
                final State state = source.getState();
                if (state == State.CANCELLED || state == State.SUCCEEDED || state == State.FAILED)
                    this.unbind(statusBar);
            });
        }

        private void bindToStatusBar(Task task) {
            previousStatus = statusBar.getText();
            statusBar.textProperty().bind(task.messageProperty());
            statusBar.progressProperty().bind(task.progressProperty());
        }

        private void unbind(StatusBar statusBar) {
            statusBar.textProperty().unbind();
            statusBar.progressProperty().unbind();
            statusBar.setText(previousStatus);
        }
    }

    @SuppressWarnings("unused")
    private static class DialogAction<R, T extends CustomDialog<? extends R>> extends CustomAction {
        T dialog;

        DialogAction(String text, StatusBar statusBar) {
            super(text, statusBar);
        }

        DialogAction(Consumer<ActionEvent> eventHandler, StatusBar statusBar) {
            super(eventHandler, statusBar);
        }

        DialogAction(String text, Consumer<ActionEvent> eventHandler, StatusBar statusBar) {
            super(text, eventHandler, statusBar);
        }

        void setDialog(T dialog) {
            this.dialog = dialog;
        }
    }

    @SuppressWarnings("unused")
    private static class NamingAction extends DialogAction<String, NamingDialog> {
        NamingAction(String text, StatusBar statusBar) {
            super(text, statusBar);
        }

        NamingAction(Consumer<ActionEvent> eventHandler, StatusBar statusBar) {
            super(eventHandler, statusBar);
        }

        NamingAction(String text, Consumer<ActionEvent> eventHandler, StatusBar statusBar) {
            super(text, eventHandler, statusBar);
        }

        void setDialog(NamingDialog dialog) {
            this.dialog = dialog;
        }

        List<String> getChildrenNames(TreeItem<FileEntity> parent) {
            return parent.getChildren().stream()
                    .map((f) -> f.getValue().getFilename())
                    .collect(Collectors.toList());
        }

        void addNameCrossingValidator(TreeItem<FileEntity> parent) {
            final List<String> childrenNames = getChildrenNames(parent);
            dialog.reattachValidator(Validator.createPredicateValidator(
                    (o) -> !childrenNames.contains(o.toString()),
                    "Already in this directory"));
        }
    }

    private TreeItem<FileEntity> createNode(final FileEntity f) {
        return new TreeItem<FileEntity>(f) {
            private boolean isLeaf;
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<FileEntity>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    FileEntity f = getValue();
                    isLeaf = f.isFile();
                }
                return isLeaf;
            }

            private ObservableList<TreeItem<FileEntity>> buildChildren(TreeItem<FileEntity> TreeItem) {
                FileEntity f = TreeItem.getValue();
                if (f != null && f.isDirectory()) {
                    final Set<FileEntity> files = f.getChildren();
                    if (files != null) {
                        ObservableList<TreeItem<FileEntity>> children = FXCollections.observableArrayList();
                        for (FileEntity childFile : files) {
                            children.add(createNode(childFile));
                        }
                        return children;
                    }
                }
                return FXCollections.emptyObservableList();
            }
        };
    }

    @Autowired
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Autowired
    public void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    @Autowired
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    @Autowired
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Autowired
    public void setCssDriver(CSSDriver cssDriver) {
        this.cssDriver = cssDriver;
    }

    @Override
    public void onApplicationEvent(NewSessionEvent newSessionEvent) {
        final Session source = (Session) newSessionEvent.getSource();
        final UserEntity userEntity = source.getUserEntity();
        this.initializeUser(userEntity);
        searchField.setText("");
        workingStatusBar.setText(userEntity.getRole().getDescription());
    }

    private void initializeUser(UserEntity userEntity) {
        workingTreeTable.setRoot(createNode(userEntity.getRoot()));

        String email = userEntity.getEmail();
        String s = email.substring(0, 1).toUpperCase();
        userNameLetter.setText(s);
    }
}
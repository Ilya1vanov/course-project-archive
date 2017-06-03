package com.ilya.ivanov.controller;

import com.google.common.collect.Lists;
import com.ilya.ivanov.Updater;
import com.ilya.ivanov.data.model.file.FileEntity;
import com.ilya.ivanov.data.model.user.UserEntity;
import com.ilya.ivanov.security.session.NewSessionEvent;
import com.ilya.ivanov.security.session.Session;
import com.ilya.ivanov.security.session.SessionManager;
import com.ilya.ivanov.service.file.FileService;
import com.ilya.ivanov.service.search.SearchService;
import com.ilya.ivanov.view.CSSDriver;
import com.ilya.ivanov.view.ViewManager;
import impl.org.controlsfx.skin.DecorationPane;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.apache.commons.lang3.ArrayUtils;
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
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

/**
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
    @FXML private Label workingStatusBar;
    @FXML private VBox searchLayout;
    @FXML private TableView<FileEntity> searchTable;
    @FXML private Text searchResultText;
    @FXML private Pagination searchPagination;

    private Action removeFilesAction;
    private Action addFilesAction;
    private Action addDirectoryAction;
    private Action renameFileAction;
    private Action openFileAction;
    private Action downloadFileAction;

    private Alert removeConfirmation;
    private CustomDialog addDirectoryDialog;
    private CustomDialog renameFileDialog;
    private ContextMenu mainTableMenu;

    private SessionManager sessionManager;
    private ViewManager viewManager;
    private FileService fileService;
    private SearchService searchService;
    private CSSDriver cssDriver;

    private final ExecutorService executor = Executors.newWorkStealingPool();

    /** JavaFX initialize */
    public void initialize() {
    }

    @PostConstruct
    private void initializePostConstruct() {
        root.addEventHandler(KeyEvent.ANY, event -> {
            if (event.getCode().equals(KeyCode.F5)) {
                event.consume();
                update();
            }
        });
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
        this.initializeWorkingLayout();
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
        searchResultText.textProperty().bind(searchService.currentNumberOfElementsProperty());
        searchPagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            final List<FileEntity> files = searchService.getSlice(newValue.intValue());
            searchTable.getItems().setAll(files);
        });
    }

    private void initializeDialogs() {
        final Window owner = viewManager.getView("mainView").getView().getScene().getWindow();
        removeConfirmation = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?") {
            {
                setTitle("Remove");
                setHeaderText("Confirm operation!");
                initModality(Modality.APPLICATION_MODAL);
                initOwner(owner);
            }
        };
        addDirectoryDialog = new CustomDialog(owner, "Add new directory", "Directory name");
        addDirectoryDialog.setInitialText("New directory");
        renameFileDialog = new CustomDialog(owner, "Rename", "New name");
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
        Predicate<Integer> emptyPredicate = (i) -> i == 0;
        final BooleanBinding removeActionLogic = or2Tables(emptyPredicate);
        removeFilesAction.disabledProperty().bind(removeActionLogic);
    }

    private void initializeAddFilesActionLogic() {
        final BooleanBinding addActionLogic = getPaneVisibleBinding(searchLayout);
        addFilesAction.disabledProperty().bind(addActionLogic);
    }

    private void initializeOpenActionLogic() {
        Predicate<Integer> singleSelectionPredicate = (i) -> i == 1;
        final BooleanBinding openActionLogic = or2Tables(singleSelectionPredicate);
        openFileAction.disabledProperty().bind(openActionLogic.not());
    }

    private void initializeAddDirectoryActionLogic() {
        addDirectoryAction.disabledProperty().bind(searchLayout.visibleProperty());
    }

    private void initializeRenameFileActionLogic() {
        Predicate<Integer> singleSelectionPredicate = (i) -> i == 1;
        final BooleanBinding renameFileActionLogic = or2Tables(singleSelectionPredicate);
        renameFileAction.disabledProperty().bind(renameFileActionLogic.not());
    }

    private BooleanBinding or2Tables(Predicate<Integer> predicate) {
        final BooleanBinding WorkingTableSelection = getWorkingTableSelectionBinding(predicate);
        final BooleanBinding SearchTableSelection = getSearchTableSelectionBinding(predicate);
        return SearchTableSelection.or(WorkingTableSelection);
    }

    private BooleanBinding getWorkingTableSelectionBinding(Predicate<Integer> predicate) {
        return getTableSelectionBinding(workingTreeTable.getSelectionModel(), predicate)
                .and(getPaneVisibleBinding(workingTreeLayout));
    }

    private BooleanBinding getSearchTableSelectionBinding(Predicate<Integer> predicate) {
        return getTableSelectionBinding(searchTable.getSelectionModel(), predicate)
                .and(getPaneVisibleBinding(searchLayout));
    }

    private BooleanBinding getTableSelectionBinding(TableSelectionModel model, Predicate<Integer> predicate) {
        return Bindings.createBooleanBinding(() -> predicate.test(model.getSelectedItems().size()), model.getSelectedItems());
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

    public void handleSearch(ActionEvent actionEvent) {
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
        cssDriver.setNext(viewManager.getCurrentView().getView().getScene());
    }

    @Bean("addFilesAction")
    private Action addFilesAction() {
        final MainController controller = this;
        return new Action("Add files") {
            private final ExecutorService executor = Executors.newWorkStealingPool();

            {
                setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN));
                setEventHandler((e) -> {
                    List<File> selectedFiles = selectFiles();
                    final TreeItem<FileEntity> parent = controller.determineParentItem();
                    executor.submit(() -> {
                        final Collection<FileEntity> files = fileService.addFiles(parent.getValue(), selectedFiles);
                        parent.getChildren().addAll(files.stream().map(controller::createNode).collect(Collectors.toList()));
                    });
                });
            }

            private List<File> selectFiles() {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select files");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All", "*.*"));
                final List<File> files = fileChooser.showOpenMultipleDialog(viewManager.getCurrentView().getView().getScene().getWindow());
                return files == null ? Lists.newArrayList() : files;
            }
        };
    }

    @Bean("removeFilesAction")
    public Action removeFilesAction() {
        return new Action("Remove") {
            {
                setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
                setEventHandler((e) -> {
                    final Optional<ButtonType> confirmation = removeConfirmation.showAndWait();
                    if (confirmation.isPresent() && confirmation.get().equals(ButtonType.OK)) {
                        final List<FileEntity> files = determineSourceItems();
                        fileService.removeFiles(files);
                        update();
                    }
                });
            }
        };
    }

    @Bean("addDirectoryAction")
    private Action addDirectoryAction() {
        return new NamingAction("Add directory") {
            {
                setEventHandler((e) -> {
                    final TreeItem<FileEntity> parent = determineParentItem();
                    final List<String> childrenNames = getChildrenNames(parent, directory);
                    addDirectoryDialog.reattachValidator(Validator.createPredicateValidator(
                            (o) -> !childrenNames.contains(o.toString()),
                            "Already in this directory"));
                    final Optional<String> result = addDirectoryDialog.showAndWait();
                    if (result.isPresent()) {
                        final Collection<FileEntity> files = fileService.addDirectory(parent.getValue(), result.get());
                        parent.getChildren().addAll(files.stream().map(MainController.this::createNode).collect(Collectors.toList()));
                    }
                });
                setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
            }
        };
    }

    @Bean("renameFileAction")
    private Action renameFileAction() {
        return new NamingAction("Rename") {
            {
                setEventHandler((e) -> {
                    final TreeItem<FileEntity> source = determineSourceItem();
                    final FileEntity value = source.getValue();
                    renameFileDialog.setInitialText(value.getFilename());
                    final List<String> childrenNames = getChildrenNames(source.getParent(), value.isFile() ? file : directory);
                    renameFileDialog.reattachValidator(Validator.createPredicateValidator(
                            (o) -> !childrenNames.contains(o.toString()),
                            "Already in this directory"));
                    Optional<String> result = renameFileDialog.showAndWait();
                    result.ifPresent(s -> {
                        fileService.renameFile(value, s);
                        update();
                    });
                });
                setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
            }
        };
    }

    @Bean("openFileAction")
    private Action openFileAction() {
        return new Action("Open...") {
            {
                setEventHandler((e) -> {
                    final TreeItem<FileEntity> source = determineSourceItem();
                    List<Future<Object>> futures;
                    try {
                        futures = fileService.openFile(source.getValue());
                    } catch (InterruptedException | IOException e1) {
                        showExceptionDialog(e1);
                        return;
                    }
                    executor.submit(() -> futures.forEach((f) -> {
                        try {
                            f.get();
                        } catch (InterruptedException | ExecutionException e1) {
                            Platform.runLater(() ->showExceptionDialog(e1));
                        }
                    }));
                });
                setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
            }

            private void showExceptionDialog(Exception e) {
                ExceptionDialog dlg;
                try {
                    dlg = new ExceptionDialog(e);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    return;
                }
                dlg.setTitle("IOException");
                dlg.setContentText("Cannot open file");
                dlg.show();
            }
        };
    }

    @Bean("downloadFileAction")
    private Action downloadFileAction() {
        final Action openAction = new Action("Download", (e) -> {
            System.out.println("Download");
        });
        openAction.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        return openAction;
    }

    private void update() {
        Updater.update(workingTreeTable);
        if (searchLayout.isVisible())
            handleSearch(new ActionEvent());
    }

    private static class CustomDialog extends Dialog<String> {
        private ValidationSupport vs = new ValidationSupport();
        private TextField textField;
        private String initialText;
        List<Validator<Object>> registeredValidators = new ArrayList<>();
        final Validator<Object> emptyValidator = Validator.createEmptyValidator("Cannot be empty");

        CustomDialog(Window owner, String title, String labelText) {
            initModality(Modality.APPLICATION_MODAL);
            initOwner(owner);
            setTitle(title);

            GridPane grid = new GridPane();
            grid.setVgap(10);
            grid.setHgap(10);

            Label label = new Label(labelText);
            grid.add(label, 0, 0);
            textField = createTextField("textField");
            registeredValidators.add(emptyValidator);
            vs.registerValidator(textField, emptyValidator);
            grid.add(textField, 1, 0);

            Platform.runLater(() -> textField.requestFocus());

            DecorationPane decorationPane = new DecorationPane();
            decorationPane.getChildren().add(grid);
            getDialogPane().setContent(decorationPane);

            final ButtonType ok = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ok);
            getDialogPane().lookupButton(ok).disableProperty().bind(vs.invalidProperty());

            setResultConverter(dialogButton -> dialogButton == ok ? textField.getText() : null);
        }

        String getInitialText() {
            return initialText;
        }

        void setInitialText(String initialText) {
            this.initialText = initialText;
            setOnShowing((e) -> textField.setText(initialText));
        }

        void resetValidators() {
            registeredValidators = Lists.newArrayList(emptyValidator);
        }

        @SuppressWarnings("unchecked")
        void reattachValidator(Validator<Object> validator) {
            final Validator<Object>[] validators = ArrayUtils.add(registeredValidators.toArray(new Validator[0]) ,validator);
            final Validator<Object> combined = Validator.combine(validators);
            vs.registerValidator(textField, combined);
        }

        @SuppressWarnings("unchecked")
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

    private static class NamingAction extends Action {
        Predicate<TreeItem<FileEntity>> file = (f) -> f.getValue().isFile();

        Predicate<TreeItem<FileEntity>> directory = (f) -> f.getValue().isDirectory();

        Predicate<TreeItem<FileEntity>> both = (f) -> true;

        public NamingAction(String text) {
            super(text);
        }

        public NamingAction(Consumer<ActionEvent> eventHandler) {
            super(eventHandler);
        }

        public NamingAction(String text, Consumer<ActionEvent> eventHandler) {
            super(text, eventHandler);
        }

        List<String> getChildrenNames(TreeItem<FileEntity> parent, Predicate<TreeItem<FileEntity>> p) {
            return parent.getChildren().stream()
                    .filter(p)
                    .map((f) -> f.getValue().getFilename())
                    .collect(Collectors.toList());
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

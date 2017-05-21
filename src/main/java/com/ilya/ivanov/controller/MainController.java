package com.ilya.ivanov.controller;

import com.ilya.ivanov.data.model.FileEntity;
import com.ilya.ivanov.security.session.SessionManager;
import com.ilya.ivanov.view.ViewManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;

/**
 * Created by ilya on 5/20/17.
 */
public class MainController {
    private static final Logger log = Logger.getLogger(MainController.class);
    @FXML private HBox controlButtons;
    @FXML private Pane addButtonWrapper;
    @FXML private Pane removeButtonWrapper;
    @FXML private Button addButton;
    @FXML private Button removeButton;
    @FXML private Text userNameLetter;
    @FXML private TextField searchField;
    @FXML private VBox workingTreeLayout;
    @FXML private TreeTableView<FileEntity> workingTreeTable;
    @FXML private Label workingStatusBar;
    @FXML private VBox searchResultsLayout;
    @FXML private TableView<FileEntity> searchResultTable;
    @FXML private AnchorPane searchStatusBar;
    @FXML private Text searchResultText;
    @FXML private Pagination searchPagination;

    @Autowired private SessionManager sessionManager;

    @Autowired private ViewManager viewManager;

    public void handleSearchCrossClicked(MouseEvent mouseEvent) {

    }

    public void handleSearch(ActionEvent actionEvent) {

    }

    public void handleSignOut() {
        sessionManager.invalidateSession();
        viewManager.hideAllAndShow("loginView");
    }

    public void handleChangeTheme(MouseEvent mouseEvent) {

    }

    public void handleAddFiles(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select files");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All", "*.*"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(viewManager.getCurrentView().getView().getScene().getWindow());

    }

    public void handleRemove(ActionEvent actionEvent) {

    }

    public HBox getControlButtons() {
        return controlButtons;
    }

    public Pane getAddButtonWrapper() {
        return addButtonWrapper;
    }

    public Pane getRemoveButtonWrapper() {
        return removeButtonWrapper;
    }

    public Button getAddButton() {
        return addButton;
    }

    public Button getRemoveButton() {
        return removeButton;
    }

    public Text getUserNameLetter() {
        return userNameLetter;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public VBox getWorkingTreeLayout() {
        return workingTreeLayout;
    }

    public TreeTableView<FileEntity> getWorkingTreeTable() {
        return workingTreeTable;
    }

    public Label getWorkingStatusBar() {
        return workingStatusBar;
    }

    public VBox getSearchResultsLayout() {
        return searchResultsLayout;
    }

    public TableView<FileEntity> getSearchResultTable() {
        return searchResultTable;
    }

    public AnchorPane getSearchStatusBar() {
        return searchStatusBar;
    }

    public Text getSearchResultText() {
        return searchResultText;
    }

    public Pagination getSearchPagination() {
        return searchPagination;
    }
}

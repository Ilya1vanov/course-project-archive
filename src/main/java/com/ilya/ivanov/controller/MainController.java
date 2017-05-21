package com.ilya.ivanov.controller;

import com.ilya.ivanov.data.model.FileEntity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;

/**
 * Created by ilya on 5/20/17.
 */
public class MainController {
    private static final Logger log = Logger.getLogger(MainController.class);
    @FXML VBox root;
    @FXML HBox controlButtons;
    @FXML Pane addButtonWrapper;
    @FXML Pane removeButtonWrapper;
    @FXML Button addButton;
    @FXML Button removeButton;
    @FXML Text userNameLetter;
    @FXML TextField searchField;
    @FXML TabPane tabPane;
    @FXML VBox workingTreeLayout;
    @FXML TreeTableView<FileEntity> workingTreeTable;
    @FXML Label workingStatusBar;
    @FXML VBox searchResultsLayout;
    @FXML TableView<FileEntity> searchResultTable;
    @FXML AnchorPane searchStatusBar;
    @FXML Text searchResultText;
    @FXML Pagination searchPagination;

    public void handleSearchCrossClicked(MouseEvent mouseEvent) {

    }

    public void handleSearch(ActionEvent actionEvent) {

    }

    public void handleSignOut(MouseEvent mouseEvent) {

    }

    public void handleChangeTheme(MouseEvent mouseEvent) {

    }

    public void handleAddFiles(ActionEvent actionEvent) {

    }

    public void handleRemove(ActionEvent actionEvent) {

    }
}

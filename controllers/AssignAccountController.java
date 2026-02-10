package com.example.demo4.controllers;

import com.example.demo4.dao.CitizenDao;
import com.example.demo4.dao.UserDao;
import com.example.demo4.models.Citizen;
import com.example.demo4.models.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AssignAccountController {

    @FXML private TableView<Citizen> tblCitizens;
    @FXML private TableColumn<Citizen,String> colName;
    @FXML private TableColumn<Citizen,String> colCCCD;

    @FXML private ComboBox<User> cmbUsers;
    @FXML private Label lblStatus;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getFullName()));
        colCCCD.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getCccd()));

        cmbUsers.setConverter(new javafx.util.StringConverter<>() {
            public String toString(User u) {
                return u == null ? "" : u.getUsername() + " (" + u.getRole() + ")";
            }
            public User fromString(String s) { return null; }
        });

        refresh();
    }

    private void refresh() {
        try {
            tblCitizens.setItems(
                    FXCollections.observableArrayList(
                            CitizenDao.findAvailableForAssign()));
            cmbUsers.setItems(
                    FXCollections.observableArrayList(
                            UserDao.findAll()));
        } catch (Exception e) {
            lblStatus.setText("Lỗi tải dữ liệu!");
        }
    }

    @FXML
    public void assignAccount() {
        Citizen c = tblCitizens.getSelectionModel().getSelectedItem();
        User u = cmbUsers.getValue();

        if (c == null || u == null) {
            lblStatus.setText("Chọn công dân và user!");
            return;
        }

        try {
            CitizenDao.assignUser(c.getId(), u.getId());
            lblStatus.setText("Gán tài khoản thành công!");
            refresh();
        } catch (Exception e) {
            lblStatus.setText("Lỗi gán tài khoản!");
        }
    }
}
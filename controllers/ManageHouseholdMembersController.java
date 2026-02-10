package com.example.demo4.controllers;

import com.example.demo4.dao.CitizenDao;
import com.example.demo4.models.Citizen;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ManageHouseholdMembersController extends BaseController {

    @FXML private TableView<Citizen> tableMembers;
    @FXML private TableView<Citizen> tableNoHouse;

    @FXML private TableColumn<Citizen, String> colName1;
    @FXML private TableColumn<Citizen, String> colRelation1;

    @FXML private TableColumn<Citizen, String> colName2;
    @FXML private TableColumn<Citizen, String> colRelation2;

    @FXML private TextField tfTargetHousehold;

    private int householdId;
    private Stage stage;

    private final ObservableList<Citizen> members = FXCollections.observableArrayList();
    private final ObservableList<Citizen> noHouse = FXCollections.observableArrayList();

    public void setHouseholdId(int householdId) {
        if (householdId <= 0) return;
        this.householdId = householdId;
        loadData();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        colName1.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getFullName()));
        colRelation1.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getRelation()));

        colName2.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getFullName()));
        colRelation2.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getRelation()));

        tableMembers.setItems(members);
        tableNoHouse.setItems(noHouse);
    }

    private void loadData() {
        try {
            members.setAll(CitizenDao.findByHouseholdId(householdId));
            noHouse.setAll(CitizenDao.findWithoutHousehold());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không tải được dữ liệu công dân!");
        }
    }

    @FXML
    private void removeMember() {
        Citizen c = tableMembers.getSelectionModel().getSelectedItem();
        if (c == null) {
            showWarning("Lỗi", "Chọn thành viên cần gỡ!");
            return;
        }

        try {
            CitizenDao.removeCitizenFromHousehold(c.getId());
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể gỡ thành viên!");
        }
    }

    @FXML
    private void addMember() {
        Citizen c = tableNoHouse.getSelectionModel().getSelectedItem();
        if (c == null) {
            showWarning("Lỗi", "Chọn công dân để thêm!");
            return;
        }

        try {
            CitizenDao.moveCitizenToHousehold(c.getId(), householdId);
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể thêm thành viên!");
        }
    }

    @FXML
    private void moveToOtherHousehold() {
        Citizen c = tableMembers.getSelectionModel().getSelectedItem();
        if (c == null) {
            showWarning("Lỗi", "Chọn thành viên để chuyển!");
            return;
        }

        int targetId;
        try {
            targetId = Integer.parseInt(tfTargetHousehold.getText().trim());
        } catch (NumberFormatException e) {
            showWarning("Sai dữ liệu", "ID hộ khẩu phải là số!");
            return;
        }

        try {
            CitizenDao.moveCitizenToHousehold(c.getId(), targetId);
            showInfo("Thành công", "Đã chuyển công dân sang hộ khẩu mới!");
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi", e.getMessage());
        }
    }


    @FXML
    private void close() {
        if (stage != null) stage.close();
    }
}

package com.example.demo4.controllers;

import com.example.demo4.dao.CitizenDao;
import com.example.demo4.dao.HouseholdDao;
import com.example.demo4.models.Citizen;
import com.example.demo4.models.Household;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class HomelessCitizenController extends BaseController {

    @FXML private TableView<Citizen> table;
    @FXML private TableColumn<Citizen,Integer> colId;
    @FXML private TableColumn<Citizen,String> colName;
    @FXML private TableColumn<Citizen,String> colRelation;
    @FXML private TableColumn<Citizen,String> colDob;
    @FXML private TableColumn<Citizen,String> colCCCD;

    @FXML private ComboBox<Household> cbHousehold;

    private final ObservableList<Citizen> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getId()));
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFullName()));
        colRelation.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRelation()));
        colDob.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDob() == null ? "" : c.getValue().getDob().toString()));
        colCCCD.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCccd()));

        table.setItems(data);

        loadCitizens();
        loadHouseholds();
    }

    private void loadCitizens() {
        try {
            data.setAll(CitizenDao.findWithoutHousehold());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không tải được danh sách cư dân");
        }
    }

    private void loadHouseholds() {
        try {
            cbHousehold.setItems(FXCollections.observableArrayList(HouseholdDao.findAll()));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không tải được danh sách hộ khẩu");
        }
    }

    @FXML
    private void assignToHousehold() {
        Citizen c = table.getSelectionModel().getSelectedItem();
        Household h = cbHousehold.getValue();

        if (c == null || h == null) {
            showWarning("Thiếu thông tin", "Chọn công dân và hộ khẩu!");
            return;
        }

        try {
            CitizenDao.moveCitizenToHousehold(c.getId(), h.getHouseholdId());
            showInfo("Thành công", "Đã gán công dân vào hộ!");
            loadCitizens();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể gán công dân");
        }
    }

    @FXML
    private void openAddCitizen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo4/add_homeless_citizen.fxml"));
            Parent root = loader.load();

            AddHomelessCitizenController c = loader.getController();
            c.setParent(this);

            Stage stage = new Stage();
            stage.setTitle("Thêm công dân (chưa có hộ)");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không mở được form");
        }
    }

    @FXML
    private void close() {
        Stage s = (Stage) table.getScene().getWindow();
        s.close();
    }

    public void reload() {
        loadCitizens();
    }
}

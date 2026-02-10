package com.example.demo4.controllers;

import com.example.demo4.Main;
import com.example.demo4.dao.CitizenDao;
import com.example.demo4.models.Citizen;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CitizenController extends BaseController {

    @FXML private TableView<Citizen> citizenTable;

    @FXML private TableColumn<Citizen, Integer> colId;
    @FXML private TableColumn<Citizen, String> colFullName;
    @FXML private TableColumn<Citizen, String> colAlias;
    @FXML private TableColumn<Citizen, String> colDob;
    @FXML private TableColumn<Citizen, String> colPlaceOfBirth;
    @FXML private TableColumn<Citizen, String> colHometown;
    @FXML private TableColumn<Citizen, String> colEthnicity;
    @FXML private TableColumn<Citizen, String> colCccd;
    @FXML private TableColumn<Citizen, String> colCccdIssueDate;
    @FXML private TableColumn<Citizen, String> colCccdIssuePlace;
    @FXML private TableColumn<Citizen, String> colJob;
    @FXML private TableColumn<Citizen, String> colWorkplace;
    @FXML private TableColumn<Citizen, String> colPreviousAddress;
    @FXML private TableColumn<Citizen, String> colRegisterDate;
    @FXML private TableColumn<Citizen, Boolean> colHouseholder;
    @FXML private TableColumn<Citizen, String> colRelation;
    @FXML private TableColumn<Citizen, Integer> colHouseholdId;
    @FXML private TableColumn<Citizen, Integer> colUserId;

    @FXML private TextField searchField;

    private Integer currentHouseholdId = null;
    private final ObservableList<Citizen> citizenList = FXCollections.observableArrayList();

    public void setCurrentHouseholdId(int householdId) {
        this.currentHouseholdId = householdId;
        loadFromDB();
    }

    @FXML
    public void initialize() {

        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getId()));
        colFullName.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getFullName()));
        colAlias.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getAlias()));
        colDob.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDob() == null ? "" : d.getValue().getDob().toString()));
        colPlaceOfBirth.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getPlaceOfBirth()));
        colHometown.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getHometown()));
        colEthnicity.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getEthnicity()));
        colCccd.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCccd()));
        colCccdIssueDate.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCccdIssueDate() == null ? "" : d.getValue().getCccdIssueDate().toString()));        colCccdIssuePlace.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCccdIssuePlace()));
        colJob.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getJob()));
        colWorkplace.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getWorkplace()));
        colPreviousAddress.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getPreviousAddress()));
        colRegisterDate.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getRegisterDate() == null ? "" : d.getValue().getRegisterDate().toString()));        colHouseholder.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getHouseholder()));
        colRelation.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getRelation()));
        colHouseholdId.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getHouseholdId()));
        colUserId.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getUserId()));

        citizenTable.setItems(citizenList);
        loadFromDB();
    }

    @FXML
    private void addCitizen() {
        if (currentHouseholdId == null) {
            showWarning("Lỗi", "Cần mở từ màn hình hộ khẩu để thêm nhân khẩu!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo4/add_citizen.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Thêm công dân");

            AddCitizenController controller = loader.getController();
            controller.setStage(stage);
            controller.setHouseholdId(currentHouseholdId);
            controller.setOnAddSuccess(this::loadFromDB);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không mở được form thêm!");
        }
    }

    @FXML
    private void editCitizen() {
        Citizen c = citizenTable.getSelectionModel().getSelectedItem();
        if (c == null) {
            showWarning("Lỗi", "Chọn công dân để sửa!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo4/edit_citizen.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Sửa công dân");

            EditCitizenController controller = loader.getController();
            controller.setStage(stage);
            controller.setCitizen(c);
            controller.setOnEditSuccess(this::loadFromDB);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không mở được form sửa!");
        }
    }

    @FXML
    private void deleteCitizen() {
        Citizen c = citizenTable.getSelectionModel().getSelectedItem();
        if (c == null) {
            showWarning("Lỗi", "Chọn công dân để xóa!");
            return;
        }

        if (!showConfirm("Xác nhận", "Xóa công dân này?")) return;

        try {
            CitizenDao.deleteById(c.getId());
            loadFromDB();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể xóa!");
        }
    }

    @FXML
    private void searchCitizen() {
        String key = searchField.getText().trim().toLowerCase();
        if (key.isEmpty()) {
            citizenTable.setItems(citizenList);
            return;
        }

        ObservableList<Citizen> filtered = FXCollections.observableArrayList();
        for (Citizen c : citizenList) {
            if ((c.getFullName() != null && c.getFullName().toLowerCase().contains(key))
                    || (c.getCccd() != null && c.getCccd().toLowerCase().contains(key))) {
                filtered.add(c);
            }
        }
        citizenTable.setItems(filtered);
    }

    @FXML
    private void refreshList() {
        searchField.clear();
        citizenTable.setItems(citizenList);
        loadFromDB();
    }

    @FXML
    private void backToMenu() throws Exception {
        Main.showMenu();
    }

    private void loadFromDB() {
        citizenList.clear();
        try {
            citizenList.addAll(
                    currentHouseholdId == null
                            ? CitizenDao.findAll()
                            : CitizenDao.findByHouseholdId(currentHouseholdId)
            );
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không tải được công dân!");
        }
    }
}

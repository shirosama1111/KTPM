package com.example.demo4.controllers;

import com.example.demo4.dao.CitizenDao;
import com.example.demo4.dao.TemporaryRecordDao;
import com.example.demo4.models.Citizen;
import com.example.demo4.models.TemporaryRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class TemporaryRecordController extends BaseController {

    @FXML private TableView<TemporaryRecord> table;

    @FXML private TableColumn<TemporaryRecord, Integer> colId;
    @FXML private TableColumn<TemporaryRecord, String> colCitizenName;
    @FXML private TableColumn<TemporaryRecord, String> colType;
    @FXML private TableColumn<TemporaryRecord, String> colStartDate;
    @FXML private TableColumn<TemporaryRecord, String> colEndDate;
    @FXML private TableColumn<TemporaryRecord, String> colLocation;
    @FXML private TableColumn<TemporaryRecord, String> colStatus;

    @FXML private ComboBox<String> cbFilterType;
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private TextField tfSearchCitizen;

    private final ObservableList<TemporaryRecord> records = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d ->
                new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getId()));

        colCitizenName.setCellValueFactory(d -> {
            try {
                Citizen c = CitizenDao.findById(d.getValue().getCitizenId());
                return new javafx.beans.property.SimpleStringProperty(
                        c != null ? c.getFullName() : "ID: " + d.getValue().getCitizenId()
                );
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Lỗi");
            }
        });

        colType.setCellValueFactory(d -> {
            String type = d.getValue().getType();
            String displayType = type.equals("TAM_VANG") ? "Tạm vắng" : "Tạm trú";
            return new javafx.beans.property.SimpleStringProperty(displayType);
        });

        colStartDate.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getStartDate() != null ? d.getValue().getStartDate().toString() : ""
                ));

        colEndDate.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getEndDate() != null ? d.getValue().getEndDate().toString() : ""
                ));

        colLocation.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getLocation()));

        colStatus.setCellValueFactory(d -> {
            LocalDate end = d.getValue().getEndDate();
            String status;
            if (end == null) {
                status = "Không thời hạn";
            } else if (end.isBefore(LocalDate.now())) {
                status = "Hết hạn";
            } else {
                status = "Còn hiệu lực";
            }
            return new javafx.beans.property.SimpleStringProperty(status);
        });

        table.setItems(records);

        cbFilterType.setItems(FXCollections.observableArrayList(
                "Tất cả", "Tạm vắng", "Tạm trú"
        ));
        cbFilterType.setValue("Tất cả");

        cbFilterStatus.setItems(FXCollections.observableArrayList(
                "Tất cả", "Còn hiệu lực", "Hết hạn", "Không thời hạn"
        ));
        cbFilterStatus.setValue("Tất cả");

        loadRecords();
    }

    private void loadRecords() {
        try {
            records.clear();
            for (Citizen c : CitizenDao.findAll()) {
                records.addAll(TemporaryRecordDao.findByCitizen(c.getId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể tải danh sách!");
        }
    }

    @FXML
    private void applyFilter() {
        try {
            ObservableList<TemporaryRecord> filtered = FXCollections.observableArrayList();
            String typeFilter = cbFilterType.getValue();
            String statusFilter = cbFilterStatus.getValue();
            String searchText = tfSearchCitizen.getText().trim().toLowerCase();

            for (TemporaryRecord r : records) {
                if (!typeFilter.equals("Tất cả")) {
                    String recordType = r.getType().equals("TAM_VANG") ? "Tạm vắng" : "Tạm trú";
                    if (!recordType.equals(typeFilter)) continue;
                }

                if (!statusFilter.equals("Tất cả")) {
                    LocalDate end = r.getEndDate();
                    String status;
                    if (end == null) status = "Không thời hạn";
                    else if (end.isBefore(LocalDate.now())) status = "Hết hạn";
                    else status = "Còn hiệu lực";

                    if (!status.equals(statusFilter)) continue;
                }

                if (!searchText.isEmpty()) {
                    Citizen c = CitizenDao.findById(r.getCitizenId());
                    if (c == null || !c.getFullName().toLowerCase().contains(searchText)) {
                        continue;
                    }
                }

                filtered.add(r);
            }

            table.setItems(filtered);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể lọc dữ liệu!");
        }
    }

    @FXML
    private void resetFilter() {
        cbFilterType.setValue("Tất cả");
        cbFilterStatus.setValue("Tất cả");
        tfSearchCitizen.clear();
        table.setItems(records);
    }

    @FXML
    private void addRecord() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/demo4/add_temporary_record.fxml")
            );
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Cấp giấy tạm vắng/tạm trú");

            AddTemporaryRecordController controller = loader.getController();
            controller.setStage(stage);
            controller.setOnAddSuccess(this::loadRecords);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể mở form!");
        }
    }

    @FXML
    private void viewDetails() {
        TemporaryRecord selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Lỗi", "Hãy chọn một bản ghi!");
            return;
        }

        try {
            Citizen c = CitizenDao.findById(selected.getCitizenId());
            String type = selected.getType().equals("TAM_VANG") ? "Tạm vắng" : "Tạm trú";
            LocalDate end = selected.getEndDate();
            String status;
            if (end == null) status = "Không thời hạn";
            else if (end.isBefore(LocalDate.now())) status = "Hết hạn";
            else status = "Còn hiệu lực";

            String details = String.format(
                    "Loại: %s\nCông dân: %s (CCCD: %s)\nTừ ngày: %s\nĐến ngày: %s\nĐịa điểm: %s\nTrạng thái: %s\nGhi chú: %s",
                    type,
                    c != null ? c.getFullName() : "Không rõ",
                    c != null ? c.getCccd() : "Không rõ",
                    selected.getStartDate(),
                    end != null ? end.toString() : "Không thời hạn",
                    selected.getLocation(),
                    status,
                    selected.getNote() != null ? selected.getNote() : ""
            );

            showInfo("Chi tiết", details);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể xem chi tiết!");
        }
    }

    @FXML
    private void deleteRecord() {
        TemporaryRecord selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Lỗi", "Hãy chọn bản ghi để xóa!");
            return;
        }

        boolean confirm = showConfirm("Xác nhận", "Xóa bản ghi này?");
        if (!confirm) return;

        try {
            TemporaryRecordDao.deleteById(selected.getId());
            loadRecords();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể xóa!");
        }
    }

    @FXML
    private void refresh() {
        loadRecords();
        resetFilter();
    }

    @FXML
    private void close() {
        Stage stage = (Stage) table.getScene().getWindow();
        stage.close();
    }
}
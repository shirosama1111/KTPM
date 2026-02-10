package com.example.demo4.controllers;

import com.example.demo4.dao.CitizenChangeDao;
import com.example.demo4.dao.CitizenDao;
import com.example.demo4.dao.HouseholdDao;
import com.example.demo4.models.Citizen;
import com.example.demo4.models.CitizenChange;
import com.example.demo4.models.Household;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CitizenHistoryController extends BaseController {

    @FXML private Label lblCitizenInfo;
    @FXML private TableView<CitizenChange> table;

    @FXML private TableColumn<CitizenChange, Integer> colId;
    @FXML private TableColumn<CitizenChange, String> colChangeType;
    @FXML private TableColumn<CitizenChange, String> colChangeDate;
    @FXML private TableColumn<CitizenChange, String> colFromHousehold;
    @FXML private TableColumn<CitizenChange, String> colToHousehold;
    @FXML private TableColumn<CitizenChange, String> colNote;

    private Citizen citizen;
    private final ObservableList<CitizenChange> changes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d ->
                new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getId()));

        colChangeType.setCellValueFactory(d -> {
            String type = d.getValue().getChangeType();
            String display = switch(type) {
                case "MOVE_OUT" -> "Chuyển hộ";
                case "DEAD" -> "Qua đời";
                case "NEW_BORN" -> "Sinh mới";
                case "UPDATE" -> "Cập nhật thông tin";
                default -> type;
            };
            return new javafx.beans.property.SimpleStringProperty(display);
        });

        colChangeDate.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getChangeDate() != null ? d.getValue().getChangeDate().toString() : ""
                ));

        colFromHousehold.setCellValueFactory(d -> {
            Integer fromId = d.getValue().getFromHouseholdId();
            if (fromId == null) return new javafx.beans.property.SimpleStringProperty("-");

            try {
                Household h = HouseholdDao.findById(fromId);
                return new javafx.beans.property.SimpleStringProperty(
                        h != null ? fromId + " - " + h.getAddress() : String.valueOf(fromId)
                );
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(fromId));
            }
        });

        colToHousehold.setCellValueFactory(d -> {
            Integer toId = d.getValue().getToHouseholdId();
            if (toId == null) return new javafx.beans.property.SimpleStringProperty("-");

            try {
                Household h = HouseholdDao.findById(toId);
                return new javafx.beans.property.SimpleStringProperty(
                        h != null ? toId + " - " + h.getAddress() : String.valueOf(toId)
                );
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(toId));
            }
        });

        colNote.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getNote() != null ? d.getValue().getNote() : ""
                ));

        table.setItems(changes);
    }

    public void setCitizen(Citizen c) {
        this.citizen = c;

        lblCitizenInfo.setText(String.format(
                "Lịch sử thay đổi: %s - CCCD: %s",
                c.getFullName(),
                c.getCccd()
        ));

        loadHistory();
    }

    private void loadHistory() {
        try {
            changes.setAll(CitizenChangeDao.findByCitizen(citizen.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể tải lịch sử!");
        }
    }

    @FXML
    private void viewDetails() {
        CitizenChange selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Lỗi", "Hãy chọn một bản ghi!");
            return;
        }

        try {
            String type = switch(selected.getChangeType()) {
                case "MOVE_OUT" -> "Chuyển hộ";
                case "DEAD" -> "Qua đời";
                case "NEW_BORN" -> "Sinh mới";
                case "UPDATE" -> "Cập nhật thông tin";
                default -> selected.getChangeType();
            };

            String fromHousehold = "-";
            if (selected.getFromHouseholdId() != null) {
                Household h = HouseholdDao.findById(selected.getFromHouseholdId());
                fromHousehold = h != null ?
                        selected.getFromHouseholdId() + " - " + h.getAddress() :
                        String.valueOf(selected.getFromHouseholdId());
            }

            String toHousehold = "-";
            if (selected.getToHouseholdId() != null) {
                Household h = HouseholdDao.findById(selected.getToHouseholdId());
                toHousehold = h != null ?
                        selected.getToHouseholdId() + " - " + h.getAddress() :
                        String.valueOf(selected.getToHouseholdId());
            }

            String details = String.format(
                    "Loại thay đổi: %s\n" +
                            "Ngày thay đổi: %s\n" +
                            "Từ hộ khẩu: %s\n" +
                            "Đến hộ khẩu: %s\n" +
                            "Ghi chú: %s",
                    type,
                    selected.getChangeDate(),
                    fromHousehold,
                    toHousehold,
                    selected.getNote() != null ? selected.getNote() : "Không có"
            );

            showInfo("Chi tiết thay đổi", details);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể xem chi tiết!");
        }
    }

    @FXML
    private void refresh() {
        loadHistory();
    }

    @FXML
    private void close() {
        Stage stage = (Stage) table.getScene().getWindow();
        stage.close();
    }
}
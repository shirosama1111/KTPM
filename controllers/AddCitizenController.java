package com.example.demo4.controllers;

import com.example.demo4.dao.CitizenDao;
import com.example.demo4.models.Citizen;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AddCitizenController extends BaseController {

    @FXML private TextField tfFullName;
    @FXML private TextField tfAlias;
    @FXML private DatePicker dpDob;
    @FXML private TextField tfPlaceOfBirth;
    @FXML private TextField tfHometown;
    @FXML private TextField tfEthnicity;

    @FXML private TextField tfCccd;
    @FXML private DatePicker dpCccdIssueDate;
    @FXML private TextField tfCccdIssuePlace;

    @FXML private TextField tfJob;
    @FXML private TextField tfWorkplace;

    @FXML private TextField tfPreviousAddress;
    @FXML private DatePicker dpRegisterDate;

    @FXML private CheckBox cbHouseholder;
    @FXML private TextField tfRelation;

    @FXML private Label lblMessage;

    private Stage stage;
    private Runnable onAddSuccess;
    private Integer householdId; // truyền từ màn Household
    private Integer userId = null; // nếu cần gán user sau

    public void setStage(Stage stage) { this.stage = stage; }
    public void setOnAddSuccess(Runnable r) { this.onAddSuccess = r; }
    public void setHouseholdId(Integer householdId) { this.householdId = householdId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    @FXML
    public void initialize() {
        // DOB không được sau hôm nay
        dpDob.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(LocalDate.now()));
            }
        });

        // checkbox chủ hộ => relation auto empty + disable
        cbHouseholder.selectedProperty().addListener((obs, oldV, isSelected) -> {
            if (isSelected) {
                tfRelation.clear();
                tfRelation.setDisable(true);
            } else {
                tfRelation.setDisable(false);
            }
        });
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        if (stage != null) stage.close();
        else {
            Stage s = (Stage) tfFullName.getScene().getWindow();
            s.close();
        }
    }

    private void setMsg(String msg) {
        if (lblMessage != null) lblMessage.setText(msg);
        else showWarning("Thông báo", msg);
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

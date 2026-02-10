package com.example.demo4.controllers;

import com.example.demo4.dao.CitizenDao;
import com.example.demo4.dao.TemporaryRecordDao;
import com.example.demo4.models.Citizen;
import com.example.demo4.models.TemporaryRecord;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;

public class AddTemporaryRecordController extends BaseController {

    @FXML private ComboBox<Citizen> cbCitizen;
    @FXML private RadioButton rbTamVang;
    @FXML private RadioButton rbTamTru;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private CheckBox cbNoExpiry;
    @FXML private TextField tfLocation;
    @FXML private TextArea taNote;

    private Stage stage;
    private Runnable onAddSuccess;

    @FXML
    public void initialize() {
        ToggleGroup typeGroup = new ToggleGroup();
        rbTamVang.setToggleGroup(typeGroup);
        rbTamTru.setToggleGroup(typeGroup);
        rbTamVang.setSelected(true);

        try {
            cbCitizen.setItems(FXCollections.observableArrayList(CitizenDao.findAll()));
            cbCitizen.setConverter(new StringConverter<>() {
                @Override
                public String toString(Citizen c) {
                    return c == null ? "" : c.getFullName() + " - " + c.getCccd();
                }
                @Override
                public Citizen fromString(String s) { return null; }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể tải danh sách công dân!");
        }

        dpStartDate.setValue(LocalDate.now());
        dpEndDate.setValue(LocalDate.now().plusMonths(6));

        cbNoExpiry.selectedProperty().addListener((obs, oldV, newV) -> {
            dpEndDate.setDisable(newV);
            if (newV) {
                dpEndDate.setValue(null);
            } else {
                dpEndDate.setValue(LocalDate.now().plusMonths(6));
            }
        });

        dpStartDate.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && dpEndDate.getValue() != null) {
                if (dpEndDate.getValue().isBefore(newV)) {
                    dpEndDate.setValue(newV.plusMonths(1));
                }
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOnAddSuccess(Runnable r) {
        this.onAddSuccess = r;
    }

    @FXML
    private void handleSave() {
        if (cbCitizen.getValue() == null) {
            showWarning("Thiếu thông tin", "Hãy chọn công dân!");
            return;
        }

        if (dpStartDate.getValue() == null) {
            showWarning("Thiếu thông tin", "Hãy chọn ngày bắt đầu!");
            return;
        }

        if (!cbNoExpiry.isSelected() && dpEndDate.getValue() == null) {
            showWarning("Thiếu thông tin", "Hãy chọn ngày kết thúc hoặc chọn 'Không thời hạn'!");
            return;
        }

        if (tfLocation.getText().trim().isEmpty()) {
            showWarning("Thiếu thông tin", "Hãy nhập địa điểm!");
            return;
        }

        if (!cbNoExpiry.isSelected()) {
            if (dpEndDate.getValue().isBefore(dpStartDate.getValue())) {
                showWarning("Lỗi", "Ngày kết thúc phải sau ngày bắt đầu!");
                return;
            }
        }

        try {
            TemporaryRecord record = new TemporaryRecord();
            record.setCitizenId(cbCitizen.getValue().getId());
            record.setType(rbTamVang.isSelected() ? "TAM_VANG" : "TAM_TRU");
            record.setStartDate(dpStartDate.getValue());
            record.setEndDate(cbNoExpiry.isSelected() ? null : dpEndDate.getValue());
            record.setLocation(tfLocation.getText().trim());
            record.setNote(taNote.getText().trim().isEmpty() ? null : taNote.getText().trim());

            TemporaryRecordDao.insert(record);

            String type = rbTamVang.isSelected() ? "tạm vắng" : "tạm trú";
            showInfo("Thành công", "Đã cấp giấy " + type + "!");

            if (onAddSuccess != null) onAddSuccess.run();
            close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể lưu: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        if (stage != null) stage.close();
    }
}
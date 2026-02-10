package com.example.demo4.controllers;

import com.example.demo4.dao.CitizenDao;
import com.example.demo4.models.Citizen;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddHomelessCitizenController extends BaseController {

    @FXML private TextField tfFullName;
    @FXML private DatePicker dpDob;
    @FXML private TextField tfCccd;
    @FXML private TextField tfJob;

    @FXML private Label lblMessage;

    private HomelessCitizenController parent;

    public void setParent(HomelessCitizenController parent) {
        this.parent = parent;
    }

    @FXML
    private void save() {
        try {
            // ✅ CHỈ CHECK TỐI THIỂU
            if (tfFullName.getText().isBlank()
                    || dpDob.getValue() == null
                    || tfCccd.getText().isBlank()) {

                lblMessage.setText("Thiếu thông tin bắt buộc!");
                return;
            }

            if (!tfCccd.getText().trim().matches("\\d{12}")) {
                lblMessage.setText("CCCD phải đúng 12 chữ số!");
                return;
            }

            Citizen c = new Citizen();

            // ===== BẮT BUỘC =====
            c.setFullName(tfFullName.getText().trim());
            c.setDob(dpDob.getValue());
            c.setCccd(tfCccd.getText().trim());

            // ===== TÙY CHỌN =====
            c.setJob(blankToNull(tfJob.getText()));

            // ===== TRẠNG THÁI HOMELESS =====
            c.setHouseholdId(null);
            c.setHouseholder(false);
            c.setRelation(null);
            c.setUserId(null);

            // ===== CÁC FIELD KHÁC ĐỂ NULL =====
            c.setAlias(null);
            c.setPlaceOfBirth(null);
            c.setHometown(null);
            c.setEthnicity(null);
            c.setCccdIssueDate(null);
            c.setCccdIssuePlace(null);
            c.setWorkplace(null);
            c.setPreviousAddress(null);
            c.setRegisterDate(null);

            CitizenDao.insert(c);

            if (parent != null) parent.reload();
            close();

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Lỗi thêm công dân!");
        }
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        Stage s = (Stage) tfFullName.getScene().getWindow();
        s.close();
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

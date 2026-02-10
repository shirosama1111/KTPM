package com.example.demo4.controllers;

import com.example.demo4.Session;
import com.example.demo4.dao.CitizenDao;
import com.example.demo4.dao.HouseholdDao;
import com.example.demo4.models.Citizen;
import com.example.demo4.models.Household;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddHouseholdController extends BaseController {

    @FXML private ComboBox<Citizen> cbHeadCitizen;
    @FXML private TextField tfAddress;

    private Stage stage;
    private Runnable onAddSuccess;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOnAddSuccess(Runnable r) {
        this.onAddSuccess = r;
    }

    @FXML
    public void initialize() {
        try {
            cbHeadCitizen.setItems(
                    FXCollections.observableArrayList(CitizenDao.findAll())
            );
            cbHeadCitizen.setConverter(new StringConverter<>() {
                @Override
                public String toString(Citizen c) {
                    return c == null ? "" : c.getFullName() + " - " + c.getCccd();
                }
                @Override public Citizen fromString(String s) { return null; }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {

        if (tfAddress.getText().trim().isEmpty()) {
            showWarning("Lỗi", "Địa chỉ không được để trống!");
            return;
        }

        Integer ownerId = Session.getCurrentUserId();
        if (ownerId == null) {
            showWarning("Lỗi", "Chưa đăng nhập!");
            return;
        }

        Citizen head = cbHeadCitizen.getValue();
        Integer headCitizenId = head == null ? null : head.getId();

        try {
            Household h = new Household(
                    0,
                    headCitizenId,
                    tfAddress.getText().trim(),
                    ownerId
            );

            // Thay cho đoạn insert cũ
            HouseholdDao.insert(h);
            int newHouseholdId = HouseholdDao.getNextHouseholdId(); // lấy id mới
            h.setHouseholdId(newHouseholdId);

// Gán chủ hộ vào hộ khẩu
            if (headCitizenId != null) {
                var citizen = CitizenDao.findById(headCitizenId);
                if (citizen != null) {
                    citizen.setHouseholdId(newHouseholdId);
                    CitizenDao.update(citizen);
                }
            }


            showInfo("Thành công", "Đã thêm hộ khẩu mới!");

            if (onAddSuccess != null) onAddSuccess.run();
            closeStage();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể thêm hộ khẩu!");
        }
    }


    @FXML
    private void handleCancel() {
        closeStage();
    }

    // ================= HELPER =================
    private Integer parseInteger(String s) {
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            showWarning("Sai dữ liệu", "ID chủ hộ phải là số!");
            return Integer.MIN_VALUE;
        }
    }

    private void closeStage() {
        if (stage != null) stage.close();
    }
}
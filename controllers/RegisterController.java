package com.example.demo4.controllers;

import com.example.demo4.Main;
import com.example.demo4.dao.UserDao;
import com.example.demo4.dao.CitizenDao;
import com.example.demo4.models.User;
import com.example.demo4.models.Citizen;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController extends BaseController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtPassword2;
    @FXML private TextField txtFullname;
    @FXML private TextField txtEmail;
    @FXML private ChoiceBox<String> roleBox;
    @FXML private Label lblMessage;

    @FXML
    public void initialize() {
        roleBox.getItems().add("CUSTOMER");
        roleBox.setValue("CUSTOMER");
    }

    @FXML
    public void onRegister(ActionEvent e) {

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String password2 = txtPassword2.getText();
        String fullName = txtFullname.getText().trim();
        String email = txtEmail.getText().trim();
        String role = roleBox.getValue();

        // ===== VALIDATE CƠ BẢN =====
        if (username.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            showWarning("Thiếu thông tin", "Vui lòng nhập đầy đủ username và password!");
            return;
        }

        if (!password.equals(password2)) {
            showWarning("Sai mật khẩu", "Mật khẩu nhập lại không khớp!");
            return;
        }

        // ===== ✅ VALIDATE MẬT KHẨU MẠNH =====
        if (password.length() < 8) {
            showWarning("Mật khẩu yếu", "Mật khẩu phải có ít nhất 8 ký tự!");
            return;
        }

        try {
            // ===== CHECK USERNAME TRÙNG =====
            if (UserDao.findByUsername(username) != null) {
                showWarning("Trùng username", "Tên đăng nhập đã tồn tại!");
                return;
            }

            // ===== TẠO CITIZEN TỐI THIỂU =====
            Citizen citizen = new Citizen();
            citizen.setFullName(fullName.isBlank() ? username : fullName);
            citizen.setRelation(null);
            citizen.setHouseholder(false);
            citizen.setHouseholdId(null);
            citizen.setUserId(null);

            // ===== ⚠️ CCCD ĐỂ NULL CHO REGISTER =====
            // Người dùng sẽ cập nhật CCCD sau khi đăng ký thành công
            citizen.setCccd(null);

            // ===== DOB ĐỂ NULL =====
            citizen.setDob(null);

            int citizenId = CitizenDao.insert(citizen);

            // ===== TẠO USER =====
            // TODO: Hash password trước khi lưu (cần thêm BCrypt library)
            User user = new User(username, password, role, fullName, email);
            int userId = UserDao.insert(user);

            // ===== GÁN USER ↔ CITIZEN =====
            CitizenDao.assignUser(citizenId, userId);

            showInfo("Thành công",
                    "Đăng ký thành công!\n" +
                            "Vui lòng cập nhật CCCD trong phần Quản lý tài khoản.");
            Main.showLogin();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Lỗi", "Không thể đăng ký: " + ex.getMessage());
        }
    }

    @FXML
    public void onBack(ActionEvent e) throws Exception {
        Main.showLogin();
    }
}

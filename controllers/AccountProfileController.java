package com.example.demo4.controllers;

import com.example.demo4.Session;
import com.example.demo4.dao.UserDao;
import com.example.demo4.dao.CitizenDao;
import com.example.demo4.models.Citizen;
import com.example.demo4.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AccountProfileController extends BaseController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblCitizenInfo;
    @FXML private Label lblMessage;

    private User currentUser;

    @FXML
    public void initialize() {
        try {
            currentUser = UserDao.findById(Session.getCurrentUserId());

            txtUsername.setText(currentUser.getUsername());

            Citizen c = CitizenDao.findByUserId(currentUser.getId());
            if (c != null) {
                lblCitizenInfo.setText(
                        c.getFullName() + " | CCCD: " + c.getCccd()
                );
            } else {
                lblCitizenInfo.setText("⚠ Chưa gán công dân cho tài khoản!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateAccount() {
        String newUsername = txtUsername.getText().trim();
        String newPassword = txtPassword.getText();
        boolean changed = false;

        try {
            if (!newUsername.equals(currentUser.getUsername())) {
                if (UserDao.findByUsername(newUsername) != null) {
                    showWarning("Trùng username",
                            "Tên đăng nhập đã tồn tại!");
                    return;
                }
                currentUser.setUsername(newUsername);
                changed = true;
            }

            if (!newPassword.isBlank()) {
                currentUser.setPassword(newPassword);
                changed = true;
            }

            if (newUsername.isBlank()) {
                showWarning("Lỗi", "Username không được để trống!");
                return;
            }

            if (!changed) {
                showWarning("Thông báo", "Bạn chưa thay đổi thông tin nào!");
                return;
            }

            UserDao.update(currentUser);
            showInfo("Thành công", "Cập nhật tài khoản thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Lỗi cập nhật tài khoản!");
        }
    }
}
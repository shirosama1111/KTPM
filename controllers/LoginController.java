package com.example.demo4.controllers;

import com.example.demo4.Main;
import com.example.demo4.Session;
import com.example.demo4.dao.UserDao;
import com.example.demo4.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Button btnRegister;
    @FXML private Label lblMessage;

    @FXML
    private void initialize() {
    }

    @FXML
    public void onLogin(javafx.event.ActionEvent e) {
        String u = txtUsername.getText().trim();
        String p = txtPassword.getText().trim();

        if (u.isEmpty() || p.isEmpty()) {
            lblMessage.setText("Nhập đầy đủ username và password");
            return;
        }

        try {
            // dùng UserDao thay vì JDBC thô
            User user = UserDao.findByUsernameAndPassword(u, p);

            if (user != null) {
                // Lưu vào Session
                Session.login(user.getId(), user.getUsername(), user.getRole());

                lblMessage.setText("Đăng nhập thành công!");
                Main.showWelcome();
            } else {
                lblMessage.setText("Sai username hoặc password");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            lblMessage.setText("Lỗi kết nối DB: " + ex.getMessage());
        }
    }

    @FXML
    public void onShowRegister(javafx.event.ActionEvent e) throws Exception {
        Main.showRegister();
    }
}
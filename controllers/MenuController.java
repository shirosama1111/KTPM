package com.example.demo4.controllers;

import com.example.demo4.Main;
import com.example.demo4.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MenuController extends BaseController{

    @FXML
    public void goToManagement() throws Exception {
        if (!Session.isLoggedIn()) {
            Main.showLogin();
            return;
        }

        String role = Session.getCurrentRole();
        if ("ADMIN".equalsIgnoreCase(role)) {
            Main.showAdmin();
        } else {
            Main.showCustomer();
        }
    }

    @FXML
    public void goToCitizenManagement() throws Exception {
        if (!Session.isLoggedIn()) {
            Main.showLogin();
            return;
        }

        String role = Session.getCurrentRole();
        if ("ADMIN".equalsIgnoreCase(role)) {
            Main.showAdminHousehold();
        } else {
            Main.showCustomerHousehold();
        }
    }

    @FXML
    public void logout() throws Exception {
        Main.logout();   // đã clear Session bên trong
    }

    @FXML private Label lblUsername;
    @FXML private VBox cardAdmin;   // Card cho Admin
    @FXML private VBox cardCustomer;
    @FXML
    public void initialize() {
        if (Session.isLoggedIn()) {
            lblUsername.setText(Session.getCurrentUsername()
                    + " (" + Session.getCurrentRole() + ")");
        }
        String role = Session.getCurrentRole();
        if ("ADMIN".equalsIgnoreCase(role)) {
            showAdminMode();
        } else {
            showCustomerMode();
        }
    }

    private void showAdminMode() {
        // HIỆN card Account
        cardAdmin.setVisible(true);
        cardAdmin.setManaged(true);

        // ẨN card Cultural (Nhà văn hóa)
        cardCustomer.setVisible(false);
        cardCustomer.setManaged(false); // Quan trọng: setManaged(false) để nó không chiếm chỗ
    }

    private void showCustomerMode() {
        // ẨN card Account
        cardAdmin.setVisible(false);
        cardAdmin.setManaged(false);

        // HIỆN card Cultural
        cardCustomer.setVisible(true);
        cardCustomer.setManaged(true);
    }
    @FXML
    private void openAccountProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/demo4/account_profile.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Quản lý tài khoản");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToStatistics() throws Exception {
        if (!Session.isLoggedIn()) {
            Main.showLogin();
            return;
        }

        String role = Session.getCurrentRole();
        if ("ADMIN".equalsIgnoreCase(role)) {
            Main.showStatistics();
        } else {
            showWarning("Không có quyền", "Chỉ ADMIN mới xem được thống kê!");
        }
    }

    @FXML
    public void goToTemporaryRecords() throws Exception {
        if (!Session.isLoggedIn()) {
            Main.showLogin();
            return;
        }

        Main.showTemporaryRecords();
    }
}
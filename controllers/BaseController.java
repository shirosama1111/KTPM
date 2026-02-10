package com.example.demo4.controllers;

import com.example.demo4.Session;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public abstract class BaseController {

    protected void showInfo(String title, String msg) {
        showAlert(title, msg, Alert.AlertType.INFORMATION);
    }

    protected void showWarning(String title, String msg) {
        showAlert(title, msg, Alert.AlertType.WARNING);
    }

    protected void showError(String title, String msg) {
        showAlert(title, msg, Alert.AlertType.ERROR);
    }

    protected void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Chỉ cho ADMIN dùng chức năng.
     * @return true nếu là ADMIN, false nếu không (và đã show Alert).
     */
    protected boolean requireAdmin() {
        String role = Session.getCurrentRole();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            showAlert("Không có quyền", "Chỉ ADMIN mới dùng chức năng này!", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    protected boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(ok, cancel);

        return alert.showAndWait().orElse(cancel) == ok;
    }


    protected boolean requireLogin() {
        if (Session.getCurrentUserId() == null) {
            showWarning("Chưa đăng nhập", "Vui lòng đăng nhập lại!");
            return false;
        }
        return true;
    }

    /**
     * Hộp thoại xác nhận, trả về true nếu user chọn OK.
     */
    protected boolean showConfirm(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    protected boolean showConfirmWithHeader(String title, String header, String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(msg);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
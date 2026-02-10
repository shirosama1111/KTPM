package com.example.demo4.controllers;

import com.example.demo4.Main;
import com.example.demo4.dao.UserDao;
import com.example.demo4.dto.UserRowData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.List;

public class AdminController extends BaseController {

    @FXML private TableView<UserRow> tblUsers;
    @FXML private TableColumn<UserRow, String> colUsername;
    @FXML private TableColumn<UserRow, String> colCitizen;
    @FXML private TableColumn<UserRow, String> colRole;

    @FXML
    public void initialize() {
        colUsername.setCellValueFactory(c -> c.getValue().usernameProperty());
        colCitizen.setCellValueFactory(c -> c.getValue().citizenNameProperty());
        colRole.setCellValueFactory(c -> c.getValue().roleProperty());
        loadUsers();
    }

    // ================= LOAD USERS =================
    private void loadUsers() {
        try {
            List<UserRowData> data = UserDao.findAllWithCitizenName();
            ObservableList<UserRow> rows = FXCollections.observableArrayList();

            for (UserRowData d : data) {
                rows.add(new UserRow(
                        d.id,
                        d.username,
                        d.citizenName,
                        d.role
                ));
            }

            tblUsers.setItems(rows);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể tải danh sách user!");
        }
    }

    // ================= ACTIONS =================
    @FXML
    public void onAssignAccount() {
        if (!requireAdmin()) return;
        openWindow("/com/example/demo4/assign_account.fxml",
                "Gán tài khoản cho công dân");
    }

    @FXML
    public void onDelete() {
        if (!requireAdmin()) return;

        UserRow sel = tblUsers.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showWarning("Thiếu chọn", "Chọn user để xóa");
            return;
        }

        try {
            UserDao.deleteById(sel.getId());
            showInfo("Thành công", "Xóa user thành công");
            loadUsers();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Lỗi", ex.getMessage());
        }
    }

    @FXML
    public void onLogout() throws Exception {
        Main.logout();
    }

    @FXML
    public void backToMenu() throws Exception {
        Main.showMenu();
    }

    @FXML
    public void onViewPastEvents() {
        if (!requireAdmin()) return;
        openWindow("/com/example/demo4/past_events.fxml",
                "Sự kiện đã quá 30 ngày");
    }

    @FXML
    public void onApproveEvents() {
        if (!requireAdmin()) return;
        openWindow("/com/example/demo4/event_approval.fxml",
                "Duyệt sự kiện");
    }

    @FXML
    public void onManageAssets() {
        if (!requireAdmin()) return;
        openWindow("/com/example/demo4/assets.fxml",
                "Quản lý cơ sở vật chất");
    }

    // ================= HELPER =================
    private void openWindow(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể mở màn hình: " + title);
        }
    }

    // ================= INNER CLASS =================
    public static class UserRow {
        private final javafx.beans.property.SimpleIntegerProperty id;
        private final javafx.beans.property.SimpleStringProperty username;
        private final javafx.beans.property.SimpleStringProperty citizenName;
        private final javafx.beans.property.SimpleStringProperty role;

        public UserRow(int id, String username, String citizenName, String role) {
            this.id = new javafx.beans.property.SimpleIntegerProperty(id);
            this.username = new javafx.beans.property.SimpleStringProperty(username);
            this.citizenName = new javafx.beans.property.SimpleStringProperty(
                    citizenName == null ? "—" : citizenName
            );
            this.role = new javafx.beans.property.SimpleStringProperty(role);
        }

        public int getId() {
            return id.get();
        }

        public javafx.beans.property.StringProperty usernameProperty() {
            return username;
        }

        public javafx.beans.property.StringProperty citizenNameProperty() {
            return citizenName;
        }

        public javafx.beans.property.StringProperty roleProperty() {
            return role;
        }
    }

    @FXML
    public void onStatistics() {
        if (!requireAdmin()) return;
        openWindow("/com/example/demo4/statistics.fxml", "Thống kê nhân khẩu");
    }

    @FXML
    public void onTemporaryRecords() {
        if (!requireAdmin()) return;
        openWindow("/com/example/demo4/temporary_records.fxml", "Quản lý tạm vắng/tạm trú");
    }

    @FXML
    public void onCitizenHistory() {
        if (!requireAdmin()) return;

        showInfo("Hướng dẫn", "Vui lòng vào Quản lý hộ khẩu, chọn công dân rồi xem lịch sử.");
    }
}
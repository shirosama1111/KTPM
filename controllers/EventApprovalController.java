package com.example.demo4.controllers;

import com.example.demo4.dao.EventDao;
import com.example.demo4.models.Event;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class EventApprovalController extends BaseController {

    @FXML private TableView<EventRow> tblEvents;
    @FXML private TableColumn<EventRow, String> colTitle;
    @FXML private TableColumn<EventRow, String> colDate;
    @FXML private TableColumn<EventRow, String> colLocation;
    @FXML private TableColumn<EventRow, String> colStatus;

    @FXML private Label lblMessage;

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(c -> c.getValue().titleProperty());
        colDate.setCellValueFactory(c -> c.getValue().dateProperty());
        colLocation.setCellValueFactory(c -> c.getValue().locationProperty());
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());

        loadEvents();
    }

    private void loadEvents() {
        ObservableList<EventRow> list = FXCollections.observableArrayList();
        try {
            for (Event e : EventDao.findAll()) {
                list.add(new EventRow(
                        e.getId(),
                        e.getTitle(),
                        e.getDate(),
                        e.getLocation(),
                        e.getStatus()
                ));
            }
            tblEvents.setItems(list);
            lblMessage.setText("");
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    @FXML
    private void onMarkPaid() {
        if (!requireAdmin()) return;
        changeStatus(Event.STATUS_PAID);
    }

    @FXML
    private void onMarkConfirmed() {
        if (!requireAdmin()) return;
        changeStatus(Event.STATUS_CONFIRMED);
    }

    @FXML
    private void onMarkCancelled() {
        if (!requireAdmin()) return;
        changeStatus(Event.STATUS_CANCELLED);
    }

    private void changeStatus(String newStatus) {
        EventRow sel = tblEvents.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showWarning("Chọn sự kiện", "Hãy chọn sự kiện để cập nhật trạng thái!");
            return;
        }

        try {
            EventDao.updateStatus(sel.getId(), newStatus);
            showInfo("Thành công", "Cập nhật trạng thái thành công!");
            loadEvents();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể cập nhật trạng thái!");
        }
    }

    // ====== Row hiển thị trong TableView ======
    public static class EventRow {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty title;
        private final SimpleStringProperty date;
        private final SimpleStringProperty location;
        private final SimpleStringProperty status;

        public EventRow(int id, String title, String date, String location, String status) {
            this.id = new SimpleIntegerProperty(id);
            this.title = new SimpleStringProperty(title);
            this.date = new SimpleStringProperty(date);
            this.location = new SimpleStringProperty(location);
            this.status = new SimpleStringProperty(status);
        }

        public int getId() { return id.get(); }
        public SimpleStringProperty titleProperty() { return title; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty locationProperty() { return location; }
        public SimpleStringProperty statusProperty() { return status; }
    }
}
package com.example.demo4.controllers;

import com.example.demo4.EventStatusUtil;
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

import java.time.LocalDate;

public class PastEventsController extends BaseController {

    private static boolean updated = false;

    @FXML private TableView<EventRow> tblPastEvents;
    @FXML private TableColumn<EventRow, String> colTitle;
    @FXML private TableColumn<EventRow, String> colDate;
    @FXML private TableColumn<EventRow, String> colLocation;
    @FXML private TableColumn<EventRow, String> colStatus;
    @FXML private Label lblMessage;

    @FXML
    public void initialize() {

        if (!requireAdmin()) {
            tblPastEvents.setDisable(true);
            lblMessage.setText("Bạn không có quyền xem danh sách này.");
            return;
        }

        if (!updated) {
            EventStatusUtil.autoUpdatePastEvents();
            updated = true;
        }

        colTitle.setCellValueFactory(c -> c.getValue().titleProperty());
        colDate.setCellValueFactory(c -> c.getValue().dateProperty());
        colLocation.setCellValueFactory(c -> c.getValue().locationProperty());
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());

        loadPastEvents();
    }


    private void loadPastEvents() {
        ObservableList<EventRow> list = FXCollections.observableArrayList();
        LocalDate limitDate = LocalDate.now().minusDays(30);

        try {
            // dùng EventDao thay vì JDBC thô
            for (Event e : EventDao.findPastBefore(limitDate)) {
                list.add(new EventRow(
                        e.getId(),
                        e.getTitle(),
                        e.getDate(),
                        e.getLocation(),
                        e.getStatus()
                ));
            }

            tblPastEvents.setItems(list);
            lblMessage.setText("Tổng số sự kiện cũ: " + list.size());

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Lỗi tải dữ liệu");
            showError("Lỗi", "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

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
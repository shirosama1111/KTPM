package com.example.demo4.controllers;

import com.example.demo4.EventStatusUtil;
import com.example.demo4.Main;
import com.example.demo4.Session;
import com.example.demo4.dao.BookingDao;
import com.example.demo4.dao.EventDao;
import com.example.demo4.models.Booking;
import com.example.demo4.models.Event;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;

public class CustomerController extends BaseController {

    public static class EventRow {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty title;
        private final SimpleStringProperty date;
        private final SimpleStringProperty start;
        private final SimpleStringProperty end;
        private final SimpleStringProperty location;
        private final SimpleStringProperty description;
        private final SimpleStringProperty status;

        public EventRow(int id, String title, String date,
                        String start, String end,
                        String location, String description, String status) {
            this.id = new SimpleIntegerProperty(id);
            this.title = new SimpleStringProperty(title == null ? "" : title);
            this.date = new SimpleStringProperty(date == null ? "" : date);
            this.start = new SimpleStringProperty(start == null ? "" : start);
            this.end = new SimpleStringProperty(end == null ? "" : end);
            this.location = new SimpleStringProperty(location == null ? "" : location);
            this.description = new SimpleStringProperty(description == null ? "" : description);
            this.status = new SimpleStringProperty(status == null ? "ĐĂNG KÝ" : status);
        }

        public int getId() { return id.get(); }

        public SimpleStringProperty titleProperty() { return title; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty startProperty() { return start; }
        public SimpleStringProperty endProperty() { return end; }
        public SimpleStringProperty locationProperty() { return location; }
        public SimpleStringProperty descriptionProperty() { return description; }
        public SimpleStringProperty statusProperty() { return status; }
    }

    @FXML private TableView<EventRow> eventTable;
    @FXML private TableColumn<EventRow,String> colTitle;
    @FXML private TableColumn<EventRow,String> colDate;
    @FXML private TableColumn<EventRow,String> colStart;
    @FXML private TableColumn<EventRow,String> colEnd;
    @FXML private TableColumn<EventRow,String> colLocation;
    @FXML private TableColumn<EventRow,String> colStatus;

    private TextField titleFilterField;
    private TextField locationFilterField;
    private DatePicker dateFilterPicker;

    private final ObservableList<EventRow> masterList = FXCollections.observableArrayList();
    private FilteredList<EventRow> filteredList;

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(c -> c.getValue().titleProperty());
        colDate.setCellValueFactory(c -> c.getValue().dateProperty());
        colStart.setCellValueFactory(c -> c.getValue().startProperty());
        colEnd.setCellValueFactory(c -> c.getValue().endProperty());
        colLocation.setCellValueFactory(c -> c.getValue().locationProperty());
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());

        filteredList = new FilteredList<>(masterList, e -> true);
        SortedList<EventRow> sorted = new SortedList<>(filteredList);
        sorted.comparatorProperty().bind(eventTable.comparatorProperty());
        eventTable.setItems(sorted);

        addColumnFilter(colTitle, "text");
        addColumnFilter(colLocation, "text");
        addColumnFilter(colDate, "date");

        loadEvents();
    }

    @FXML
    public void loadEvents() {
        EventStatusUtil.autoUpdatePastEvents();
        LocalDate minDate = LocalDate.now().minusDays(30);

        masterList.clear();
        try {
            for (Event e : EventDao.findUpcomingFrom(minDate)) {
                masterList.add(new EventRow(
                        e.getId(),
                        e.getTitle(),
                        e.getDate(),
                        trimTime(e.getStartTime()),
                        trimTime(e.getEndTime()),
                        e.getLocation(),
                        e.getDescription(),
                        e.getStatus()
                ));
            }
        } catch (Exception e) {
            showError("Lỗi", "Không tải được sự kiện!");
        }
    }

    private String trimTime(String t) {
        return t == null ? "" : t.substring(0, 5);
    }

    @FXML
    private void onResetTable() {
        if (titleFilterField != null) titleFilterField.clear();
        if (locationFilterField != null) locationFilterField.clear();
        if (dateFilterPicker != null) dateFilterPicker.setValue(null);
        filteredList.setPredicate(e -> true);
    }

    // ===== FILTER HEADER =====
    private void addColumnFilter(TableColumn<EventRow, ?> column, String type) {

        if (type.equals("text")) {
            TextField tf = new TextField();
            tf.setPromptText("Lọc");

            if (column == colTitle) titleFilterField = tf;
            if (column == colLocation) locationFilterField = tf;

            tf.textProperty().addListener((obs,o,n) -> applyFilter());
            column.setGraphic(new VBox(new Label(column.getText()), tf));
        }

        if (type.equals("date")) {
            DatePicker dp = new DatePicker();
            dateFilterPicker = dp;
            dp.valueProperty().addListener((obs,o,n) -> applyFilter());
            column.setGraphic(new VBox(new Label(column.getText()), dp));
        }
    }

    private void applyFilter() {
        filteredList.setPredicate(e -> {
            boolean okTitle = titleFilterField == null
                    || titleFilterField.getText().isBlank()
                    || e.titleProperty().get().toLowerCase()
                    .contains(titleFilterField.getText().toLowerCase());

            boolean okLocation = locationFilterField == null
                    || locationFilterField.getText().isBlank()
                    || e.locationProperty().get().toLowerCase()
                    .contains(locationFilterField.getText().toLowerCase());

            boolean okDate = dateFilterPicker == null
                    || dateFilterPicker.getValue() == null
                    || e.dateProperty().get()
                    .equals(dateFilterPicker.getValue().toString());

            return okTitle && okLocation && okDate;
        });
    }

    @FXML
    private void backToMenu() {
        try {
            Main.showMenu();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không quay lại menu được!");
        }
    }

    @FXML
    private void onLogout() {
        try {
            Main.logout();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không đăng xuất được!");
        }
    }

    @FXML
    private void openAssetRental() {

        // 1. Phải chọn sự kiện
        EventRow selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn", "Vui lòng chọn một sự kiện!");
            return;
        }

        try {
            // 2. Lấy booking của sự kiện
            Booking booking = BookingDao.findByEventId(selected.getId());

            if (booking == null) {
                showWarning("Không hợp lệ", "Sự kiện chưa có booking!");
                return;
            }

            // 3. Kiểm tra quyền: chỉ chủ sự kiện mới được thuê
            if (booking.getUserId() != Session.getCurrentUserId()) {
                showWarning(
                        "Không có quyền",
                        "Bạn không phải người tạo sự kiện này!"
                );
                return;
            }

            // 4. ĐÚNG NGƯỜI → mở form thuê tài sản
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/demo4/rent_asset.fxml")
            );
            Parent root = loader.load();

            RentAssetController controller = loader.getController();
            controller.setBookingId(booking.getId()); // 🔥 QUAN TRỌNG

            Stage stage = new Stage();
            stage.setTitle("Thuê tài sản");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không mở được thuê tài sản!");
        }
    }


    @FXML
    private void onAddEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/demo4/add_event.fxml")
            );
            Parent root = loader.load();

            AddEventController controller = loader.getController();
            controller.setCustomerController(this);

            Stage stage = new Stage();
            stage.setTitle("Thêm sự kiện");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không mở được form thêm sự kiện");
        }
    }

    @FXML
    private void onOpenUpdate() {

        EventRow selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn", "Vui lòng chọn một sự kiện để cập nhật!");
            return;
        }

        try {
            // 🔒 CHECK QUYỀN
            Booking booking = BookingDao.findByEventId(selected.getId());

            if (booking == null) {
                showWarning("Không hợp lệ", "Sự kiện chưa có booking!");
                return;
            }

            if (booking.getUserId() != Session.getCurrentUserId()) {
                showWarning(
                        "Không có quyền",
                        "Bạn không phải người tạo sự kiện này!"
                );
                return;
            }

            // ✅ ĐÚNG NGƯỜI → CHO SỬA
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/demo4/update_event.fxml")
            );
            Parent root = loader.load();

            UpdateEventController controller = loader.getController();
            controller.setCustomerController(this);
            controller.setEventData(
                    selected.getId(),
                    selected.titleProperty().get(),
                    LocalDate.parse(selected.dateProperty().get()),
                    selected.startProperty().get(),
                    selected.endProperty().get(),
                    selected.locationProperty().get(),
                    selected.descriptionProperty().get(),
                    selected.statusProperty().get()
            );

            Stage stage = new Stage();
            stage.setTitle("Cập nhật sự kiện");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không mở được form cập nhật");
        }
    }

}
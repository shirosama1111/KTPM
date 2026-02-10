package com.example.demo4.controllers;

import com.example.demo4.dao.EventDao;
import com.example.demo4.models.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.example.demo4.Session;
import com.example.demo4.dao.BookingDao;
import com.example.demo4.models.Booking;

import java.time.LocalDate;
import java.time.LocalTime;

public class AddEventController extends BaseController {

    @FXML private TextField txtTitle;
    @FXML private TextArea txtDesc;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> cbStartHour, cbStartMinute, cbEndHour, cbEndMinute;
    @FXML private ComboBox<String> txtLocation;
    @FXML private Label lblMessage;

    private Stage stage;
    private CustomerController customerController;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCustomerController(CustomerController controller) {
        this.customerController = controller;
    }

    @FXML
    public void initialize() {
        txtLocation.getItems().addAll(
                "Hội trường rộng tầng 1",
                "Phòng chức năng tầng 2"
        );

        for (int h = 0; h < 24; h++) {
            cbStartHour.getItems().add(String.format("%02d", h));
            cbEndHour.getItems().add(String.format("%02d", h));
        }
        for (int m = 0; m < 60; m += 5) {
            cbStartMinute.getItems().add(String.format("%02d", m));
            cbEndMinute.getItems().add(String.format("%02d", m));
        }
    }

    @FXML
    public void onSave() {

        if (!validateInput()) return;

        LocalDate date = datePicker.getValue();
        String start = cbStartHour.getValue() + ":" + cbStartMinute.getValue();
        String end   = cbEndHour.getValue()   + ":" + cbEndMinute.getValue();

        try {
            if (EventDao.hasTimeConflict(date, start, end)){
                showError("Trùng giờ", "Sự kiện này trùng giờ với sự kiện khác!");
                return;
            }

            Event event = new Event(
                    -1,
                    txtTitle.getText().trim(),
                    date.toString(),
                    start,
                    end,
                    txtLocation.getValue(),
                    txtDesc.getText().trim(),
                    Event.STATUS_REGISTERED
            );

            // ✅ 1. INSERT EVENT
            int eventId = EventDao.insertWithCheck(event);

            // ✅ 2. INSERT BOOKING NGƯỜI TẠO
            Booking booking = new Booking(
                    0,
                    Session.getCurrentUserId(),
                    eventId,
                    "CREATOR",
                    "UNPAID"
            );
            BookingDao.insert(booking);

            showInfo("Thành công", "Tạo sự kiện thành công!");

            if (customerController != null)
                customerController.loadEvents();

            closeStage();

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("❌ Lỗi: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (txtTitle.getText().trim().isEmpty() ||
                datePicker.getValue() == null ||
                cbStartHour.getValue() == null ||
                cbStartMinute.getValue() == null ||
                cbEndHour.getValue() == null ||
                cbEndMinute.getValue() == null ||
                txtLocation.getValue() == null) {

            showWarning("Lỗi", "Nhập đầy đủ thông tin!");
            return false;
        }

        try {
            LocalTime start = LocalTime.parse(cbStartHour.getValue() + ":" + cbStartMinute.getValue());
            LocalTime end   = LocalTime.parse(cbEndHour.getValue() + ":" + cbEndMinute.getValue());

            if (!start.isBefore(end)) {
                showWarning("Lỗi giờ", "Giờ bắt đầu phải nhỏ hơn giờ kết thúc!");
                return false;
            }
        } catch (Exception e) {
            showWarning("Lỗi giờ", "Định dạng giờ không hợp lệ!");
            return false;
        }

        if (datePicker.getValue().isBefore(LocalDate.now())) {
            showWarning("Lỗi ngày", "Không thể tạo sự kiện trong quá khứ!");
            return false;
        }

        return true;
    }

    private void closeStage() {
        if (stage != null) stage.close();
    }

    @FXML
    public void onCancel() {
        if (stage != null) stage.close();
    }
}
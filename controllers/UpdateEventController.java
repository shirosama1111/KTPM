package com.example.demo4.controllers;

import com.example.demo4.dao.EventDao;
import com.example.demo4.models.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class UpdateEventController extends BaseController {

    private CustomerController customerController;

    public void setCustomerController(CustomerController controller) {
        this.customerController = controller;
    }

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> cbStartHour, cbStartMinute, cbEndHour, cbEndMinute;
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDesc;
    @FXML private ComboBox<String> txtLocation;

    private int eventId;
    private String currentStatus;   // giữ status cũ để không làm mất

    public void setEventData(int id, String title, LocalDate date,
                             String start, String end,
                             String location, String desc,
                             String status) {
        this.eventId = id;
        this.currentStatus = status;

        txtTitle.setText(title);
        datePicker.setValue(date);
        txtLocation.setValue(location);
        txtDesc.setText(desc);

        String[] startSplit = start.split(":");
        String[] endSplit   = end.split(":");

        if (startSplit.length >= 2) {
            cbStartHour.setValue(startSplit[0]);
            cbStartMinute.setValue(startSplit[1]);
        }
        if (endSplit.length >= 2) {
            cbEndHour.setValue(endSplit[0]);
            cbEndMinute.setValue(endSplit[1]);
        }
    }

    @FXML
    public void initialize() {
        // populate giờ/phút
        for (int h = 0; h < 24; h++) {
            String s = String.format("%02d", h);
            cbStartHour.getItems().add(s);
            cbEndHour.getItems().add(s);
        }
        for (int m = 0; m < 60; m += 5) {
            String s = String.format("%02d", m);
            cbStartMinute.getItems().add(s);
            cbEndMinute.getItems().add(s);
        }

        // populate địa điểm
        txtLocation.getItems().addAll(
                "Hội trường rộng tầng 1",
                "Phòng chức năng tầng 2"
        );
    }

    @FXML
    public void onSave() {
        String title    = txtTitle.getText().trim();
        LocalDate date  = datePicker.getValue();
        String sh       = cbStartHour.getValue();
        String sm       = cbStartMinute.getValue();
        String eh       = cbEndHour.getValue();
        String em       = cbEndMinute.getValue();
        String location = txtLocation.getValue();
        String desc     = txtDesc.getText().trim();

        if (title.isEmpty() || date == null ||
                sh == null || sm == null || eh == null || em == null ||
                location == null || location.isEmpty()) {
            showWarning("Nhập thiếu", "Vui lòng nhập đầy đủ thông tin sự kiện!");
            return;
        }

        String start = sh + ":" + sm;
        String end   = eh + ":" + em;

        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(start);
            endTime   = LocalTime.parse(end);
        } catch (DateTimeParseException ex) {
            showWarning("Lỗi giờ", "Định dạng giờ không hợp lệ (HH:mm)!");
            return;
        }

        if (!startTime.isBefore(endTime)) {
            showWarning("Lỗi giờ", "Giờ bắt đầu phải nhỏ hơn giờ kết thúc!");
            return;
        }

        try {
            // Tạo Event và cập nhật qua DAO
            Event e = new Event(
                    eventId,
                    title,
                    date.toString(),
                    start,
                    end,
                    location,
                    desc,
                    currentStatus
            );

            EventDao.update(e);
            showInfo("Thành công", "Cập nhật sự kiện thành công!");

            if (customerController != null) {
                customerController.loadEvents(); // reload bảng ở CustomerController
            }

            Stage stage = (Stage) txtTitle.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể cập nhật sự kiện: " + e.getMessage());
        }
    }

    @FXML
    public void onCancel() {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.close();
    }


}
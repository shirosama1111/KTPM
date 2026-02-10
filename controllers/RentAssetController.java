package com.example.demo4.controllers;

import com.example.demo4.dao.BookingAssetDao;
import com.example.demo4.dao.BookingDao;
import com.example.demo4.dao.EventDao;
import com.example.demo4.models.Booking;
import com.example.demo4.models.Event;
import com.example.demo4.models.assets;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RentAssetController extends BaseController {

    @FXML private ComboBox<assets> cbAsset;
    @FXML private TextField tfQuantity;
    @FXML private TextArea taConditionOut;

    private int bookingId;

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    @FXML
    public void initialize() {
        try {
            cbAsset.getItems().addAll(
                    com.example.demo4.dao.AssetDao.findAllAvailable()
            );
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không tải được danh sách tài sản!");
        }
    }

    @FXML
    private void handleRent() {

        // ================== 🚫 CHECK EVENT THEO BOOKING ID ==================
        try {
            // 1️⃣ Lấy booking
            Booking booking = BookingDao.findById(bookingId);
            if (booking == null) {
                showError("Lỗi", "Booking không tồn tại!");
                return;
            }

            // 2️⃣ Lấy event từ booking
            Event event = EventDao.findById(booking.getEventId());
            if (event == null) {
                showError("Lỗi", "Sự kiện không tồn tại!");
                return;
            }

            // 3️⃣ Check trạng thái event
            if (!Event.STATUS_CONFIRMED.equals(event.getStatus())) {
                showWarning(
                        "Không thể thuê tài sản",
                        "Chỉ được thuê tài sản khi sự kiện đã được XÁC NHẬN!"
                );
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không kiểm tra được trạng thái sự kiện!");
            return;
        }
        // ====================================================================

        // ===== CHECK DỮ LIỆU NHẬP =====
        if (cbAsset.getValue() == null) {
            showWarning("Thiếu thông tin", "Chọn tài sản!");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(tfQuantity.getText().trim());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showWarning("Sai số lượng", "Số lượng phải là số dương!");
            return;
        }

        if (taConditionOut.getText().isBlank()) {
            showWarning("Thiếu thông tin", "Nhập tình trạng khi xuất!");
            return;
        }

        assets asset = cbAsset.getValue();
        String assetStatus = asset.getStatus().toLowerCase();

        if (assetStatus.contains("hư") || assetStatus.contains("hu hong")
                || assetStatus.contains("đang sử dụng")
                || assetStatus.contains("dang su dung")) {

            showWarning(
                    "Không thể thuê",
                    "Tài sản đang hư hỏng hoặc đang được sử dụng!"
            );
            return;
        }

        // ===== THUÊ TÀI SẢN =====
        try {
            BookingAssetDao.rentAsset(
                    bookingId,
                    asset.getId(),
                    quantity,
                    taConditionOut.getText().trim()
            );

            showInfo("Thành công", "Thuê tài sản thành công!");
            closeStage();

        } catch (Exception e) {
            showError("Không thể thuê", e.getMessage());
        }
    }


    private void closeStage() {
        cbAsset.getScene().getWindow().hide();
    }
}
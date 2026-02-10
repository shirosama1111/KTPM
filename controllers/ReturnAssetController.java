package com.example.demo4.controllers;

import com.example.demo4.dao.BookingAssetDao;
import com.example.demo4.models.BookingAsset;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ReturnAssetController extends BaseController {

    @FXML private TableView<BookingAsset> tblBookingAssets;
    @FXML private TableColumn<BookingAsset, String> colAsset;
    @FXML private TableColumn<BookingAsset, Integer> colQty;
    @FXML private TableColumn<BookingAsset, Integer> colReturned;

    @FXML private Label lblAssetInfo;
    @FXML private TextArea taConditionIn;
    @FXML private TextField tfReturnedQty;

    private BookingAsset selected;

    // ==========================
    // INIT
    // ==========================
    @FXML
    public void initialize() {

        // Cột tài sản
        colAsset.setCellValueFactory(c ->
                new SimpleStringProperty(
                        "Asset ID: " + c.getValue().getAssetId()
                )
        );

        // Cột số lượng thuê
        colQty.setCellValueFactory(c ->
                new SimpleIntegerProperty(
                        c.getValue().getQuantity()
                ).asObject()
        );

        // Cột đã trả (returned_qty)
        colReturned.setCellValueFactory(c ->
                new SimpleIntegerProperty(
                        c.getValue().getReturnedQty()
                ).asObject()
        );

        // Khi chọn dòng
        tblBookingAssets.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, cur) -> {
                    selected = cur;
                    if (cur != null) {
                        int remain =
                                cur.getQuantity()
                                        - cur.getReturnedQty();

                        lblAssetInfo.setText(
                                "Đang trả asset ID: "
                                        + cur.getAssetId()
                                        + " | Còn lại: "
                                        + remain
                        );
                    } else {
                        lblAssetInfo.setText("");
                    }
                });

        // Load toàn bộ booking_assets CHƯA TRẢ
        loadBookingAssets();
    }

    // ==========================
    // LOAD DATA
    // ==========================
    private void loadBookingAssets() {
        try {
            tblBookingAssets.getItems().setAll(
                    BookingAssetDao.findAllUnreturned()
            );
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không tải được danh sách tài sản chưa trả!");
        }
    }

    // ==========================
    // CONFIRM RETURN
    // ==========================
    @FXML
    private void handleConfirmReturn() {

        if (selected == null) {
            showWarning("Chưa chọn", "Vui lòng chọn tài sản cần trả!");
            return;
        }

        int returnedNow;
        try {
            returnedNow = Integer.parseInt(tfReturnedQty.getText().trim());
        } catch (Exception e) {
            showWarning("Sai dữ liệu", "Số lượng phải là số!");
            return;
        }

        int remain =
                selected.getQuantity()
                        - selected.getReturnedQty();

        if (returnedNow <= 0 || returnedNow > remain) {
            showWarning(
                    "Không hợp lệ",
                    "Số lượng trả phải từ 1 đến " + remain
            );
            return;
        }

        if (taConditionIn.getText().isBlank()) {
            showWarning(
                    "Thiếu thông tin",
                    "Nhập tình trạng khi nhận về!"
            );
            return;
        }

        try {
            BookingAssetDao.confirmReturnPartial(
                    selected.getId(),
                    returnedNow,
                    taConditionIn.getText().trim()
            );

            showInfo("Thành công", "Đã trả tài sản!");

            // reset form
            tfReturnedQty.clear();
            taConditionIn.clear();
            lblAssetInfo.setText("");

            // reload bảng
            loadBookingAssets();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", e.getMessage());
        }
    }

    // ==========================
    // CANCEL
    // ==========================
    @FXML
    private void handleCancel() {
        tblBookingAssets.getScene().getWindow().hide();
    }
}
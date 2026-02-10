package com.example.demo4.dao;

import com.example.demo4.Database;
import com.example.demo4.models.BookingAsset;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingAssetDao {

    // ==========================
    // THUÊ TÀI SẢN
    // ==========================
    public static void rentAsset(
            int bookingId,
            int assetId,
            int quantity,
            String conditionOut
    ) throws SQLException {

        String selectAsset = """
            SELECT quantity FROM assets WHERE id=?
        """;

        String updateAsset = """
            UPDATE assets SET quantity = quantity - ? WHERE id=?
        """;

        String insertBookingAsset = """
            INSERT INTO booking_assets
            (booking_id, asset_id, quantity, returned_qty, condition_out)
            VALUES (?,?,?,?,?)
        """;

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int available;

                try (PreparedStatement ps = conn.prepareStatement(selectAsset)) {
                    ps.setInt(1, assetId);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        throw new SQLException("Tài sản không tồn tại");
                    }
                    available = rs.getInt("quantity");
                }

                if (quantity <= 0 || quantity > available) {
                    throw new SQLException("Số lượng không hợp lệ. Còn lại: " + available);
                }

                try (PreparedStatement ps = conn.prepareStatement(updateAsset)) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, assetId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(insertBookingAsset)) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, assetId);
                    ps.setInt(3, quantity);
                    ps.setInt(4, 0); // returned_qty
                    ps.setString(5, conditionOut);
                    ps.executeUpdate();
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ==========================
    // LOAD THEO BOOKING (CHƯA TRẢ)
    // ==========================
    public static List<BookingAsset> findUnreturnedByBooking(int bookingId)
            throws SQLException {

        List<BookingAsset> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM booking_assets
            WHERE booking_id = ?
              AND returned_qty < quantity
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new BookingAsset(
                        rs.getInt("id"),
                        rs.getInt("booking_id"),
                        rs.getInt("asset_id"),
                        rs.getInt("quantity"),
                        rs.getInt("returned_qty"),
                        rs.getString("condition_out"),
                        rs.getString("condition_in")
                ));
            }
        }
        return list;
    }

    // ==========================
    // TRẢ TỪNG PHẦN
    // ==========================
    public static void confirmReturnPartial(
            int bookingAssetId,
            int returnedNow,
            String conditionIn
    ) throws SQLException {

        String selectSql = """
            SELECT asset_id, quantity, returned_qty
            FROM booking_assets
            WHERE id=?
        """;

        String updateBookingAsset = """
            UPDATE booking_assets
            SET returned_qty = returned_qty + ?,
                condition_in = ?
            WHERE id=?
        """;

        String updateAsset = """
            UPDATE assets
            SET quantity = quantity + ?
            WHERE id=?
        """;

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int assetId, rentedQty, returnedQty;

                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setInt(1, bookingAssetId);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        throw new SQLException("Booking asset không tồn tại");
                    }
                    assetId = rs.getInt("asset_id");
                    rentedQty = rs.getInt("quantity");
                    returnedQty = rs.getInt("returned_qty");
                }

                int remain = rentedQty - returnedQty;
                if (returnedNow <= 0 || returnedNow > remain) {
                    throw new SQLException("Số lượng trả không hợp lệ. Còn lại: " + remain);
                }

                try (PreparedStatement ps = conn.prepareStatement(updateBookingAsset)) {
                    ps.setInt(1, returnedNow);
                    ps.setString(2, conditionIn);
                    ps.setInt(3, bookingAssetId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(updateAsset)) {
                    ps.setInt(1, returnedNow);
                    ps.setInt(2, assetId);
                    ps.executeUpdate();
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ==========================
    // LOAD TẤT CẢ CHƯA TRẢ
    // ==========================
    public static List<BookingAsset> findAllUnreturned()
            throws SQLException {

        List<BookingAsset> list = new ArrayList<>();

        String sql = """
            SELECT id, booking_id, asset_id, quantity, returned_qty,
                   condition_out, condition_in
            FROM booking_assets
            WHERE returned_qty < quantity
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new BookingAsset(
                        rs.getInt("id"),
                        rs.getInt("booking_id"),
                        rs.getInt("asset_id"),
                        rs.getInt("quantity"),
                        rs.getInt("returned_qty"),
                        rs.getString("condition_out"),
                        rs.getString("condition_in")
                ));
            }
        }
        return list;
    }
}

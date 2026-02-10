package com.example.demo4.dao;

import com.example.demo4.Database;
import com.example.demo4.models.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.example.demo4.dao.BaseDao.getConn;

public class BookingDao {

    public static Booking findByEventId(int eventId) throws SQLException {
        String sql = """
        SELECT * FROM bookings
        WHERE event_id = ?
    """;

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Booking(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("event_id"),
                        rs.getString("status"),
                        rs.getString("payment_status")
                );
            }
        }
        return null;
    }

    public static void insert(Booking b) throws SQLException {

        String sql = """
        INSERT INTO dbo.bookings(user_id, event_id, status, payment_status)
        VALUES (?, ?, ?, ?)
    """;

        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, b.getUserId());
            ps.setInt(2, b.getEventId());
            ps.setString(3, b.getStatus());
            ps.setString(4, b.getPaymentStatus());

            ps.executeUpdate();
        }
    }

    public static Booking findById(int bookingId) throws Exception {

        String sql = """
            SELECT id, user_id, event_id, status, payment_status
            FROM bookings
            WHERE id = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Booking(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("event_id"),
                            rs.getString("status"),
                            rs.getString("payment_status")
                    );
                }
            }
        }
        return null;
    }

}

package com.example.demo4.dao;

import com.example.demo4.Database;
import com.example.demo4.dto.UserRowData;
import com.example.demo4.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao extends BaseDao {

    private static final String BASE_SELECT =
            "SELECT id, username, password, role, fullname, email FROM users";

    // ⚠️ GIỮ NGUYÊN – CONTROLLER ĐANG DÙNG
    public static User findByUsernameAndPassword(String username, String password)
            throws SQLException {

        String sql = BASE_SELECT + " WHERE username=? AND password=?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        }
    }

    public static List<UserRowData> findAllWithCitizenName() throws SQLException {

        String sql = """
            SELECT u.id,
                   u.username,
                   u.role,
                   c.full_name AS citizen_name
            FROM users u
            LEFT JOIN citizens c ON c.user_id = u.id
            ORDER BY u.id
        """;

        List<UserRowData> list = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new UserRowData(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("citizen_name"),
                        rs.getString("role")
                ));
            }
        }
        return list;
    }
    public static User findByUsername(String username) throws SQLException {
        String sql = BASE_SELECT + " WHERE username=?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        }
    }

    public static User findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE id=?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        }
    }

    public static List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public static int insert(User u) throws Exception {
        String sql = """
            INSERT INTO users(username,password,role,fullname,email)
            OUTPUT INSERTED.id
            VALUES (?,?,?,?,?)
        """;

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRole());
            ps.setString(4, u.getFullname());
            ps.setString(5, u.getEmail());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                u.setId(rs.getInt(1));
                return u.getId();
            }
        }
        throw new Exception("Không thể tạo user!");
    }

    public static void deleteById(int userId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try {

                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM temporary_records WHERE citizen_id IN (SELECT id FROM citizens WHERE user_id = ?)")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                // Xóa dữ liệu trong bảng citizen_changes liên quan tới citizen của user
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM citizen_changes WHERE citizen_id IN (SELECT id FROM citizens WHERE user_id = ?)")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                // Xóa citizen trước
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM citizens WHERE user_id = ?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                // Xóa booking_assets trước
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM booking_assets WHERE booking_id IN (SELECT id FROM bookings WHERE user_id = ?)")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                // Xóa booking
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM bookings WHERE user_id = ?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                // Cuối cùng xóa user
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM users WHERE id = ?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }



    public static List<User> findAvailableForAssign() throws Exception {
        String sql = """
            SELECT u.*
            FROM users u
            LEFT JOIN citizens c ON u.id = c.user_id
            WHERE c.user_id IS NULL
        """;

        List<User> list = new ArrayList<>();

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getString("fullname"),
                rs.getString("email")
        );
    }

    // ⭐ Cập nhật thông tin user
    public static void update(User u) throws SQLException {
        String sql = """
        UPDATE users
        SET username = ?, password = ?, fullname = ?, email = ?
        WHERE id = ?
    """;

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getFullname());
            ps.setString(4, u.getEmail());
            ps.setInt(5, u.getId());

            ps.executeUpdate();
        }
    }

}

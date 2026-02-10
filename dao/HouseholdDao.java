package com.example.demo4.dao;

import com.example.demo4.Database;
import com.example.demo4.models.Household;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HouseholdDao {

    // ================== FIND ALL ==================
    public static List<Household> findAll() throws Exception {
        String sql = "SELECT * FROM households";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Household> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        }
    }

    // ================== FIND BY ID ==================
    public static Household findById(int householdId) throws SQLException {
        String sql = "SELECT * FROM households WHERE household_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, householdId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    // ================== FIND BY OWNER ==================
    public static List<Household> findByOwner(int ownerUserId) {
        List<Household> list = new ArrayList<>();

        String sql = "SELECT * FROM households WHERE owner_user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ownerUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================== FIND BY USER ==================
    public static List<Household> findByUser(int userId) throws SQLException {
        List<Household> list = new ArrayList<>();
        String sql = "SELECT * FROM households WHERE owner_user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // Insert với Connection
    public static void insert(Connection conn, Household h) throws SQLException {
        String sql = """
        INSERT INTO households
        (head_citizen_id, address, street, ward, district, owner_user_id)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (h.getHeadCitizenId() != null)
                ps.setInt(1, h.getHeadCitizenId());
            else
                ps.setNull(1, Types.INTEGER);

            ps.setString(2, h.getAddress());
            ps.setString(3, h.getStreet());
            ps.setString(4, h.getWard());
            ps.setString(5, h.getDistrict());

            if (h.getOwnerUserId() != null)
                ps.setInt(6, h.getOwnerUserId());
            else
                ps.setNull(6, Types.INTEGER);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                h.setHouseholdId(rs.getInt(1)); // set ID mới
            }
        }
    }

    // Update với Connection
    public static void update(Connection conn, Household h) throws SQLException {
        String sql = """
        UPDATE households
        SET head_citizen_id = ?, address = ?
        WHERE household_id = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (h.getHeadCitizenId() != null)
                ps.setInt(1, h.getHeadCitizenId());
            else
                ps.setNull(1, Types.INTEGER);

            ps.setString(2, h.getAddress());
            ps.setInt(3, h.getHouseholdId());

            ps.executeUpdate();
        }
    }


    // ================== INSERT ==================
    public static void insert(Household h) throws SQLException {
        String sql = """
            INSERT INTO households
            (head_citizen_id, address, street, ward, district, owner_user_id)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (h.getHeadCitizenId() != null)
                ps.setInt(1, h.getHeadCitizenId());
            else
                ps.setNull(1, Types.INTEGER);

            ps.setString(2, h.getAddress());
            ps.setString(3, h.getStreet());
            ps.setString(4, h.getWard());
            ps.setString(5, h.getDistrict());

            if (h.getOwnerUserId() != null)
                ps.setInt(6, h.getOwnerUserId());
            else
                ps.setNull(6, Types.INTEGER);

            ps.executeUpdate();
        }
    }

    // ================== UPDATE ==================
    public static void update(Household h) throws Exception {
        String sql = """
            UPDATE households
            SET head_citizen_id = ?, address = ?
            WHERE household_id = ?
        """;

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (h.getHeadCitizenId() == null)
                ps.setNull(1, java.sql.Types.INTEGER);
            else
                ps.setInt(1, h.getHeadCitizenId());

            ps.setString(2, h.getAddress());
            ps.setInt(3, h.getHouseholdId());

            ps.executeUpdate();
        }
    }

    // ================== DELETE (TRANSACTION) ==================
    public static void delete(int householdId) throws SQLException {
        Connection conn = Database.getConnection();
        try {
            conn.setAutoCommit(false);

            // 1. Xóa tất cả bản ghi liên quan trong household_changes
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM household_changes WHERE household_id = ?")) {
                ps.setInt(1, householdId);
                ps.executeUpdate();
            }

            // 2. Cập nhật citizen để household_id = NULL
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE citizens SET household_id = NULL WHERE household_id = ?")) {
                ps.setInt(1, householdId);
                ps.executeUpdate();
            }

            // 3. Xóa hộ khẩu
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM households WHERE household_id = ?")) {
                ps.setInt(1, householdId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.close();
        }
    }

    public static void deleteById(int householdId) throws SQLException {
        delete(householdId);
    }

    // ================== MAP ROW ==================
    private static Household mapRow(ResultSet rs) throws SQLException {
        Household h = new Household(
                rs.getInt("household_id"), // Sử dụng đúng tên cột
                rs.getObject("head_citizen_id", Integer.class),
                rs.getString("address"),
                rs.getObject("owner_user_id", Integer.class)
        );

        h.setStreet(rs.getString("street"));
        h.setWard(rs.getString("ward"));
        h.setDistrict(rs.getString("district"));
        h.setChangeNote(rs.getString("change_note"));

        if (rs.getDate("last_change_date") != null) {
            h.setLastChangeDate(rs.getDate("last_change_date").toLocalDate());
        }

        return h;
    }

    // Lấy household_id lớn nhất hiện tại
    public static int getNextHouseholdId() throws SQLException {
        String sql = "SELECT MAX(household_id) FROM households";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1); // trả về max id, nếu null sẽ trả về 0
            }
            return 0;
        }
    }

}

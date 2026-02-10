package com.example.demo4.dao;

import com.example.demo4.Database;
import com.example.demo4.models.TemporaryRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TemporaryRecordDao {

    public static int insert(TemporaryRecord r) throws Exception {
        String sql = """
            INSERT INTO temporary_records
            (citizen_id, type, start_date, end_date, location, note)
            VALUES (?,?,?,?,?,?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, r.getCitizenId());
            ps.setString(2, r.getType());
            setDate(ps, 3, r.getStartDate());
            setDate(ps, 4, r.getEndDate());
            ps.setString(5, r.getLocation());
            ps.setString(6, r.getNote());

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public static List<TemporaryRecord> findByCitizen(int citizenId) throws Exception {
        List<TemporaryRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM temporary_records WHERE citizen_id=? ORDER BY start_date DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, citizenId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private static TemporaryRecord mapRow(ResultSet rs) throws SQLException {
        TemporaryRecord r = new TemporaryRecord();
        r.setId(rs.getInt("id"));
        r.setCitizenId(rs.getInt("citizen_id"));
        r.setType(rs.getString("type"));

        Date s = rs.getDate("start_date");
        Date e = rs.getDate("end_date");
        r.setStartDate(s == null ? null : s.toLocalDate());
        r.setEndDate(e == null ? null : e.toLocalDate());

        r.setLocation(rs.getString("location"));
        r.setNote(rs.getString("note"));
        return r;
    }

    private static void setDate(PreparedStatement ps, int idx, LocalDate d) throws SQLException {
        if (d != null) ps.setDate(idx, Date.valueOf(d));
        else ps.setNull(idx, Types.DATE);
    }

    public static void deleteById(int id) throws Exception {
        String sql = "DELETE FROM temporary_records WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public static List<TemporaryRecord> findAll() throws Exception {
        List<TemporaryRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM temporary_records ORDER BY start_date DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }
}

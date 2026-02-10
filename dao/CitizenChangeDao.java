package com.example.demo4.dao;

import com.example.demo4.Database;
import com.example.demo4.models.CitizenChange;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CitizenChangeDao {

    // ================= INSERT =================
    // Trong CitizenChangeDao
    public static void insert(Connection conn, CitizenChange cc) throws SQLException {
        String sql = """
        INSERT INTO citizen_changes
        (citizen_id, from_household_id, to_household_id, change_type, change_date, destination, note)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cc.getCitizenId());
            if (cc.getFromHouseholdId() != null) ps.setInt(2, cc.getFromHouseholdId());
            else ps.setNull(2, Types.INTEGER);

            if (cc.getToHouseholdId() != null) ps.setInt(3, cc.getToHouseholdId());
            else ps.setNull(3, Types.INTEGER);

            ps.setString(4, cc.getChangeType());
            ps.setDate(5, Date.valueOf(cc.getChangeDate()));
            ps.setString(6, cc.getDestination());
            ps.setString(7, cc.getNote());

            ps.executeUpdate();
        }
    }


    // ================= FIND =================
    public static List<CitizenChange> findByCitizen(int citizenId) throws Exception {
        List<CitizenChange> list = new ArrayList<>();
        String sql = "SELECT * FROM citizen_changes WHERE citizen_id=? ORDER BY change_date DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, citizenId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ================= MAP =================
    private static CitizenChange mapRow(ResultSet rs) throws SQLException {
        CitizenChange c = new CitizenChange();
        c.setId(rs.getInt("id"));                 // id tự sinh
        c.setCitizenId(rs.getInt("citizen_id"));  // giữ citizen_id
        c.setFromHouseholdId((Integer) rs.getObject("from_household_id"));
        c.setToHouseholdId((Integer) rs.getObject("to_household_id"));
        c.setChangeType(rs.getString("change_type"));

        Date d = rs.getDate("change_date");
        c.setChangeDate(d == null ? null : d.toLocalDate());

        c.setDestination(rs.getString("destination"));
        c.setNote(rs.getString("note"));
        return c;
    }

    // ================= HELPERS =================
    private static void setDate(PreparedStatement ps, int idx, LocalDate d) throws SQLException {
        if (d != null) ps.setDate(idx, Date.valueOf(d));
        else ps.setNull(idx, Types.DATE);
    }

    private static void setInt(PreparedStatement ps, int idx, Integer v) throws SQLException {
        if (v != null) ps.setInt(idx, v);
        else ps.setNull(idx, Types.INTEGER);
    }
}

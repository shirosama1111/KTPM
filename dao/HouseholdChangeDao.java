package com.example.demo4.dao;

import com.example.demo4.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.example.demo4.dao.BaseDao.setDateOrNull;

public class HouseholdChangeDao {

    public static void insert(
            Connection conn,
            int householdId,
            String changeDate,
            String content
    ) throws SQLException {

        String sql = """
            INSERT INTO household_changes
            (household_id, change_date, change_content)
            VALUES (?,?,?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, householdId);
            setDateOrNull(ps, 2, changeDate);
            ps.setString(3, content);
            ps.executeUpdate();
        }
    }

    public static void insert(
            int householdId,
            String changeDate,
            String content
    ) throws Exception {

        try (Connection conn = Database.getConnection()) {
            insert(conn, householdId, changeDate, content);
        }
    }
}

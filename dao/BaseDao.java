package com.example.demo4.dao;

import com.example.demo4.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Date;

public abstract class BaseDao {

    protected static Connection getConn() throws SQLException {
        return Database.getConnection();
    }

    protected static void setIntOrNull(PreparedStatement ps, int idx, Integer value) throws SQLException {
        if (value != null) ps.setInt(idx, value);
        else ps.setNull(idx, Types.INTEGER);
    }

    protected static void setDateOrNull(PreparedStatement ps, int idx, String yyyyMMdd) throws SQLException {
        if (yyyyMMdd != null && !yyyyMMdd.isBlank()) ps.setDate(idx, Date.valueOf(yyyyMMdd));
        else ps.setNull(idx, Types.DATE);
    }

    protected static void setStringOrNull(PreparedStatement ps, int idx, String s) throws SQLException {
        if (s != null && !s.isBlank()) ps.setString(idx, s);
        else ps.setNull(idx, Types.NVARCHAR);
    }
}

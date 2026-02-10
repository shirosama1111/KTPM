package com.example.demo4;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {
    // 🔹 JDBC URL cho SQL Server
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=ktpm;encrypt=false";
    private static final String USER = "sa"; // 👈 Tài khoản mặc định của SQL Server
    private static final String PASSWORD = "123456"; // 👈 Thay bằng mật khẩu bạn đặt khi cài SQL Server

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Kết nối SQL Server thành công!");
            return conn;
        } catch (Exception e) {
            System.out.println("❌ Kết nối SQL Server thất bại!");
            e.printStackTrace();
            return null;
        }
    }
}

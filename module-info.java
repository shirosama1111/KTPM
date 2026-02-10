module com.example.demo4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires java.sql;

    // Nếu bạn dùng SQL Server JDBC driver
    requires com.microsoft.sqlserver.jdbc;
    requires java.desktop;

    // Mở cho JavaFX controller có thể truy cập
    opens com.example.demo4 to javafx.fxml;
    opens com.example.demo4.controllers to javafx.fxml;

    // Cho phép export ra ngoài nếu cần
    exports com.example.demo4;
    exports com.example.demo4.controllers;
    exports com.example.demo4.models;
}

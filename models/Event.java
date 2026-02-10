package com.example.demo4.models;

public class Event {
    private int id;
    private String title;
    private String date;       // yyyy-MM-dd (tùy bạn)
    private String startTime;  // HH:mm (nếu dùng)
    private String endTime;    // HH:mm
    private String location;
    private String description;
    private String status;

    // Các hằng số trạng thái
    public static final String STATUS_REGISTERED = "ĐĂNG KÝ";
    public static final String STATUS_PAID       = "ĐÃ THANH TOÁN";
    public static final String STATUS_CONFIRMED  = "XÁC NHẬN";
    public static final String STATUS_CANCELLED  = "HUỶ";

    // Constructor đầy đủ (dùng khi load từ DB)
    public Event(int id, String title, String date,
                 String startTime, String endTime,
                 String location, String description,
                 String status) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.description = description;
        this.status = status;
    }

    // Constructor tiện lợi khi KHÔNG quan tâm start/end time
    // (vd: tương thích với code cũ nếu cần)
    public Event(int id, String title, String date,
                 String location, String description, String status) {
        this(id, title, date, null, null, location, description, status);
    }

    // Constructor dùng khi tạo mới (chưa có id, chưa set status → mặc định ĐĂNG KÝ)
    public Event(String title, String date,
                 String startTime, String endTime,
                 String location, String description) {
        this(-1, title, date, startTime, endTime, location, description, STATUS_REGISTERED);
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDate(String date) { this.date = date; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
}

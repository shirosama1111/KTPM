package com.example.demo4.models;

public class Booking {
    private int id;
    private int userId;
    private int eventId;
    private String status;
    private String paymentStatus;

    public Booking(int id, int userId, int eventId, String status, String paymentStatus) {
        this.id=id; this.userId=userId; this.eventId=eventId; this.status=status; this.paymentStatus=paymentStatus;
    }
    // getters + setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}

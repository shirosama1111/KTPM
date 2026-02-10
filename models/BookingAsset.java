package com.example.demo4.models;

public class BookingAsset {

    private int id;
    private int bookingId;
    private int assetId;
    private int quantity;
    private int returnedQty;
    private String conditionOut;
    private String conditionIn;
    private String status;

    public BookingAsset(
            int id,
            int bookingId,
            int assetId,
            int quantity,
            int returnedQuantity,
            String conditionOut,
            String conditionIn
    ) {
        this.id = id;
        this.bookingId = bookingId;
        this.assetId = assetId;
        this.quantity = quantity;
        this.returnedQty = returnedQuantity;
        this.conditionOut = conditionOut;
        this.conditionIn = conditionIn;
    }

    // getters + setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getConditionIn() {
        return conditionIn;
    }

    public void setConditionIn(String conditionIn) {
        this.conditionIn = conditionIn;
    }

    public String getConditionOut() {
        return conditionOut;
    }

    public void setConditionOut(String conditionOut) {
        this.conditionOut = conditionOut;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getReturnedQty() {
        return returnedQty;
    }

    public void setReturnedQty(int returnedQty) {
        this.returnedQty = returnedQty;
    }
}

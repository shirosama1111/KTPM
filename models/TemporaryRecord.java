package com.example.demo4.models;

import java.time.LocalDate;

public class TemporaryRecord {
    private int id;
    private int citizenId;
    private String type;          // TAM_TRU / TAM_VANG
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;      // Nơi tạm trú / Nơi đến khi tạm vắng
    private String note;

    // Getters and Setters ...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCitizenId() { return citizenId; }
    public void setCitizenId(int citizenId) { this.citizenId = citizenId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
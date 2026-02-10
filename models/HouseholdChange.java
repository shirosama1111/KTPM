package com.example.demo4.models;

import java.time.LocalDate;

public class HouseholdChange {

    private int id;
    private int householdId;
    private LocalDate changeDate;
    private String changeContent;

    public HouseholdChange() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHouseholdId() { return householdId; }
    public void setHouseholdId(int householdId) { this.householdId = householdId; }

    public LocalDate getChangeDate() { return changeDate; }
    public void setChangeDate(LocalDate changeDate) { this.changeDate = changeDate; }

    public String getChangeContent() { return changeContent; }
    public void setChangeContent(String changeContent) { this.changeContent = changeContent; }
}

package com.example.demo4.dto;

public class UserRowData {
    public int id;
    public String username;
    public String citizenName;
    public String role;

    public UserRowData(int id, String username, String citizenName, String role) {
        this.id = id;
        this.username = username;
        this.citizenName = citizenName;
        this.role = role;
    }
}

package com.example.demo4.models;

import java.time.LocalDate;

public class Citizen {
    private int id; // Mã định danh duy nhất
    private String fullName;
    private String alias; // Bí danh
    private String gender;
    private LocalDate dob;
    private String placeOfBirth;
    private String hometown; // Nguyên quán
    private String ethnicity; // Dân tộc

    private String cccd;
    private LocalDate cccdIssueDate;
    private String cccdIssuePlace;

    private String job;
    private String workplace;

    private String previousAddress; // Địa chỉ trước khi chuyển đến (Ghi "mới sinh" nếu là trẻ em)
    private LocalDate registerDate; // Ngày đăng ký thường trú

    private String status; // Thường trú / Tạm trú / Tạm vắng / Đã qua đời
    private Boolean isHouseholder;
    private String relation; // Quan hệ với chủ hộ

    private Integer householdId;
    private Integer userId;

    public Citizen() {}

    // Getters and Setters ...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getPlaceOfBirth() { return placeOfBirth; }
    public void setPlaceOfBirth(String placeOfBirth) { this.placeOfBirth = placeOfBirth; }
    public String getHometown() { return hometown; }
    public void setHometown(String hometown) { this.hometown = hometown; }
    public String getEthnicity() { return ethnicity; }
    public void setEthnicity(String ethnicity) { this.ethnicity = ethnicity; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public LocalDate getCccdIssueDate() { return cccdIssueDate; }
    public void setCccdIssueDate(LocalDate cccdIssueDate) { this.cccdIssueDate = cccdIssueDate; }
    public String getCccdIssuePlace() { return cccdIssuePlace; }
    public void setCccdIssuePlace(String cccdIssuePlace) { this.cccdIssuePlace = cccdIssuePlace; }
    public String getJob() { return job; }
    public void setJob(String job) { this.job = job; }
    public String getWorkplace() { return workplace; }
    public void setWorkplace(String workplace) { this.workplace = workplace; }
    public String getPreviousAddress() { return previousAddress; }
    public void setPreviousAddress(String previousAddress) { this.previousAddress = previousAddress; }
    public LocalDate getRegisterDate() { return registerDate; }
    public void setRegisterDate(LocalDate registerDate) { this.registerDate = registerDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getHouseholder() { return isHouseholder; }
    public void setHouseholder(Boolean householder) { isHouseholder = householder; }
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
    public Integer getHouseholdId() { return householdId; }
    public void setHouseholdId(Integer householdId) { this.householdId = householdId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}
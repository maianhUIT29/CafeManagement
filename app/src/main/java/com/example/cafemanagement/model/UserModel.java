package com.example.cafemanagement.model;

public class UserModel {
    private String userId;
    private String name;
    private String phone; // Chuyển sang kiểu String để giữ số 0 ở đầu
    private String email;
    private String role;
    private int points;
    private double salaryRate;

    // Constructor mặc định bắt buộc cho Firebase
    public UserModel() {
    }

    // Getter và Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public double getSalaryRate() { return salaryRate; }
    public void setSalaryRate(double salaryRate) { this.salaryRate = salaryRate; }
}
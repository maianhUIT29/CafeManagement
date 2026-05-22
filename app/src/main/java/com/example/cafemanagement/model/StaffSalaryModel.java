package com.example.cafemanagement.model;

public class StaffSalaryModel {
    private String userId;
    private String name;
    private String role;
    private double salaryRate;
    private double totalHours;
    private double totalSalary;

    // Constructor đầy đủ tham số
    public StaffSalaryModel(String userId, String name, String role, double salaryRate, double totalHours, double totalSalary) {
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.salaryRate = salaryRate;
        this.totalHours = totalHours;
        this.totalSalary = totalSalary;
    }

    // Các hàm Getter bắt buộc để Adapter có thể lấy dữ liệu hiển thị
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public double getSalaryRate() {
        return salaryRate;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public double getTotalSalary() {
        return totalSalary;
    }

    // Các hàm Setter (có thể dùng nếu sau này bạn cần chỉnh sửa dữ liệu trực tiếp trên List)
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setSalaryRate(double salaryRate) {
        this.salaryRate = salaryRate;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public void setTotalSalary(double totalSalary) {
        this.totalSalary = totalSalary;
    }
}
package com.example.cafemanagement.model;

public class VoucherModel {
    private String code;
    private int discount;
    private String description;
    private int pointsRequired;


    public VoucherModel() {}

    // Getter và Setter
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
    }
}
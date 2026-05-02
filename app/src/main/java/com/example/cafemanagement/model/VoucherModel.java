package com.example.cafemanagement.model;

public class VoucherModel {
    private String code;
    private int discount;
    private String description;

    public VoucherModel() {}

    // Getter và Setter
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
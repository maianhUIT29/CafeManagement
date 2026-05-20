package com.example.cafemanagement.model;

import java.io.Serializable;

public class CartItemModel implements Serializable {
    private String productId;
    private String productName;
    private double basePrice;
    private int quantity;
    private String size;
    private String sweetness;
    private String note;
    private double totalPrice;

    public CartItemModel() {
        // Constructor mặc định cho Firebase
    }

    public CartItemModel(String productId, String productName, double basePrice, int quantity, String size, String sweetness, String note, double totalPrice) {
        this.productId = productId;
        this.productName = productName;
        this.basePrice = basePrice;
        this.quantity = quantity;
        this.size = size;
        this.sweetness = sweetness;
        this.note = note;
        this.totalPrice = totalPrice;
    }

    // Getters và Setters
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getBasePrice() { return basePrice; }
    public int getQuantity() { return quantity; }
    public String getSize() { return size; }
    public String getSweetness() { return sweetness; }
    public String getNote() { return note; }
    public double getTotalPrice() { return totalPrice; }
}
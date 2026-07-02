package com.example.cafemanagement.model;

import java.util.Map;

public class OrderItemModel {
    private String productId;
    private String productName;
    private int basePrice;
    private int finalPrice;      // basePrice + size delta + toppings
    private int quantity;
    private String size;         // "M", "L", "XL", "S"
    private int sugar;           // 0, 30, 50, 70, 100
    private int ice;             // 0, 50, 100
    private Map<String, Integer> toppings; // {"Trân châu đen": 8000}
    private String note;

    public OrderItemModel() {}

    public OrderItemModel(String productId, String productName, int basePrice,
                          int finalPrice, int quantity, String size,
                          int sugar, int ice,
                          Map<String, Integer> toppings, String note) {
        this.productId = productId;
        this.productName = productName;
        this.basePrice = basePrice;
        this.finalPrice = finalPrice;
        this.quantity = quantity;
        this.size = size;
        this.sugar = sugar;
        this.ice = ice;
        this.toppings = toppings;
        this.note = note;
    }

    public int getSubtotal() {
        return finalPrice * quantity;
    }

    // Getters & Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getBasePrice() { return basePrice; }
    public void setBasePrice(int basePrice) { this.basePrice = basePrice; }

    public int getFinalPrice() { return finalPrice; }
    public void setFinalPrice(int finalPrice) { this.finalPrice = finalPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public int getSugar() { return sugar; }
    public void setSugar(int sugar) { this.sugar = sugar; }

    public int getIce() { return ice; }
    public void setIce(int ice) { this.ice = ice; }

    public Map<String, Integer> getToppings() { return toppings; }
    public void setToppings(Map<String, Integer> toppings) { this.toppings = toppings; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
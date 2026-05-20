package com.example.cafemanagement.model;

public class BasketItemModel {
    private String productName;
    private String imageUrl;
    private String optionsDisplay;
    private double price;
    private int quantity;

    public BasketItemModel(String productName, String imageUrl, String optionsDisplay, double price, int quantity) {
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.optionsDisplay = optionsDisplay;
        this.price = price;
        this.quantity = quantity;
    }

    // Các phương thức Getter/Setter chuẩn chỉnh
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOptionsDisplay() { return optionsDisplay; }
    public void setOptionsDisplay(String optionsDisplay) { this.optionsDisplay = optionsDisplay; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
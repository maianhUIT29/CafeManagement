package com.example.cafemanagement.model;

public class HistoryRevenueModel {
    private long total;
    private int orderCount;

    public HistoryRevenueModel() {}

    // Getter và Setter
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
}
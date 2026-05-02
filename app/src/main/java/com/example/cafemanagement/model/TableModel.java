package com.example.cafemanagement.model;

public class TableModel {
    private String name;
    private String status;
    private String zone;
    private String currentOrderId; // Có thể null nếu bàn trống

    public TableModel() {
    }

    public TableModel(String name, String status, String zone) {
        this.name = name;
        this.status = status;
        this.zone = zone;
    }

    // Getter và Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public String getCurrentOrderId() { return currentOrderId; }
    public void setCurrentOrderId(String currentOrderId) { this.currentOrderId = currentOrderId; }
}
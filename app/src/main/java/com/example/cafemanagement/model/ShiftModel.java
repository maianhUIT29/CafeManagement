package com.example.cafemanagement.model;

public class ShiftModel {

    public static final String STATUS_OPEN   = "OPEN";
    public static final String STATUS_CLOSED = "CLOSED";

    private String shiftConfigId;   // "SH_MORNING" | "SH_AFTERNOON" | "SH_EVENING"
    private String shiftName;       // "Ca Sáng" | "Ca Trưa" | "Ca Tối"
    private String cashierId;       // UID Firebase Auth
    private String cashierName;
    private long   openedAt;        // System.currentTimeMillis()
    private long   closedAt;        // 0 nếu chưa đóng
    private String status;          // OPEN | CLOSED

    // Tổng kết (tính khi đóng ca)
    private int    totalOrders;
    private long   totalRevenue;

    public ShiftModel() {}

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public String getShiftConfigId() { return shiftConfigId; }
    public void setShiftConfigId(String shiftConfigId) { this.shiftConfigId = shiftConfigId; }

    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }

    public String getCashierId() { return cashierId; }
    public void setCashierId(String cashierId) { this.cashierId = cashierId; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public long getOpenedAt() { return openedAt; }
    public void setOpenedAt(long openedAt) { this.openedAt = openedAt; }

    public long getClosedAt() { return closedAt; }
    public void setClosedAt(long closedAt) { this.closedAt = closedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public long getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(long totalRevenue) { this.totalRevenue = totalRevenue; }
}
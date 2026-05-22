package com.example.cafemanagement.model;

public class HistoryRevenueModel {

    // 1. CÁC BIẾN CŨ (Giữ nguyên để không lỗi tính năng thống kê ngày cũ)
    private long total;
    private int orderCount;

    // 2. CÁC BIẾN THÔNG TIN CA LÀM VIỆC (Khớp với cấu trúc Firebase hiện tại)
    private String cashierId;
    private String cashierName;
    private String shiftConfigId;
    private String shiftName;
    private String status;
    private long openedAt;
    private long closedAt;
    private int totalOrders;
    private double totalRevenue;

    // 3. CÁC BIẾN MỚI DÀNH CHO TÍNH NĂNG NHẬT KÝ DÒNG TIỀN (PETTY CASH)
    private double expectedCashInDrawer; // Tiền dự kiến trong két (Đã cộng 1tr gốc)
    private double depositAmount;        // Tiền cần nộp/cất đi (Chỉ dùng cho Ca Tối)

    public HistoryRevenueModel() {}

    // ================= GETTER VÀ SETTER =================

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }

    public String getCashierId() { return cashierId; }
    public void setCashierId(String cashierId) { this.cashierId = cashierId; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public String getShiftConfigId() { return shiftConfigId; }
    public void setShiftConfigId(String shiftConfigId) { this.shiftConfigId = shiftConfigId; }

    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getOpenedAt() { return openedAt; }
    public void setOpenedAt(long openedAt) { this.openedAt = openedAt; }

    public long getClosedAt() { return closedAt; }
    public void setClosedAt(long closedAt) { this.closedAt = closedAt; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public double getExpectedCashInDrawer() { return expectedCashInDrawer; }
    public void setExpectedCashInDrawer(double expectedCashInDrawer) { this.expectedCashInDrawer = expectedCashInDrawer; }

    public double getDepositAmount() { return depositAmount; }
    public void setDepositAmount(double depositAmount) { this.depositAmount = depositAmount; }
}
package com.example.cafemanagement.model;

import java.util.Map;

public class OrderModel {
    // Status constants
    public static final String STATUS_PENDING   = "PENDING";    // Mới tạo, chờ xử lý
    public static final String STATUS_PREPARING = "PREPARING";  // Đã gửi bếp / đang pha
    public static final String STATUS_DONE      = "DONE";       // Bếp/barista xong
    public static final String STATUS_PAID      = "PAID";       // Đã thanh toán → bàn OCCUPIED
    public static final String STATUS_COMPLETE  = "COMPLETE";   // Khách rời bàn → bàn AVAILABLE

    // Payment method constants
    public static final String PAY_CASH     = "CASH";
    public static final String PAY_TRANSFER = "TRANSFER";

    // Order type constants  ← MỚI
    public static final String TYPE_DINE_IN   = "DINE_IN";    // Tại chỗ
    public static final String TYPE_TAKE_AWAY = "TAKE_AWAY";  // Mang đi

    private String tableId;
    private String tableName;
    private String cashierId;
    private String cashierName;
    private String shiftId;
    private String status;
    private String orderType;       // ← MỚI: DINE_IN | TAKE_AWAY
    private long   createdAt;
    private long   updatedAt;

    private Map<String, OrderItemModel> items;

    private int    subtotal;
    private int    discountAmount;
    private String voucherCode;
    private int    total;

    private String paymentMethod;
    private String note;

    // Khai báo thêm các biến phục vụ cho tính năng tích điểm
    private String orderId;
    private String customerId;
    private int pointsEarned;

    public OrderModel() {}

    public void recalculate() {
        subtotal = 0;
        if (items != null) {
            for (OrderItemModel item : items.values()) {
                subtotal += item.getSubtotal();
            }
        }
        total = subtotal - discountAmount;
        if (total < 0) total = 0;
        updatedAt = System.currentTimeMillis();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getTableId()                        { return tableId; }
    public void   setTableId(String tableId)          { this.tableId = tableId; }

    public String getTableName()                      { return tableName; }
    public void   setTableName(String tableName)      { this.tableName = tableName; }

    public String getCashierId()                      { return cashierId; }
    public void   setCashierId(String cashierId)      { this.cashierId = cashierId; }

    public String getCashierName()                    { return cashierName; }
    public void   setCashierName(String cashierName)  { this.cashierName = cashierName; }

    public String getShiftId()                        { return shiftId; }
    public void   setShiftId(String shiftId)          { this.shiftId = shiftId; }

    public String getStatus()                         { return status; }
    public void   setStatus(String status)            { this.status = status; }

    /** DINE_IN hoặc TAKE_AWAY */
    public String getOrderType()                      { return orderType; }
    public void   setOrderType(String orderType)      { this.orderType = orderType; }

    public long   getCreatedAt()                      { return createdAt; }
    public void   setCreatedAt(long createdAt)        { this.createdAt = createdAt; }

    public long   getUpdatedAt()                      { return updatedAt; }
    public void   setUpdatedAt(long updatedAt)        { this.updatedAt = updatedAt; }

    public Map<String, OrderItemModel> getItems()     { return items; }
    public void setItems(Map<String, OrderItemModel> items) { this.items = items; }

    public int    getSubtotal()                       { return subtotal; }
    public void   setSubtotal(int subtotal)           { this.subtotal = subtotal; }

    public int    getDiscountAmount()                 { return discountAmount; }
    public void   setDiscountAmount(int discountAmount) { this.discountAmount = discountAmount; }

    public String getVoucherCode()                    { return voucherCode; }
    public void   setVoucherCode(String voucherCode)  { this.voucherCode = voucherCode; }

    public int    getTotal()                          { return total; }
    public void   setTotal(int total)                 { this.total = total; }

    public String getPaymentMethod()                  { return paymentMethod; }
    public void   setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getNote()                           { return note; }
    public void   setNote(String note)                { this.note = note; }

    /** Helper: đơn tại chỗ (có bàn cần quản lý) */
    public boolean isDineIn() {
        return TYPE_DINE_IN.equals(orderType);
    }



    // Các phương thức Getter và Setter bắt buộc
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }
}
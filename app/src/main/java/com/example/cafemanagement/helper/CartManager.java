package com.example.cafemanagement.helper;

import com.example.cafemanagement.model.OrderItemModel;
import com.example.cafemanagement.model.OrderModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton giữ trạng thái giỏ hàng trong một phiên tạo đơn.
 * Gọi clear() khi bắt đầu đơn mới hoặc sau khi thanh toán xong.
 */
public class CartManager {

    private static CartManager instance;

    private String tableId;
    private String tableName;
    private String orderType      = OrderModel.TYPE_DINE_IN; // default
    private String customerPhone  = "";                      // tích điểm sau

    private final List<OrderItemModel> items = new ArrayList<>();

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    // ── Bàn ──────────────────────────────────────────────────────────────────

    public void setTable(String tableId, String tableName) {
        this.tableId   = tableId;
        this.tableName = tableName;
    }

    public String getTableId()   { return tableId; }
    public String getTableName() { return tableName; }

    // ── Hình thức gọi ─────────────────────────────────────────────────────────

    public void setOrderType(String orderType) { this.orderType = orderType; }
    public String getOrderType() { return orderType; }

    // ── SĐT khách (Mang đi) ───────────────────────────────────────────────────

    public void setCustomerPhone(String phone) { this.customerPhone = phone; }
    public String getCustomerPhone() { return customerPhone; }

    // ── Items ─────────────────────────────────────────────────────────────────

    public List<OrderItemModel> getItems() { return items; }

    public void addItem(OrderItemModel item) {
        // Nếu cùng productId thì tăng số lượng
        // getSubtotal() tự tính = finalPrice * quantity nên không cần setSubtotal
        for (OrderItemModel existing : items) {
            if (existing.getProductId() != null
                    && existing.getProductId().equals(item.getProductId())) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                // getSubtotal() computed tự động — không cần set
                return;
            }
        }
        items.add(item);
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) items.remove(index);
    }

    public void updateQuantity(int index, int newQty) {
        if (index < 0 || index >= items.size()) return;
        if (newQty <= 0) {
            items.remove(index);
        } else {
            items.get(index).setQuantity(newQty);
            // getSubtotal() computed tự động — không cần set
        }
    }

    public int getSubtotal() {
        int sum = 0;
        for (OrderItemModel i : items) sum += i.getSubtotal();
        return sum;
    }

    public int getItemCount() {
        int count = 0;
        for (OrderItemModel i : items) count += i.getQuantity();
        return count;
    }

    // ── Reset ─────────────────────────────────────────────────────────────────

    public void clear() {
        tableId       = null;
        tableName     = null;
        orderType     = OrderModel.TYPE_DINE_IN;
        customerPhone = "";
        items.clear();
    }
}
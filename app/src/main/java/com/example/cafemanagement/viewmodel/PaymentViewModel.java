package com.example.cafemanagement.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.helper.CartManager;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.helper.ShiftManager;
import com.example.cafemanagement.model.OrderItemModel;
import com.example.cafemanagement.model.OrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PaymentViewModel extends ViewModel {

    // --- LiveData ---
    private final MutableLiveData<Integer> subtotal       = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> discountAmount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> total          = new MutableLiveData<>(0);
    private final MutableLiveData<String>  payMethod      = new MutableLiveData<>(OrderModel.PAY_CASH);
    private final MutableLiveData<VoucherResult> voucherResult = new MutableLiveData<>();
    private final MutableLiveData<OrderResult>   orderResult   = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // --- State nội bộ ---
    private String appliedVoucher = null;

    // --- Getters ---
    public LiveData<Integer>       getSubtotal()      { return subtotal; }
    public LiveData<Integer>       getDiscountAmount(){ return discountAmount; }
    public LiveData<Integer>       getTotal()         { return total; }
    public LiveData<String>        getPayMethod()     { return payMethod; }
    public LiveData<VoucherResult> getVoucherResult() { return voucherResult; }
    public LiveData<OrderResult>   getOrderResult()   { return orderResult; }
    public LiveData<Boolean>       getIsLoading()     { return isLoading; }

    // --- Init: tính subtotal từ CartManager ---
    public void initCart() {
        int sub = 0;
        for (OrderItemModel item : CartManager.getInstance().getItems()) {
            sub += item.getSubtotal();
        }
        subtotal.setValue(sub);
        recalcTotal();
    }

    // --- Cập nhật số lượng item trong giỏ ---
    public void updateItemQuantity(int index, int newQty) {
        CartManager.getInstance().updateQuantity(index, newQty);
        int sub = 0;
        for (OrderItemModel item : CartManager.getInstance().getItems()) {
            sub += item.getSubtotal();
        }
        subtotal.setValue(sub);
        // Tính lại discount nếu có voucher đang áp
        if (appliedVoucher != null) {
            // Giữ nguyên % — cần tính lại từ subtotal mới
            // discountAmount đã set khi applyVoucher, cần recalc
        }
        recalcTotal();
    }

    // --- Phương thức thanh toán ---
    public void selectPayMethod(String method) {
        payMethod.setValue(method);
    }

    // --- Voucher ---
    public void applyVoucher(String code) {
        if (code == null || code.isEmpty()) {
            voucherResult.setValue(new VoucherResult(false, "Nhập mã voucher trước", 0, 0));
            return;
        }

        String upperCode = code.toUpperCase().trim();

        FirebaseHelper.getVouchersRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            voucherResult.setValue(
                                    new VoucherResult(false, "Mã không hợp lệ ✗", 0, 0));
                            return;
                        }

                        // Filter thủ công thay vì orderByChild
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String storedCode = ds.child("code").getValue(String.class);
                            if (upperCode.equals(storedCode)) {
                                Long   discPercent  = ds.child("discount").getValue(Long.class);
                                String description  = ds.child("description").getValue(String.class);
                                if (discPercent == null) discPercent = 0L;

                                int sub  = subtotal.getValue() != null ? subtotal.getValue() : 0;
                                int disc = (int) (sub * discPercent / 100.0);

                                appliedVoucher = upperCode;
                                discountAmount.setValue(disc);
                                recalcTotal();

                                String msg = "✓ Giảm " + discPercent + "%"
                                        + (description != null ? " — " + description : "");
                                voucherResult.setValue(
                                        new VoucherResult(true, msg, (int)(long)discPercent, disc));
                                return;
                            }
                        }

                        // Không tìm thấy
                        voucherResult.setValue(
                                new VoucherResult(false, "Mã không hợp lệ ✗", 0, 0));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        voucherResult.setValue(
                                new VoucherResult(false, "Lỗi kết nối", 0, 0));
                    }
                });
       }

    public void removeVoucher() {
        appliedVoucher = null;
        discountAmount.setValue(0);
        recalcTotal();
        voucherResult.setValue(null);
    }

    private void recalcTotal() {
        int sub  = subtotal.getValue()       != null ? subtotal.getValue()       : 0;
        int disc = discountAmount.getValue() != null ? discountAmount.getValue() : 0;
        total.setValue(Math.max(0, sub - disc));
    }

    // --- Confirm order ---
    public void confirmPayment(String selectedOrderType) {
        CartManager  cart = CartManager.getInstance();
        if (cart.getItems().isEmpty()) {
            orderResult.setValue(OrderResult.error("Giỏ hàng trống!"));
            return;
        }

        isLoading.setValue(true);

        FirebaseUser user         = FirebaseAuth.getInstance().getCurrentUser();
        int          finalSub     = subtotal.getValue()       != null ? subtotal.getValue()       : 0;
        int          finalDisc    = discountAmount.getValue() != null ? discountAmount.getValue() : 0;
        int          finalTotal   = total.getValue()          != null ? total.getValue()          : 0;
        String       finalMethod  = payMethod.getValue()      != null ? payMethod.getValue()      : OrderModel.PAY_CASH;

        OrderModel order = new OrderModel();
        order.setTableId(cart.getTableId());
        order.setTableName(cart.getTableName());
        order.setOrderType(selectedOrderType);
        order.setStatus(OrderModel.STATUS_PREPARING);
        order.setPaymentMethod(finalMethod);
        order.setSubtotal(finalSub);
        order.setDiscountAmount(finalDisc);
        order.setVoucherCode(appliedVoucher);
        order.setTotal(finalTotal);
        order.setCreatedAt(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());

        if (user != null) {
            order.setCashierId(user.getUid());
            order.setCashierName(user.getDisplayName() != null ? user.getDisplayName() : "");
        }
        if (ShiftManager.getInstance().getCurrentShiftId() != null) {
            order.setShiftId(ShiftManager.getInstance().getCurrentShiftId());
        }

        Map<String, OrderItemModel> itemMap = new LinkedHashMap<>();
        for (int i = 0; i < cart.getItems().size(); i++) {
            itemMap.put("item_" + i, cart.getItems().get(i));
        }
        order.setItems(itemMap);

        String orderKey = FirebaseHelper.getOrdersRef().push().getKey();
        if (orderKey == null) {
            isLoading.setValue(false);
            orderResult.setValue(OrderResult.error("Không thể tạo đơn, thử lại!"));
            return;
        }

        final String finalKey = orderKey;
        FirebaseHelper.getOrdersRef().child(orderKey).setValue(order)
                .addOnSuccessListener(unused -> {
                    // DINE_IN: cập nhật bàn
                    String tableId = cart.getTableId();
                    if (tableId != null && OrderModel.TYPE_DINE_IN.equals(selectedOrderType)) {
                        Map<String, Object> tableUpdate = new HashMap<>();
                        tableUpdate.put("currentOrderId", finalKey);
                        tableUpdate.put("status", "OCCUPIED");
                        FirebaseHelper.getTablesRef().child(tableId).updateChildren(tableUpdate);
                    }
                    cart.clear();
                    isLoading.setValue(false);
                    orderResult.setValue(OrderResult.success(finalKey, finalMethod, finalTotal));
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    orderResult.setValue(OrderResult.error("Lỗi: " + e.getMessage()));
                });
    }

    // ── Result wrappers ───────────────────────────────────────────────────────

    public static class VoucherResult {
        public final boolean success;
        public final String  message;
        public final int     discountPercent;
        public final int     discountAmount;

        public VoucherResult(boolean success, String message,
                             int discountPercent, int discountAmount) {
            this.success         = success;
            this.message         = message;
            this.discountPercent = discountPercent;
            this.discountAmount  = discountAmount;
        }
    }

    public static class OrderResult {
        public final boolean success;
        public final String  errorMessage;
        public final String  orderId;
        public final String  paymentMethod;
        public final int     total;

        private OrderResult(boolean success, String errorMessage,
                            String orderId, String paymentMethod, int total) {
            this.success       = success;
            this.errorMessage  = errorMessage;
            this.orderId       = orderId;
            this.paymentMethod = paymentMethod;
            this.total         = total;
        }

        public static OrderResult success(String orderId, String payMethod, int total) {
            return new OrderResult(true, null, orderId, payMethod, total);
        }

        public static OrderResult error(String message) {
            return new OrderResult(false, message, null, null, 0);
        }
    }
}
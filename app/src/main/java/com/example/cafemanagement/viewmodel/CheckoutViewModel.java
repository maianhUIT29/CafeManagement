package com.example.cafemanagement.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.BasketManager;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.BasketItemModel;
import com.example.cafemanagement.model.CartItemModel;
import com.example.cafemanagement.model.OrderItemModel;
import com.example.cafemanagement.model.OrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutViewModel extends ViewModel {

    private final MutableLiveData<List<CartItemModel>> cartItemsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Double> subtotalLiveData = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalLiveData    = new MutableLiveData<>(0.0);
    private final MutableLiveData<String> paymentMethodLiveData = new MutableLiveData<>("CASH");
    private final MutableLiveData<Boolean> isLoading   = new MutableLiveData<>(false);
    private final MutableLiveData<String>  paymentUrl  = new MutableLiveData<>();
    private final MutableLiveData<String>  errorMessage = new MutableLiveData<>();

    // Lưu thông tin bàn / loại đơn để gắn vào OrderModel
    private String  pendingTableId   = null;
    private String  pendingTableName = null;
    private boolean pendingIsDineIn  = false;

    // ── Gọi từ Activity để truyền thông tin bàn ──────────────────────────────
    public void setOrderContext(boolean isDineIn, String tableId, String tableName) {
        this.pendingIsDineIn  = isDineIn;
        this.pendingTableId   = tableId;
        this.pendingTableName = tableName;
    }

    public void loadCartData(List<CartItemModel> items) {
        cartItemsLiveData.setValue(items);
        double subtotal = 0;
        for (CartItemModel item : items) subtotal += item.getTotalPrice();
        subtotalLiveData.setValue(subtotal);
        totalLiveData.setValue(subtotal);
    }

    public void setPaymentMethod(String method) {
        paymentMethodLiveData.setValue(method);
    }

    /**
     * Hàm xử lý luồng thanh toán Tiền mặt.
     */
    public void processPayment() {
        String method = paymentMethodLiveData.getValue();
        if ("CASH".equals(method)) {
            isLoading.setValue(true);
            saveOrderToFirebase("CASH", orderId -> {
                paymentUrl.setValue("SUCCESS_CASH");
                isLoading.setValue(false);
            });
        }
    }

    /**
     * Gọi sau khi thanh toán Online thành công từ Activity
     */
    public void processOnlinePaymentSuccess(String paymentMethod, OnOrderSavedCallback callback) {
        isLoading.setValue(true);
        saveOrderToFirebase(paymentMethod, callback);
    }

    private void saveOrderToFirebase(String paymentMethod, OnOrderSavedCallback callback) {
        List<CartItemModel> cartItems = cartItemsLiveData.getValue();
        if (cartItems == null || cartItems.isEmpty()) {
            errorMessage.setValue("Giỏ hàng trống");
            isLoading.setValue(false);
            return;
        }

        DatabaseReference ordersRef = FirebaseHelper.getOrdersRef();
        String orderId = ordersRef.push().getKey();
        if (orderId == null) {
            errorMessage.setValue("Không thể tạo ID đơn hàng");
            isLoading.setValue(false);
            return;
        }

        // Chuyển đổi CartItem sang OrderItem để lưu vào Firebase
        Map<String, OrderItemModel> itemsMap = new HashMap<>();
        for (CartItemModel item : cartItems) {
            OrderItemModel oItem = new OrderItemModel();
            oItem.setProductId(item.getProductId());
            oItem.setProductName(item.getProductName());
            oItem.setBasePrice((int) item.getBasePrice());
            int quantity = Math.max(item.getQuantity(), 1);
            oItem.setFinalPrice((int) (item.getTotalPrice() / quantity));
            oItem.setQuantity(item.getQuantity());
            oItem.setSize(item.getSize());
            oItem.setNote(item.getNote());
            // Dùng một key ngẫu nhiên cho mỗi item trong map
            itemsMap.put(ordersRef.push().getKey(), oItem);
        }

        double subtotalValue = subtotalLiveData.getValue() != null ? subtotalLiveData.getValue() : 0.0;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String customerId = user != null ? user.getUid() : "GUEST";
        String customerName = user != null ? (user.getDisplayName() != null ? user.getDisplayName() : user.getEmail()) : "Guest";

        // Build OrderModel
        OrderModel order = new OrderModel();
        order.setOrderId(orderId);
        order.setCustomerId(customerId);
        order.setCashierName(customerName);
        order.setOrderType(pendingIsDineIn ? OrderModel.TYPE_DINE_IN : OrderModel.TYPE_TAKE_AWAY);
        order.setTableId(pendingIsDineIn   ? pendingTableId   : null);
        order.setTableName(pendingIsDineIn ? pendingTableName : null);
        order.setItems(itemsMap);
        order.setSubtotal((int) subtotalValue);
        order.setDiscountAmount(0);
        order.setTotal((int) subtotalValue);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(OrderModel.STATUS_PREPARING);   // ← Barista lắng nghe status này
        order.setCreatedAt(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());

        ordersRef.child(orderId).setValue(order)
                .addOnSuccessListener(u -> {
                    if (callback != null) callback.onSaved(orderId);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Lỗi lưu đơn hàng: " + e.getMessage());
                    isLoading.setValue(false);
                });
    }

    public interface OnOrderSavedCallback {
        void onSaved(String orderId);
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public LiveData<List<CartItemModel>> getCartItems()    { return cartItemsLiveData; }
    public LiveData<Double>             getSubtotal()      { return subtotalLiveData; }
    public LiveData<Double>             getTotal()         { return totalLiveData; }
    public LiveData<String>             getPaymentMethod() { return paymentMethodLiveData; }
    public LiveData<Boolean>            getIsLoading()     { return isLoading; }
    public LiveData<String>             getPaymentUrl()    { return paymentUrl; }
    public LiveData<String>             getErrorMessage()  { return errorMessage; }
}

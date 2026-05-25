package com.example.cafemanagement.viewmodel;

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

    // ── Xử lý thanh toán ─────────────────────────────────────────────────────
    public void processPayment() {
        String method = paymentMethodLiveData.getValue();
        if ("CASH".equals(method)) {
            isLoading.setValue(true);
            saveOrderToFirebase(method, orderId -> {
                isLoading.setValue(false);
                paymentUrl.setValue("SUCCESS_CASH");
                BasketManager.getInstance().getBasketItems().clear();
            });
        }
    }

    /**
     * Gọi sau khi thanh toán online thành công (VNPAY / ZaloPay).
     * Activity gọi hàm này rồi tự navigate.
     */
    public void processOnlinePaymentSuccess(String method, OnOrderSavedCallback callback) {
        saveOrderToFirebase(method, callback);
        BasketManager.getInstance().getBasketItems().clear();
    }

    // ── Lưu đơn hàng vào Firebase ────────────────────────────────────────────
    private void saveOrderToFirebase(String paymentMethod, OnOrderSavedCallback callback) {
        DatabaseReference ordersRef = FirebaseHelper.getOrdersRef();
        String orderId = ordersRef.push().getKey();
        if (orderId == null) {
            errorMessage.setValue("Không thể tạo đơn hàng. Vui lòng thử lại.");
            isLoading.setValue(false);
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String customerId   = user != null ? user.getUid()          : "guest";
        String customerName = user != null ? user.getDisplayName()  : "Khách";

        // Build items map từ BasketManager
        Map<String, OrderItemModel> itemsMap = new HashMap<>();
        List<BasketItemModel> basketItems = BasketManager.getInstance().getBasketItems();
        int subtotal = 0;

        for (int i = 0; i < basketItems.size(); i++) {
            BasketItemModel b = basketItems.get(i);
            int unitPrice = (int) Math.round(b.getPrice());
            OrderItemModel oi = new OrderItemModel(
                    "",                          // productId (không có trong BasketItemModel)
                    b.getProductName(),
                    unitPrice,
                    unitPrice,                   // finalPrice = basePrice (không có topping delta)
                    b.getQuantity(),
                    b.getOptionsDisplay(),        // size/options
                    100, 100,                    // sugar, ice – mặc định (BasketItemModel không lưu)
                    null,
                    ""
            );
            subtotal += oi.getSubtotal();
            itemsMap.put("item_" + i, oi);
        }

        // Build OrderModel
        OrderModel order = new OrderModel();
        order.setOrderId(orderId);
        order.setCustomerId(customerId);
        order.setCashierName(customerName);
        order.setOrderType(pendingIsDineIn ? OrderModel.TYPE_DINE_IN : OrderModel.TYPE_TAKE_AWAY);
        order.setTableId(pendingIsDineIn   ? pendingTableId   : null);
        order.setTableName(pendingIsDineIn ? pendingTableName : null);
        order.setItems(itemsMap);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(0);
        order.setTotal(subtotal);
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
package com.example.cafemanagement.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.OrderModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderListViewModel extends ViewModel {

    // --- LiveData ---
    private final MutableLiveData<List<OrderModel>> displayOrders = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>>     displayKeys   = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer>          totalCount    = new MutableLiveData<>(0);
    private final MutableLiveData<Long>             totalRevenue  = new MutableLiveData<>(0L);
    private final MutableLiveData<String>           toastMessage  = new MutableLiveData<>();
    private final MutableLiveData<String>           currentFilter = new MutableLiveData<>("ALL");

    // --- State nội bộ ---
    private final Map<String, OrderModel> allOrdersMap = new LinkedHashMap<>();
    private ValueEventListener ordersListener;

    // --- Getters ---
    public LiveData<List<OrderModel>> getDisplayOrders() { return displayOrders; }
    public LiveData<List<String>>     getDisplayKeys()   { return displayKeys; }
    public LiveData<Integer>          getTotalCount()    { return totalCount; }
    public LiveData<Long>             getTotalRevenue()  { return totalRevenue; }
    public LiveData<String>           getToastMessage()  { return toastMessage; }
    public LiveData<String>           getCurrentFilter() { return currentFilter; }

    // --- Load ---
    public void loadTodayOrders() {
        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allOrdersMap.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    OrderModel o = ds.getValue(OrderModel.class);
                    if (o != null) allOrdersMap.put(ds.getKey(), o);
                }
                updateSummary();
                applyFilter(currentFilter.getValue() != null
                        ? currentFilter.getValue() : "ALL");
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        FirebaseHelper.getOrdersRef()
                .orderByChild("createdAt")
                .startAt((double) getTodayStartMillis())
                .addValueEventListener(ordersListener);
    }

    // --- Filter ---
    public void applyFilter(String filter) {
        currentFilter.setValue(filter);

        List<String>     tempKeys   = new ArrayList<>();
        List<OrderModel> tempOrders = new ArrayList<>();

        for (Map.Entry<String, OrderModel> entry : allOrdersMap.entrySet()) {
            OrderModel o = entry.getValue();
            boolean match = "ALL".equals(filter)
                    ? !OrderModel.STATUS_COMPLETE.equals(o.getStatus())
                    : filter.equals(o.getStatus());
            if (match) {
                tempKeys.add(entry.getKey());
                tempOrders.add(o);
            }
        }

        // Reverse: mới nhất lên đầu
        List<String>     keys   = new ArrayList<>();
        List<OrderModel> orders = new ArrayList<>();
        for (int i = tempKeys.size() - 1; i >= 0; i--) {
            keys.add(tempKeys.get(i));
            orders.add(tempOrders.get(i));
        }

        displayKeys.setValue(keys);
        displayOrders.setValue(orders);
    }

    private void updateSummary() {
        int count = (int) allOrdersMap.values().stream()
                .filter(o -> !OrderModel.STATUS_COMPLETE.equals(o.getStatus()))
                .count();
        long revenue = 0;
        for (OrderModel o : allOrdersMap.values()) {
            if (OrderModel.STATUS_PAID.equals(o.getStatus())
                    || OrderModel.STATUS_COMPLETE.equals(o.getStatus())) {
                revenue += o.getTotal();
            }
        }
        totalCount.setValue(count);
        totalRevenue.setValue(revenue);
    }

    // --- Firebase writes ---
    public void updateStatus(String key, String newStatus) {
        FirebaseHelper.getOrdersRef().child(key).child("status").setValue(newStatus)
                .addOnFailureListener(e -> toastMessage.setValue("Lỗi: " + e.getMessage()));
    }

    public void markAsPaid(String key) {
        FirebaseHelper.getOrdersRef().child(key).child("status")
                .setValue(OrderModel.STATUS_PAID)
                .addOnSuccessListener(u ->
                        toastMessage.setValue("✅ Đã thanh toán — bàn vẫn đang dùng"));
    }

    public void releaseTable(String key, OrderModel order) {
        FirebaseHelper.getOrdersRef().child(key).child("status")
                .setValue(OrderModel.STATUS_COMPLETE)
                .addOnSuccessListener(u -> {
                    if (order.getTableId() != null) {
                        FirebaseHelper.getTablesRef().child(order.getTableId())
                                .child("status").setValue("AVAILABLE");
                        FirebaseHelper.getTablesRef().child(order.getTableId())
                                .child("currentOrderId").setValue(null);
                    }
                    String tableName = order.getTableName() != null
                            ? order.getTableName() : "Bàn";
                    toastMessage.setValue("🪑 " + tableName + " đã trống ✓");
                })
                .addOnFailureListener(e -> toastMessage.setValue("Lỗi: " + e.getMessage()));
    }

    public void cancelOrder(String key, OrderModel order) {
        FirebaseHelper.getOrdersRef().child(key).removeValue()
                .addOnSuccessListener(u -> {
                    if (order.getTableId() != null) {
                        FirebaseHelper.getTablesRef().child(order.getTableId())
                                .child("status").setValue("AVAILABLE");
                        FirebaseHelper.getTablesRef().child(order.getTableId())
                                .child("currentOrderId").setValue(null);
                    }
                    toastMessage.setValue("Đã hủy đơn");
                });
    }

    // --- Utils ---
    private long getTodayStartMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,      0);
        cal.set(Calendar.SECOND,      0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (ordersListener != null)
            FirebaseHelper.getOrdersRef().removeEventListener(ordersListener);
    }
}
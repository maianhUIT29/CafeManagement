package com.example.cafemanagement.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.OrderModel;
import com.example.cafemanagement.model.TableModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardViewModel extends ViewModel {

    // --- LiveData ---
    private final MutableLiveData<String>          greetingName   = new MutableLiveData<>();
    private final MutableLiveData<String>          tableOccupied  = new MutableLiveData<>();
    private final MutableLiveData<Long>            revenueToday   = new MutableLiveData<>(0L);
    private final MutableLiveData<Integer>         orderCount     = new MutableLiveData<>(0);
    private final MutableLiveData<List<OrderModel>> activeOrders  = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>>    activeOrderIds = new MutableLiveData<>(new ArrayList<>());

    // Giữ listener để remove khi ViewModel bị destroy
    private ValueEventListener tableListener;
    private ValueEventListener statsListener;
    private ValueEventListener activeOrdersListener;

    // --- Getters ---
    public LiveData<String>          getGreetingName()   { return greetingName; }
    public LiveData<String>          getTableOccupied()  { return tableOccupied; }
    public LiveData<Long>            getRevenueToday()   { return revenueToday; }
    public LiveData<Integer>         getOrderCount()     { return orderCount; }
    public LiveData<List<OrderModel>> getActiveOrders()  { return activeOrders; }
    public LiveData<List<String>>    getActiveOrderIds() { return activeOrderIds; }

    // --- Load tất cả ---
    public void loadAll() {
        loadGreeting();
        loadTableStats();
        loadRevenueAndOrderCount();
        loadActiveOrders();
    }

    private void loadGreeting() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = (user != null && user.getDisplayName() != null)
                ? user.getDisplayName() : "Nhân viên";
        greetingName.setValue("Xin chào, " + name + " 👋");
    }

    private void loadTableStats() {
        tableListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int occupied = 0, total = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    total++;
                    TableModel t = ds.getValue(TableModel.class);
                    if (t != null && "OCCUPIED".equals(t.getStatus())) occupied++;
                }
                tableOccupied.setValue(occupied + "/" + total);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        FirebaseHelper.getTablesRef().addValueEventListener(tableListener);
    }

    private void loadRevenueAndOrderCount() {
        statsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long revenue = 0;
                int  count   = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    OrderModel o = ds.getValue(OrderModel.class);
                    if (o != null && (
                            OrderModel.STATUS_DONE.equals(o.getStatus())
                                    || OrderModel.STATUS_COMPLETE.equals(o.getStatus()))) {
                        revenue += o.getTotal();
                        count++;
                    }
                }
                revenueToday.setValue(revenue);
                orderCount.setValue(count);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        FirebaseHelper.getOrdersRef()
                .orderByChild("createdAt")
                .startAt((double) getTodayStartMillis())
                .addValueEventListener(statsListener);
    }

    private void loadActiveOrders() {
        activeOrdersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<OrderModel> orders = new ArrayList<>();
                List<String>     ids    = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    OrderModel o = ds.getValue(OrderModel.class);
                    if (o != null && (
                            OrderModel.STATUS_PENDING.equals(o.getStatus())
                                    || OrderModel.STATUS_PREPARING.equals(o.getStatus()))) {
                        orders.add(o);
                        ids.add(ds.getKey());
                    }
                }
                activeOrders.setValue(orders);
                activeOrderIds.setValue(ids);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        FirebaseHelper.getOrdersRef()
                .orderByChild("createdAt")
                .startAt((double) getTodayStartMillis())
                .addValueEventListener(activeOrdersListener);
    }

    private long getTodayStartMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,      0);
        cal.set(Calendar.SECOND,      0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    // --- Cleanup: gỡ listener khi ViewModel bị destroy ---
    @Override
    protected void onCleared() {
        super.onCleared();
        if (tableListener != null)
            FirebaseHelper.getTablesRef().removeEventListener(tableListener);
        if (statsListener != null)
            FirebaseHelper.getOrdersRef().removeEventListener(statsListener);
        if (activeOrdersListener != null)
            FirebaseHelper.getOrdersRef().removeEventListener(activeOrdersListener);
    }
}
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
import java.util.List;

public class ShiftReportViewModel extends ViewModel {

    // --- LiveData ---
    private final MutableLiveData<SummaryResult>   summary   = new MutableLiveData<>();
    private final MutableLiveData<List<OrderModel>> orders   = new MutableLiveData<>(new ArrayList<>());

    private ValueEventListener liveListener;

    // --- Getters ---
    public LiveData<SummaryResult>   getSummary() { return summary; }
    public LiveData<List<OrderModel>> getOrders() { return orders; }

    // --- Bind summary trực tiếp từ Bundle (closed mode) ---
    public void bindClosedSummary(long revenue, int totalOrders) {
        summary.setValue(new SummaryResult(revenue, totalOrders));
    }

    // --- Live mode: realtime query ---
    public void loadOrdersLive(String cashierId, long openedAt) {
        liveListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long             revenue  = 0;
                int              count    = 0;
                List<OrderModel> list     = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    OrderModel o = ds.getValue(OrderModel.class);
                    if (o == null) continue;
                    if (o.getCreatedAt() >= openedAt
                            && (OrderModel.STATUS_DONE.equals(o.getStatus())
                            || OrderModel.STATUS_COMPLETE.equals(o.getStatus()))) {
                        revenue += o.getTotal();
                        count++;
                        list.add(o);
                    }
                }
                summary.setValue(new SummaryResult(revenue, count));
                orders.setValue(list);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        FirebaseHelper.getOrdersRef()
                .orderByChild("cashierId")
                .equalTo(cashierId)
                .addValueEventListener(liveListener);
    }

    // --- Closed mode: one-time query chỉ lấy danh sách đơn ---
    public void loadOrderListForShift(String cashierId, long openedAt, long closedAt) {
        FirebaseHelper.getOrdersRef()
                .orderByChild("cashierId")
                .equalTo(cashierId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<OrderModel> list = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            OrderModel o = ds.getValue(OrderModel.class);
                            if (o == null) continue;
                            boolean afterOpen   = o.getCreatedAt() >= openedAt;
                            boolean beforeClose = (closedAt == 0) || (o.getCreatedAt() <= closedAt);
                            if (afterOpen && beforeClose
                                    && (OrderModel.STATUS_DONE.equals(o.getStatus())
                                    || OrderModel.STATUS_COMPLETE.equals(o.getStatus()))) {
                                list.add(o);
                            }
                        }
                        orders.setValue(list);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (liveListener != null)
            FirebaseHelper.getOrdersRef().removeEventListener(liveListener);
    }

    // --- Result wrapper ---
    public static class SummaryResult {
        public final long revenue;
        public final int  totalOrders;
        public SummaryResult(long revenue, int totalOrders) {
            this.revenue     = revenue;
            this.totalOrders = totalOrders;
        }
    }
}
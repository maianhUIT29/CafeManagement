package com.example.cafemanagement.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.OrderModel;
import com.example.cafemanagement.view.barista.BaristaOrderAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaristaViewModel extends ViewModel {

    // --- LiveData ---
    private final MutableLiveData<Boolean>                      shiftActive   = new MutableLiveData<>(false);
    private final MutableLiveData<Long>                         shiftOpenedAt = new MutableLiveData<>(0L);
    private final MutableLiveData<Integer>                      doneCount     = new MutableLiveData<>(0);
    private final MutableLiveData<List<BaristaOrderAdapter.OrderEntry>> pendingOrders = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<BaristaOrderAdapter.OrderEntry>> doneOrders    = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String>                       toastMessage  = new MutableLiveData<>();

    // --- State nội bộ ---
    private String             currentShiftId  = null;
    private ValueEventListener ordersListener  = null;

    // --- Getters ---
    public LiveData<Boolean>                                  getShiftActive()   { return shiftActive; }
    public LiveData<Long>                                     getShiftOpenedAt() { return shiftOpenedAt; }
    public LiveData<Integer>                                  getDoneCount()     { return doneCount; }
    public LiveData<List<BaristaOrderAdapter.OrderEntry>>     getPendingOrders() { return pendingOrders; }
    public LiveData<List<BaristaOrderAdapter.OrderEntry>>     getDoneOrders()    { return doneOrders; }
    public LiveData<String>                                   getToastMessage()  { return toastMessage; }

    // --- Check ca hiện có khi mở app ---
    public void checkExistingShift() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        FirebaseHelper.getActiveShiftsRef()
                .child("barista_" + user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        if (snap.exists()) {
                            currentShiftId = snap.child("shiftId").getValue(String.class);
                            Long done  = snap.child("doneCount").getValue(Long.class);
                            Long start = snap.child("startTime").getValue(Long.class);
                            doneCount.setValue(done  != null ? done.intValue() : 0);
                            shiftOpenedAt.setValue(start != null ? start : 0L);
                            shiftActive.setValue(true);
                            listenOrders();
                        } else {
                            shiftActive.setValue(false);
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    // --- Mở ca ---
    public void openShift() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String shiftKey = "shift_barista_" + System.currentTimeMillis();
        long   now      = System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("shiftId",   shiftKey);
        data.put("baristaId", user.getUid());
        data.put("startTime", now);
        data.put("doneCount", 0);

        FirebaseHelper.getActiveShiftsRef()
                .child("barista_" + user.getUid())
                .setValue(data)
                .addOnSuccessListener(u -> {
                    currentShiftId = shiftKey;
                    shiftOpenedAt.setValue(now);
                    doneCount.setValue(0);
                    doneOrders.setValue(new ArrayList<>());
                    shiftActive.setValue(true);
                    listenOrders();
                    toastMessage.setValue("✅ Đã mở ca làm việc");
                });
    }

    // --- Đóng ca ---
    public void closeShift() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Long startTime = shiftOpenedAt.getValue();
        int  finalDone = doneCount.getValue() != null ? doneCount.getValue() : 0;

        if (currentShiftId != null) {
            Map<String, Object> history = new HashMap<>();
            history.put("shiftId",   currentShiftId);
            history.put("baristaId", user.getUid());
            history.put("startTime", startTime != null ? startTime : 0L);
            history.put("endTime",   System.currentTimeMillis());
            history.put("doneCount", finalDone);
            FirebaseHelper.getDatabaseInstance()
                    .getReference("BaristaShifts")
                    .child(currentShiftId).setValue(history);
        }

        FirebaseHelper.getActiveShiftsRef()
                .child("barista_" + user.getUid())
                .removeValue()
                .addOnSuccessListener(u -> {
                    stopListenOrders();
                    currentShiftId = null;
                    shiftOpenedAt.setValue(0L);
                    doneCount.setValue(0);
                    pendingOrders.setValue(new ArrayList<>());
                    doneOrders.setValue(new ArrayList<>());
                    shiftActive.setValue(false);
                    toastMessage.setValue("Ca kết thúc! Đã hoàn thành " + finalDone + " đơn.");
                });
    }

    // --- Hoàn thành đơn ---
    public void completeOrder(String orderId) {
        FirebaseHelper.getOrdersRef().child(orderId).child("status")
                .setValue(OrderModel.STATUS_DONE)
                .addOnSuccessListener(u -> {
                    int current = doneCount.getValue() != null ? doneCount.getValue() : 0;
                    int updated = current + 1;
                    doneCount.setValue(updated);

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null && currentShiftId != null) {
                        FirebaseHelper.getActiveShiftsRef()
                                .child("barista_" + user.getUid())
                                .child("doneCount").setValue(updated);
                    }
                    toastMessage.setValue("✅ Đơn hoàn thành!");
                })
                .addOnFailureListener(e ->
                        toastMessage.setValue("Lỗi: " + e.getMessage()));
    }

    // --- Lắng nghe đơn realtime ---
    private void listenOrders() {
        if (ordersListener != null) return;
        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long openedAt = shiftOpenedAt.getValue();

                List<BaristaOrderAdapter.OrderEntry> pending = new ArrayList<>();
                List<BaristaOrderAdapter.OrderEntry> done    = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    OrderModel order = child.getValue(OrderModel.class);
                    if (order == null) continue;

                    if (OrderModel.STATUS_PREPARING.equals(order.getStatus())) {
                        pending.add(new BaristaOrderAdapter.OrderEntry(child.getKey(), order));
                    } else if (OrderModel.STATUS_DONE.equals(order.getStatus())
                            && openedAt != null && openedAt > 0
                            && order.getCreatedAt() >= openedAt) {
                        done.add(new BaristaOrderAdapter.OrderEntry(child.getKey(), order));
                    }
                }

                // Sort pending: DINE_IN trước, rồi theo thời gian
                pending.sort((a, b) -> {
                    boolean aDine = OrderModel.TYPE_DINE_IN.equals(a.order.getOrderType());
                    boolean bDine = OrderModel.TYPE_DINE_IN.equals(b.order.getOrderType());
                    if (aDine != bDine) return aDine ? -1 : 1;
                    return Long.compare(a.order.getCreatedAt(), b.order.getCreatedAt());
                });

                // Sort done: mới nhất lên đầu
                done.sort((a, b) ->
                        Long.compare(b.order.getCreatedAt(), a.order.getCreatedAt()));

                pendingOrders.setValue(pending);
                doneOrders.setValue(done);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                toastMessage.setValue("Lỗi: " + e.getMessage());
            }
        };
        FirebaseHelper.getOrdersRef().addValueEventListener(ordersListener);
    }

    private void stopListenOrders() {
        if (ordersListener != null) {
            FirebaseHelper.getOrdersRef().removeEventListener(ordersListener);
            ordersListener = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListenOrders();
    }
}
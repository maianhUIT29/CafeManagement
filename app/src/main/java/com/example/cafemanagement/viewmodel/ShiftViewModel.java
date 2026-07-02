package com.example.cafemanagement.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.helper.ShiftManager;
import com.example.cafemanagement.model.OrderModel;
import com.example.cafemanagement.model.ShiftConfigModel;
import com.example.cafemanagement.model.ShiftModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShiftViewModel extends ViewModel {

    // --- LiveData ---
    private final MutableLiveData<ShiftState>      shiftState         = new MutableLiveData<>();
    private final MutableLiveData<StatsResult>     liveStats          = new MutableLiveData<>();
    private final MutableLiveData<String>          toastMessage       = new MutableLiveData<>();
    private final MutableLiveData<String>          greetingName       = new MutableLiveData<>();
    private final MutableLiveData<String>          detectedShiftLabel = new MutableLiveData<>();
    private final MutableLiveData<ShiftReportArgs> reportArgs         = new MutableLiveData<>();

    // --- State nội bộ ---
    private String             detectedShiftConfigId = null;
    private String             detectedShiftName     = null;
    private ValueEventListener statsListener;

    // --- Getters ---
    public LiveData<ShiftState>      getShiftState()         { return shiftState; }
    public LiveData<StatsResult>     getLiveStats()          { return liveStats; }
    public LiveData<String>          getToastMessage()       { return toastMessage; }
    public LiveData<String>          getGreetingName()       { return greetingName; }
    public LiveData<String>          getDetectedShiftLabel() { return detectedShiftLabel; }
    public LiveData<ShiftReportArgs> getReportArgs()         { return reportArgs; }

    // Reset sau khi đã navigate
    public void clearReportArgs() {
        reportArgs.setValue(null);
    }

    // --- Init ---
    public void init() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = (user != null && user.getDisplayName() != null)
                ? user.getDisplayName() : "Nhân viên";
        greetingName.setValue("Xin chào, " + name);
        detectShiftFromConfig();
    }

    // --- Detect ca theo giờ ---
    private void detectShiftFromConfig() {
        FirebaseHelper.getShiftConfigRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String nowHHmm = new SimpleDateFormat("HH:mm", Locale.getDefault())
                                .format(new Date());

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ShiftConfigModel cfg = ds.getValue(ShiftConfigModel.class);
                            if (cfg == null) continue;
                            if (isTimeInRange(nowHHmm, cfg.getStart(), cfg.getEnd())) {
                                detectedShiftConfigId = ds.getKey();
                                detectedShiftName     = cfg.getName();
                                break;
                            }
                        }

                        if (detectedShiftName != null) {
                            detectedShiftLabel.setValue(detectedShiftName);
                        } else {
                            detectedShiftLabel.setValue("Ngoài ca");
                        }

                        checkExistingShift();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void checkExistingShift() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseHelper.getActiveShiftsRef().child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ShiftModel shift = snapshot.getValue(ShiftModel.class);
                        if (shift != null && ShiftModel.STATUS_OPEN.equals(shift.getStatus())) {
                            ShiftManager.getInstance().openShift(user.getUid(), shift.getShiftName());
                            shiftState.setValue(ShiftState.open(shift));
                            loadLiveShiftStats(user.getUid(), shift.getOpenedAt());
                        } else {
                            shiftState.setValue(ShiftState.noShift(detectedShiftConfigId != null));
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    // --- Mở ca ---
    public void openShift() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        if (detectedShiftConfigId == null) {
            toastMessage.setValue("Không xác định được ca làm việc");
            return;
        }

        shiftState.setValue(ShiftState.loading());

        ShiftModel shift = new ShiftModel();
        shift.setShiftConfigId(detectedShiftConfigId);
        shift.setShiftName(detectedShiftName);
        shift.setCashierId(user.getUid());
        shift.setCashierName(user.getDisplayName() != null ? user.getDisplayName() : "");
        shift.setOpenedAt(System.currentTimeMillis());
        shift.setStatus(ShiftModel.STATUS_OPEN);
        shift.setTotalOrders(0);
        shift.setTotalRevenue(0);

        FirebaseHelper.getActiveShiftsRef().child(user.getUid()).setValue(shift)
                .addOnSuccessListener(unused -> {
                    ShiftManager.getInstance().openShift(user.getUid(), detectedShiftName);
                    shiftState.setValue(ShiftState.open(shift));
                    loadLiveShiftStats(user.getUid(), shift.getOpenedAt());
                    toastMessage.setValue("✅ Đã mở " + detectedShiftName);
                })
                .addOnFailureListener(e -> {
                    shiftState.setValue(ShiftState.noShift(true));
                    toastMessage.setValue("Lỗi: " + e.getMessage());
                });
    }

    // --- Đóng ca ---
    public void closeShift() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        shiftState.setValue(ShiftState.closing());

        FirebaseHelper.getActiveShiftsRef().child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ShiftModel shift = snapshot.getValue(ShiftModel.class);
                        if (shift == null) return;

                        FirebaseHelper.getOrdersRef()
                                .orderByChild("cashierId")
                                .equalTo(user.getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot ordersSnap) {
                                        long revenue    = 0;
                                        int  orders     = 0;
                                        long openedAtMs = shift.getOpenedAt();

                                        for (DataSnapshot ds : ordersSnap.getChildren()) {
                                            OrderModel o = ds.getValue(OrderModel.class);
                                            if (o != null
                                                    && o.getCreatedAt() >= openedAtMs
                                                    && (OrderModel.STATUS_DONE.equals(o.getStatus())
                                                    || OrderModel.STATUS_COMPLETE.equals(o.getStatus()))) {
                                                revenue += o.getTotal();
                                                orders++;
                                            }
                                        }

                                        final long finalRevenue = revenue;
                                        final int  finalOrders  = orders;

                                        shift.setStatus(ShiftModel.STATUS_CLOSED);
                                        shift.setClosedAt(System.currentTimeMillis());
                                        shift.setTotalOrders(finalOrders);
                                        shift.setTotalRevenue(finalRevenue);

                                        FirebaseHelper.getActiveShiftsRef()
                                                .child(user.getUid()).setValue(shift)
                                                .addOnSuccessListener(u -> {
                                                    saveToHistory(shift);
                                                    ShiftManager.getInstance().closeShift();
                                                    shiftState.setValue(
                                                            ShiftState.noShift(detectedShiftConfigId != null));
                                                    reportArgs.setValue(
                                                            ShiftReportArgs.closed(shift, finalRevenue, finalOrders));
                                                });
                                    }
                                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                                });
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void saveToHistory(ShiftModel shift) {
        String key = FirebaseHelper.getHistoryRevenueRef().push().getKey();
        if (key == null) return;
        FirebaseHelper.getHistoryRevenueRef().child(key).setValue(shift);
    }

    // --- Live stats ---
    private void loadLiveShiftStats(String cashierId, long openedAt) {
        if (statsListener != null)
            FirebaseHelper.getOrdersRef().removeEventListener(statsListener);

        statsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long revenue = 0;
                int  count   = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    OrderModel o = ds.getValue(OrderModel.class);
                    if (o != null
                            && o.getCreatedAt() >= openedAt
                            && (OrderModel.STATUS_DONE.equals(o.getStatus())
                            || OrderModel.STATUS_COMPLETE.equals(o.getStatus()))) {
                        revenue += o.getTotal();
                        count++;
                    }
                }
                liveStats.setValue(new StatsResult(revenue, count));
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };

        FirebaseHelper.getOrdersRef()
                .orderByChild("cashierId")
                .equalTo(cashierId)
                .addValueEventListener(statsListener);
    }

    // --- Xem báo cáo ca đang mở ---
    public void openShiftReport() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        FirebaseHelper.getActiveShiftsRef().child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ShiftModel shift = snapshot.getValue(ShiftModel.class);
                        if (shift == null) return;
                        reportArgs.setValue(ShiftReportArgs.live(shift));
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    // --- Utils ---
    private boolean isTimeInRange(String now, String start, String end) {
        try {
            int nowMin   = toMinutes(now);
            int startMin = toMinutes(start);
            int endMin   = toMinutes(end);
            return startMin < endMin
                    ? nowMin >= startMin && nowMin < endMin
                    : nowMin >= startMin || nowMin < endMin;
        } catch (Exception e) {
            return false;
        }
    }

    private int toMinutes(String hhmm) {
        String[] parts = hhmm.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (statsListener != null)
            FirebaseHelper.getOrdersRef().removeEventListener(statsListener);
    }

    // ── Result / State wrappers ───────────────────────────────────────────────

    public static class ShiftState {
        public enum Type { NO_SHIFT, OPEN, LOADING, CLOSING }

        public final Type       type;
        public final ShiftModel shift;
        public final boolean    canOpen;

        private ShiftState(Type type, ShiftModel shift, boolean canOpen) {
            this.type    = type;
            this.shift   = shift;
            this.canOpen = canOpen;
        }

        public static ShiftState noShift(boolean canOpen) {
            return new ShiftState(Type.NO_SHIFT, null, canOpen);
        }
        public static ShiftState open(ShiftModel shift) {
            return new ShiftState(Type.OPEN, shift, false);
        }
        public static ShiftState loading() {
            return new ShiftState(Type.LOADING, null, false);
        }
        public static ShiftState closing() {
            return new ShiftState(Type.CLOSING, null, false);
        }
    }

    public static class StatsResult {
        public final long revenue;
        public final int  orders;
        public StatsResult(long revenue, int orders) {
            this.revenue = revenue;
            this.orders  = orders;
        }
    }

    public static class ShiftReportArgs {
        public final ShiftModel shift;
        public final long       revenue;
        public final int        orders;
        public final boolean    isLive;

        private ShiftReportArgs(ShiftModel shift, long revenue, int orders, boolean isLive) {
            this.shift   = shift;
            this.revenue = revenue;
            this.orders  = orders;
            this.isLive  = isLive;
        }

        public static ShiftReportArgs live(ShiftModel shift) {
            return new ShiftReportArgs(shift, 0, 0, true);
        }
        public static ShiftReportArgs closed(ShiftModel shift, long revenue, int orders) {
            return new ShiftReportArgs(shift, revenue, orders, false);
        }
    }
}
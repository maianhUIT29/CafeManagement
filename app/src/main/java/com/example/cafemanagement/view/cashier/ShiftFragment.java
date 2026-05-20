package com.example.cafemanagement.view.cashier;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cafemanagement.R;
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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ShiftFragment extends Fragment {

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView     tvCurrentTime, tvCurrentShiftLabel;
    private TextView     tvGreeting;

    // Panel khi chưa mở ca
    private LinearLayout panelNoShift;
    private TextView     tvDetectedShift;
    private Button       btnOpenShift;

    // Panel khi đã mở ca
    private LinearLayout panelShiftOpen;
    private TextView     tvShiftName, tvShiftOpenedAt, tvShiftRevenue, tvShiftOrders;
    private Button       btnCloseShift, btnViewReport;

    // ── State ─────────────────────────────────────────────────────────────────
    private String detectedShiftConfigId;
    private String detectedShiftName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shift, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        tvCurrentTime      = view.findViewById(R.id.tv_current_time);
        tvCurrentShiftLabel= view.findViewById(R.id.tv_current_shift_label);
        tvGreeting         = view.findViewById(R.id.tv_shift_greeting);
        panelNoShift       = view.findViewById(R.id.panel_no_shift);
        tvDetectedShift    = view.findViewById(R.id.tv_detected_shift);
        btnOpenShift       = view.findViewById(R.id.btn_open_shift);
        panelShiftOpen     = view.findViewById(R.id.panel_shift_open);
        tvShiftName        = view.findViewById(R.id.tv_shift_name);
        tvShiftOpenedAt    = view.findViewById(R.id.tv_shift_opened_at);
        tvShiftRevenue     = view.findViewById(R.id.tv_shift_revenue);
        tvShiftOrders      = view.findViewById(R.id.tv_shift_orders);
        btnCloseShift      = view.findViewById(R.id.btn_close_shift);
        btnViewReport      = view.findViewById(R.id.btn_view_report);

        // Hiển thị giờ hiện tại
        updateCurrentTime();

        // Hiển thị tên cashier
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = (user != null && user.getDisplayName() != null)
                ? user.getDisplayName() : "Nhân viên";
        tvGreeting.setText("Xin chào, " + name);

        // Detect ca theo giờ rồi kiểm tra ca đang mở
        detectShiftFromConfig();

        btnOpenShift.setOnClickListener(v -> openShift());
        btnCloseShift.setOnClickListener(v -> closeShift());
        btnViewReport.setOnClickListener(v -> openShiftReport());
    }

    // ── Detect ca theo giờ hiện tại từ ShiftConfig ────────────────────────────

    private void detectShiftFromConfig() {
        FirebaseHelper.getShiftConfigRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;

                        String nowHHmm = new SimpleDateFormat("HH:mm", Locale.getDefault())
                                .format(new Date());

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ShiftConfigModel cfg = ds.getValue(ShiftConfigModel.class);
                            if (cfg == null) continue;

                            if (isTimeInRange(nowHHmm, cfg.getStart(), cfg.getEnd())) {
                                detectedShiftConfigId = ds.getKey();   // "SH_MORNING" ...
                                detectedShiftName     = cfg.getName(); // "Ca Sáng" ...
                                break;
                            }
                        }

                        if (detectedShiftName != null) {
                            tvCurrentShiftLabel.setText(detectedShiftName);
                            tvDetectedShift.setText("Phiên làm việc: " + detectedShiftName);
                        } else {
                            tvCurrentShiftLabel.setText("Ngoài ca");
                            tvDetectedShift.setText("Hiện tại ngoài giờ làm việc");
                            btnOpenShift.setEnabled(false);
                        }

                        // Sau khi biết ca, kiểm tra xem cashier đã mở ca chưa
                        checkExistingShift();
                    }

                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    /** Kiểm tra Firebase xem cashier đã có ca đang mở chưa (sau app restart) */
    private void checkExistingShift() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseHelper.getActiveShiftsRef().child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        ShiftModel shift = snapshot.getValue(ShiftModel.class);

                        if (shift != null && ShiftModel.STATUS_OPEN.equals(shift.getStatus())) {
                            // Khôi phục ShiftManager
                            ShiftManager.getInstance().openShift(user.getUid(), shift.getShiftName());
                            showShiftOpenPanel(shift);
                            loadLiveShiftStats(user.getUid(), shift.getOpenedAt());
                        } else {
                            showNoShiftPanel();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    // ── Mở ca ─────────────────────────────────────────────────────────────────

    private void openShift() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        if (detectedShiftConfigId == null) {
            Toast.makeText(requireContext(),
                    "Không xác định được ca làm việc", Toast.LENGTH_SHORT).show();
            return;
        }

        btnOpenShift.setEnabled(false);
        btnOpenShift.setText("Đang mở ca...");

        ShiftModel shift = new ShiftModel();
        shift.setShiftConfigId(detectedShiftConfigId);
        shift.setShiftName(detectedShiftName);
        shift.setCashierId(user.getUid());
        shift.setCashierName(user.getDisplayName() != null ? user.getDisplayName() : "");
        shift.setOpenedAt(System.currentTimeMillis());
        shift.setStatus(ShiftModel.STATUS_OPEN);
        shift.setTotalOrders(0);
        shift.setTotalRevenue(0);

        // Key = cashierId → mỗi người chỉ có 1 ca tại một thời điểm
        FirebaseHelper.getActiveShiftsRef().child(user.getUid()).setValue(shift)
                .addOnSuccessListener(unused -> {
                    if (!isAdded()) return;
                    ShiftManager.getInstance().openShift(user.getUid(), detectedShiftName);
                    showShiftOpenPanel(shift);
                    loadLiveShiftStats(user.getUid(), shift.getOpenedAt());
                    Toast.makeText(requireContext(),
                            "✅ Đã mở " + detectedShiftName, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    btnOpenShift.setEnabled(true);
                    btnOpenShift.setText("Mở ca làm việc");
                    Toast.makeText(requireContext(),
                            "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ── Đóng ca ───────────────────────────────────────────────────────────────

    private void closeShift() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        btnCloseShift.setEnabled(false);
        btnCloseShift.setText("Đang đóng ca...");

        // Tính tổng kết rồi mới đóng
        String shiftId = user.getUid();
        long openedAt  = ShiftManager.getInstance().getCurrentShiftId() != null
                ? 0 : 0; // lấy từ Firebase

        FirebaseHelper.getActiveShiftsRef().child(shiftId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        ShiftModel shift = snapshot.getValue(ShiftModel.class);
                        if (shift == null) return;

                        // Query orders trong ca để tính tổng
                        FirebaseHelper.getOrdersRef()
                                .orderByChild("cashierId")
                                .equalTo(shiftId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot ordersSnap) {
                                        if (!isAdded()) return;
                                        long revenue = 0;
                                        int  orders  = 0;
                                        long openedAtMs = shift.getOpenedAt();

                                        for (DataSnapshot ds : ordersSnap.getChildren()) {
                                            OrderModel o = ds.getValue(OrderModel.class);
                                            if (o != null
                                                    && OrderModel.STATUS_PAID.equals(o.getStatus())
                                                    && o.getCreatedAt() >= openedAtMs) {
                                                revenue += o.getTotal();
                                                orders++;
                                            }
                                        }

                                        final long finalRevenue = revenue;
                                        final int  finalOrders  = orders;

                                        // Cập nhật ActiveShifts
                                        shift.setStatus(ShiftModel.STATUS_CLOSED);
                                        shift.setClosedAt(System.currentTimeMillis());
                                        shift.setTotalOrders(finalOrders);
                                        shift.setTotalRevenue(finalRevenue);

                                        FirebaseHelper.getActiveShiftsRef()
                                                .child(shiftId).setValue(shift)
                                                .addOnSuccessListener(u -> {
                                                    if (!isAdded()) return;
                                                    // Lưu vào HistoryRevenue
                                                    saveToHistory(shift);
                                                    ShiftManager.getInstance().closeShift();
                                                    showNoShiftPanel();
                                                    // Mở màn hình tổng kết
                                                    openShiftReportWithData(
                                                            shift, finalRevenue, finalOrders);
                                                });
                                    }
                                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                                });
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    /** Lưu bản tóm tắt ca vào HistoryRevenue để báo cáo lâu dài */
    private void saveToHistory(ShiftModel shift) {
        String key = FirebaseHelper.getHistoryRevenueRef().push().getKey();
        if (key == null) return;
        FirebaseHelper.getHistoryRevenueRef().child(key).setValue(shift);
    }

    // ── Live stats trong ca ───────────────────────────────────────────────────

    private void loadLiveShiftStats(String cashierId, long openedAt) {
        FirebaseHelper.getOrdersRef()
                .orderByChild("cashierId")
                .equalTo(cashierId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        long revenue = 0;
                        int  count   = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            OrderModel o = ds.getValue(OrderModel.class);
                            if (o != null
                                    && OrderModel.STATUS_PAID.equals(o.getStatus())
                                    && o.getCreatedAt() >= openedAt) {
                                revenue += o.getTotal();
                                count++;
                            }
                        }
                        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
                        tvShiftRevenue.setText(fmt.format(revenue) + "đ");
                        tvShiftOrders.setText(String.valueOf(count));
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private void showNoShiftPanel() {
        panelNoShift.setVisibility(View.VISIBLE);
        panelShiftOpen.setVisibility(View.GONE);
        btnOpenShift.setEnabled(detectedShiftConfigId != null);
        btnOpenShift.setText("Mở ca làm việc");
    }

    private void showShiftOpenPanel(ShiftModel shift) {
        panelNoShift.setVisibility(View.GONE);
        panelShiftOpen.setVisibility(View.VISIBLE);

        tvShiftName.setText(shift.getShiftName());
        String openedTime = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
                .format(new Date(shift.getOpenedAt()));
        tvShiftOpenedAt.setText("Bắt đầu: " + openedTime);
        btnCloseShift.setEnabled(true);
        btnCloseShift.setText("Kết ca");
    }

    private void updateCurrentTime() {
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        tvCurrentTime.setText(time);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void openShiftReport() {
        // Lấy data từ Firebase rồi mở ShiftReportFragment
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        FirebaseHelper.getActiveShiftsRef().child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        ShiftModel shift = snapshot.getValue(ShiftModel.class);
                        if (shift == null) return;
                        Bundle args = new Bundle();
                        args.putString("shiftName",  shift.getShiftName());
                        args.putLong("openedAt",     shift.getOpenedAt());
                        args.putString("cashierId",  shift.getCashierId());
                        args.putString("cashierName",shift.getCashierName());
                        args.putBoolean("isLive", true);

                        ShiftReportFragment report = new ShiftReportFragment();
                        report.setArguments(args);
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.cashier_fragment_container, report)
                                .addToBackStack("shift_report")
                                .commit();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void openShiftReportWithData(ShiftModel shift, long revenue, int orders) {
        Bundle args = new Bundle();
        args.putString("shiftName",   shift.getShiftName());
        args.putLong("openedAt",      shift.getOpenedAt());
        args.putLong("closedAt",      shift.getClosedAt());
        args.putString("cashierId",   shift.getCashierId());
        args.putString("cashierName", shift.getCashierName());
        args.putLong("totalRevenue",  revenue);
        args.putInt("totalOrders",    orders);
        args.putBoolean("isLive",     false);

        ShiftReportFragment report = new ShiftReportFragment();
        report.setArguments(args);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.cashier_fragment_container, report)
                .addToBackStack("shift_report")
                .commit();
    }

    // ── Utils ─────────────────────────────────────────────────────────────────

    /**
     * Kiểm tra giờ hiện tại có nằm trong khoảng [start, end) không.
     * Format: "HH:mm"
     */
    private boolean isTimeInRange(String now, String start, String end) {
        try {
            int nowMin   = toMinutes(now);
            int startMin = toMinutes(start);
            int endMin   = toMinutes(end);
            if (startMin < endMin) {
                return nowMin >= startMin && nowMin < endMin;
            } else {
                // Qua nửa đêm (ví dụ: 22:00 → 02:00)
                return nowMin >= startMin || nowMin < endMin;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private int toMinutes(String hhmm) {
        String[] parts = hhmm.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}
package com.example.cafemanagement.view.cashier;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.OrderModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderListFragment extends Fragment {

    // ── Filter constants ──────────────────────────────────────────────────────
    private static final String FILTER_ALL       = "ALL";
    private static final String FILTER_PENDING   = OrderModel.STATUS_PENDING;
    private static final String FILTER_PREPARING = OrderModel.STATUS_PREPARING;
    private static final String FILTER_PAID      = OrderModel.STATUS_PAID;

    // ── Views ─────────────────────────────────────────────────────────────────
    private RecyclerView rvOrders;
    private TextView     tvEmpty, tvTotalCount, tvTotalRevenue;

    private LinearLayout tabAll, tabPending, tabPreparing, tabPaid;
    private TextView     lblAll, lblPending, lblPreparing, lblPaid;
    private View         indicatorAll, indicatorPending, indicatorPreparing, indicatorPaid;

    // ── Data ──────────────────────────────────────────────────────────────────
    private final Map<String, OrderModel> allOrdersMap  = new LinkedHashMap<>();
    private final List<OrderModel>        displayOrders = new ArrayList<>();
    private final List<String>            displayKeys   = new ArrayList<>();

    private OrderListAdapter adapter;
    private String currentFilter = FILTER_ALL;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEmpty        = view.findViewById(R.id.tv_order_list_empty);
        tvTotalCount   = view.findViewById(R.id.tv_total_count);
        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue);

        tabAll       = view.findViewById(R.id.tab_all);
        tabPending   = view.findViewById(R.id.tab_pending);
        tabPreparing = view.findViewById(R.id.tab_preparing);
        tabPaid      = view.findViewById(R.id.tab_paid);
        lblAll       = view.findViewById(R.id.lbl_all);
        lblPending   = view.findViewById(R.id.lbl_pending);
        lblPreparing = view.findViewById(R.id.lbl_preparing);
        lblPaid      = view.findViewById(R.id.lbl_paid);
        indicatorAll       = view.findViewById(R.id.indicator_all);
        indicatorPending   = view.findViewById(R.id.indicator_pending);
        indicatorPreparing = view.findViewById(R.id.indicator_preparing);
        indicatorPaid      = view.findViewById(R.id.indicator_paid);

        rvOrders = view.findViewById(R.id.rv_order_list);
        adapter  = new OrderListAdapter(displayOrders, displayKeys, this::showOrderActions);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOrders.setAdapter(adapter);

        tabAll.setOnClickListener(v       -> applyFilter(FILTER_ALL));
        tabPending.setOnClickListener(v   -> applyFilter(FILTER_PENDING));
        tabPreparing.setOnClickListener(v -> applyFilter(FILTER_PREPARING));
        tabPaid.setOnClickListener(v      -> applyFilter(FILTER_PAID));

        highlightTab(FILTER_ALL);
        loadTodayOrders();
    }

    // ── Load đơn hôm nay ─────────────────────────────────────────────────────

    private void loadTodayOrders() {
        long todayStart = getTodayStartMillis();
        FirebaseHelper.getOrdersRef()
                .orderByChild("createdAt")
                .startAt((double) todayStart)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        allOrdersMap.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            OrderModel o = ds.getValue(OrderModel.class);
                            if (o != null) allOrdersMap.put(ds.getKey(), o);
                        }
                        updateSummary();
                        applyFilter(currentFilter);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    // ── Filter ────────────────────────────────────────────────────────────────

    private void applyFilter(String filter) {
        currentFilter = filter;
        highlightTab(filter);
        displayOrders.clear();
        displayKeys.clear();

        List<String>     tempKeys   = new ArrayList<>();
        List<OrderModel> tempOrders = new ArrayList<>();

        for (Map.Entry<String, OrderModel> entry : allOrdersMap.entrySet()) {
            OrderModel o = entry.getValue();
            boolean match;
            if (FILTER_ALL.equals(filter)) {
                // Tab "Tất cả": ẩn COMPLETE (đã trả bàn, không cần hiện)
                match = !OrderModel.STATUS_COMPLETE.equals(o.getStatus());
            } else {
                match = filter.equals(o.getStatus());
            }
            if (match) {
                tempKeys.add(entry.getKey());
                tempOrders.add(o);
            }
        }

        // Reverse: mới nhất lên đầu
        for (int i = tempKeys.size() - 1; i >= 0; i--) {
            displayKeys.add(tempKeys.get(i));
            displayOrders.add(tempOrders.get(i));
        }

        tvEmpty.setVisibility(displayOrders.isEmpty() ? View.VISIBLE : View.GONE);
        rvOrders.setVisibility(displayOrders.isEmpty() ? View.GONE   : View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    private void updateSummary() {
        int  totalOrders  = (int) allOrdersMap.values().stream()
                .filter(o -> !OrderModel.STATUS_COMPLETE.equals(o.getStatus()))
                .count();
        long totalRevenue = 0;
        for (OrderModel o : allOrdersMap.values()) {
            if (OrderModel.STATUS_PAID.equals(o.getStatus())
                    || OrderModel.STATUS_COMPLETE.equals(o.getStatus())) {
                totalRevenue += o.getTotal();
            }
        }
        tvTotalCount.setText(totalOrders + " đơn hôm nay");
        tvTotalRevenue.setText(
                NumberFormat.getInstance(new Locale("vi", "VN")).format(totalRevenue) + "đ");
    }

    private void highlightTab(String filter) {
        int activeColor   = getResources().getColor(R.color.accent_brown,       requireActivity().getTheme());
        int inactiveColor = getResources().getColor(R.color.text_secondary,      requireActivity().getTheme());
        int transparent   = getResources().getColor(android.R.color.transparent, requireActivity().getTheme());

        lblAll.setTextColor(      FILTER_ALL.equals(filter)       ? activeColor : inactiveColor);
        lblPending.setTextColor(  FILTER_PENDING.equals(filter)   ? activeColor : inactiveColor);
        lblPreparing.setTextColor(FILTER_PREPARING.equals(filter) ? activeColor : inactiveColor);
        lblPaid.setTextColor(     FILTER_PAID.equals(filter)      ? activeColor : inactiveColor);

        indicatorAll.setBackgroundColor(      FILTER_ALL.equals(filter)       ? activeColor : transparent);
        indicatorPending.setBackgroundColor(  FILTER_PENDING.equals(filter)   ? activeColor : transparent);
        indicatorPreparing.setBackgroundColor(FILTER_PREPARING.equals(filter) ? activeColor : transparent);
        indicatorPaid.setBackgroundColor(     FILTER_PAID.equals(filter)      ? activeColor : transparent);
    }

    // ── Action menu ───────────────────────────────────────────────────────────
    //
    // Flow mới:
    //   PENDING   → [Gửi bếp] → PREPARING
    //             → [Thanh toán] → PAID  (bàn giữ OCCUPIED)
    //             → [Hủy đơn]
    //
    //   PREPARING → [Hoàn thành] → DONE
    //             → [Thanh toán] → PAID  (bàn giữ OCCUPIED)
    //
    //   DONE      → [Thanh toán] → PAID  (bàn giữ OCCUPIED)
    //
    //   PAID      → [Trả bàn ✓] → COMPLETE + bàn AVAILABLE  ← MỚI
    //               (chỉ hiện khi DINE_IN)

    private void showOrderActions(int position) {
        if (position >= displayOrders.size()) return;
        OrderModel order  = displayOrders.get(position);
        String     key    = displayKeys.get(position);
        String     status = order.getStatus() != null ? order.getStatus() : "";

        String title = (order.getTableName() != null ? order.getTableName() : "Đơn mang đi")
                + "  •  " + formatMoney(order.getTotal());

        List<String> options = new ArrayList<>();

        switch (status) {
            case OrderModel.STATUS_PENDING:
                options.add("📤  Gửi xuống bếp");
                options.add("💳  Thanh toán ngay");
                options.add("🗑️  Hủy đơn");
                break;

            case OrderModel.STATUS_PREPARING:
                options.add("✅  Đánh dấu hoàn thành");
                options.add("💳  Thanh toán ngay");
                break;

            case OrderModel.STATUS_DONE:
                options.add("💳  Thanh toán");
                break;

            case OrderModel.STATUS_PAID:
                // Chỉ đơn tại chỗ mới cần trả bàn
                if (OrderModel.TYPE_DINE_IN.equals(order.getOrderType())
                        && order.getTableId() != null) {
                    options.add("🪑  Trả bàn (khách đã về)");
                }
                break;
        }

        if (options.isEmpty()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setItems(options.toArray(new String[0]), (dialog, which) -> {
                    String action = options.get(which);
                    if      (action.contains("bếp"))          updateStatus(key, OrderModel.STATUS_PREPARING);
                    else if (action.contains("hoàn thành"))   updateStatus(key, OrderModel.STATUS_DONE);
                    else if (action.contains("Thanh toán"))   markAsPaid(key, order);
                    else if (action.contains("Trả bàn"))      confirmReleaseTable(key, order);
                    else if (action.contains("Hủy"))          confirmCancel(key, order);
                })
                .show();
    }

    // ── Cập nhật trạng thái đơn ───────────────────────────────────────────────

    private void updateStatus(String key, String newStatus) {
        FirebaseHelper.getOrdersRef().child(key).child("status").setValue(newStatus)
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Thanh toán đơn từ OrderList (không qua PaymentFragment):
     *   - Đơn DINE_IN → bàn giữ OCCUPIED (khách vẫn ngồi, chờ trả bàn)
     *   - Đơn TAKE_AWAY → không thay đổi bàn
     */
    private void markAsPaid(String key, OrderModel order) {
        FirebaseHelper.getOrdersRef().child(key).child("status")
                .setValue(OrderModel.STATUS_PAID)
                .addOnSuccessListener(u -> {
                    if (!isAdded()) return;
                    // DINE_IN: bàn giữ nguyên OCCUPIED, không set AVAILABLE
                    // TAKE_AWAY: không có bàn
                    Toast.makeText(requireContext(),
                            "✅ Đã thanh toán — bàn vẫn đang dùng", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Trả bàn: đơn PAID + DINE_IN → COMPLETE, bàn → AVAILABLE
     * Đây là bước cuối: khách đã thanh toán VÀ đã rời khỏi bàn.
     */
    private void confirmReleaseTable(String key, OrderModel order) {
        String tableName = order.getTableName() != null ? order.getTableName() : "bàn";
        new AlertDialog.Builder(requireContext())
                .setTitle("Trả bàn — " + tableName)
                .setMessage("Khách đã rời bàn?\n\nThao tác này sẽ trả " + tableName
                        + " về trạng thái trống.")
                .setPositiveButton("Trả bàn ✓", (d, w) -> releaseTable(key, order))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void releaseTable(String key, OrderModel order) {
        // 1. Đơn → COMPLETE
        FirebaseHelper.getOrdersRef().child(key).child("status")
                .setValue(OrderModel.STATUS_COMPLETE)
                .addOnSuccessListener(u -> {
                    if (!isAdded()) return;

                    // 2. Bàn → AVAILABLE + xóa currentOrderId
                    if (order.getTableId() != null) {
                        FirebaseHelper.getTablesRef().child(order.getTableId())
                                .child("status").setValue("AVAILABLE");
                        FirebaseHelper.getTablesRef().child(order.getTableId())
                                .child("currentOrderId").setValue(null);
                    }

                    Toast.makeText(requireContext(),
                            "🪑 " + (order.getTableName() != null ? order.getTableName() : "Bàn")
                                    + " đã trống ✓", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ── Hủy đơn ──────────────────────────────────────────────────────────────

    private void confirmCancel(String key, OrderModel order) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hủy đơn?")
                .setMessage("Xác nhận hủy đơn " + order.getTableName() + "?")
                .setPositiveButton("Hủy đơn", (d, w) ->
                        FirebaseHelper.getOrdersRef().child(key).removeValue()
                                .addOnSuccessListener(u -> {
                                    if (!isAdded()) return;
                                    // Hủy đơn → trả bàn về AVAILABLE
                                    if (order.getTableId() != null) {
                                        FirebaseHelper.getTablesRef()
                                                .child(order.getTableId())
                                                .child("status").setValue("AVAILABLE");
                                        FirebaseHelper.getTablesRef()
                                                .child(order.getTableId())
                                                .child("currentOrderId").setValue(null);
                                    }
                                    Toast.makeText(requireContext(),
                                            "Đã hủy đơn", Toast.LENGTH_SHORT).show();
                                }))
                .setNegativeButton("Không", null)
                .show();
    }

    // ── Utils ─────────────────────────────────────────────────────────────────

    private long getTodayStartMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private String formatMoney(int amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + "đ";
    }

    // ════════════════════════════════════════════════════════════════════════
    // Inner Adapter
    // ════════════════════════════════════════════════════════════════════════

    public static class OrderListAdapter
            extends RecyclerView.Adapter<OrderListAdapter.VH> {

        interface OnItemClick { void onClick(int position); }

        private final List<OrderModel> data;
        private final List<String>     keys;
        private final OnItemClick      listener;

        public OrderListAdapter(List<OrderModel> data, List<String> keys, OnItemClick listener) {
            this.data = data; this.keys = keys; this.listener = listener;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_list, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            OrderModel o = data.get(pos);

            // Tên bàn hoặc "Mang đi"
            boolean isTakeAway = OrderModel.TYPE_TAKE_AWAY.equals(o.getOrderType());
            String tableLabel  = isTakeAway ? "🥤 Mang đi"
                    : (o.getTableName() != null ? o.getTableName() : "—");
            h.tvTable.setText(tableLabel);

            h.tvTotal.setText(
                    NumberFormat.getInstance(new Locale("vi", "VN")).format(o.getTotal()) + "đ");
            h.tvTime.setText(
                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(o.getCreatedAt())));

            int itemCount = o.getItems() != null ? o.getItems().size() : 0;
            h.tvItemCount.setText(itemCount + " món");

            // Status chip
            String status = o.getStatus() != null ? o.getStatus() : "";
            switch (status) {
                case OrderModel.STATUS_PENDING:
                    h.tvStatus.setText("Chờ xử lý");
                    h.tvStatus.setTextColor(0xFFF07D28);
                    break;
                case OrderModel.STATUS_PREPARING:
                    h.tvStatus.setText("Đang pha");
                    h.tvStatus.setTextColor(0xFF2196F3);
                    break;
                case OrderModel.STATUS_DONE:
                    h.tvStatus.setText("Hoàn thành");
                    h.tvStatus.setTextColor(0xFF2D9B5A);
                    break;
                case OrderModel.STATUS_PAID:
                    // Phân biệt DINE_IN (cần trả bàn) vs TAKE_AWAY (hoàn tất)
                    if (isTakeAway) {
                        h.tvStatus.setText("Đã thanh toán ✓");
                        h.tvStatus.setTextColor(0xFF9E9E9E);
                    } else {
                        h.tvStatus.setText("Đã TT — chờ trả bàn");
                        h.tvStatus.setTextColor(0xFF8D6E63); // brownish
                    }
                    break;
                case OrderModel.STATUS_COMPLETE:
                    h.tvStatus.setText("Hoàn tất ✓");
                    h.tvStatus.setTextColor(0xFF9E9E9E);
                    break;
                default:
                    h.tvStatus.setText(status);
                    h.tvStatus.setTextColor(0xFF9E9E9E);
            }

            // COMPLETE → dim, không click; PAID (DINE_IN) → còn click được để trả bàn
            boolean isDone = OrderModel.STATUS_COMPLETE.equals(status)
                    || (OrderModel.STATUS_PAID.equals(status) && isTakeAway);
            h.itemView.setOnClickListener(isDone ? null : v -> listener.onClick(h.getAdapterPosition()));
            h.itemView.setAlpha(OrderModel.STATUS_COMPLETE.equals(status) ? 0.45f : 1f);
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTable, tvTotal, tvTime, tvStatus, tvItemCount;
            VH(@NonNull View v) {
                super(v);
                tvTable     = v.findViewById(R.id.tv_order_table);
                tvTotal     = v.findViewById(R.id.tv_order_total);
                tvTime      = v.findViewById(R.id.tv_order_time);
                tvStatus    = v.findViewById(R.id.tv_order_status);
                tvItemCount = v.findViewById(R.id.tv_order_item_count);
            }
        }
    }
}
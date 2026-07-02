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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.OrderModel;
import com.example.cafemanagement.view.cashier.adapter.OrderListAdapter;
import com.example.cafemanagement.viewmodel.OrderListViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderListFragment extends Fragment {

    private static final String FILTER_ALL       = "ALL";
    private static final String FILTER_PENDING   = OrderModel.STATUS_PENDING;
    private static final String FILTER_PREPARING = OrderModel.STATUS_PREPARING;
    private static final String FILTER_PAID      = OrderModel.STATUS_PAID;

    private OrderListViewModel viewModel;

    private RecyclerView rvOrders;
    private TextView     tvEmpty, tvTotalCount, tvTotalRevenue;
    private TextView     lblAll, lblPending, lblPreparing, lblPaid;
    private View         indicatorAll, indicatorPending, indicatorPreparing, indicatorPaid;

    private OrderListAdapter adapter;

    // Snapshot list để dùng trong dialog (luôn sync với LiveData)
    private List<OrderModel> currentOrders = new ArrayList<>();
    private List<String>     currentKeys   = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(OrderListViewModel.class);

        // Bind views
        tvEmpty        = view.findViewById(R.id.tv_order_list_empty);
        tvTotalCount   = view.findViewById(R.id.tv_total_count);
        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue);

        LinearLayout tabAll       = view.findViewById(R.id.tab_all);
        LinearLayout tabPending   = view.findViewById(R.id.tab_pending);
        LinearLayout tabPreparing = view.findViewById(R.id.tab_preparing);
        LinearLayout tabPaid      = view.findViewById(R.id.tab_paid);
        lblAll        = view.findViewById(R.id.lbl_all);
        lblPending    = view.findViewById(R.id.lbl_pending);
        lblPreparing  = view.findViewById(R.id.lbl_preparing);
        lblPaid       = view.findViewById(R.id.lbl_paid);
        indicatorAll       = view.findViewById(R.id.indicator_all);
        indicatorPending   = view.findViewById(R.id.indicator_pending);
        indicatorPreparing = view.findViewById(R.id.indicator_preparing);
        indicatorPaid      = view.findViewById(R.id.indicator_paid);

        rvOrders = view.findViewById(R.id.rv_order_list);
        adapter  = new OrderListAdapter(new ArrayList<>(), new ArrayList<>(),
                this::showOrderActions);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOrders.setAdapter(adapter);

        // Tab clicks
        tabAll.setOnClickListener(v       -> viewModel.applyFilter(FILTER_ALL));
        tabPending.setOnClickListener(v   -> viewModel.applyFilter(FILTER_PENDING));
        tabPreparing.setOnClickListener(v -> viewModel.applyFilter(FILTER_PREPARING));
        tabPaid.setOnClickListener(v      -> viewModel.applyFilter(FILTER_PAID));

        // Observe
        setupObservers();

        // Load
        if (savedInstanceState == null) viewModel.loadTodayOrders();
    }

    private void setupObservers() {
        viewModel.getDisplayOrders().observe(getViewLifecycleOwner(), orders -> {
            currentOrders = orders;
            adapter.updateData(orders,
                    viewModel.getDisplayKeys().getValue() != null
                            ? viewModel.getDisplayKeys().getValue() : new ArrayList<>());
            tvEmpty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
            rvOrders.setVisibility(orders.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getDisplayKeys().observe(getViewLifecycleOwner(), keys -> {
            currentKeys = keys;
            adapter.updateData(
                    viewModel.getDisplayOrders().getValue() != null
                            ? viewModel.getDisplayOrders().getValue() : new ArrayList<>(),
                    keys);
        });

        viewModel.getTotalCount().observe(getViewLifecycleOwner(),
                count -> tvTotalCount.setText(count + " đơn hôm nay"));

        viewModel.getTotalRevenue().observe(getViewLifecycleOwner(), revenue ->
                tvTotalRevenue.setText(
                        NumberFormat.getInstance(new Locale("vi", "VN")).format(revenue) + "đ"));

        viewModel.getCurrentFilter().observe(getViewLifecycleOwner(),
                this::highlightTab);

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    // --- Dialog actions (UI only, gọi ViewModel để write Firebase) ---

    private void showOrderActions(int position) {
        if (position >= currentOrders.size()) return;
        OrderModel order  = currentOrders.get(position);
        String     key    = currentKeys.get(position);
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
                    if      (action.contains("bếp"))        viewModel.updateStatus(key, OrderModel.STATUS_PREPARING);
                    else if (action.contains("hoàn thành")) viewModel.updateStatus(key, OrderModel.STATUS_DONE);
                    else if (action.contains("Thanh toán")) viewModel.markAsPaid(key);
                    else if (action.contains("Trả bàn"))    confirmReleaseTable(key, order);
                    else if (action.contains("Hủy"))        confirmCancel(key, order);
                })
                .show();
    }

    private void confirmReleaseTable(String key, OrderModel order) {
        String tableName = order.getTableName() != null ? order.getTableName() : "bàn";
        new AlertDialog.Builder(requireContext())
                .setTitle("Trả bàn — " + tableName)
                .setMessage("Khách đã rời bàn?\n\nThao tác này sẽ trả "
                        + tableName + " về trạng thái trống.")
                .setPositiveButton("Trả bàn ✓", (d, w) -> viewModel.releaseTable(key, order))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmCancel(String key, OrderModel order) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hủy đơn?")
                .setMessage("Xác nhận hủy đơn " + order.getTableName() + "?")
                .setPositiveButton("Hủy đơn", (d, w) -> viewModel.cancelOrder(key, order))
                .setNegativeButton("Không", null)
                .show();
    }

    // --- UI helpers ---

    private void highlightTab(String filter) {
        int activeColor   = getResources().getColor(R.color.accent_brown,        requireActivity().getTheme());
        int inactiveColor = getResources().getColor(R.color.text_secondary,       requireActivity().getTheme());
        int transparent   = getResources().getColor(android.R.color.transparent,  requireActivity().getTheme());

        lblAll.setTextColor(      FILTER_ALL.equals(filter)       ? activeColor : inactiveColor);
        lblPending.setTextColor(  FILTER_PENDING.equals(filter)   ? activeColor : inactiveColor);
        lblPreparing.setTextColor(FILTER_PREPARING.equals(filter) ? activeColor : inactiveColor);
        lblPaid.setTextColor(     FILTER_PAID.equals(filter)      ? activeColor : inactiveColor);

        indicatorAll.setBackgroundColor(      FILTER_ALL.equals(filter)       ? activeColor : transparent);
        indicatorPending.setBackgroundColor(  FILTER_PENDING.equals(filter)   ? activeColor : transparent);
        indicatorPreparing.setBackgroundColor(FILTER_PREPARING.equals(filter) ? activeColor : transparent);
        indicatorPaid.setBackgroundColor(     FILTER_PAID.equals(filter)      ? activeColor : transparent);
    }

    private String formatMoney(int amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + "đ";
    }
}
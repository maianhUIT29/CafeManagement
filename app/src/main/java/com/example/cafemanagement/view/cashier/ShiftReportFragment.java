package com.example.cafemanagement.view.cashier;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.view.cashier.adapter.ShiftOrderAdapter;
import com.example.cafemanagement.viewmodel.ShiftReportViewModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ShiftReportFragment extends Fragment {

    private ShiftReportViewModel viewModel;

    private TextView     tvShiftTitle, tvCashierName;
    private TextView     tvTimeRange, tvTotalRevenue, tvTotalOrders, tvAvgOrder;
    private RecyclerView rvOrders;
    private ShiftOrderAdapter orderAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shift_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ShiftReportViewModel.class);

        // Bind views
        tvShiftTitle   = view.findViewById(R.id.tv_report_shift_title);
        tvCashierName  = view.findViewById(R.id.tv_report_cashier);
        tvTimeRange    = view.findViewById(R.id.tv_report_time_range);
        tvTotalRevenue = view.findViewById(R.id.tv_report_revenue);
        tvTotalOrders  = view.findViewById(R.id.tv_report_orders);
        tvAvgOrder     = view.findViewById(R.id.tv_report_avg);
        rvOrders       = view.findViewById(R.id.rv_report_orders);
        Button btnBack = view.findViewById(R.id.btn_report_back);

        // RecyclerView
        orderAdapter = new ShiftOrderAdapter(new ArrayList<>());
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOrders.setAdapter(orderAdapter);

        btnBack.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Observe
        setupObservers();

        // Load data từ arguments
        Bundle args = getArguments();
        if (args == null) return;

        String  shiftName   = args.getString("shiftName",   "Ca làm việc");
        String  cashierName = args.getString("cashierName", "Nhân viên");
        String  cashierId   = args.getString("cashierId",   "");
        long    openedAt    = args.getLong("openedAt",   0);
        long    closedAt    = args.getLong("closedAt",   0);
        boolean isLive      = args.getBoolean("isLive",  true);

        // Header (UI thuần — không cần ViewModel)
        tvShiftTitle.setText("Báo cáo — " + shiftName);
        tvCashierName.setText("Nhân viên: " + cashierName);
        SimpleDateFormat sdf      = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
        String           closeStr = isLive ? "Đang mở" : sdf.format(new Date(closedAt));
        tvTimeRange.setText(sdf.format(new Date(openedAt)) + "  →  " + closeStr);

        if (savedInstanceState == null) {
            if (!isLive) {
                long revenue = args.getLong("totalRevenue", 0);
                int  orders  = args.getInt("totalOrders",  0);
                viewModel.bindClosedSummary(revenue, orders);
                viewModel.loadOrderListForShift(cashierId, openedAt, 0);
            } else {
                viewModel.loadOrdersLive(cashierId, openedAt);
            }
        }
    }

    private void setupObservers() {
        viewModel.getSummary().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvTotalRevenue.setText(fmt.format(result.revenue) + "đ");
            tvTotalOrders.setText(String.valueOf(result.totalOrders));
            long avg = result.totalOrders > 0 ? result.revenue / result.totalOrders : 0;
            tvAvgOrder.setText(fmt.format(avg) + "đ");
        });

        viewModel.getOrders().observe(getViewLifecycleOwner(),
                orders -> orderAdapter.updateData(orders));
    }
}
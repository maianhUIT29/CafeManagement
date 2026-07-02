package com.example.cafemanagement.view.cashier;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.CartManager;
import com.example.cafemanagement.view.cashier.adapter.ActiveOrderAdapter;
import com.example.cafemanagement.viewmodel.DashboardViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;

    private TextView tvGreeting, tvRevenue, tvOrderCount, tvTableOccupied;
    private ActiveOrderAdapter activeOrderAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Bind views
        tvGreeting      = view.findViewById(R.id.tv_greeting);
        tvRevenue       = view.findViewById(R.id.tv_revenue_today);
        tvOrderCount    = view.findViewById(R.id.tv_order_count);
        tvTableOccupied = view.findViewById(R.id.tv_table_occupied);
        RecyclerView rvActiveOrders = view.findViewById(R.id.rv_active_orders);

        // Setup RecyclerView với list rỗng ban đầu
        activeOrderAdapter = new ActiveOrderAdapter(
                new ArrayList<>(), new ArrayList<>(),
                order -> { /* TODO: navigate to order detail */ });
        rvActiveOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvActiveOrders.setAdapter(activeOrderAdapter);

        // Observe
        setupObservers();

        // Events
        view.findViewById(R.id.btn_new_order).setOnClickListener(v -> {
            CartManager.getInstance().clear();
            ((CashierActivity) requireActivity())
                    .navigateTo(CashierActivity.SCREEN_ORDER_TYPE, null, true);
        });

        // Nút đăng xuất trên Dashboard
        View btnLogout = view.findViewById(R.id.btn_logout_dashboard);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                if (getActivity() instanceof CashierActivity) {
                    ((CashierActivity) getActivity()).showLogoutConfirmationDialog();
                }
            });
        }

        // Load data
        if (savedInstanceState == null) viewModel.loadAll();
    }

    private void setupObservers() {
        viewModel.getGreetingName().observe(getViewLifecycleOwner(),
                name -> tvGreeting.setText(name));

        viewModel.getTableOccupied().observe(getViewLifecycleOwner(),
                stat -> tvTableOccupied.setText(stat));

        viewModel.getRevenueToday().observe(getViewLifecycleOwner(), revenue -> {
            NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvRevenue.setText(fmt.format(revenue) + "đ");
        });

        viewModel.getOrderCount().observe(getViewLifecycleOwner(),
                count -> tvOrderCount.setText(String.valueOf(count)));

        // Cập nhật adapter khi cả orders lẫn ids đều sẵn sàng
        viewModel.getActiveOrders().observe(getViewLifecycleOwner(), orders -> {
            activeOrderAdapter.updateData(
                    orders,
                    viewModel.getActiveOrderIds().getValue() != null
                            ? viewModel.getActiveOrderIds().getValue() : new ArrayList<>()
            );
        });

        viewModel.getActiveOrderIds().observe(getViewLifecycleOwner(), ids -> {
            activeOrderAdapter.updateData(
                    viewModel.getActiveOrders().getValue() != null
                            ? viewModel.getActiveOrders().getValue() : new ArrayList<>(),
                    ids
            );
        });
    }
}
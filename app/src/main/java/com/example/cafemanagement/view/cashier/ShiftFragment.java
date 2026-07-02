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
import androidx.lifecycle.ViewModelProvider;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.ShiftModel;
import com.example.cafemanagement.viewmodel.ShiftViewModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShiftFragment extends Fragment {

    private ShiftViewModel viewModel;

    private TextView     tvCurrentTime, tvCurrentShiftLabel, tvGreeting;
    private LinearLayout panelNoShift, panelShiftOpen;
    private TextView     tvDetectedShift;
    private Button       btnOpenShift;
    private TextView     tvShiftName, tvShiftOpenedAt, tvShiftRevenue, tvShiftOrders;
    private Button       btnCloseShift, btnViewReport;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shift, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ShiftViewModel.class);

        tvCurrentTime       = view.findViewById(R.id.tv_current_time);
        tvCurrentShiftLabel = view.findViewById(R.id.tv_current_shift_label);
        tvGreeting          = view.findViewById(R.id.tv_shift_greeting);
        panelNoShift        = view.findViewById(R.id.panel_no_shift);
        tvDetectedShift     = view.findViewById(R.id.tv_detected_shift);
        btnOpenShift        = view.findViewById(R.id.btn_open_shift);
        panelShiftOpen      = view.findViewById(R.id.panel_shift_open);
        tvShiftName         = view.findViewById(R.id.tv_shift_name);
        tvShiftOpenedAt     = view.findViewById(R.id.tv_shift_opened_at);
        tvShiftRevenue      = view.findViewById(R.id.tv_shift_revenue);
        tvShiftOrders       = view.findViewById(R.id.tv_shift_orders);
        btnCloseShift       = view.findViewById(R.id.btn_close_shift);
        btnViewReport       = view.findViewById(R.id.btn_view_report);

        updateCurrentTime();

        btnOpenShift.setOnClickListener(v  -> viewModel.openShift());
        btnCloseShift.setOnClickListener(v -> viewModel.closeShift());
        btnViewReport.setOnClickListener(v -> viewModel.openShiftReport());

        setupObservers();

        if (savedInstanceState == null) viewModel.init();
    }

    private void setupObservers() {
        viewModel.getGreetingName().observe(getViewLifecycleOwner(),
                name -> tvGreeting.setText(name));

        viewModel.getDetectedShiftLabel().observe(getViewLifecycleOwner(), label -> {
            tvCurrentShiftLabel.setText(label);
            tvDetectedShift.setText("Ngoài ca".equals(label)
                    ? "Hiện tại ngoài giờ làm việc"
                    : "Phiên làm việc: " + label);
        });

        viewModel.getShiftState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            switch (state.type) {
                case NO_SHIFT:
                    showNoShiftPanel(state.canOpen);
                    break;
                case OPEN:
                    showShiftOpenPanel(state.shift);
                    break;
                case LOADING:
                    btnOpenShift.setEnabled(false);
                    btnOpenShift.setText("Đang mở ca...");
                    break;
                case CLOSING:
                    btnCloseShift.setEnabled(false);
                    btnCloseShift.setText("Đang đóng ca...");
                    break;
            }
        });

        viewModel.getLiveStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) return;
            NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvShiftRevenue.setText(fmt.format(stats.revenue) + "đ");
            tvShiftOrders.setText(String.valueOf(stats.orders));
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        // ── FIX: clear reportArgs sau khi navigate để tránh re-trigger khi back ──
        viewModel.getReportArgs().observe(getViewLifecycleOwner(), args -> {
            if (args == null) return;
            navigateToReport(args);
            viewModel.clearReportArgs();
        });
    }

    private void showNoShiftPanel(boolean canOpen) {
        panelNoShift.setVisibility(View.VISIBLE);
        panelShiftOpen.setVisibility(View.GONE);
        btnOpenShift.setEnabled(canOpen);
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
        tvCurrentTime.setText(
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
    }

    private void navigateToReport(ShiftViewModel.ShiftReportArgs args) {
        Bundle bundle = new Bundle();
        bundle.putString("shiftName",   args.shift.getShiftName());
        bundle.putLong("openedAt",      args.shift.getOpenedAt());
        bundle.putString("cashierId",   args.shift.getCashierId());
        bundle.putString("cashierName", args.shift.getCashierName());
        bundle.putBoolean("isLive",     args.isLive);

        if (!args.isLive) {
            bundle.putLong("closedAt",     args.shift.getClosedAt());
            bundle.putLong("totalRevenue", args.revenue);
            bundle.putInt("totalOrders",   args.orders);
        }

        ((CashierActivity) requireActivity())
                .navigateTo(CashierActivity.SCREEN_SHIFT_REPORT, bundle, true);
    }
}
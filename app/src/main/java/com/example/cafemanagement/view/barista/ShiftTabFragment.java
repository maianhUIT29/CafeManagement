package com.example.cafemanagement.view.barista;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.OrderModel;
import com.example.cafemanagement.view.barista.BaristaOrderAdapter.OrderEntry;
import com.example.cafemanagement.viewmodel.BaristaViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ShiftTabFragment extends Fragment {

    private BaristaViewModel viewModel;

    private TextView       tvStateLabel, tvOpenTime, tvStatPending, tvStatDone, tvStatDuration;
    private MaterialButton btnToggleShift;
    private View           viewDot;
    private LinearLayout   llDoneOrders;
    private TextView       tvNoDoneOrders;
    private View           cardStats, cardDoneOrders;

    private final Handler  timerHandler  = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override public void run() {
            updateDuration();
            timerHandler.postDelayed(this, 60_000);
        }
    };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             ViewGroup container, Bundle b) {
        return inf.inflate(R.layout.fragment_barista_shift, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);

        // Dùng chung ViewModel với Activity
        viewModel = new ViewModelProvider(requireActivity()).get(BaristaViewModel.class);

        tvStateLabel   = v.findViewById(R.id.tv_shift_state_label);
        tvOpenTime     = v.findViewById(R.id.tv_shift_open_time);
        tvStatPending  = v.findViewById(R.id.tv_stat_pending);
        tvStatDone     = v.findViewById(R.id.tv_stat_done);
        tvStatDuration = v.findViewById(R.id.tv_stat_duration);
        btnToggleShift = v.findViewById(R.id.btn_toggle_shift);
        viewDot        = v.findViewById(R.id.view_shift_dot);
        llDoneOrders   = v.findViewById(R.id.ll_done_orders);
        tvNoDoneOrders = v.findViewById(R.id.tv_no_done_orders);
        cardStats      = v.findViewById(R.id.card_shift_stats);
        cardDoneOrders = v.findViewById(R.id.card_done_orders);

        btnToggleShift.setOnClickListener(x -> {
            if (getActivity() instanceof BaristaActivity)
                ((BaristaActivity) getActivity()).toggleShift();
        });

        setupObservers();
    }

    private void setupObservers() {
        viewModel.getShiftActive().observe(getViewLifecycleOwner(), active -> {
            if (active) {
                tvStateLabel.setText("Đang trong ca");
                tvStateLabel.setTextColor(
                        requireContext().getResources().getColor(R.color.green_success,
                                requireContext().getTheme()));
                viewDot.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                requireContext().getResources().getColor(R.color.green_success,
                                        requireContext().getTheme())));
                btnToggleShift.setText("Kết thúc ca làm việc");
                btnToggleShift.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                requireContext().getResources().getColor(R.color.red_error,
                                        requireContext().getTheme())));
                btnToggleShift.setIconResource(R.drawable.ic_close);
                cardStats.setVisibility(View.VISIBLE);
                cardDoneOrders.setVisibility(View.VISIBLE);
                timerHandler.post(timerRunnable);
            } else {
                tvStateLabel.setText("Chưa mở ca");
                tvStateLabel.setTextColor(
                        requireContext().getResources().getColor(R.color.text_secondary,
                                requireContext().getTheme()));
                viewDot.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFFEF5350));
                tvOpenTime.setVisibility(View.GONE);
                btnToggleShift.setText("Mở ca làm việc");
                btnToggleShift.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                requireContext().getResources().getColor(R.color.accent_brown,
                                        requireContext().getTheme())));
                btnToggleShift.setIconResource(R.drawable.ic_notification);
                cardStats.setVisibility(View.GONE);
                cardDoneOrders.setVisibility(View.GONE);
                timerHandler.removeCallbacks(timerRunnable);
            }
        });

        viewModel.getShiftOpenedAt().observe(getViewLifecycleOwner(), openedAt -> {
            if (openedAt != null && openedAt > 0) {
                String openStr = new SimpleDateFormat("HH:mm, dd/MM", Locale.getDefault())
                        .format(new Date(openedAt));
                tvOpenTime.setText("Mở ca lúc " + openStr);
                tvOpenTime.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getPendingOrders().observe(getViewLifecycleOwner(),
                orders -> tvStatPending.setText(String.valueOf(orders.size())));

        viewModel.getDoneCount().observe(getViewLifecycleOwner(), count -> {
            tvStatDone.setText(String.valueOf(count));
            updateDuration();
        });

        viewModel.getDoneOrders().observe(getViewLifecycleOwner(),
                this::renderDoneOrders);
    }

    private void updateDuration() {
        Long openedAt = viewModel.getShiftOpenedAt().getValue();
        Boolean active = viewModel.getShiftActive().getValue();
        if (!Boolean.TRUE.equals(active) || openedAt == null || openedAt == 0) return;
        long elapsed = System.currentTimeMillis() - openedAt;
        long hours   = TimeUnit.MILLISECONDS.toHours(elapsed);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60;
        tvStatDuration.setText(hours + " giờ " + minutes + " phút");
    }

    private void renderDoneOrders(List<OrderEntry> doneOrderList) {
        if (getView() == null || llDoneOrders == null) return;
        llDoneOrders.removeAllViews();

        if (doneOrderList == null || doneOrderList.isEmpty()) {
            tvNoDoneOrders.setVisibility(View.VISIBLE);
            return;
        }

        tvNoDoneOrders.setVisibility(View.GONE);
        NumberFormat    fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        for (OrderEntry entry : doneOrderList) {
            OrderModel order = entry.order;

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, 12);
            row.setLayoutParams(rowParams);
            row.setBackgroundResource(R.drawable.bg_cat_unselected);
            row.setPadding(28, 20, 28, 20);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            LinearLayout left = new LinearLayout(requireContext());
            left.setOrientation(LinearLayout.VERTICAL);
            left.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvName = new TextView(requireContext());
            boolean  isDine = OrderModel.TYPE_DINE_IN.equals(order.getOrderType());
            tvName.setText(isDine
                    ? (order.getTableName() != null ? order.getTableName() : "Tại chỗ")
                    : "Mang về");
            tvName.setTextSize(14f);
            tvName.setTextColor(requireContext().getResources()
                    .getColor(R.color.text_primary, requireContext().getTheme()));
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvDetail = new TextView(requireContext());
            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            tvDetail.setText(itemCount + " món  •  "
                    + sdf.format(new Date(order.getCreatedAt())));
            tvDetail.setTextSize(12f);
            tvDetail.setTextColor(requireContext().getResources()
                    .getColor(R.color.text_secondary, requireContext().getTheme()));

            left.addView(tvName);
            left.addView(tvDetail);

            TextView tvTotal = new TextView(requireContext());
            tvTotal.setText(fmt.format(order.getTotal()) + "đ");
            tvTotal.setTextSize(14f);
            tvTotal.setTextColor(requireContext().getResources()
                    .getColor(R.color.green_success, requireContext().getTheme()));
            tvTotal.setTypeface(null, android.graphics.Typeface.BOLD);

            if (llDoneOrders.getChildCount() > 0) {
                View divider = new View(requireContext());
                LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1);
                dp.setMargins(0, 0, 0, 8);
                divider.setLayoutParams(dp);
                divider.setBackgroundColor(requireContext().getResources()
                        .getColor(R.color.divider, requireContext().getTheme()));
                llDoneOrders.addView(divider);
            }

            row.addView(left);
            row.addView(tvTotal);
            llDoneOrders.addView(row);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timerHandler.removeCallbacks(timerRunnable);
    }
}
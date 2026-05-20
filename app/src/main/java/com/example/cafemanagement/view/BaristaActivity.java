package com.example.cafemanagement.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cafemanagement.R;
import com.example.cafemanagement.viewmodel.BaristaViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BaristaActivity extends AppCompatActivity {

    BaristaViewModel viewModel;

    private TextView tvGreeting, tvShiftStatus, tvBadgePending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barista);

        viewModel = new ViewModelProvider(this).get(BaristaViewModel.class);

        tvGreeting     = findViewById(R.id.tv_barista_greeting);
        tvShiftStatus  = findViewById(R.id.tv_shift_status);
        tvBadgePending = findViewById(R.id.tv_badge_pending);
        TabLayout  tabLayout  = findViewById(R.id.tab_layout);
        ViewPager2 viewPager  = findViewById(R.id.view_pager);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = (user != null && user.getDisplayName() != null)
                ? user.getDisplayName() : "Barista";
        tvGreeting.setText("Xin chào, " + name + " ☕");

        viewPager.setAdapter(new BaristaPageAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager, (tab, pos) -> {
            if (pos == 0) tab.setText("📋 Đơn hàng");
            else          tab.setText("⏱ Quản lý ca");
        }).attach();

        // Observe shift state → cập nhật header
        viewModel.getShiftActive().observe(this, active -> {
            if (active) {
                tvShiftStatus.setText("🟢 Đang trong ca");
                tvShiftStatus.setTextColor(
                        getResources().getColor(R.color.green_success, getTheme()));
            } else {
                tvShiftStatus.setText("🔴 Chưa mở ca");
                tvShiftStatus.setTextColor(
                        getResources().getColor(R.color.text_secondary, getTheme()));
                tvBadgePending.setVisibility(View.GONE);
            }
        });

        // Badge số đơn đang chờ
        viewModel.getPendingOrders().observe(this, orders -> {
            boolean active = Boolean.TRUE.equals(viewModel.getShiftActive().getValue());
            if (active && !orders.isEmpty()) {
                tvBadgePending.setVisibility(View.VISIBLE);
                tvBadgePending.setText(String.valueOf(orders.size()));
            } else {
                tvBadgePending.setVisibility(View.GONE);
            }
        });

        if (savedInstanceState == null) viewModel.checkExistingShift();
    }

    // Gọi từ ShiftTabFragment khi bấm nút toggle
    void toggleShift() {
        boolean active = Boolean.TRUE.equals(viewModel.getShiftActive().getValue());
        if (!active) {
            viewModel.openShift();
        } else {
            int done = viewModel.getDoneCount().getValue() != null
                    ? viewModel.getDoneCount().getValue() : 0;
            new AlertDialog.Builder(this)
                    .setTitle("Kết ca")
                    .setMessage("Bạn đã hoàn thành " + done
                            + " đơn trong ca này.\nXác nhận kết ca?")
                    .setPositiveButton("Kết ca", (d, w) -> viewModel.closeShift())
                    .setNegativeButton("Huỷ", null)
                    .show();
        }
    }

    // ─── ViewPager2 Adapter ───────────────────────────────────────────────────

    static class BaristaPageAdapter extends FragmentStateAdapter {
        BaristaPageAdapter(FragmentActivity fa) { super(fa); }
        @Override public int getItemCount() { return 2; }
        @NonNull @Override
        public Fragment createFragment(int position) {
            return position == 0 ? new OrdersTabFragment() : new ShiftTabFragment();
        }
    }
}
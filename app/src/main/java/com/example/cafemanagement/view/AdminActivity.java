package com.example.cafemanagement.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.AdminChartHelper;
import com.example.cafemanagement.helper.PriceFormatter;
import com.example.cafemanagement.model.AdminDashboardStats;
import com.example.cafemanagement.view.chart.DashboardBarChartView;
import com.example.cafemanagement.view.chart.DashboardLineChartView;
import com.example.cafemanagement.view.chart.DashboardPieChartView;
import com.example.cafemanagement.viewmodel.AdminDashboardViewModel;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminActivity extends AdminBaseActivity {

    private AdminDashboardViewModel viewModel;
    private DashboardPieChartView chartTableStatus;
    private DashboardLineChartView chartWeeklyRevenue;
    private DashboardBarChartView chartProductsByCategory;
    private DashboardBarChartView chartStaffByRole;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.content_admin_dashboard;
    }

    @Override
    protected int getNavItemId() {
        return R.id.nav_admin_dashboard;
    }

    @Override
    protected String getAdminTitle() {
        return "Tổng quan";
    }


    @Override
    protected void onAdminContentReady(Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);

        // Ánh xạ các biểu đồ
        chartTableStatus = findViewById(R.id.chartTableStatus);
        chartWeeklyRevenue = findViewById(R.id.chartWeeklyRevenue);
        chartProductsByCategory = findViewById(R.id.chartProductsByCategory);
        chartStaffByRole = findViewById(R.id.chartStaffByRole);

        // Hiển thị ngày tháng
        TextView txtHeaderDate = findViewById(R.id.txtHeaderDate);
        if (txtHeaderDate != null) {
            String dateStr = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("vi", "VN"))
                    .format(new Date());
            txtHeaderDate.setText(dateStr);
        }

        setupQuickActions();
        setupInitialStats();

        viewModel.getStats().observe(this, this::bindStats);

        // --- BỔ SUNG LOGIC AI ---
        findViewById(R.id.btnTriggerAi).setOnClickListener(v -> viewModel.triggerAiForecast());

        viewModel.getIsAiLoading().observe(this, isLoading -> {
            ProgressBar progressBar = findViewById(R.id.aiProgressBar);
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getAiForecast().observe(this, result -> {
            if (result != null) showAiResultDialog(result);
        });
        // Lắng nghe lỗi
        viewModel.getAiError().observe(this, error -> {
            if (error != null) {
                android.widget.Toast.makeText(this, "AI Lỗi: " + error, android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- Đưa phương thức này ra ngoài onAdminContentReady ---
    private void showAiResultDialog(com.example.cafemanagement.model.AiForecastModel result) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_ai_forecast, null);
        builder.setView(view);

        android.widget.TextView txtRevenue = view.findViewById(R.id.txtAiPredictedRevenue);
        android.widget.TextView txtAdvice = view.findViewById(R.id.txtAiAdvice);

        // Tạo đối tượng dialog để quản lý
        android.app.AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnCloseAiDialog).setOnClickListener(v -> dialog.dismiss());

        // Format danh sách dự báo
        StringBuilder sb = new StringBuilder();
        for (Double val : result.getPredictedRevenue()) {
            sb.append(com.example.cafemanagement.helper.PriceFormatter.formatPrice(val)).append("\n");
        }

        txtRevenue.setText(sb.toString());
        txtAdvice.setText(result.getBusinessAdvice());

        dialog.show();
    }

    private void setupQuickActions() {
        View cardTables = findViewById(R.id.cardManageTables);
        if (cardTables != null) {
            cardTables.setOnClickListener(v -> startActivity(new Intent(this, AdminTableActivity.class)));
        }

        View cardMenu = findViewById(R.id.cardManageMenu);
        if (cardMenu != null) {
            cardMenu.setOnClickListener(v -> startActivity(new Intent(this, AdminMenuActivity.class)));
        }

        View cardStaff = findViewById(R.id.cardManageStaff);
        if (cardStaff != null) {
            cardStaff.setOnClickListener(v -> startActivity(new Intent(this, AdminStaffActivity.class)));
        }
    }

    private void setupInitialStats() {
        setupStatCard(R.id.statTables, "🪑", R.color.stat_green, R.color.stat_green_icon,
                "0", "Tổng số bàn", null);
        setupStatCard(R.id.statProducts, "☕", R.color.stat_orange, R.color.stat_orange_icon,
                "0", "Món trong menu", null);
        setupStatCard(R.id.statStaff, "👥", R.color.stat_purple, R.color.stat_purple_icon,
                "0", "Nhân sự", null);
        setupStatCard(R.id.statCategories, "📂", R.color.stat_blue, R.color.stat_blue_icon,
                "0", "Danh mục", null);
    }

    private void setupStatCard(int includeId, String icon, int bgColor, int iconColor,
                               String value, String label, String sub) {
        View root = findViewById(includeId);
        if (root == null) return;

        MaterialCardView iconContainer = root.findViewById(R.id.cardStatIconContainer);
        if (iconContainer != null) {
            iconContainer.setCardBackgroundColor(ContextCompat.getColor(this, bgColor));
        }

        TextView txtIcon = root.findViewById(R.id.txtStatIcon);
        if (txtIcon != null) {
            txtIcon.setText(icon);
            txtIcon.setTextColor(ContextCompat.getColor(this, iconColor));
        }

        TextView txtValue = root.findViewById(R.id.txtStatValue);
        if (txtValue != null) txtValue.setText(value);

        TextView txtLabel = root.findViewById(R.id.txtStatLabel);
        if (txtLabel != null) txtLabel.setText(label);

        TextView txtStatSub = root.findViewById(R.id.txtStatSub);
        if (txtStatSub != null) {
            if (sub != null) {
                txtStatSub.setVisibility(View.VISIBLE);
                txtStatSub.setText(sub);
            } else {
                txtStatSub.setVisibility(View.GONE);
            }
        }
    }

    private void bindStats(AdminDashboardStats stats) {
        if (stats == null) return;

        setText(R.id.txtOccupancyBadge, stats.getOccupancyPercent() + "%");
        setText(R.id.txtTodayRevenue, PriceFormatter.formatPrice(stats.getEstimatedTodayRevenue()));
        setText(R.id.txtStatTablesDetail,
                stats.getOccupiedTables() + " bàn bận / " + stats.getTotalTables() + " bàn");

        updateStatValue(R.id.statTables, String.valueOf(stats.getTotalTables()), 
                stats.getAvailableTables() + " bàn trống");
        updateStatValue(R.id.statProducts, String.valueOf(stats.getTotalProducts()), null);
        updateStatValue(R.id.statStaff, String.valueOf(stats.getTotalStaff()), null);
        updateStatValue(R.id.statCategories, String.valueOf(stats.getTotalCategories()), null);

        if (chartTableStatus != null) {
            AdminChartHelper.setupTablePieChart(chartTableStatus,
                    stats.getAvailableTables(), stats.getOccupiedTables());
        }
        if (chartWeeklyRevenue != null) {
            AdminChartHelper.setupRevenueLineChart(chartWeeklyRevenue, stats.getWeeklyRevenue());
        }
        if (chartProductsByCategory != null) {
            AdminChartHelper.setupCategoryBarChart(chartProductsByCategory,
                    stats.getCategoryLabels(), stats.getCategoryCounts());
        }
        if (chartStaffByRole != null) {
            AdminChartHelper.setupStaffBarChart(chartStaffByRole,
                    stats.getAdminCount(), stats.getCashierCount(), stats.getBaristaCount());
        }
    }

    private void updateStatValue(int includeId, String value, String sub) {
        View root = findViewById(includeId);
        if (root == null) return;
        
        TextView txtValue = root.findViewById(R.id.txtStatValue);
        if (txtValue != null) txtValue.setText(value);
        
        TextView txtStatSub = root.findViewById(R.id.txtStatSub);
        if (txtStatSub != null) {
            if (sub != null) {
                txtStatSub.setVisibility(View.VISIBLE);
                txtStatSub.setText(sub);
            } else {
                txtStatSub.setVisibility(View.GONE);
            }
        }
    }

    private void setText(int id, String value) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(value);
    }
}

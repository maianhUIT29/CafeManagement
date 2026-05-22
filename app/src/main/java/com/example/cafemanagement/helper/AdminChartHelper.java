package com.example.cafemanagement.helper;

import com.example.cafemanagement.view.chart.DashboardBarChartView;
import com.example.cafemanagement.view.chart.DashboardLineChartView;
import com.example.cafemanagement.view.chart.DashboardPieChartView;

import java.util.List;

public final class AdminChartHelper {

    private AdminChartHelper() {
    }

    /**
     * Cấu hình biểu đồ tròn tình trạng bàn với màu sắc hiện đại
     */
    public static void setupTablePieChart(DashboardPieChartView chart, int available, int occupied) {
        chart.setData(
                new float[]{available, occupied},
                new int[]{0xFF388E3C, 0xFFE53935}, // Green 700, Red 600
                new String[]{"Trống", "Bận"}
        );
    }

    /**
     * Cấu hình biểu đồ cột món theo danh mục với dải màu gradient
     */
    public static void setupCategoryBarChart(DashboardBarChartView chart,
                                             List<String> labels, List<Integer> counts) {
        int n = Math.min(labels != null ? labels.size() : 0, counts != null ? counts.size() : 0);
        float[] values = new float[Math.max(n, 1)];
        String[] axisLabels = new String[values.length];
        int[] colors = new int[values.length];
        
        // Sử dụng màu Blue 700 làm chủ đạo
        int baseColor = 0xFF1976D2; 
        for (int i = 0; i < n; i++) {
            values[i] = counts.get(i);
            axisLabels[i] = labels.get(i);
            colors[i] = blendToAmber(baseColor, i, n);
        }
        if (n == 0) {
            values[0] = 0;
            axisLabels[0] = "—";
            colors[0] = baseColor;
        }
        chart.setData(values, axisLabels, colors);
    }

    /**
     * Cấu hình biểu đồ cột nhân sự với các màu phân biệt rõ ràng
     */
    public static void setupStaffBarChart(DashboardBarChartView chart,
                                          int admin, int cashier, int barista) {
        chart.setData(
                new float[]{admin, cashier, barista},
                new String[]{"Quản trị", "Thu ngân", "Pha chế"},
                new int[]{0xFF7B1FA2, 0xFFF57C00, 0xFF1976D2} // Purple, Orange, Blue
        );
    }

    /**
     * Cấu hình biểu đồ đường doanh thu
     */
    public static void setupRevenueLineChart(DashboardLineChartView chart, float[] weeklyMillions) {
        chart.setValues(weeklyMillions != null ? weeklyMillions : new float[7]);
    }

    private static int blendToAmber(int base, int index, int total) {
        float t = total <= 1 ? 0 : (float) index / (total - 1);
        int r = (base >> 16) & 0xFF;
        int g = (base >> 8) & 0xFF;
        int b = base & 0xFF;
        // Pha trộn dần sang màu Amber (#FFC107)
        int r2 = 0xFF, g2 = 0xC1, b2 = 0x07;
        int nr = (int) (r + (r2 - r) * t);
        int ng = (int) (g + (g2 - g) * t);
        int nb = (int) (b + (b2 - b) * t);
        return 0xFF000000 | (nr << 16) | (ng << 8) | nb;
    }
}

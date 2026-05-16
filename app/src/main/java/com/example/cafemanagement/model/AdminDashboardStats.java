package com.example.cafemanagement.model;

import java.util.Collections;
import java.util.List;

public class AdminDashboardStats {
    private int totalTables;
    private int availableTables;
    private int occupiedTables;
    private int totalProducts;
    private int totalStaff;
    private int adminCount;
    private int cashierCount;
    private int baristaCount;
    private int totalCategories;
    private int occupancyPercent;
    private long estimatedTodayRevenue;
    private List<String> categoryLabels = Collections.emptyList();
    private List<Integer> categoryCounts = Collections.emptyList();
    private float[] weeklyRevenue = new float[7];

    public AdminDashboardStats() {
    }

    public int getTotalTables() { return totalTables; }
    public void setTotalTables(int totalTables) { this.totalTables = totalTables; }

    public int getAvailableTables() { return availableTables; }
    public void setAvailableTables(int availableTables) { this.availableTables = availableTables; }

    public int getOccupiedTables() { return occupiedTables; }
    public void setOccupiedTables(int occupiedTables) { this.occupiedTables = occupiedTables; }

    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }

    public int getTotalStaff() { return totalStaff; }
    public void setTotalStaff(int totalStaff) { this.totalStaff = totalStaff; }

    public int getAdminCount() { return adminCount; }
    public void setAdminCount(int adminCount) { this.adminCount = adminCount; }

    public int getCashierCount() { return cashierCount; }
    public void setCashierCount(int cashierCount) { this.cashierCount = cashierCount; }

    public int getBaristaCount() { return baristaCount; }
    public void setBaristaCount(int baristaCount) { this.baristaCount = baristaCount; }

    public int getTotalCategories() { return totalCategories; }
    public void setTotalCategories(int totalCategories) { this.totalCategories = totalCategories; }

    public int getOccupancyPercent() { return occupancyPercent; }
    public void setOccupancyPercent(int occupancyPercent) { this.occupancyPercent = occupancyPercent; }

    public long getEstimatedTodayRevenue() { return estimatedTodayRevenue; }
    public void setEstimatedTodayRevenue(long estimatedTodayRevenue) {
        this.estimatedTodayRevenue = estimatedTodayRevenue;
    }

    public List<String> getCategoryLabels() { return categoryLabels; }
    public void setCategoryLabels(List<String> categoryLabels) {
        this.categoryLabels = categoryLabels != null ? categoryLabels : Collections.emptyList();
    }

    public List<Integer> getCategoryCounts() { return categoryCounts; }
    public void setCategoryCounts(List<Integer> categoryCounts) {
        this.categoryCounts = categoryCounts != null ? categoryCounts : Collections.emptyList();
    }

    public float[] getWeeklyRevenue() { return weeklyRevenue; }
    public void setWeeklyRevenue(float[] weeklyRevenue) {
        this.weeklyRevenue = weeklyRevenue != null ? weeklyRevenue : new float[7];
    }
}

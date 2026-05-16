package com.example.cafemanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.model.AdminDashboardStats;
import com.example.cafemanagement.model.CategoryModel;
import com.example.cafemanagement.model.ProductModel;
import com.example.cafemanagement.model.TableModel;
import com.example.cafemanagement.model.UserModel;
import com.example.cafemanagement.repository.CategoryRepository;
import com.example.cafemanagement.repository.ProductRepository;
import com.example.cafemanagement.repository.TableRepository;
import com.example.cafemanagement.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardViewModel extends ViewModel {
    private final TableRepository tableRepository = new TableRepository();
    private final ProductRepository productRepository = new ProductRepository();
    private final UserRepository userRepository = new UserRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();

    private final MediatorLiveData<AdminDashboardStats> statsLiveData = new MediatorLiveData<>();

    public AdminDashboardViewModel() {
        LiveData<List<TableModel>> tables = tableRepository.getAllTables();
        LiveData<List<ProductModel>> products = productRepository.getAllProducts();
        LiveData<List<UserModel>> staff = userRepository.getStaffUsers();
        LiveData<List<CategoryModel>> categories = categoryRepository.getCategories();

        statsLiveData.addSource(tables, list -> updateStats(tables, products, staff, categories));
        statsLiveData.addSource(products, list -> updateStats(tables, products, staff, categories));
        statsLiveData.addSource(staff, list -> updateStats(tables, products, staff, categories));
        statsLiveData.addSource(categories, list -> updateStats(tables, products, staff, categories));
    }

    public LiveData<AdminDashboardStats> getStats() {
        return statsLiveData;
    }

    private void updateStats(LiveData<List<TableModel>> tables,
                             LiveData<List<ProductModel>> products,
                             LiveData<List<UserModel>> staff,
                             LiveData<List<CategoryModel>> categories) {
        List<TableModel> tableList = tables.getValue();
        List<ProductModel> productList = products.getValue();
        List<UserModel> staffList = staff.getValue();
        List<CategoryModel> categoryList = categories.getValue();

        AdminDashboardStats stats = new AdminDashboardStats();

        int totalTables = tableList != null ? tableList.size() : 0;
        int available = 0;
        int occupied = 0;
        if (tableList != null) {
            for (TableModel t : tableList) {
                if ("OCCUPIED".equals(t.getStatus())) {
                    occupied++;
                } else {
                    available++;
                }
            }
        }
        stats.setTotalTables(totalTables);
        stats.setAvailableTables(available);
        stats.setOccupiedTables(occupied);
        stats.setOccupancyPercent(totalTables > 0 ? Math.round(occupied * 100f / totalTables) : 0);

        int totalProducts = productList != null ? productList.size() : 0;
        stats.setTotalProducts(totalProducts);

        int admin = 0, cashier = 0, barista = 0;
        if (staffList != null) {
            for (UserModel u : staffList) {
                if (u.getRole() == null) continue;
                switch (u.getRole()) {
                    case "admin": admin++; break;
                    case "cashier": cashier++; break;
                    case "barista": barista++; break;
                    default: break;
                }
            }
        }
        stats.setTotalStaff(staffList != null ? staffList.size() : 0);
        stats.setAdminCount(admin);
        stats.setCashierCount(cashier);
        stats.setBaristaCount(barista);

        int catTotal = categoryList != null ? categoryList.size() : 0;
        stats.setTotalCategories(catTotal);

        buildCategoryChart(stats, productList, categoryList);
        buildWeeklyRevenue(stats, occupied, totalProducts);

        long todayEstimate = (long) occupied * 185_000L + totalProducts * 12_000L;
        stats.setEstimatedTodayRevenue(todayEstimate);

        statsLiveData.setValue(stats);
    }

    private void buildCategoryChart(AdminDashboardStats stats,
                                    List<ProductModel> products,
                                    List<CategoryModel> categories) {
        if (categories == null || categories.isEmpty()) {
            stats.setCategoryLabels(new ArrayList<>());
            stats.setCategoryCounts(new ArrayList<>());
            return;
        }

        Map<String, Integer> countMap = new HashMap<>();
        for (CategoryModel c : categories) {
            countMap.put(c.getId(), 0);
        }
        if (products != null) {
            for (ProductModel p : products) {
                String catId = p.getCategoryId();
                if (catId != null && countMap.containsKey(catId)) {
                    countMap.put(catId, countMap.get(catId) + 1);
                }
            }
        }

        List<String> labels = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (CategoryModel c : categories) {
            labels.add(c.getName() != null ? c.getName() : "Khác");
            counts.add(countMap.getOrDefault(c.getId(), 0));
        }
        stats.setCategoryLabels(labels);
        stats.setCategoryCounts(counts);
    }

    /** Ước tính doanh thu 7 ngày từ mức sử dụng bàn (demo cho dashboard). */
    private void buildWeeklyRevenue(AdminDashboardStats stats, int occupied, int totalProducts) {
        float base = occupied * 1.85f + totalProducts * 0.12f;
        if (base < 0.5f) base = 0.5f;
        float[] week = new float[7];
        float[] factors = {0.82f, 0.91f, 0.88f, 1.0f, 1.12f, 1.25f, 1.08f};
        for (int i = 0; i < 7; i++) {
            week[i] = base * factors[i];
        }
        stats.setWeeklyRevenue(week);
    }
}

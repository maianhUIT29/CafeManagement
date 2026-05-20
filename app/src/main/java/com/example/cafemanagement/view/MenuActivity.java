package com.example.cafemanagement.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.BasketManager;
import com.example.cafemanagement.R;
import com.example.cafemanagement.adapter.CategoryAdapter;
import com.example.cafemanagement.adapter.ProductAdapter;
import com.example.cafemanagement.model.ProductModel;
import com.example.cafemanagement.viewmodel.MenuViewModel;
import com.google.android.material.card.MaterialCardView;

public class MenuActivity extends AppCompatActivity {
    private String id;
    private MenuViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private TextView txtServiceInfo;
    private RecyclerView recyclerCategories, recyclerProducts;

    // Bổ sung các thành phần cho thanh giỏ hàng
    private MaterialCardView layoutCartBar;
    private TextView txtCartTotal, txtCartCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initViews();
        handleIntentData();
        setupAdapters();
        setupViewModel();
        setupListeners(); // Thiết lập sự kiện nhấn
    }

    private void initViews() {
        txtServiceInfo = findViewById(R.id.txtServiceInfo);
        recyclerCategories = findViewById(R.id.recyclerCategories);
        recyclerProducts = findViewById(R.id.recyclerProducts);

        // Ánh xạ thành phần giỏ hàng
        layoutCartBar = findViewById(R.id.layoutCartBar);
        txtCartTotal = findViewById(R.id.txtCartTotal);
        txtCartCount = findViewById(R.id.txtCartCount);
    }

    private void handleIntentData() {
        String tableName = getIntent().getStringExtra("TABLE_NAME");
        if (tableName != null) {
            txtServiceInfo.setText(tableName + " | Change");
        } else {
            txtServiceInfo.setText("Takeaway | Change");
        }
    }

    /**
     * Thiết lập các sự kiện tương tác
     */
    private void setupListeners() {
        // Sự kiện chuyển màn hình khi bấm vào toàn bộ thanh giỏ hàng
        layoutCartBar.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, BasketActivity.class);
            startActivity(intent);
        });
    }

    private void setupAdapters() {
        categoryAdapter = new CategoryAdapter(category -> {
            viewModel.filterByCategory(category.getId());
        });

        recyclerCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerCategories.setAdapter(categoryAdapter);

        productAdapter = new ProductAdapter(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onAddQuickClick(ProductModel product) {
                // Lưu vào kho dữ liệu
                BasketManager.getInstance().addProduct(product);

                // Thông báo cho người dùng
                Toast.makeText(MenuActivity.this, "Đã thêm " + product.getName(), Toast.LENGTH_SHORT).show();

                // Cập nhật lại UI ngay lập tức
                updateCartSummary();
            }

            @Override
            public void onItemDetailClick(ProductModel product) {
                Intent intent = new Intent(MenuActivity.this, DrinkCustomizeActivity.class);
                intent.putExtra("PRODUCT_ID", product.getProductId());
                intent.putExtra("PRODUCT_NAME", product.getName());
                intent.putExtra("PRODUCT_PRICE", product.getPrice());
                if (product.getImageUrl() != null) {
                    intent.putExtra("PRODUCT_IMAGE", product.getImageUrl());
                }
                startActivity(intent);
            }
        });

        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerProducts.setAdapter(productAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MenuViewModel.class);

        viewModel.getCategories().observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                categoryAdapter.setCategoryList(categories);
                viewModel.filterByCategory(categories.get(0).getId());
            }
        });

        viewModel.getFilteredProducts().observe(this, products -> {
            if (products != null) {
                productAdapter.setProductList(products);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật lại thanh giỏ hàng mỗi khi quay lại màn hình này
        updateCartSummary();
    }

    /**
     * Hàm tính toán và cập nhật giao diện thanh giỏ hàng
     */
    private void updateCartSummary() {
        // Lấy dữ liệu thực tế từ lớp quản lý giỏ hàng
        double total = BasketManager.getInstance().getTotalPrice();

        // Sử dụng getTotalQuantity() thay vì getBasketItems().size()
        int count = BasketManager.getInstance().getTotalQuantity();

        // 1. Luôn hiển thị thanh giỏ hàng
        layoutCartBar.setVisibility(View.VISIBLE);

        // 2. Cập nhật con số hiển thị (Đã sửa định dạng chuẩn)
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#0.##");
        txtCartTotal.setText(formatter.format(total) + "đ");

        txtCartCount.setText(count + " Xem đơn hàng");

        // 3. Khóa nút bấm nếu giỏ hàng trống (0 món) để tránh lỗi chuyển đổi màn hình
        if (count == 0) {
            layoutCartBar.setEnabled(false);
            layoutCartBar.setAlpha(0.6f);
        } else {
            layoutCartBar.setEnabled(true);
            layoutCartBar.setAlpha(1.0f);
        }
    }
}
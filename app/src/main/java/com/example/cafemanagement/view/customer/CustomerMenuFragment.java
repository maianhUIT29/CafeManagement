package com.example.cafemanagement.view.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class CustomerMenuFragment extends Fragment {
    private String id;
    private MenuViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private TextView txtServiceInfo;
    private RecyclerView recyclerCategories, recyclerProducts;

    // Bổ sung các thành phần cho thanh giỏ hàng
    private MaterialCardView layoutCartBar;
    private TextView txtCartTotal, txtCartCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        handleIntentData();
        setupAdapters();
        setupViewModel();
        setupListeners(); // Thiết lập sự kiện nhấn
    }

    private void initViews(View view) {
        txtServiceInfo = view.findViewById(R.id.txtServiceInfo);
        recyclerCategories = view.findViewById(R.id.recyclerCategories);
        recyclerProducts = view.findViewById(R.id.recyclerProducts);

        // Ánh xạ thành phần giỏ hàng
        layoutCartBar = view.findViewById(R.id.layoutCartBar);
        txtCartTotal = view.findViewById(R.id.txtCartTotal);
        txtCartCount = view.findViewById(R.id.txtCartCount);
    }

    private void handleIntentData() {
        if (getActivity() != null && getActivity().getIntent() != null) {
            String tableName = getActivity().getIntent().getStringExtra("TABLE_NAME");
            if (tableName != null) {
                txtServiceInfo.setText(tableName + " | Change");
            } else {
                txtServiceInfo.setText("Takeaway | Change");
            }
        }
    }

    /**
     * Thiết lập các sự kiện tương tác
     */
    private void setupListeners() {
        // Sự kiện chuyển màn hình khi bấm vào toàn bộ thanh giỏ hàng
        layoutCartBar.setOnClickListener(v -> {
            // ĐÃ SỬA: Chuyển tab sang Giỏ hàng thông qua BottomNavigationView của MainActivity
            if (getActivity() instanceof CustomerMainActivity) {
                CustomerMainActivity activity = (CustomerMainActivity) getActivity();
                activity.findViewById(R.id.nav_cart).performClick();
            }
        });

        // Sự kiện bấm vào thông tin dịch vụ (Bàn/Mang về) để đổi lại
        txtServiceInfo.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CustomerOrderSetupActivity.class);
            startActivity(intent);
        });
    }

    private void setupAdapters() {
        categoryAdapter = new CategoryAdapter(category -> {
            viewModel.filterByCategory(category.getId());
        });

        recyclerCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerCategories.setAdapter(categoryAdapter);

        productAdapter = new ProductAdapter(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onAddQuickClick(ProductModel product) {
                // Lưu vào kho dữ liệu
                BasketManager.getInstance().addProduct(product);

                // Thông báo cho người dùng
                Toast.makeText(getContext(), "Đã thêm " + product.getName(), Toast.LENGTH_SHORT).show();

                // Cập nhật lại UI ngay lập tức
                updateCartSummary();
            }

            @Override
            public void onItemDetailClick(ProductModel product) {
                Intent intent = new Intent(getContext(), CustomerDrinkCustomizeActivity.class);
                intent.putExtra("PRODUCT_ID", product.getProductId());
                intent.putExtra("PRODUCT_NAME", product.getName());
                intent.putExtra("PRODUCT_PRICE", product.getPrice());
                if (product.getImageUrl() != null) {
                    intent.putExtra("PRODUCT_IMAGE", product.getImageUrl());
                }
                startActivity(intent);
            }
        });

        recyclerProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerProducts.setAdapter(productAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MenuViewModel.class);

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                categoryAdapter.setCategoryList(categories);
                viewModel.filterByCategory(categories.get(0).getId());
            }
        });

        viewModel.getFilteredProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                productAdapter.setProductList(products);
            }
        });
    }

    @Override
    public void onResume() {
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
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
        txtCartTotal.setText(formatter.format(total) + "đ");

        txtCartCount.setText(count + " món | Xem giỏ hàng");

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
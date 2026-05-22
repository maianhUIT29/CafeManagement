package com.example.cafemanagement.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.adapter.AdminProductAdapter;
import com.example.cafemanagement.model.CategoryModel;
import com.example.cafemanagement.model.ProductModel;
import com.example.cafemanagement.viewmodel.AdminMenuViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminMenuActivity extends AdminBaseActivity {

    private AdminMenuViewModel viewModel;
    private AdminProductAdapter productAdapter;

    private Spinner spinnerCategoryFilter;

    private TextView txtMenuStatCategories;
    private TextView txtMenuStatProducts;
    private TextView txtMenuStatFiltered;
    private TextView txtSelectedCategoryLabel;
    private TextView txtProductListCount;
    private TextView txtEmptyProducts;
    private TextView txtEmptyMenuHint;

    private View layoutEmptyMenu;

    private final List<CategoryModel> allCategories = new ArrayList<>();
    private final List<ProductModel> allProducts = new ArrayList<>();

    private final List<CategoryModel> categoryDropdownItems = new ArrayList<>();
    private final List<String> categoryDropdownNames = new ArrayList<>();

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_admin_menu;
    }

    @Override
    protected int getNavItemId() {
        return R.id.nav_admin_menu;
    }

    @Override
    protected String getAdminTitle() {
        return "Quản lý thực đơn";
    }

    @Override
    protected void onAdminContentReady(Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AdminMenuViewModel.class);

        bindViews();
        setupProductRecycler();
        setupActions();
        observeData();
    }

    private void bindViews() {
        spinnerCategoryFilter = findViewById(R.id.spinnerCategoryFilter);

        txtMenuStatCategories = findViewById(R.id.txtMenuStatCategories);
        txtMenuStatProducts = findViewById(R.id.txtMenuStatProducts);
        txtMenuStatFiltered = findViewById(R.id.txtMenuStatFiltered);
        txtSelectedCategoryLabel = findViewById(R.id.txtSelectedCategoryLabel);
        txtProductListCount = findViewById(R.id.txtProductListCount);
        txtEmptyProducts = findViewById(R.id.txtEmptyProducts);
        txtEmptyMenuHint = findViewById(R.id.txtEmptyMenuHint);

        layoutEmptyMenu = findViewById(R.id.layoutEmptyMenu);
    }

    private void setupProductRecycler() {
        RecyclerView recyclerProducts = findViewById(R.id.recyclerProducts);
        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));

        productAdapter = new AdminProductAdapter(new AdminProductAdapter.ProductActionListener() {
            @Override
            public void onEdit(ProductModel product) {
                showProductDialog(product);
            }

            @Override
            public void onDelete(ProductModel product) {
                confirmDeleteProduct(product);
            }
        });

        recyclerProducts.setAdapter(productAdapter);
    }

    private void setupActions() {
        findViewById(R.id.btnAddCategory).setOnClickListener(v -> showCategoryDialog(null));
        findViewById(R.id.fabAddProduct).setOnClickListener(v -> showProductDialog(null));
    }

    private void observeData() {
        viewModel.getCategories().observe(this, categories -> {
            allCategories.clear();

            if (categories != null) {
                allCategories.addAll(categories);
            }

            txtMenuStatCategories.setText(String.valueOf(allCategories.size()));
            setupCategoryDropdown();
        });

        viewModel.getAllProducts().observe(this, products -> {
            allProducts.clear();

            if (products != null) {
                allProducts.addAll(products);
            }

            txtMenuStatProducts.setText(String.valueOf(allProducts.size()));
            viewModel.applyFilter(allProducts);
        });

        viewModel.getFilteredProducts().observe(this, products -> {
            List<ProductModel> list = products != null ? products : new ArrayList<>();

            productAdapter.setItems(list);

            txtMenuStatFiltered.setText(String.valueOf(list.size()));
            txtProductListCount.setText(list.size() + " món");

            if (list.isEmpty()) {
                layoutEmptyMenu.setVisibility(View.VISIBLE);
                txtEmptyProducts.setText("Chưa có món");
                txtEmptyMenuHint.setText("Thêm danh mục trước, sau đó thêm món");
            } else {
                layoutEmptyMenu.setVisibility(View.GONE);
            }
        });
    }

    private void setupCategoryDropdown() {
        categoryDropdownItems.clear();
        categoryDropdownNames.clear();

        categoryDropdownItems.add(null);
        categoryDropdownNames.add("Tất cả danh mục");

        for (CategoryModel category : allCategories) {
            if (category == null) continue;

            categoryDropdownItems.add(category);

            String name = category.getName();
            if (TextUtils.isEmpty(name)) {
                name = "Danh mục chưa đặt tên";
            }

            categoryDropdownNames.add(name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryDropdownNames
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoryFilter.setAdapter(adapter);

        spinnerCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CategoryModel selected = categoryDropdownItems.get(position);

                if (selected == null) {
                    viewModel.setCategoryFilter(null);
                    txtSelectedCategoryLabel.setText("Tất cả danh mục");
                } else {
                    viewModel.setCategoryFilter(selected.getId());
                    txtSelectedCategoryLabel.setText(selected.getName());
                }

                viewModel.applyFilter(allProducts);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                viewModel.setCategoryFilter(null);
                txtSelectedCategoryLabel.setText("Tất cả danh mục");
                viewModel.applyFilter(allProducts);
            }
        });
    }

    private void showCategoryDialog(CategoryModel existing) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_category, null);

        TextInputEditText edtName = dialogView.findViewById(R.id.edtCategoryName);
        TextInputEditText edtSort = dialogView.findViewById(R.id.edtCategorySort);

        boolean isEdit = existing != null;

        if (isEdit) {
            edtName.setText(existing.getName());
            edtSort.setText(String.valueOf(existing.getSortOrder()));
        } else {
            edtSort.setText("0");
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(isEdit ? "Sửa danh mục" : "Thêm danh mục")
                .setView(dialogView)
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = textOf(edtName);
                String sortText = textOf(edtSort);

                if (TextUtils.isEmpty(name)) {
                    edtName.setError("Vui lòng nhập tên danh mục");
                    edtName.requestFocus();
                    return;
                }

                int sortOrder = 0;
                try {
                    sortOrder = TextUtils.isEmpty(sortText) ? 0 : Integer.parseInt(sortText);
                } catch (NumberFormatException e) {
                    edtSort.setError("Thứ tự phải là số");
                    edtSort.requestFocus();
                    return;
                }

                CategoryModel category = isEdit ? existing : new CategoryModel();
                category.setName(name);
                category.setSortOrder(sortOrder);

                viewModel.saveCategory(
                        category,
                        isEdit,
                        adminRepositoryCallback(isEdit ? "Đã cập nhật danh mục" : "Đã thêm danh mục")
                );

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showProductDialog(ProductModel existing) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_product, null);

        TextInputEditText edtName = dialogView.findViewById(R.id.edtProductName);
        TextInputEditText edtPrice = dialogView.findViewById(R.id.edtProductPrice);
        TextInputEditText edtDescription = dialogView.findViewById(R.id.edtProductDescription);
        TextInputEditText edtImageUrl = dialogView.findViewById(R.id.edtProductImageUrl);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerProductCategory);

        List<String> categoryNames = new ArrayList<>();

        for (CategoryModel category : allCategories) {
            String name = category != null ? category.getName() : "";
            categoryNames.add(TextUtils.isEmpty(name) ? "Danh mục chưa đặt tên" : name);
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
        );

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        boolean isEdit = existing != null;

        if (isEdit) {
            edtName.setText(existing.getName());
            edtPrice.setText(String.valueOf(existing.getPrice()));
            edtDescription.setText(existing.getDescription());
            edtImageUrl.setText(existing.getImageUrl());

            int selectedIndex = findCategoryIndex(existing.getCategoryId());
            if (selectedIndex >= 0) {
                spinnerCategory.setSelection(selectedIndex);
            }
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(isEdit ? "Sửa món" : "Thêm món")
                .setView(dialogView)
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (allCategories.isEmpty()) {
                    adminToastInfo("Vui lòng thêm danh mục trước");
                    return;
                }

                String name = textOf(edtName);
                String priceText = textOf(edtPrice);

                if (TextUtils.isEmpty(name)) {
                    edtName.setError("Vui lòng nhập tên món");
                    edtName.requestFocus();
                    return;
                }

                int price;
                try {
                    price = Integer.parseInt(priceText);
                } catch (NumberFormatException e) {
                    edtPrice.setError("Giá phải là số");
                    edtPrice.requestFocus();
                    return;
                }

                int categoryPosition = spinnerCategory.getSelectedItemPosition();
                if (categoryPosition < 0 || categoryPosition >= allCategories.size()) {
                    adminToastInfo("Vui lòng chọn danh mục");
                    return;
                }

                CategoryModel selectedCategory = allCategories.get(categoryPosition);

                ProductModel product = isEdit ? existing : new ProductModel();
                product.setName(name);
                product.setPrice(price);
                product.setCategoryId(selectedCategory.getId());
                product.setDescription(textOf(edtDescription));
                product.setImageUrl(textOf(edtImageUrl));

                viewModel.saveProduct(
                        product,
                        isEdit,
                        adminRepositoryCallback(isEdit ? "Đã cập nhật món" : "Đã thêm món")
                );

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private int findCategoryIndex(String categoryId) {
        if (TextUtils.isEmpty(categoryId)) {
            return -1;
        }

        for (int i = 0; i < allCategories.size(); i++) {
            CategoryModel category = allCategories.get(i);
            if (category != null && categoryId.equals(category.getId())) {
                return i;
            }
        }

        return -1;
    }

    private void confirmDeleteProduct(ProductModel product) {
        if (product == null) return;

        String name = TextUtils.isEmpty(product.getName()) ? "món này" : product.getName();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa món")
                .setMessage("Bạn có chắc muốn xóa \"" + name + "\"?")
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Xóa", (dialog, which) ->
                        viewModel.deleteProduct(
                                product.getProductId(),
                                adminRepositoryCallback("Đã xóa món")
                        )
                )
                .show();
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null
                ? editText.getText().toString().trim()
                : "";
    }
}

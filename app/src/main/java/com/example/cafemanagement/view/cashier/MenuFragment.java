package com.example.cafemanagement.view.cashier;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.CartManager;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.CategoryModel;
import com.example.cafemanagement.model.ProductModel;
import com.example.cafemanagement.view.CashierActivity;
import com.example.cafemanagement.view.cashier.adapter.CategoryTabAdapter;
import com.example.cafemanagement.view.cashier.adapter.ProductGridAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MenuFragment extends Fragment {

    private RecyclerView       rvCategories, rvProducts;
    private TextView           tvViewOrder, tvOrderBadge;
    private CategoryTabAdapter categoryAdapter;
    private ProductGridAdapter productAdapter;

    private final Map<String, CategoryModel> categoriesMap = new LinkedHashMap<>();
    private final Map<String, ProductModel>  productsMap   = new LinkedHashMap<>();
    private final List<String>               catIds        = new ArrayList<>();
    private final List<CategoryModel>        catList       = new ArrayList<>();
    private final List<String>               prodIds       = new ArrayList<>();
    private final List<ProductModel>         prodList      = new ArrayList<>();

    private String  currentCatId        = null;
    // Flag: đã navigate sang Payment chưa — nếu có thì KHÔNG giải phóng bàn khi back
    private boolean navigatedToPayment  = false;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCategories = view.findViewById(R.id.rv_categories);
        rvProducts   = view.findViewById(R.id.rv_products);
        tvViewOrder  = view.findViewById(R.id.tv_view_order_label);
        tvOrderBadge = view.findViewById(R.id.tv_order_badge);

        // Category tabs (horizontal)
        categoryAdapter = new CategoryTabAdapter(catList, catIds, catId -> {
            currentCatId = catId;
            filterProducts(catId);
            categoryAdapter.setSelectedId(catId);
        });
        rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Product grid
        productAdapter = new ProductGridAdapter(prodList, prodIds, (productId, product) -> {
            Bundle args = new Bundle();
            args.putString("productId", productId);
            ((CashierActivity) requireActivity())
                    .navigateTo(CashierActivity.SCREEN_CUSTOMIZE, args, true);
        });
        rvProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvProducts.setAdapter(productAdapter);

        // Nút xem đơn → Payment
        view.findViewById(R.id.btn_view_order).setOnClickListener(v -> {
            if (CartManager.getInstance().getItemCount() > 0) {
                navigatedToPayment = true;   // ← đánh dấu trước khi rời
                ((CashierActivity) requireActivity())
                        .navigateTo(CashierActivity.SCREEN_PAYMENT, null, true);
            }
        });

        updateOrderBar();
        loadData();

        ((CashierActivity) requireActivity())
                .setToolbarTitle(CartManager.getInstance().getTableName());

        // ── Fix back: giải phóng bàn nếu back mà chưa thêm món ──────────────
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        CartManager cart = CartManager.getInstance();
                        if (!navigatedToPayment
                                && cart.getItems().isEmpty()
                                && cart.getTableId() != null) {
                            // Chưa thêm món → trả bàn về AVAILABLE
                            FirebaseHelper.getTablesRef()
                                    .child(cart.getTableId())
                                    .child("status").setValue("AVAILABLE");
                            cart.clear();
                        }
                        setEnabled(false);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reset flag khi quay lại từ Payment (user bấm back từ Payment)
        navigatedToPayment = false;
        updateOrderBar();
    }

    // ── onDetach đã được thay bằng OnBackPressedCallback ở trên ──────────────
    // KHÔNG dùng onDetach nữa vì nó fire cả khi navigate forward (sang Payment)
    // dẫn đến giải phóng bàn sai lúc.

    private void updateOrderBar() {
        int count = CartManager.getInstance().getItemCount();
        int total = CartManager.getInstance().getSubtotal();
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));

        tvOrderBadge.setText(String.valueOf(count));
        tvViewOrder.setText("Xem đơn (" + count + ")   " + fmt.format(total) + "đ");
        View btnView = requireView().findViewById(R.id.btn_view_order);
        btnView.setAlpha(count > 0 ? 1f : 0.5f);
    }

    private void loadData() {
        // Load categories
        FirebaseHelper.getCategoriesRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        categoriesMap.clear();
                        catIds.clear();
                        catList.clear();

                        catIds.add("all");
                        catList.add(makeCatAll());

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            CategoryModel c = ds.getValue(CategoryModel.class);
                            if (c != null) {
                                categoriesMap.put(ds.getKey(), c);
                                catIds.add(ds.getKey());
                                catList.add(c);
                            }
                        }
                        if (currentCatId == null) currentCatId = "all";
                        categoryAdapter.setSelectedId(currentCatId);
                        categoryAdapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });

        // Load products
        FirebaseHelper.getProductsRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        productsMap.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ProductModel p = ds.getValue(ProductModel.class);
                            if (p != null) productsMap.put(ds.getKey(), p);
                        }
                        filterProducts(currentCatId != null ? currentCatId : "all");
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void filterProducts(String catId) {
        prodIds.clear();
        prodList.clear();
        for (Map.Entry<String, ProductModel> e : productsMap.entrySet()) {
            if ("all".equals(catId) || catId.equals(e.getValue().getCategoryId())) {
                prodIds.add(e.getKey());
                prodList.add(e.getValue());
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private CategoryModel makeCatAll() {
        CategoryModel c = new CategoryModel();
        c.setName("Tất cả");
        return c;
    }
}
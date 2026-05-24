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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.CartManager;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.view.cashier.adapter.CategoryTabAdapter;
import com.example.cafemanagement.view.cashier.adapter.ProductGridAdapter;
import com.example.cafemanagement.viewmodel.MenuViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MenuFragment extends Fragment {

    private MenuViewModel viewModel;

    private TextView           tvViewOrder, tvOrderBadge;
    private CategoryTabAdapter categoryAdapter;
    private ProductGridAdapter productAdapter;

    // UI state — không cần ViewModel vì chỉ dùng trong Fragment lifecycle
    private boolean navigatedToPayment = false;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MenuViewModel.class);

        // Bind views
        RecyclerView rvCategories = view.findViewById(R.id.rv_categories);
        RecyclerView rvProducts   = view.findViewById(R.id.rv_products);
        tvViewOrder               = view.findViewById(R.id.tv_view_order_label);
        tvOrderBadge              = view.findViewById(R.id.tv_order_badge);

        // Setup adapters với list rỗng — data đến qua observe
        categoryAdapter = new CategoryTabAdapter(new ArrayList<>(), new ArrayList<>(), catId -> {
            viewModel.selectCategory(catId);
            categoryAdapter.setSelectedId(catId);
        });
        rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        productAdapter = new ProductGridAdapter(new ArrayList<>(), new ArrayList<>(),
                (productId, product) -> {
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
                navigatedToPayment = true;
                ((CashierActivity) requireActivity())
                        .navigateTo(CashierActivity.SCREEN_PAYMENT, null, true);
            }
        });

        // Observe
        setupObservers();

        // Load data chỉ lần đầu
        if (savedInstanceState == null) viewModel.loadData();

        ((CashierActivity) requireActivity())
                .setToolbarTitle(CartManager.getInstance().getTableName());

        // Back pressed: giải phóng bàn nếu chưa thêm món
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        CartManager cart = CartManager.getInstance();
                        if (!navigatedToPayment
                                && cart.getItems().isEmpty()
                                && cart.getTableId() != null) {
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
        navigatedToPayment = false;
        updateOrderBar();
    }

    private void setupObservers() {
        // Categories
        viewModel.getCatList().observe(getViewLifecycleOwner(), list ->
                categoryAdapter.updateData(list,
                        viewModel.getCatIds().getValue() != null
                                ? viewModel.getCatIds().getValue() : new ArrayList<>()));

        viewModel.getCatIds().observe(getViewLifecycleOwner(), ids ->
                categoryAdapter.updateData(
                        viewModel.getCatList().getValue() != null
                                ? viewModel.getCatList().getValue() : new ArrayList<>(),
                        ids));

        // Sync selected tab
        viewModel.getSelectedCatId().observe(getViewLifecycleOwner(),
                catId -> categoryAdapter.setSelectedId(catId));

        // Products
        viewModel.getProdList().observe(getViewLifecycleOwner(), list ->
                productAdapter.updateData(list,
                        viewModel.getProdIds().getValue() != null
                                ? viewModel.getProdIds().getValue() : new ArrayList<>()));

        viewModel.getProdIds().observe(getViewLifecycleOwner(), ids ->
                productAdapter.updateData(
                        viewModel.getProdList().getValue() != null
                                ? viewModel.getProdList().getValue() : new ArrayList<>(),
                        ids));
    }

    private void updateOrderBar() {
        int count = CartManager.getInstance().getItemCount();
        int total = CartManager.getInstance().getSubtotal();
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvOrderBadge.setText(String.valueOf(count));
        tvViewOrder.setText("Xem đơn (" + count + ")   " + fmt.format(total) + "đ");
        View btnView = requireView().findViewById(R.id.btn_view_order);
        btnView.setAlpha(count > 0 ? 1f : 0.5f);
    }
}
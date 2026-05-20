package com.example.cafemanagement.view.cashier;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.cafemanagement.R;
import com.example.cafemanagement.model.ProductModel;
import com.example.cafemanagement.viewmodel.CustomizeViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomizeFragment extends Fragment {

    private CustomizeViewModel viewModel;

    private ImageView ivProduct;
    private TextView  tvName, tvDescription, tvPrice;
    private ChipGroup cgSizes, cgSugar, cgIce, cgToppings;
    private TextView  tvQtyCount;
    private Button    btnAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customize, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CustomizeViewModel.class);

        // Bind views
        ivProduct     = view.findViewById(R.id.iv_product_image);
        tvName        = view.findViewById(R.id.tv_product_name);
        tvDescription = view.findViewById(R.id.tv_product_description);
        tvPrice       = view.findViewById(R.id.tv_product_price);
        cgSizes       = view.findViewById(R.id.cg_sizes);
        cgSugar       = view.findViewById(R.id.cg_sugar);
        cgIce         = view.findViewById(R.id.cg_ice);
        cgToppings    = view.findViewById(R.id.cg_toppings);
        tvQtyCount    = view.findViewById(R.id.tv_qty_count);
        btnAdd        = view.findViewById(R.id.btn_add_to_cart);

        // Observe
        setupObservers();

        // Events
        view.findViewById(R.id.btn_qty_minus).setOnClickListener(v -> viewModel.decreaseQty());
        view.findViewById(R.id.btn_qty_plus).setOnClickListener(v  -> viewModel.increaseQty());
        btnAdd.setOnClickListener(v -> viewModel.addToCart());

        // Load data (chỉ load lần đầu)
        if (savedInstanceState == null) {
            String productId = getArguments() != null
                    ? getArguments().getString("productId") : null;
            if (productId != null) viewModel.loadProduct(productId);
        }
    }

    private void setupObservers() {
        // Khi product load xong → render UI
        viewModel.getProduct().observe(getViewLifecycleOwner(), this::renderProduct);

        // Khi giá hoặc qty thay đổi → cập nhật TextView + Button
        viewModel.getFinalPrice().observe(getViewLifecycleOwner(), price -> {
            int qty = viewModel.getQuantity().getValue() != null
                    ? viewModel.getQuantity().getValue() : 1;
            tvPrice.setText(formatMoney(price) + "đ");
            btnAdd.setText("Thêm vào giỏ  " + formatMoney(price * qty) + "đ");
        });

        viewModel.getQuantity().observe(getViewLifecycleOwner(), qty -> {
            tvQtyCount.setText(String.valueOf(qty));
            int price = viewModel.getFinalPrice().getValue() != null
                    ? viewModel.getFinalPrice().getValue() : 0;
            btnAdd.setText("Thêm vào giỏ  " + formatMoney(price * qty) + "đ");
        });

        // Khi thêm giỏ thành công → back
        viewModel.getAddedToCart().observe(getViewLifecycleOwner(), added -> {
            if (Boolean.TRUE.equals(added)) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void renderProduct(ProductModel product) {
        tvName.setText(product.getName());
        tvDescription.setText(product.getDescription());

        Glide.with(this).load(product.getImageUrl())
                .placeholder(R.drawable.ic_coffee_placeholder)
                .into(ivProduct);

        ProductModel.Options opts = product.getOptions();
        if (opts == null) return;

        // Sizes
        if (opts.getSizes() != null && !opts.getSizes().isEmpty()) {
            cgSizes.removeAllViews();
            requireView().findViewById(R.id.label_size).setVisibility(View.VISIBLE);
            boolean first = true;
            for (Map.Entry<String, Integer> e : opts.getSizes().entrySet()) {
                Chip chip = makeChip(e.getKey()
                        + (e.getValue() > 0 ? " +" + formatMoney(e.getValue()) : ""));
                chip.setOnCheckedChangeListener((c, checked) -> {
                    if (checked) viewModel.setSelectedSize(e.getKey());
                });
                cgSizes.addView(chip);
                if (first) { chip.setChecked(true); first = false; }
            }
        } else {
            requireView().findViewById(R.id.label_size).setVisibility(View.GONE);
        }

        // Sugar
        buildIntChipGroup(cgSugar, opts.getSugar(),
                val -> viewModel.setSelectedSugar(val), "%");
        requireView().findViewById(R.id.label_sugar)
                .setVisibility(opts.getSugar() != null && !opts.getSugar().isEmpty()
                        ? View.VISIBLE : View.GONE);

        // Ice
        buildIntChipGroup(cgIce, opts.getIce(),
                val -> viewModel.setSelectedIce(val), "%");
        requireView().findViewById(R.id.label_ice)
                .setVisibility(opts.getIce() != null && !opts.getIce().isEmpty()
                        ? View.VISIBLE : View.GONE);

        // Toppings
        if (opts.getToppings() != null && !opts.getToppings().isEmpty()) {
            cgToppings.removeAllViews();
            requireView().findViewById(R.id.label_topping).setVisibility(View.VISIBLE);
            for (Map.Entry<String, Integer> e : opts.getToppings().entrySet()) {
                Chip chip = makeChip(e.getKey() + " +" + formatMoney(e.getValue()));
                chip.setCheckable(true);
                chip.setOnCheckedChangeListener((c, checked) -> {
                    if (checked) viewModel.addTopping(e.getKey(), e.getValue());
                    else         viewModel.removeTopping(e.getKey());
                });
                cgToppings.addView(chip);
            }
        } else {
            requireView().findViewById(R.id.label_topping).setVisibility(View.GONE);
        }
    }

    private void buildIntChipGroup(ChipGroup group, List<Integer> values,
                                   java.util.function.Consumer<Integer> onSelect,
                                   String suffix) {
        group.removeAllViews();
        if (values == null || values.isEmpty()) return;
        boolean first = true;
        for (int val : values) {
            Chip chip = makeChip(val + suffix);
            chip.setOnCheckedChangeListener((c, checked) -> {
                if (checked) onSelect.accept(val);
            });
            group.addView(chip);
            if (first) { chip.setChecked(true); first = false; }
        }
    }

    private Chip makeChip(String label) {
        Chip chip = new Chip(requireContext());
        chip.setText(label);
        chip.setCheckable(true);
        chip.setChipBackgroundColorResource(R.color.chip_state_color);
        chip.setTextColor(getResources().getColorStateList(R.color.chip_text_state_color));
        return chip;
    }

    private String formatMoney(int amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount);
    }
}
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

import com.bumptech.glide.Glide;
import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.CartManager;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.OrderItemModel;
import com.example.cafemanagement.model.ProductModel;
import com.example.cafemanagement.view.CashierActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomizeFragment extends Fragment {

    private String productId;
    private ProductModel product;

    private ImageView ivProduct;
    private TextView  tvName, tvDescription, tvPrice;
    private ChipGroup cgSizes, cgSugar, cgIce, cgToppings;
    private TextView  tvQtyCount, tvAddBtn;
    private Button    btnAdd;

    private String selectedSize    = null;
    private int    selectedSugVal  = -1;
    private int    selectedIceVal  = -1;
    private final Map<String, Integer> selectedToppings = new HashMap<>();
    private int    quantity        = 1;
    private int    basePrice       = 0;

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

        productId = getArguments() != null ? getArguments().getString("productId") : null;

        ivProduct    = view.findViewById(R.id.iv_product_image);
        tvName       = view.findViewById(R.id.tv_product_name);
        tvDescription= view.findViewById(R.id.tv_product_description);
        tvPrice      = view.findViewById(R.id.tv_product_price);
        cgSizes      = view.findViewById(R.id.cg_sizes);
        cgSugar      = view.findViewById(R.id.cg_sugar);
        cgIce        = view.findViewById(R.id.cg_ice);
        cgToppings   = view.findViewById(R.id.cg_toppings);
        tvQtyCount   = view.findViewById(R.id.tv_qty_count);
        btnAdd       = view.findViewById(R.id.btn_add_to_cart);

        view.findViewById(R.id.btn_qty_minus).setOnClickListener(v -> {
            if (quantity > 1) { quantity--; updateQtyUi(); }
        });
        view.findViewById(R.id.btn_qty_plus).setOnClickListener(v -> {
            quantity++; updateQtyUi();
        });

        btnAdd.setOnClickListener(v -> addToCart());

        if (productId != null) loadProduct();
    }

    private void loadProduct() {
        FirebaseHelper.getProductsRef().child(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        product = snapshot.getValue(ProductModel.class);
                        if (product != null) renderProduct();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void renderProduct() {
        basePrice = product.getPrice();
        tvName.setText(product.getName());
        tvDescription.setText(product.getDescription());

        Glide.with(this).load(product.getImageUrl())
                .placeholder(R.drawable.ic_coffee_placeholder)
                .into(ivProduct);

        ProductModel.Options opts = product.getOptions();
        if (opts == null) { updatePriceUi(); return; }

        // Sizes
        if (opts.getSizes() != null && !opts.getSizes().isEmpty()) {
            cgSizes.removeAllViews();
            requireView().findViewById(R.id.label_size).setVisibility(View.VISIBLE);
            boolean first = true;
            for (Map.Entry<String, Integer> e : opts.getSizes().entrySet()) {
                Chip chip = makeChip(e.getKey()
                        + (e.getValue() > 0 ? " +" + formatMoney(e.getValue()) : ""));
                chip.setTag(e);
                chip.setOnCheckedChangeListener((c, checked) -> {
                    if (checked) {
                        selectedSize = e.getKey();
                        updatePriceUi();
                    }
                });
                cgSizes.addView(chip);
                if (first) { chip.setChecked(true); first = false; }
            }
        } else {
            requireView().findViewById(R.id.label_size).setVisibility(View.GONE);
        }

        // Sugar
        buildIntChipGroup(cgSugar, opts.getSugar(), val -> {
            selectedSugVal = val;
        }, "%");
        requireView().findViewById(R.id.label_sugar)
                .setVisibility(opts.getSugar() != null && !opts.getSugar().isEmpty()
                        ? View.VISIBLE : View.GONE);

        // Ice
        buildIntChipGroup(cgIce, opts.getIce(), val -> {
            selectedIceVal = val;
        }, "%");
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
                    if (checked) selectedToppings.put(e.getKey(), e.getValue());
                    else         selectedToppings.remove(e.getKey());
                    updatePriceUi();
                });
                cgToppings.addView(chip);
            }
        } else {
            requireView().findViewById(R.id.label_topping).setVisibility(View.GONE);
        }

        updatePriceUi();
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

    private int calcFinalPrice() {
        int price = basePrice;
        // Size delta
        if (selectedSize != null && product.getOptions() != null
                && product.getOptions().getSizes() != null) {
            Integer delta = product.getOptions().getSizes().get(selectedSize);
            if (delta != null) price += delta;
        }
        // Toppings
        for (int v : selectedToppings.values()) price += v;
        return price;
    }

    private void updatePriceUi() {
        int finalPrice = calcFinalPrice();
        tvPrice.setText(formatMoney(finalPrice) + "đ");
        btnAdd.setText("Thêm vào giỏ  " + formatMoney(finalPrice * quantity) + "đ");
    }

    private void updateQtyUi() {
        tvQtyCount.setText(String.valueOf(quantity));
        updatePriceUi();
    }

    private void addToCart() {
        if (product == null) return;
        OrderItemModel item = new OrderItemModel(
                productId,
                product.getName(),
                basePrice,
                calcFinalPrice(),
                quantity,
                selectedSize,
                selectedSugVal,
                selectedIceVal,
                new HashMap<>(selectedToppings),
                null
        );
        CartManager.getInstance().addItem(item);
        // Back to menu
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private String formatMoney(int amount) {
        return NumberFormat.getInstance(new Locale("vi","VN")).format(amount);
    }
}
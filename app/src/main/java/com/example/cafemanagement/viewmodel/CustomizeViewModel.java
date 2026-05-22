package com.example.cafemanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.helper.CartManager;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.OrderItemModel;
import com.example.cafemanagement.model.ProductModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class CustomizeViewModel extends ViewModel {

    // --- LiveData để Fragment observe ---
    private final MutableLiveData<ProductModel> product = new MutableLiveData<>();
    private final MutableLiveData<Integer> finalPrice   = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> quantity     = new MutableLiveData<>(1);
    private final MutableLiveData<Boolean> addedToCart  = new MutableLiveData<>();

    // --- State nội bộ ---
    private String productId;
    private int basePrice = 0;
    private String selectedSize   = null;
    private int selectedSugVal    = -1;
    private int selectedIceVal    = -1;
    private final Map<String, Integer> selectedToppings = new HashMap<>();

    // --- Getters LiveData ---
    public LiveData<ProductModel> getProduct()    { return product; }
    public LiveData<Integer> getFinalPrice()      { return finalPrice; }
    public LiveData<Integer> getQuantity()        { return quantity; }
    public LiveData<Boolean> getAddedToCart()     { return addedToCart; }

    // --- Load product từ Firebase ---
    public void loadProduct(String productId) {
        this.productId = productId;
        FirebaseHelper.getProductsRef().child(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ProductModel p = snapshot.getValue(ProductModel.class);
                        if (p != null) {
                            basePrice = p.getPrice();
                            product.setValue(p);
                            recalcPrice();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    // --- Các hàm cập nhật lựa chọn từ Fragment ---
    public void setSelectedSize(String size) {
        selectedSize = size;
        recalcPrice();
    }

    public void setSelectedSugar(int val) {
        selectedSugVal = val;
    }

    public void setSelectedIce(int val) {
        selectedIceVal = val;
    }

    public void addTopping(String name, int price) {
        selectedToppings.put(name, price);
        recalcPrice();
    }

    public void removeTopping(String name) {
        selectedToppings.remove(name);
        recalcPrice();
    }

    public void increaseQty() {
        int current = quantity.getValue() != null ? quantity.getValue() : 1;
        quantity.setValue(current + 1);
        recalcPrice();
    }

    public void decreaseQty() {
        int current = quantity.getValue() != null ? quantity.getValue() : 1;
        if (current > 1) {
            quantity.setValue(current - 1);
            recalcPrice();
        }
    }

    // --- Tính giá ---
    private void recalcPrice() {
        ProductModel p = product.getValue();
        int price = basePrice;

        if (selectedSize != null && p != null
                && p.getOptions() != null
                && p.getOptions().getSizes() != null) {
            Integer delta = p.getOptions().getSizes().get(selectedSize);
            if (delta != null) price += delta;
        }

        for (int v : selectedToppings.values()) price += v;

        finalPrice.setValue(price);
    }

    // Getter thuần để Fragment dùng khi cần tính total hiển thị nút
    public int getCurrentUnitPrice() {
        return finalPrice.getValue() != null ? finalPrice.getValue() : 0;
    }

    // --- Thêm vào giỏ ---
    public void addToCart() {
        ProductModel p = product.getValue();
        if (p == null || productId == null) return;

        int qty = quantity.getValue() != null ? quantity.getValue() : 1;

        OrderItemModel item = new OrderItemModel(
                productId,
                p.getName(),
                basePrice,
                getCurrentUnitPrice(),
                qty,
                selectedSize,
                selectedSugVal,
                selectedIceVal,
                new HashMap<>(selectedToppings),
                null
        );
        CartManager.getInstance().addItem(item);
        addedToCart.setValue(true);
    }
}
package com.example.cafemanagement;

import com.example.cafemanagement.model.BasketItemModel;
import com.example.cafemanagement.model.ProductModel;

import java.util.ArrayList;
import java.util.List;

public class BasketManager {
    // Thể hiện duy nhất (Singleton Pattern)
    private static BasketManager instance;

    // Danh sách các món ăn trong giỏ
    private List<BasketItemModel> basketItems;

    private BasketManager() {
        basketItems = new ArrayList<>();
    }

    public static synchronized BasketManager getInstance() {
        if (instance == null) {
            instance = new BasketManager();
        }
        return instance;
    }

    /**
     * Thêm món ăn NHANH từ màn hình Menu (Chỉ có tên, không có tùy chọn)
     */
    public void addProduct(ProductModel product) {
        for (BasketItemModel item : basketItems) {
            if (item.getProductName().equals(product.getName()) && item.getOptionsDisplay().equals("Default")) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        basketItems.add(new BasketItemModel(
                product.getName(),
                product.getImageUrl(),
                "Default",
                product.getPrice(),
                1
        ));
    }

    /**
     * BỔ SUNG: Thêm món ăn TÙY CHỈNH từ màn hình DrinkCustomizeActivity
     */
    public void addCustomItem(BasketItemModel newItem) {
        for (BasketItemModel item : basketItems) {
            // So sánh TRÙNG TÊN và TRÙNG TÙY CHỌN (Size, Đường)
            if (item.getProductName().equals(newItem.getProductName()) &&
                    item.getOptionsDisplay().equals(newItem.getOptionsDisplay())) {

                // Nếu trùng hoàn toàn thì chỉ cộng dồn số lượng
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                return;
            }
        }

        // Nếu khác tùy chọn (VD: 1 ly ít đường, 1 ly nhiều đường), thêm thành 1 dòng mới
        basketItems.add(newItem);
    }

    public List<BasketItemModel> getBasketItems() {
        return basketItems;
    }

    public double getTotalPrice() {
        double total = 0.0;
        for (BasketItemModel item : basketItems) {
            total += (item.getPrice() * item.getQuantity());
        }
        return total;
    }
    /**
     * Hàm tính tổng số lượng ly/món thực tế đang có trong giỏ hàng
     */
    public int getTotalQuantity() {
        int totalQty = 0;
        for (BasketItemModel item : basketItems) {
            totalQty += item.getQuantity(); // Cộng dồn thuộc tính quantity của từng dòng
        }
        return totalQty;
    }
}
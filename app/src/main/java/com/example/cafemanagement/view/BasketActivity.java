package com.example.cafemanagement.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.adapter.BasketAdapter;
import com.example.cafemanagement.model.BasketItemModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class BasketActivity extends AppCompatActivity {

    // Thành phần giao diện
    private RecyclerView recyclerBasket;
    private TextView txtTotalPrice, btnClearAll;
    private MaterialButton btnToCheckout;

    // Thành phần dữ liệu
    private BasketAdapter basketAdapter;
    private List<BasketItemModel> basketList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        initViews();
        setupData();
        setupListeners();
    }

    /**
     * Ánh xạ các thành phần View từ tệp XML activity_basket
     */
    private void initViews() {
        recyclerBasket = findViewById(R.id.recyclerCart);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        btnClearAll = findViewById(R.id.btnClearAll);
        btnToCheckout = findViewById(R.id.btnCheckout);
    }

    /**
     * Khởi tạo danh sách và thiết lập RecyclerView
     */
    private void setupData() {
        // LẤY DỮ LIỆU THỰC TẾ: Trỏ trực tiếp vào danh sách đã lưu trong BasketManager
        basketList = com.example.cafemanagement.BasketManager.getInstance().getBasketItems();

        // Khởi tạo Adapter với Interface xử lý sự kiện
        basketAdapter = new BasketAdapter(basketList, new BasketAdapter.OnBasketActionListener() {
            @Override
            public void onIncrease(int position) {
                // Tăng số lượng món ăn
                int currentQty = basketList.get(position).getQuantity();
                basketList.get(position).setQuantity(currentQty + 1);

                basketAdapter.notifyItemChanged(position);
                updateTotal();
            }

            @Override
            public void onDecrease(int position) {
                int currentQty = basketList.get(position).getQuantity();
                if (currentQty > 1) {
                    // Giảm số lượng
                    basketList.get(position).setQuantity(currentQty - 1);
                    basketAdapter.notifyItemChanged(position);
                } else {
                    // Nếu số lượng là 1 mà bấm giảm -> Tự động xóa món khỏi giỏ
                    basketList.remove(position);
                    basketAdapter.notifyItemRemoved(position);
                    // Cập nhật lại dải index để tránh lỗi vị trí khi xóa
                    basketAdapter.notifyItemRangeChanged(position, basketList.size());
                }
                updateTotal();
            }
        });

        recyclerBasket.setLayoutManager(new LinearLayoutManager(this));
        recyclerBasket.setAdapter(basketAdapter);

        updateTotal();
    }

    /**
     * Thiết lập các sự kiện bấm nút trên màn hình
     */
    private void setupListeners() {
        // Nút quay lại
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Nút Xóa tất cả
        btnClearAll.setOnClickListener(v -> showClearAllDialog());

        // Nút Checkout
        btnToCheckout.setOnClickListener(v -> {
            if (basketList.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
            } else {
                // Chuyển sang màn hình Checkout
                Intent intent = new Intent(BasketActivity.this, CheckoutActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Hiển thị hộp thoại xác nhận trước khi xóa toàn bộ giỏ hàng
     */
    private void showClearAllDialog() {
        if (basketList.isEmpty()) return;

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ món ăn trong giỏ hàng không?")
                .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                    basketList.clear();
                    basketAdapter.notifyDataSetChanged();
                    updateTotal();
                    Toast.makeText(this, "Đã dọn sạch giỏ hàng", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Tính toán lại tổng tiền dựa trên danh sách hiện tại
     */
    private void updateTotal() {
        double total = 0;
        for (BasketItemModel item : basketList) {
            // Cộng dồn tổng tiền của từng món (Giá x Số lượng)
            total += (item.getPrice() * item.getQuantity());
        }

        // ĐÃ SỬA: Gọi lớp PriceFormatter dùng chung để định dạng biến 'total'
        txtTotalPrice.setText(com.example.cafemanagement.helper.PriceFormatter.formatPrice(total));

        // Cập nhật trạng thái nút thanh toán
        if (basketList.isEmpty()) {
            btnToCheckout.setEnabled(false);
            btnToCheckout.setAlpha(0.5f);
        } else {
            btnToCheckout.setEnabled(true);
            btnToCheckout.setAlpha(1.0f);
        }
    }
}
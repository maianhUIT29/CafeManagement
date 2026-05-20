package com.example.cafemanagement.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cafemanagement.BasketManager;
import com.example.cafemanagement.R;
import com.example.cafemanagement.model.BasketItemModel;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderStatusActivity extends AppCompatActivity {

    private TextView txtOrderId, txtOrderTime, txtConfirmTime;
    private LinearLayout layoutOrderItems;
    private MaterialButton btnBackToMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        initViews();
        setupData();
        setupListeners();
    }

    private void initViews() {
        txtOrderId = findViewById(R.id.txtOrderId);
        txtOrderTime = findViewById(R.id.txtOrderTime);
        txtConfirmTime = findViewById(R.id.txtConfirmTime);
        layoutOrderItems = findViewById(R.id.layoutOrderItems);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);
    }

    private void setupData() {
        // 1. Nhận mã đơn hàng từ Checkout truyền sang
        String orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            orderId = String.valueOf(System.currentTimeMillis()).substring(5); // Fallback ngẫu nhiên
        }
        txtOrderId.setText("#" + orderId);

        // 2. Lấy giờ hiện tại chuẩn định dạng
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        txtOrderTime.setText(currentTime);
        txtConfirmTime.setText(currentTime);

        // 3. Đổ danh sách món ăn từ Giỏ hàng hiện tại vào Giao diện
        List<BasketItemModel> currentCart = BasketManager.getInstance().getBasketItems();
        for (BasketItemModel item : currentCart) {
            addOrderItemToView(item.getQuantity(), item.getProductName());
        }

        // 4. QUAN TRỌNG: Đơn hàng đã đặt thành công, phải dọn sạch giỏ hàng.
        BasketManager.getInstance().getBasketItems().clear();
    }

    /**
     * Hàm sinh tự động các dòng món ăn bằng mã Java thay vì dùng Adapter phức tạp
     */
    private void addOrderItemToView(int quantity, String name) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);
        row.setGravity(Gravity.CENTER_VERTICAL);

        // Nền ô vuông hiển thị số lượng
        TextView txtQty = new TextView(this);
        txtQty.setText(quantity + "x");
        txtQty.setTextColor(android.graphics.Color.parseColor("#3E2723"));
        txtQty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        txtQty.setTypeface(null, android.graphics.Typeface.BOLD);
        txtQty.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
        txtQty.setPadding(16, 8, 16, 8);

        // Tên món ăn
        TextView txtName = new TextView(this);
        txtName.setText(name);
        txtName.setTextColor(android.graphics.Color.parseColor("#424242"));
        txtName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(24, 0, 0, 0);
        txtName.setLayoutParams(params);

        row.addView(txtQty);
        row.addView(txtName);

        layoutOrderItems.addView(row);
    }

    private void setupListeners() {
        // Xử lý sự kiện bấm nút UI trên màn hình
        findViewById(R.id.btnBack).setOnClickListener(v -> returnToMenu());
        btnBackToMenu.setOnClickListener(v -> returnToMenu());

        // CHUẨN KỸ THUẬT MỚI: Xử lý sự kiện bấm nút Back vật lý / Vuốt cạnh màn hình của thiết bị
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                returnToMenu();
            }
        });
    }

    private void returnToMenu() {
        Intent intent = new Intent(OrderStatusActivity.this, MenuActivity.class);
        // Lệnh này đảm bảo xóa mọi màn hình (Checkout, Basket) đang xếp chồng phía sau
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
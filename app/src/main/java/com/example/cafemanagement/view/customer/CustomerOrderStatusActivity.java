package com.example.cafemanagement.view.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.OrderItemModel;
import com.example.cafemanagement.model.OrderModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomerOrderStatusActivity extends AppCompatActivity {

    private TextView txtOrderId, txtOrderTime, txtConfirmTime;
    private LinearLayout layoutOrderItems;
    private MaterialButton btnBackToMenu;

    // Views cho timeline
    private TextView txtStep2Label, txtStep2Desc, txtStep3Label, txtStep3Desc;
    private ImageView imgStep2, imgStep3;
    private View lineStep2, lineStep3;

    private String orderId;
    private ValueEventListener orderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        orderId = getIntent().getStringExtra("ORDER_ID");

        initViews();
        setupListeners();
        listenOrderStatus();
    }

    private void initViews() {
        txtOrderId      = findViewById(R.id.txtOrderId);
        txtOrderTime    = findViewById(R.id.txtOrderTime);
        txtConfirmTime  = findViewById(R.id.txtConfirmTime);
        layoutOrderItems= findViewById(R.id.layoutOrderItems);
        btnBackToMenu   = findViewById(R.id.btnBackToMenu);

        // Thời gian hiện tại
        String now = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date());
        txtOrderTime.setText(now);
        txtConfirmTime.setText(now);

        if (orderId != null)
            txtOrderId.setText("#" + orderId.substring(
                    Math.max(0, orderId.length() - 8)));
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> goToMain());
        btnBackToMenu.setOnClickListener(v -> goToMain());
    }

    private void goToMain() {
        Intent intent = new Intent(this, CustomerMainActivity.class);
        intent.putExtra("NAVIGATE_TO", "ORDER_SETUP"); // báo hiệu reset về setup
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void listenOrderStatus() {
        if (orderId == null) return;

        orderListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    OrderModel order = child.getValue(OrderModel.class);
                    if (order == null) continue;

                    // Hiện items lần đầu
                    if (layoutOrderItems.getChildCount() == 0 && order.getItems() != null) {
                        for (com.example.cafemanagement.model.OrderItemModel item
                                : order.getItems().values()) {
                            addItemRow(item.getQuantity(), item.getProductName());
                        }
                    }

                    updateTimeline(order.getStatus());
                    return;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        FirebaseHelper.getOrdersRef()
                .orderByChild("orderId").equalTo(orderId)
                .addValueEventListener(orderListener);
    }

    private void updateTimeline(String status) {
        // Tìm LinearLayout chứa "Đang pha chế"
        // Layout hiện tại dùng static text → cần update text "Đã giao món"
        // Tìm TextView cuối trong layout
        LinearLayout contentLayout = findViewById(R.id.layoutOrderItems);
        // Đã xử lý qua text color trong XML — chỉ cần update màu step 3

        if (OrderModel.STATUS_DONE.equals(status)) {
            // Tìm và update TextView "Đã giao món" sang màu đậm
            updateDoneStep();
        }
    }

    private void updateDoneStep() {
        TextView txtStep3Label = findViewById(R.id.txtStep3Label);
        TextView txtStep3Desc  = findViewById(R.id.txtStep3Desc);

        if (txtStep3Label != null) {
            txtStep3Label.setTextColor(
                    android.graphics.Color.parseColor("#2E7D32"));
        }
        if (txtStep3Desc != null) {
            txtStep3Desc.setTextColor(
                    android.graphics.Color.parseColor("#2E7D32"));
        }

        btnBackToMenu.setText("✅ Đồ đã sẵn sàng! Quay về thực đơn");
        btnBackToMenu.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#2E7D32")));
        btnBackToMenu.setTextColor(android.graphics.Color.WHITE);
    }

    private void addItemRow(int quantity, String name) {
        if (layoutOrderItems == null) return;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView txtQty = new TextView(this);
        txtQty.setText(quantity + "x");
        txtQty.setTextColor(android.graphics.Color.parseColor("#3E2723"));
        txtQty.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12);
        txtQty.setTypeface(null, android.graphics.Typeface.BOLD);
        txtQty.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
        txtQty.setPadding(16, 8, 16, 8);

        TextView txtName = new TextView(this);
        txtName.setText(name);
        txtName.setTextColor(android.graphics.Color.parseColor("#424242"));
        txtName.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(24, 0, 0, 0);
        txtName.setLayoutParams(lp);

        row.addView(txtQty);
        row.addView(txtName);
        layoutOrderItems.addView(row);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null)
            FirebaseHelper.getOrdersRef().removeEventListener(orderListener);
    }
}
package com.example.cafemanagement.view.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cafemanagement.BasketManager;
import com.example.cafemanagement.R;
import com.example.cafemanagement.model.BasketItemModel;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerOrderStatusFragment extends Fragment {

    private TextView txtOrderId, txtOrderTime, txtConfirmTime;
    private LinearLayout layoutOrderItems;
    private MaterialButton btnBackToMenu;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_order_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupData();
        setupListeners(view);
    }

    private void initViews(View view) {
        txtOrderId = view.findViewById(R.id.txtOrderId);
        txtOrderTime = view.findViewById(R.id.txtOrderTime);
        txtConfirmTime = view.findViewById(R.id.txtConfirmTime);
        layoutOrderItems = view.findViewById(R.id.layoutOrderItems);
        btnBackToMenu = view.findViewById(R.id.btnBackToMenu);
    }

    private void setupData() {
        // 1. Nhận mã đơn hàng từ Checkout truyền sang (Fragment dùng getArguments)
        String orderId = null;
        if (getArguments() != null) {
            orderId = getArguments().getString("ORDER_ID");
        }
        
        // Fallback check if it was passed via Activity Intent (for backward compatibility if needed)
        if ((orderId == null || orderId.isEmpty()) && getActivity() != null && getActivity().getIntent() != null) {
            orderId = getActivity().getIntent().getStringExtra("ORDER_ID");
        }

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
        if (getContext() == null) return;
        
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);
        row.setGravity(Gravity.CENTER_VERTICAL);

        // Nền ô vuông hiển thị số lượng
        TextView txtQty = new TextView(getContext());
        txtQty.setText(quantity + "x");
        txtQty.setTextColor(android.graphics.Color.parseColor("#3E2723"));
        txtQty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        txtQty.setTypeface(null, android.graphics.Typeface.BOLD);
        txtQty.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
        txtQty.setPadding(16, 8, 16, 8);

        // Tên món ăn
        TextView txtName = new TextView(getContext());
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

    private void setupListeners(View view) {
        // Xử lý sự kiện bấm nút UI trên màn hình
        view.findViewById(R.id.btnBack).setOnClickListener(v -> returnToMenu());
        btnBackToMenu.setOnClickListener(v -> returnToMenu());

        // CHUẨN KỸ THUẬT MỚI: Xử lý sự kiện bấm nút Back vật lý / Vuốt cạnh màn hình của thiết bị
        if (getActivity() != null) {
            getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    returnToMenu();
                }
            });
        }
    }

    private void returnToMenu() {
        if (getActivity() instanceof CustomerMainActivity) {
            // Nếu đang ở trong MainActivity, đơn giản là chuyển tab
            ((CustomerMainActivity) getActivity()).findViewById(R.id.nav_menu).performClick();
        } else if (getActivity() != null) {
            // Nếu được mở từ Activity khác (như Checkout), quay về MainActivity
            Intent intent = new Intent(getActivity(), CustomerMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}

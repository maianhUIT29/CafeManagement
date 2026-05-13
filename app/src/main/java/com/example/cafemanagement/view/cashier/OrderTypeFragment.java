package com.example.cafemanagement.view.cashier;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.CartManager;
import com.example.cafemanagement.model.OrderModel;
import com.example.cafemanagement.view.CashierActivity;
import com.google.android.material.button.MaterialButton;

public class OrderTypeFragment extends Fragment {

    private CardView       cardDineIn, cardTakeAway;
    private LinearLayout   panelTakeAwayPhone;
    private EditText       etPhone;
    private TextView       tvDineInCheck, tvTakeAwayCheck;
    private MaterialButton btnNext;

    private String selectedType = null; // chưa chọn

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardDineIn        = view.findViewById(R.id.card_dine_in);
        cardTakeAway      = view.findViewById(R.id.card_take_away);
        panelTakeAwayPhone= view.findViewById(R.id.panel_take_away_phone);
        etPhone           = view.findViewById(R.id.et_customer_phone);
        tvDineInCheck     = view.findViewById(R.id.tv_dine_in_check);
        tvTakeAwayCheck   = view.findViewById(R.id.tv_take_away_check);
        btnNext           = view.findViewById(R.id.btn_next);

        btnNext.setEnabled(false);

        cardDineIn.setOnClickListener(v   -> selectType(OrderModel.TYPE_DINE_IN));
        cardTakeAway.setOnClickListener(v -> selectType(OrderModel.TYPE_TAKE_AWAY));

        btnNext.setOnClickListener(v -> proceed());
    }

    private void selectType(String type) {
        selectedType = type;
        boolean isDineIn = OrderModel.TYPE_DINE_IN.equals(type);

        // Highlight card được chọn
        cardDineIn.setCardBackgroundColor(getResources().getColor(
                isDineIn ? R.color.accent_yellow : R.color.surface,
                requireActivity().getTheme()));
        cardTakeAway.setCardBackgroundColor(getResources().getColor(
                isDineIn ? R.color.surface : R.color.accent_yellow,
                requireActivity().getTheme()));

        // Check mark
        tvDineInCheck.setVisibility(isDineIn   ? View.VISIBLE : View.INVISIBLE);
        tvTakeAwayCheck.setVisibility(isDineIn ? View.INVISIBLE : View.VISIBLE);

        // Hiện ô nhập SĐT khi chọn Mang đi
        panelTakeAwayPhone.setVisibility(isDineIn ? View.GONE : View.VISIBLE);

        btnNext.setEnabled(true);
        btnNext.setText(isDineIn ? "Chọn bàn →" : "Tiếp tục →");
    }

    private void proceed() {
        if (selectedType == null) return;

        // Lưu orderType vào CartManager để PaymentFragment đọc
        CartManager cart = CartManager.getInstance();
        cart.setOrderType(selectedType);

        if (OrderModel.TYPE_TAKE_AWAY.equals(selectedType)) {
            // Lưu SĐT (optional — tính năng tích điểm sau)
            String phone = etPhone.getText() != null
                    ? etPhone.getText().toString().trim() : "";
            cart.setCustomerPhone(phone);

            // Mang đi → không cần chọn bàn → thẳng vào Menu
            cart.setTable(null, "Mang đi");
            ((CashierActivity) requireActivity())
                    .navigateTo(CashierActivity.SCREEN_MENU, null, true);
        } else {
            // Tại chỗ → chọn bàn
            ((CashierActivity) requireActivity())
                    .navigateTo(CashierActivity.SCREEN_SELECT_TABLE, null, true);
        }
    }
}
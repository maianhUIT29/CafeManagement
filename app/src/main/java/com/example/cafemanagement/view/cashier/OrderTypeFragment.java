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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.CartManager;
import com.example.cafemanagement.model.OrderModel;
import com.google.android.material.button.MaterialButton;

public class OrderTypeFragment extends Fragment {

    private CardView       cardDineIn, cardTakeAway;
    private LinearLayout   panelTakeAwayPhone;
    private EditText       etPhone;
    private TextView       tvDineInCheck, tvTakeAwayCheck;
    private MaterialButton btnNext;

    private String selectedType = null;

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

        // ĐÃ SỬA: Sử dụng ContextCompat.getColor để lấy màu an toàn, tránh crash
        int activeColor = ContextCompat.getColor(requireContext(), R.color.accent_yellow);
        int surfaceColor = ContextCompat.getColor(requireContext(), R.color.surface);

        cardDineIn.setCardBackgroundColor(isDineIn ? activeColor : surfaceColor);
        cardTakeAway.setCardBackgroundColor(isDineIn ? surfaceColor : activeColor);

        tvDineInCheck.setVisibility(isDineIn   ? View.VISIBLE : View.INVISIBLE);
        tvTakeAwayCheck.setVisibility(isDineIn ? View.INVISIBLE : View.VISIBLE);

        panelTakeAwayPhone.setVisibility(isDineIn ? View.GONE : View.VISIBLE);

        btnNext.setEnabled(true);
        btnNext.setText(isDineIn ? "Tiếp tục: Chọn bàn →" : "Tiếp tục: Chọn món →");
    }

    private void proceed() {
        if (selectedType == null || getActivity() == null) return;

        CartManager cart = CartManager.getInstance();
        cart.setOrderType(selectedType);

        if (OrderModel.TYPE_TAKE_AWAY.equals(selectedType)) {
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            cart.setCustomerPhone(phone);
            cart.setTable(null, "Mang đi");
            
            ((CashierActivity) getActivity()).navigateTo(CashierActivity.SCREEN_MENU, null, true);
        } else {
            // Tại chỗ → chuyển sang màn hình chọn bàn
            ((CashierActivity) getActivity()).navigateTo(CashierActivity.SCREEN_SELECT_TABLE, null, true);
        }
    }
}
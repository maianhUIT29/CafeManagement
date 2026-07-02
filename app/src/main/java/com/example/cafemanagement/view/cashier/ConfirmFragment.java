package com.example.cafemanagement.view.cashier;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cafemanagement.R;

import java.text.NumberFormat;
import java.util.Locale;

public class ConfirmFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args      = getArguments();
        String orderId   = args != null ? args.getString("orderId", "#---")   : "#---";
        String payMethod = args != null ? args.getString("paymentMethod", "") : "";
        int    total     = args != null ? args.getInt("total", 0)             : 0;

        TextView tvOrderId   = view.findViewById(R.id.tv_confirm_order_id);
        TextView tvPayMethod = view.findViewById(R.id.tv_confirm_pay_method);
        TextView tvTotal     = view.findViewById(R.id.tv_confirm_total);

        // Hiển thị ID rút gọn
        tvOrderId.setText("#" + orderId
                .substring(Math.max(0, orderId.length() - 6))
                .toUpperCase());

        tvPayMethod.setText(
                "CASH".equals(payMethod) || "Tiền mặt".equals(payMethod)
                        ? "Tiền mặt" : "Chuyển khoản");

        tvTotal.setText(
                NumberFormat.getInstance(new Locale("vi", "VN")).format(total) + "đ");

        // Nút quay về dashboard
        view.findViewById(R.id.btn_back_dashboard).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack(
                    null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            ((CashierActivity) requireActivity())
                    .navigateTo(CashierActivity.SCREEN_DASHBOARD, null, false);
        });

        // Nút tạo đơn mới
        view.findViewById(R.id.btn_new_order_again).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack(
                    null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            ((CashierActivity) requireActivity())
                    .navigateTo(CashierActivity.SCREEN_ORDER_TYPE, null, false);
        });
    }
}
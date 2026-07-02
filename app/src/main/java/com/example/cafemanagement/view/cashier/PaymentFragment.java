package com.example.cafemanagement.view.cashier;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.CartManager;
import com.example.cafemanagement.model.OrderModel;
import com.example.cafemanagement.view.cashier.adapter.CartItemAdapter;
import com.example.cafemanagement.viewmodel.PaymentViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentFragment extends Fragment {

    private PaymentViewModel viewModel;

    // --- Views ---
    private TextView          tvTableName, tvSubtotal, tvDiscount, tvTotal, tvVoucherMsg;
    private TextView          tvOrderTypeLabel;
    private RecyclerView      rvCartItems;
    private TextInputEditText etVoucher;
    private Button            btnApplyVoucher;
    private ImageButton       btnRemoveVoucher;
    private TextView          btnPayCash, btnPayTransfer;
    private MaterialButton    btnConfirm;

    private String selectedOrderType = OrderModel.TYPE_DINE_IN;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PaymentViewModel.class);

        // Bind views
        tvTableName      = view.findViewById(R.id.tv_table_name);
        tvSubtotal       = view.findViewById(R.id.tv_subtotal);
        tvDiscount       = view.findViewById(R.id.tv_discount);
        tvTotal          = view.findViewById(R.id.tv_total);
        tvVoucherMsg     = view.findViewById(R.id.tv_voucher_msg);
        tvOrderTypeLabel = view.findViewById(R.id.tv_order_type_label);
        rvCartItems      = view.findViewById(R.id.rv_cart_items);
        etVoucher        = view.findViewById(R.id.et_voucher);
        btnApplyVoucher  = view.findViewById(R.id.btn_apply_voucher);
        btnRemoveVoucher = view.findViewById(R.id.btn_remove_voucher);
        btnPayCash       = view.findViewById(R.id.btn_pay_cash);
        btnPayTransfer   = view.findViewById(R.id.btn_pay_transfer);
        btnConfirm       = view.findViewById(R.id.btn_confirm_payment);

        // Cart info
        CartManager cart = CartManager.getInstance();
        tvTableName.setText(cart.getTableName());
        selectedOrderType = cart.getOrderType();
        tvOrderTypeLabel.setText(
                OrderModel.TYPE_TAKE_AWAY.equals(selectedOrderType) ? "🥤 Mang đi" : "🪑 Tại chỗ");

        // RecyclerView
        rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCartItems.setAdapter(new CartItemAdapter(cart.getItems(), (index, delta) -> {
            int newQty = cart.getItems().get(index).getQuantity() + delta;
            viewModel.updateItemQuantity(index, newQty);
            rvCartItems.getAdapter().notifyDataSetChanged();
        }));

        // Observe
        setupObservers();

        // Events
        btnPayCash.setOnClickListener(v     -> viewModel.selectPayMethod(OrderModel.PAY_CASH));
        btnPayTransfer.setOnClickListener(v -> viewModel.selectPayMethod(OrderModel.PAY_TRANSFER));

        btnApplyVoucher.setOnClickListener(v -> {
            String code = etVoucher.getText() != null
                    ? etVoucher.getText().toString().trim() : "";
            viewModel.applyVoucher(code);
        });

        btnRemoveVoucher.setOnClickListener(v -> {
            viewModel.removeVoucher();
            etVoucher.setText("");
            etVoucher.setEnabled(true);
            btnApplyVoucher.setEnabled(true);
            btnRemoveVoucher.setVisibility(View.GONE);
        });

        btnConfirm.setOnClickListener(v -> viewModel.confirmPayment(selectedOrderType));

        // Init cart data
        if (savedInstanceState == null) viewModel.initCart();
    }

    private void setupObservers() {
        viewModel.getSubtotal().observe(getViewLifecycleOwner(),
                sub -> tvSubtotal.setText(formatMoney(sub)));

        viewModel.getDiscountAmount().observe(getViewLifecycleOwner(),
                disc -> tvDiscount.setText("- " + formatMoney(disc)));

        viewModel.getTotal().observe(getViewLifecycleOwner(),
                t -> tvTotal.setText(formatMoney(t)));

        viewModel.getPayMethod().observe(getViewLifecycleOwner(),
                this::updatePayMethodUi);

        viewModel.getVoucherResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) {
                tvVoucherMsg.setVisibility(View.GONE);
                return;
            }
            showVoucherMsg(result.message, result.success);
            if (result.success) {
                btnRemoveVoucher.setVisibility(View.VISIBLE);
                etVoucher.setEnabled(false);
                btnApplyVoucher.setEnabled(false);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            btnConfirm.setEnabled(!loading);
            btnConfirm.setText(loading ? "Đang xử lý..." : "Hoàn tất giao dịch");
        });

        viewModel.getOrderResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            if (!result.success) {
                Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(requireContext(),
                    "✅ Đã gửi đơn xuống Barista!", Toast.LENGTH_SHORT).show();

            Bundle args = new Bundle();
            args.putString("orderId",       result.orderId);
            args.putString("paymentMethod", result.paymentMethod);
            args.putInt("total",            result.total);
            ((CashierActivity) requireActivity())
                    .navigateTo(CashierActivity.SCREEN_CONFIRM, args, false, false);
        });
    }

    // --- UI helpers ---

    private void updatePayMethodUi(String method) {
        boolean isCash = OrderModel.PAY_CASH.equals(method);
        btnPayCash.setBackgroundResource(
                isCash ? R.drawable.bg_pay_selected : R.drawable.bg_pay_unselected);
        btnPayTransfer.setBackgroundResource(
                isCash ? R.drawable.bg_pay_unselected : R.drawable.bg_pay_selected);
        btnPayCash.setTextColor(    isCash ? 0xFFFFFFFF : 0xFF888888);
        btnPayTransfer.setTextColor(isCash ? 0xFF888888 : 0xFFFFFFFF);
    }

    private void showVoucherMsg(String msg, boolean success) {
        tvVoucherMsg.setVisibility(View.VISIBLE);
        tvVoucherMsg.setText(msg);
        tvVoucherMsg.setTextColor(success ? 0xFF2D9B5A : 0xFFE53935);
    }

    private String formatMoney(int amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + "đ";
    }
}
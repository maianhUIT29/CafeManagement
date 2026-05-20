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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.CartManager;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.helper.ShiftManager;
import com.example.cafemanagement.model.OrderModel;
import com.example.cafemanagement.view.CashierActivity;
import com.example.cafemanagement.view.cashier.adapter.CartItemAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class PaymentFragment extends Fragment {

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView          tvTableName, tvSubtotal, tvDiscount, tvTotal, tvVoucherMsg;
    private TextView          tvOrderTypeLabel;// ← hiển thị hình thức đã chọn
    private RecyclerView      rvCartItems;
    private TextInputEditText etVoucher;
    private Button            btnApplyVoucher;
    private ImageButton       btnRemoveVoucher;
    private TextView btnPayCash, btnPayTransfer;
    private MaterialButton    btnConfirm;

    // ── State ─────────────────────────────────────────────────────────────────
    private String selectedPayMethod = OrderModel.PAY_CASH;
    private String selectedOrderType = OrderModel.TYPE_DINE_IN;
    private int    discountAmount    = 0;
    private String appliedVoucher    = null;
    private int    subtotal          = 0;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        btnPayCash     = view.findViewById(R.id.btn_pay_cash);
        btnPayTransfer = view.findViewById(R.id.btn_pay_transfer);
        btnConfirm       = view.findViewById(R.id.btn_confirm_payment);

        // Table name
        CartManager cart = CartManager.getInstance();
        tvTableName.setText(cart.getTableName());

        // Hình thức gọi — đọc từ CartManager, đã chọn từ bước trước
        selectedOrderType = cart.getOrderType();
        tvOrderTypeLabel.setText(
                OrderModel.TYPE_TAKE_AWAY.equals(selectedOrderType) ? "🥤 Mang đi" : "🪑 Tại chỗ"
        );

        // Cart items
        rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCartItems.setAdapter(new CartItemAdapter(cart.getItems(), (index, delta) -> {
            int newQty = cart.getItems().get(index).getQuantity() + delta;
            cart.updateQuantity(index, newQty);
            subtotal = 0;
            for (var item : cart.getItems()) subtotal += item.getSubtotal();
            refreshSummary();
            rvCartItems.getAdapter().notifyDataSetChanged();
        }));

        // Tính subtotal từ cart
        subtotal = 0;
        for (var item : cart.getItems()) subtotal += item.getSubtotal();
        refreshSummary();

        // ── Phương thức thanh toán ────────────────────────────────────────────
        btnPayCash.setOnClickListener(v -> selectPayMethod(OrderModel.PAY_CASH));
        btnPayTransfer.setOnClickListener(v -> selectPayMethod(OrderModel.PAY_TRANSFER));
        selectPayMethod(OrderModel.PAY_CASH);

        // ── Voucher ───────────────────────────────────────────────────────────
        btnApplyVoucher.setOnClickListener(v -> applyVoucher());
        btnRemoveVoucher.setOnClickListener(v -> removeVoucher());

        // ── Xác nhận ─────────────────────────────────────────────────────────
        btnConfirm.setOnClickListener(v -> confirmPayment());
    }

    // ── Phương thức thanh toán ────────────────────────────────────────────────

    private void selectPayMethod(String method) {
        selectedPayMethod = method;
        boolean isCash = OrderModel.PAY_CASH.equals(method);

        btnPayCash.setBackgroundResource(
                isCash ? R.drawable.bg_pay_selected : R.drawable.bg_pay_unselected);
        btnPayTransfer.setBackgroundResource(
                isCash ? R.drawable.bg_pay_unselected : R.drawable.bg_pay_selected);

        btnPayCash.setTextColor(isCash ? 0xFFFFFFFF : 0xFF888888);
        btnPayTransfer.setTextColor(isCash ? 0xFF888888 : 0xFFFFFFFF);
    }

    // ── Voucher ───────────────────────────────────────────────────────────────

    private void applyVoucher() {
        String code = etVoucher.getText() != null
                ? etVoucher.getText().toString().trim().toUpperCase()
                : "";
        if (code.isEmpty()) {
            showVoucherMsg("Nhập mã voucher trước", false);
            return;
        }

        FirebaseHelper.getVouchersRef().child(code)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        if (!snapshot.exists()) {
                            showVoucherMsg("Mã không hợp lệ ✗", false);
                            return;
                        }
                        Long discount = snapshot.child("discountAmount").getValue(Long.class);
                        if (discount == null) discount = 0L;

                        appliedVoucher = code;
                        discountAmount = discount.intValue();
                        showVoucherMsg("Áp dụng thành công! Giảm " + formatMoney(discountAmount), true);
                        btnRemoveVoucher.setVisibility(View.VISIBLE);
                        etVoucher.setEnabled(false);
                        btnApplyVoucher.setEnabled(false);
                        refreshSummary();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {
                        showVoucherMsg("Lỗi kết nối", false);
                    }
                });
    }

    private void removeVoucher() {
        appliedVoucher = null;
        discountAmount = 0;
        etVoucher.setText("");
        etVoucher.setEnabled(true);
        btnApplyVoucher.setEnabled(true);
        btnRemoveVoucher.setVisibility(View.GONE);
        tvVoucherMsg.setVisibility(View.GONE);
        refreshSummary();
    }

    private void showVoucherMsg(String msg, boolean success) {
        tvVoucherMsg.setVisibility(View.VISIBLE);
        tvVoucherMsg.setText(msg);
        tvVoucherMsg.setTextColor(success ? 0xFF2D9B5A : 0xFFE53935);
    }

    private void refreshSummary() {
        int total = Math.max(0, subtotal - discountAmount);
        tvSubtotal.setText(formatMoney(subtotal));
        tvDiscount.setText("- " + formatMoney(discountAmount));
        tvTotal.setText(formatMoney(total));
    }

    // ── Xác nhận thanh toán ───────────────────────────────────────────────────

    private void confirmPayment() {
        CartManager cart = CartManager.getInstance();
        if (cart.getItems().isEmpty()) {
            Toast.makeText(requireContext(), "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Đang xử lý...");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        int finalSubtotal = subtotal;
        int finalDiscount = discountAmount;
        int finalTotal    = Math.max(0, finalSubtotal - finalDiscount);

        OrderModel order = new OrderModel();
        order.setTableId(cart.getTableId());
        order.setTableName(cart.getTableName());
        order.setOrderType(selectedOrderType);
        order.setStatus(OrderModel.STATUS_PAID);
        order.setPaymentMethod(selectedPayMethod);
        order.setSubtotal(finalSubtotal);
        order.setDiscountAmount(finalDiscount);
        order.setVoucherCode(appliedVoucher);
        order.setTotal(finalTotal);
        order.setCreatedAt(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());

        if (user != null) {
            order.setCashierId(user.getUid());
            order.setCashierName(user.getDisplayName() != null ? user.getDisplayName() : "");
        }
        if (ShiftManager.getInstance().getCurrentShiftId() != null) {
            order.setShiftId(ShiftManager.getInstance().getCurrentShiftId());
        }

        Map<String, com.example.cafemanagement.model.OrderItemModel> itemMap = new java.util.LinkedHashMap<>();
        for (int i = 0; i < cart.getItems().size(); i++) {
            itemMap.put("item_" + i, cart.getItems().get(i));
        }
        order.setItems(itemMap);

        String orderKey = FirebaseHelper.getOrdersRef().push().getKey();
        if (orderKey == null) {
            resetConfirmButton();
            return;
        }

        FirebaseHelper.getOrdersRef().child(orderKey).setValue(order)
                .addOnSuccessListener(unused -> {
                    if (!isAdded()) return;

                    String tableId = cart.getTableId();
                    if (tableId != null && OrderModel.TYPE_DINE_IN.equals(selectedOrderType)) {
                        FirebaseHelper.getTablesRef().child(tableId)
                                .child("currentOrderId").setValue(orderKey);
                    }

                    cart.clear();
                    Toast.makeText(requireContext(),
                            "✅ Thanh toán thành công!", Toast.LENGTH_SHORT).show();

                    ((CashierActivity) requireActivity())
                            .navigateTo(CashierActivity.SCREEN_DASHBOARD, null, false, false);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    resetConfirmButton();
                    Toast.makeText(requireContext(),
                            "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resetConfirmButton() {
        btnConfirm.setEnabled(true);
        btnConfirm.setText("Hoàn tất giao dịch");
    }

    private String formatMoney(int amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + "đ";
    }
}
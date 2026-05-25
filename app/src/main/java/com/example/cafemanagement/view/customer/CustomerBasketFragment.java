package com.example.cafemanagement.view.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.adapter.BasketAdapter;
import com.example.cafemanagement.model.BasketItemModel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CustomerBasketFragment extends Fragment {

    // Thành phần giao diện
    private RecyclerView recyclerBasket;
    private TextView txtTotalPrice, btnClearAll;
    private MaterialButton btnToCheckout;

    // Thành phần dữ liệu
    private BasketAdapter basketAdapter;
    private List<BasketItemModel> basketList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_basket, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupData();
        setupListeners(view);
    }

    /**
     * Ánh xạ các thành phần View từ tệp XML activity_basket
     */
    private void initViews(View view) {
        recyclerBasket = view.findViewById(R.id.recyclerCart);
        txtTotalPrice = view.findViewById(R.id.txtTotalPrice);
        btnClearAll = view.findViewById(R.id.btnClearAll);
        btnToCheckout = view.findViewById(R.id.btnCheckout);
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

        recyclerBasket.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerBasket.setAdapter(basketAdapter);

        updateTotal();
    }

    /**
     * Thiết lập các sự kiện bấm nút trên màn hình
     */
    private void setupListeners(View view) {
        // Nút quay lại
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getParentFragment() != null) {
                getParentFragment().getChildFragmentManager().popBackStack();
            }
        });;

        // Nút Xóa tất cả
        btnClearAll.setOnClickListener(v -> showClearAllDialog());

        // Nút Checkout
        btnToCheckout.setOnClickListener(v -> {
            if (basketList.isEmpty()) {
                Toast.makeText(getContext(), "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getContext(), CustomerCheckoutActivity.class);
                if (getActivity() instanceof CustomerMainActivity) {
                    CustomerMainActivity main = (CustomerMainActivity) getActivity();
                    intent.putExtra("IS_DINE_IN", main.isDineIn());
                    intent.putExtra("TABLE_ID",   main.getTableId());
                    intent.putExtra("TABLE_NAME", main.getTableName());
                }
                startActivity(intent);
            }
        });;
    }

    /**
     * Hiển thị hộp thoại xác nhận trước khi xóa toàn bộ giỏ hàng
     */
    private void showClearAllDialog() {
        if (basketList.isEmpty()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ món ăn trong giỏ hàng không?")
                .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                    basketList.clear();
                    basketAdapter.notifyDataSetChanged();
                    updateTotal();
                    Toast.makeText(getContext(), "Đã dọn sạch giỏ hàng", Toast.LENGTH_SHORT).show();
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

package com.example.cafemanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cafemanagement.R;
import com.example.cafemanagement.model.BasketItemModel;

import java.util.List;

/**
 * Adapter quản lý danh sách món ăn trong giỏ hàng (Basket)
 * Sử dụng interface OnBasketActionListener để gửi sự kiện bấm nút về Activity
 */
public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.BasketViewHolder> {

    private final List<BasketItemModel> basketList;
    private final OnBasketActionListener listener;

    // Interface để định nghĩa các hành động trên mỗi dòng món ăn
    public interface OnBasketActionListener {
        void onIncrease(int position);
        void onDecrease(int position);
    }

    public BasketAdapter(List<BasketItemModel> basketList, OnBasketActionListener listener) {
        this.basketList = basketList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BasketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate tệp layout item_basket_product đã đặt tên lại để tránh trùng lặp
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_basket_product, parent, false);
        return new BasketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BasketViewHolder holder, int position) {
        BasketItemModel item = basketList.get(position);

        // 1. Đổ dữ liệu văn bản
        holder.txtName.setText(item.getProductName());
        holder.txtDetails.setText(item.getOptionsDisplay()); // Hiển thị Size, Độ ngọt, v.v.
        holder.txtQty.setText(String.valueOf(item.getQuantity()));

        // 2. Tính toán giá hiển thị (Giá đơn vị * Số lượng)
        double itemPrice = item.getPrice() * item.getQuantity();

        // ĐÃ CẬP NHẬT: Sử dụng lớp PriceFormatter dùng chung để hiển thị dấu chấm phần ngàn thống nhất
        holder.txtPrice.setText(com.example.cafemanagement.helper.PriceFormatter.formatPrice(itemPrice));

        // 3. Tải hình ảnh sản phẩm bằng Glide
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder_coffee) // Ảnh thay thế khi đang load
                .error(R.drawable.placeholder_coffee)      // Ảnh hiển thị nếu lỗi
                .into(holder.imgProduct);

        // 4. Thiết lập sự kiện nút Tăng (+)
        holder.btnPlus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIncrease(position);
            }
        });

        // 5. Thiết lập sự kiện nút Giảm (-)
        holder.btnMinus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecrease(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return basketList != null ? basketList.size() : 0;
    }

    /**
     * ViewHolder ánh xạ chính xác các ID từ tệp item_basket_product.xml
     */
    public static class BasketViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnPlus, btnMinus;
        TextView txtName, txtDetails, txtPrice, txtQty;

        public BasketViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgBasketItem);
            txtName = itemView.findViewById(R.id.txtBasketItemName);
            txtDetails = itemView.findViewById(R.id.txtBasketItemDetails);
            txtPrice = itemView.findViewById(R.id.txtBasketItemPrice);
            txtQty = itemView.findViewById(R.id.txtQtyBasket);
            btnPlus = itemView.findViewById(R.id.btnPlusBasket);
            btnMinus = itemView.findViewById(R.id.btnMinusBasket);
        }
    }
}
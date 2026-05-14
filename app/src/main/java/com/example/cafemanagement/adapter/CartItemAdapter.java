package com.example.cafemanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafemanagement.R;
import com.example.cafemanagement.model.CartItemModel;
import java.util.ArrayList;
import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartViewHolder> {

    private List<CartItemModel> cartList = new ArrayList<>();

    public void setCartList(List<CartItemModel> cartList) {
        this.cartList = cartList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItemModel item = cartList.get(position);

        // Hiển thị tên theo dạng: 1x Espresso
        holder.txtCartItemName.setText(item.getQuantity() + "x " + item.getProductName());

        // Hiển thị chi tiết: Size: Lớn | Đường: 30%
        String details = "Size: " + item.getSize();
        if (item.getSweetness() != null && !item.getSweetness().isEmpty()) {
            details += " | Đường: " + item.getSweetness();
        }
        holder.txtCartItemDetails.setText(details);

        // ĐÃ CẬP NHẬT: Sử dụng lớp PriceFormatter dùng chung để hiển thị dấu chấm phần ngàn
        double itemPrice = item.getBasePrice() * item.getQuantity();
        holder.txtCartItemPrice.setText(com.example.cafemanagement.helper.PriceFormatter.formatPrice(itemPrice));
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView txtCartItemName, txtCartItemDetails, txtCartItemPrice;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCartItemName = itemView.findViewById(R.id.txtCartItemName);
            txtCartItemDetails = itemView.findViewById(R.id.txtCartItemDetails);
            txtCartItemPrice = itemView.findViewById(R.id.txtCartItemPrice);
        }
    }
}
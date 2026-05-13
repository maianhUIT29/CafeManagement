// ─── CartItemAdapter.java ──────────────────────────────────────────────────
package com.example.cafemanagement.view.cashier.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.OrderItemModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.VH> {

    public interface OnQtyChange { void onChange(int index, int delta); }

    private final List<OrderItemModel> items;
    private final OnQtyChange          listener;

    public CartItemAdapter(List<OrderItemModel> items, OnQtyChange listener) {
        this.items    = items;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        OrderItemModel item = items.get(pos);
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));

        h.tvName.setText(item.getProductName());
        h.tvOptions.setText(buildOptionsText(item));
        h.tvQty.setText(String.valueOf(item.getQuantity()));
        h.tvSubtotal.setText(fmt.format(item.getSubtotal()) + "đ");

        h.btnMinus.setOnClickListener(v -> listener.onChange(h.getAdapterPosition(), -1));
        h.btnPlus.setOnClickListener(v  -> listener.onChange(h.getAdapterPosition(), +1));
    }

    private String buildOptionsText(OrderItemModel item) {
        StringBuilder sb = new StringBuilder();
        if (item.getSize() != null) sb.append("Size ").append(item.getSize());
        if (item.getSugar() >= 0)   sb.append(" · Đường ").append(item.getSugar()).append("%");
        if (item.getIce()   >= 0)   sb.append(" · Đá ").append(item.getIce()).append("%");
        if (item.getToppings() != null && !item.getToppings().isEmpty()) {
            sb.append(" · ").append(String.join(", ", item.getToppings().keySet()));
        }
        return sb.toString();
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView    tvName, tvOptions, tvQty, tvSubtotal;
        ImageButton btnMinus, btnPlus;
        VH(View v) {
            super(v);
            tvName     = v.findViewById(R.id.tv_item_name);
            tvOptions  = v.findViewById(R.id.tv_item_options);
            tvQty      = v.findViewById(R.id.tv_item_qty);
            tvSubtotal = v.findViewById(R.id.tv_item_subtotal);
            btnMinus   = v.findViewById(R.id.btn_qty_minus);
            btnPlus    = v.findViewById(R.id.btn_qty_plus);
        }
    }
}
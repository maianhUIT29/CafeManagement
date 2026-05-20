package com.example.cafemanagement.view.cashier.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cafemanagement.R;
import com.example.cafemanagement.model.ProductModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.VH> {

    public interface OnProductClick { void onClick(String productId, ProductModel product); }

    private final List<ProductModel> products;
    private final List<String>       ids;
    private final OnProductClick     listener;

    public ProductGridAdapter(List<ProductModel> products, List<String> ids,
                              OnProductClick listener) {
        this.products = products;
        this.ids      = ids;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ProductModel p  = products.get(pos);
        String       id = ids.get(pos);

        h.tvName.setText(p.getName());
        h.tvPrice.setText(NumberFormat.getInstance(new Locale("vi", "VN"))
                .format(p.getPrice()) + "đ");

        Glide.with(h.ivProduct.getContext())
                .load(p.getImageUrl())
                .placeholder(R.drawable.ic_coffee_placeholder)
                .centerCrop()
                .into(h.ivProduct);

        h.itemView.setOnClickListener(v -> listener.onClick(id, p));
    }

    @Override public int getItemCount() { return products.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView  tvName, tvPrice;
        VH(View v) {
            super(v);
            ivProduct = v.findViewById(R.id.iv_product);
            tvName    = v.findViewById(R.id.tv_product_name);
            tvPrice   = v.findViewById(R.id.tv_product_price);
        }
    }
    public void updateData(List<ProductModel> newList, List<String> newIds) {
        this.products.clear();
        this.products.addAll(newList);
        this.ids.clear();
        this.ids.addAll(newIds);
        notifyDataSetChanged();
    }
}
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
import com.example.cafemanagement.model.ProductModel;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductModel> productList;
    private final OnProductClickListener listener;

    // Interface xử lý sự kiện click
    public interface OnProductClickListener {
        void onAddQuickClick(ProductModel product);
        void onItemDetailClick(ProductModel product);
    }

    public ProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void setProductList(List<ProductModel> list) {
        this.productList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_vertical, parent, false);

        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {

        ProductModel product = productList.get(position);

        holder.txtName.setText(product.getName());

        // ĐÃ CẬP NHẬT: Sử dụng lớp PriceFormatter dùng chung để hiển thị dấu chấm phần ngàn thống nhất
        holder.txtPrice.setText(com.example.cafemanagement.helper.PriceFormatter.formatPrice(product.getPrice()));

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder_coffee)
                .error(R.drawable.placeholder_coffee)
                .into(holder.imgProduct);

        // Click nút add
        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {
                    listener.onAddQuickClick(product);
                }
            }
        });

        // Click item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {
                    listener.onItemDetailClick(product);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtPrice;
        ImageView imgProduct;
        View btnAdd;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            imgProduct = itemView.findViewById(R.id.imgProduct);

            // Thay bằng id nút add thật trong XML
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}
package com.example.cafemanagement.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.PriceFormatter;
import com.example.cafemanagement.model.ProductModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    public interface ProductActionListener {
        void onEdit(ProductModel product);
        void onDelete(ProductModel product);
    }

    private List<ProductModel> items = new ArrayList<>();
    private Map<String, String> categoryNames = Map.of();
    private final ProductActionListener listener;

    public AdminProductAdapter(ProductActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ProductModel> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setCategoryNames(Map<String, String> categoryNames) {
        this.categoryNames = categoryNames != null ? categoryNames : Map.of();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductModel product = items.get(position);
        holder.txtName.setText(product.getName());
        holder.txtPrice.setText(PriceFormatter.formatPrice(product.getPrice()));
        String catName = categoryNames.getOrDefault(product.getCategoryId(), "Chưa phân loại");
        holder.txtCategory.setText(catName);

        String desc = product.getDescription();
        if (!TextUtils.isEmpty(desc)) {
            holder.txtDescription.setVisibility(View.VISIBLE);
            holder.txtDescription.setText(desc);
        } else {
            holder.txtDescription.setVisibility(View.GONE);
        }

        String imageUrl = product.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            holder.txtPlaceholder.setVisibility(View.GONE);
            holder.imgProduct.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(28)))
                    .placeholder(R.drawable.placeholder_coffee)
                    .error(R.drawable.placeholder_coffee)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setVisibility(View.GONE);
            holder.txtPlaceholder.setVisibility(View.VISIBLE);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(product));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(product));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtPlaceholder, txtPrice, txtName, txtCategory, txtDescription;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtPlaceholder = itemView.findViewById(R.id.txtProductPlaceholder);
            txtPrice = itemView.findViewById(R.id.txtProductPrice);
            txtName = itemView.findViewById(R.id.txtProductName);
            txtCategory = itemView.findViewById(R.id.txtProductCategory);
            txtDescription = itemView.findViewById(R.id.txtProductDescription);
            btnEdit = itemView.findViewById(R.id.btnEditProduct);
            btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
        }
    }
}

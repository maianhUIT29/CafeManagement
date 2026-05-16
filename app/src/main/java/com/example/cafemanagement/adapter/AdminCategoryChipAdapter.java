package com.example.cafemanagement.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.CategoryModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminCategoryChipAdapter extends RecyclerView.Adapter<AdminCategoryChipAdapter.ViewHolder> {

    public interface CategorySelectListener {
        void onCategorySelected(CategoryModel category);
        void onCategoryLongClick(CategoryModel category);
    }

    private List<CategoryModel> items = new ArrayList<>();
    private Map<String, Integer> productCounts = new HashMap<>();
    private String selectedId;
    private final CategorySelectListener listener;

    public AdminCategoryChipAdapter(CategorySelectListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CategoryModel> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setProductCounts(Map<String, Integer> counts) {
        this.productCounts = counts != null ? counts : new HashMap<>();
        notifyDataSetChanged();
    }

    public void setSelectedId(String selectedId) {
        this.selectedId = selectedId;
        notifyDataSetChanged();
    }

    public String getSelectedId() {
        return selectedId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_category_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryModel category = items.get(position);
        holder.txtName.setText(category.getName());
        int count = productCounts.getOrDefault(category.getId(), 0);
        holder.txtCount.setText(String.valueOf(count));

        boolean selected = category.getId() != null && category.getId().equals(selectedId);
        if (selected) {
            holder.card.setCardBackgroundColor(Color.parseColor("#5D4037"));
            holder.card.setStrokeColor(Color.parseColor("#5D4037"));
            holder.txtName.setTextColor(Color.WHITE);
            holder.txtCount.setTextColor(Color.parseColor("#5D4037"));
            holder.txtCount.setBackgroundColor(Color.parseColor("#FFD54F"));
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE);
            holder.card.setStrokeColor(Color.parseColor("#335D4037"));
            holder.txtName.setTextColor(Color.parseColor("#5D4037"));
            holder.txtCount.setTextColor(Color.parseColor("#5D4037"));
            holder.txtCount.setBackgroundResource(R.drawable.bg_rounded_light_grey);
        }

        holder.itemView.setOnClickListener(v -> listener.onCategorySelected(category));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onCategoryLongClick(category);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView txtName, txtCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardCategoryChip);
            txtName = itemView.findViewById(R.id.txtCategoryChip);
            txtCount = itemView.findViewById(R.id.txtCategoryCount);
        }
    }
}

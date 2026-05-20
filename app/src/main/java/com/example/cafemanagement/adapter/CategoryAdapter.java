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

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<CategoryModel> categoryList;
    private final OnCategoryClickListener listener;
    private int selectedPosition = 0; // Mặc định tô màu đậm cho danh mục đầu tiên

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryModel category);
    }

    public CategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setCategoryList(List<CategoryModel> list) {
        this.categoryList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel category = categoryList.get(position);
        holder.txtCategoryName.setText(category.getName());

        // Xử lý đổi màu sắc tương phản khi thẻ được chọn
        if (selectedPosition == position) {
            // Trạng thái được chọn: Nền nâu đậm, chữ trắng
            holder.cardCategory.setCardBackgroundColor(Color.parseColor("#3E2723"));
            holder.txtCategoryName.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            // Trạng thái không được chọn: Nền xám nhạt, chữ nâu đậm
            holder.cardCategory.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            holder.txtCategoryName.setTextColor(Color.parseColor("#3E2723"));
        }

        // Bắt sự kiện người dùng bấm vào thẻ
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Yêu cầu RecyclerView vẽ lại màu sắc cho thẻ cũ và thẻ mới
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            // Gửi dữ liệu về MenuActivity để lọc món ăn
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardCategory;
        TextView txtCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCategory = itemView.findViewById(R.id.cardCategory);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
        }
    }
}
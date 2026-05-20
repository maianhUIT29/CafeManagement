// ─── CategoryTabAdapter.java ───────────────────────────────────────────────
package com.example.cafemanagement.view.cashier.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.CategoryModel;

import java.util.List;

public class CategoryTabAdapter extends RecyclerView.Adapter<CategoryTabAdapter.VH> {

    public interface OnCategoryClick { void onClick(String catId); }

    private final List<CategoryModel> cats;
    private final List<String>        ids;
    private final OnCategoryClick     listener;
    private String                    selectedId = null;

    public CategoryTabAdapter(List<CategoryModel> cats, List<String> ids,
                              OnCategoryClick listener) {
        this.cats     = cats;
        this.ids      = ids;
        this.listener = listener;
    }

    public void setSelectedId(String id) { selectedId = id; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_tab, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        CategoryModel c  = cats.get(pos);
        String        id = ids.get(pos);
        h.tvName.setText(c.getName());
        boolean sel = id.equals(selectedId);
        h.itemView.setBackgroundResource(
                sel ? R.drawable.bg_cat_selected : R.drawable.bg_cat_unselected);
        h.tvName.setTextColor(h.itemView.getContext().getColor(
                sel ? R.color.white : R.color.text_secondary));
        h.itemView.setOnClickListener(v -> listener.onClick(id));
    }

    @Override public int getItemCount() { return cats.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        VH(View v) { super(v); tvName = v.findViewById(R.id.tv_cat_name); }
    }
    public void updateData(List<CategoryModel> newList, List<String> newIds) {
        this.cats.clear();
        this.cats.addAll(newList);
        this.ids.clear();
        this.ids.addAll(newIds);
        notifyDataSetChanged();
    }
}
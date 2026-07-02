package com.example.cafemanagement.view.cashier.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.TableModel;

import java.util.List;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.VH> {

    public interface OnTableClick {
        void onClick(String tableId, TableModel table);
    }

    private final List<TableModel> tables;
    private final List<String>     ids;
    private final OnTableClick     onClick;
    private final OnTableClick     onLongClick;
    private String                 selectedId = null;

    public TableAdapter(List<TableModel> tables, List<String> ids,
                        OnTableClick onClick, OnTableClick onLongClick) {
        this.tables      = tables;
        this.ids         = ids;
        this.onClick     = onClick;
        this.onLongClick = onLongClick;
    }

    public void setSelectedId(String id) {
        this.selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ĐÃ SỬA: Sử dụng item_table_cashier thay vì item_table chung
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_table_cashier, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        if (pos >= tables.size() || pos >= ids.size()) return;
        
        TableModel t  = tables.get(pos);
        String     id = ids.get(pos);

        h.tvName.setText(t.getName());

        boolean isSelected = id.equals(selectedId);
        boolean isOccupied = "OCCUPIED".equals(t.getStatus());

        h.tvStatus.setText(isOccupied ? "Đang dùng" : "Trống");

        // Cập nhật giao diện dựa trên trạng thái
        if (isSelected) {
            h.itemView.setBackgroundResource(R.drawable.bg_table_selected);
            h.tvName.setTextColor(android.graphics.Color.WHITE);
            h.tvStatus.setTextColor(android.graphics.Color.WHITE);
        } else if (isOccupied) {
            h.itemView.setBackgroundResource(R.drawable.bg_table_occupied);
            h.tvName.setTextColor(android.graphics.Color.parseColor("#7A6658"));
            h.tvStatus.setTextColor(android.graphics.Color.parseColor("#D94F4F"));
        } else {
            h.itemView.setBackgroundResource(R.drawable.bg_table_available);
            h.tvName.setTextColor(android.graphics.Color.parseColor("#1A1209"));
            h.tvStatus.setTextColor(android.graphics.Color.parseColor("#2D9B5A"));
        }

        h.itemView.setAlpha(isOccupied && !isSelected ? 0.7f : 1f);
        h.itemView.setOnClickListener(v -> onClick.onClick(id, t));

        if (onLongClick != null) {
            h.itemView.setOnLongClickListener(v -> {
                onLongClick.onClick(id, t);
                return true;
            });
        }
    }

    @Override public int getItemCount() { return Math.min(tables.size(), ids.size()); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus;
        VH(View v) {
            super(v);
            tvName   = v.findViewById(R.id.tv_table_name);
            tvStatus = v.findViewById(R.id.tv_table_status);
        }
    }

    public void updateData(List<TableModel> newTables, List<String> newIds) {
        this.tables.clear();
        this.tables.addAll(newTables);
        this.ids.clear();
        this.ids.addAll(newIds);
        notifyDataSetChanged();
    }
}
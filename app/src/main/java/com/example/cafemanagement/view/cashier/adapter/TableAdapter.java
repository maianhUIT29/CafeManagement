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

    // Short click và long click riêng biệt
    private final List<TableModel> tables;
    private final List<String>     ids;
    private final OnTableClick     onClick;
    private final OnTableClick     onLongClick;   // nullable
    private String                 selectedId = null;

    /** Constructor với long click (dùng cho SelectTableFragment) */
    public TableAdapter(List<TableModel> tables, List<String> ids,
                        OnTableClick onClick, OnTableClick onLongClick) {
        this.tables      = tables;
        this.ids         = ids;
        this.onClick     = onClick;
        this.onLongClick = onLongClick;
    }

    /** Constructor không có long click (tương thích ngược) */
    public TableAdapter(List<TableModel> tables, List<String> ids, OnTableClick onClick) {
        this(tables, ids, onClick, null);
    }

    public void setSelectedId(String id) {
        this.selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_table, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        TableModel t  = tables.get(pos);
        String     id = ids.get(pos);

        h.tvName.setText(t.getName());

        boolean isSelected = id.equals(selectedId);
        boolean isOccupied = "OCCUPIED".equals(t.getStatus());

        h.tvStatus.setText(isOccupied ? "Đang dùng" : "Trống");

        // Background
        if (isSelected) {
            h.itemView.setBackgroundResource(R.drawable.bg_table_selected);
        } else if (isOccupied) {
            h.itemView.setBackgroundResource(R.drawable.bg_table_occupied);
        } else {
            h.itemView.setBackgroundResource(R.drawable.bg_table_available);
        }

        h.itemView.setAlpha(isOccupied && !isSelected ? 0.6f : 1f);

        // Short click
        h.itemView.setOnClickListener(v -> onClick.onClick(id, t));

        // Long click → trả bàn
        if (onLongClick != null) {
            h.itemView.setOnLongClickListener(v -> {
                onLongClick.onClick(id, t);
                return true; // consume event
            });
        }
    }

    @Override public int getItemCount() { return tables.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus;
        VH(View v) {
            super(v);
            tvName   = v.findViewById(R.id.tv_table_name);
            tvStatus = v.findViewById(R.id.tv_table_status);
        }
    }
}
package com.example.cafemanagement.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.TableModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AdminTableManageAdapter extends RecyclerView.Adapter<AdminTableManageAdapter.ViewHolder> {

    public interface TableActionListener {
        void onEdit(TableModel table);
        void onDelete(TableModel table);
    }

    private List<TableModel> items = new ArrayList<>();
    private final TableActionListener listener;

    public AdminTableManageAdapter(TableActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<TableModel> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_table_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TableModel table = items.get(position);

        boolean occupied = table != null
                && "OCCUPIED".equalsIgnoreCase(table.getStatus());

        String tableName = table != null && table.getName() != null && !table.getName().trim().isEmpty()
                ? table.getName()
                : "Chưa đặt tên";

        String tableZone = table != null && table.getZone() != null && !table.getZone().trim().isEmpty()
                ? table.getZone()
                : "Chưa gán khu vực";

        holder.txtName.setText(tableName);
        holder.txtZone.setText(tableZone);
        holder.txtStatus.setText(occupied ? "Đang dùng" : "Trống");
        holder.txtIcon.setText(extractTableNumber(tableName));

        if (occupied) {
            holder.cardRoot.setCardBackgroundColor(Color.parseColor("#FFF5F5"));
            holder.cardRoot.setStrokeColor(Color.parseColor("#EF9A9A"));
            holder.txtStatus.setTextColor(Color.parseColor("#C62828"));
            holder.txtIcon.setTextColor(Color.parseColor("#C62828"));
        } else {
            holder.cardRoot.setCardBackgroundColor(Color.WHITE);
            holder.cardRoot.setStrokeColor(Color.parseColor("#C8E6C9"));
            holder.txtStatus.setTextColor(Color.parseColor("#2E7D32"));
            holder.txtIcon.setTextColor(Color.parseColor("#2E7D32"));
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null && table != null) {
                listener.onEdit(table);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null && table != null) {
                listener.onDelete(table);
            }
        });
    }

    private String extractTableNumber(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "—";
        }

        String digits = name.replaceAll("[^0-9]", "");

        if (!digits.isEmpty()) {
            return digits.length() > 2
                    ? digits.substring(digits.length() - 2)
                    : digits;
        }

        return name.length() > 2
                ? name.substring(0, 2).toUpperCase()
                : name.toUpperCase();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView cardRoot;
        TextView txtIcon;
        TextView txtName;
        TextView txtZone;
        TextView txtStatus;
        View btnEdit;
        View btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardRoot = itemView.findViewById(R.id.cardTable);
            txtIcon = itemView.findViewById(R.id.txtTableIcon);
            txtName = itemView.findViewById(R.id.txtTableName);
            txtZone = itemView.findViewById(R.id.txtTableZone);
            txtStatus = itemView.findViewById(R.id.txtTableStatus);
            btnEdit = itemView.findViewById(R.id.btnEditTable);
            btnDelete = itemView.findViewById(R.id.btnDeleteTable);
        }
    }
}
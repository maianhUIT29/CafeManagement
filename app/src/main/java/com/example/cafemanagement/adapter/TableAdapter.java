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

import java.util.List;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder> {
    private List<TableModel> tableList;
    private TableModel selectedTable;
    private final OnTableClickListener listener;

    public interface OnTableClickListener {
        void onTableClick(TableModel table);
    }

    public TableAdapter(OnTableClickListener listener) {
        this.listener = listener;
    }

    public void setTableList(List<TableModel> list) {
        this.tableList = list;
        notifyDataSetChanged();
    }

    public void setSelectedTable(TableModel table) {
        this.selectedTable = table;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        TableModel table = tableList.get(position);
        if (table == null) return;

        // CẬP NHẬT: Sử dụng đúng ID từ item_table.xml (tv_table_name)
        String name = table.getName() != null ? table.getName() : "Bàn " + (position + 1);
        holder.tv_table_name.setText(name.replace("Bàn ", ""));

        String status = table.getStatus() != null ? table.getStatus() : "AVAILABLE";
        boolean isOccupied = "OCCUPIED".equalsIgnoreCase(status);
        
        // Hiển thị trạng thái text (tv_table_status)
        if (holder.tv_table_status != null) {
            holder.tv_table_status.setText(isOccupied ? "Đang dùng" : "Trống");
        }

        boolean isSelected = false;
        if (selectedTable != null && selectedTable.getTableId() != null && table.getTableId() != null) {
            isSelected = selectedTable.getTableId().equals(table.getTableId());
        }

        // Đổi màu nền dựa trên trạng thái
        if (isOccupied) {
            holder.cardTable.setCardBackgroundColor(Color.parseColor("#E0E0E0"));
            holder.tv_table_name.setTextColor(Color.parseColor("#9E9E9E"));
            if (holder.tv_table_status != null) holder.tv_table_status.setTextColor(Color.parseColor("#9E9E9E"));
        } else if (isSelected) {
            holder.cardTable.setCardBackgroundColor(Color.parseColor("#FFD54F"));
            holder.tv_table_name.setTextColor(Color.parseColor("#3E2723"));
            if (holder.tv_table_status != null) holder.tv_table_status.setTextColor(Color.parseColor("#3E2723"));
        } else {
            holder.cardTable.setCardBackgroundColor(Color.parseColor("#F1F8E9"));
            holder.tv_table_name.setTextColor(Color.parseColor("#7CB342"));
            if (holder.tv_table_status != null) holder.tv_table_status.setTextColor(Color.parseColor("#7CB342"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (!isOccupied) {
                listener.onTableClick(table);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tableList != null ? tableList.size() : 0;
    }

    static class TableViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardTable;
        TextView tv_table_name, tv_table_status;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            // SỬA LỖI ID: Ánh xạ đúng các ID trong item_table.xml
            cardTable = itemView.findViewById(R.id.cardTable);
            tv_table_name = itemView.findViewById(R.id.tv_table_name);
            tv_table_status = itemView.findViewById(R.id.tv_table_status);
        }
    }
}

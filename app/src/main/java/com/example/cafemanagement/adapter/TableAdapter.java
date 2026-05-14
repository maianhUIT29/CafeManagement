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
        holder.txtTableName.setText(table.getName().replace("Bàn ", ""));

        boolean isOccupied = "OCCUPIED".equals(table.getStatus());
        boolean isSelected = selectedTable != null && selectedTable.getTableId().equals(table.getTableId());

        if (isOccupied) {
            holder.cardTable.setCardBackgroundColor(Color.parseColor("#E0E0E0"));
            holder.txtTableName.setTextColor(Color.parseColor("#9E9E9E"));
            holder.itemView.setEnabled(false);
        } else if (isSelected) {
            holder.cardTable.setCardBackgroundColor(Color.parseColor("#FFD54F"));
            holder.txtTableName.setTextColor(Color.parseColor("#3E2723"));
            holder.itemView.setEnabled(true);
        } else {
            holder.cardTable.setCardBackgroundColor(Color.parseColor("#F1F8E9"));
            holder.txtTableName.setTextColor(Color.parseColor("#7CB342"));
            holder.itemView.setEnabled(true);
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
        TextView txtTableName;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTable = itemView.findViewById(R.id.cardTable);
            txtTableName = itemView.findViewById(R.id.txtTableName);
        }
    }
}
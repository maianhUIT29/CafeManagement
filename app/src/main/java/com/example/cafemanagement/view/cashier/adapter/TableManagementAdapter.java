package com.example.cafemanagement.view.cashier.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.TableModel;

import java.util.List;

public class TableManagementAdapter
        extends RecyclerView.Adapter<TableManagementAdapter.VH> {

    public interface OnRelease { void onRelease(String tableId, TableModel table); }

    private final List<TableModel> tables;
    private final List<String>     ids;
    private final OnRelease        onRelease;

    public TableManagementAdapter(List<TableModel> tables, List<String> ids,
                                  OnRelease onRelease) {
        this.tables    = tables;
        this.ids       = ids;
        this.onRelease = onRelease;
    }

    public void updateData(List<TableModel> newTables, List<String> newIds) {
        tables.clear();
        tables.addAll(newTables);
        ids.clear();
        ids.addAll(newIds);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_table_management, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        TableModel t  = tables.get(pos);
        String     id = ids.get(pos);

        boolean isOccupied = "OCCUPIED".equals(t.getStatus());

        h.tvName.setText(t.getName());
        h.tvZone.setText(t.getZone() != null ? t.getZone() : "");

        if (isOccupied) {
            h.tvStatus.setText("🔴 Đang dùng");
            h.tvStatus.setTextColor(0xFFE53935);
            h.itemView.setBackgroundResource(R.drawable.bg_table_occupied);
            h.btnRelease.setVisibility(View.VISIBLE);
            h.btnRelease.setOnClickListener(v -> onRelease.onRelease(id, t));
        } else {
            h.tvStatus.setText("🟢 Trống");
            h.tvStatus.setTextColor(0xFF2D9B5A);
            h.itemView.setBackgroundResource(R.drawable.bg_table_available);
            h.btnRelease.setVisibility(View.GONE);
        }
    }

    @Override public int getItemCount() { return tables.size(); }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvZone, tvStatus;
        Button   btnRelease;

        VH(View v) {
            super(v);
            tvName     = v.findViewById(R.id.tv_table_name);
            tvZone     = v.findViewById(R.id.tv_table_management_zone);
            tvStatus   = v.findViewById(R.id.tv_table_status);
            btnRelease = v.findViewById(R.id.btn_table_release);
        }
    }
}
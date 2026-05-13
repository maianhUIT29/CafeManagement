package com.example.cafemanagement.view.cashier.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.OrderModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ActiveOrderAdapter extends RecyclerView.Adapter<ActiveOrderAdapter.VH> {

    public interface OnOrderClick { void onClick(OrderModel order); }

    private final List<OrderModel> orders;
    private final List<String>     ids;
    private final OnOrderClick     listener;

    public ActiveOrderAdapter(List<OrderModel> orders, List<String> ids,
                              OnOrderClick listener) {
        this.orders   = orders;
        this.ids      = ids;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_active_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        OrderModel o = orders.get(pos);
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));

        h.tvTable.setText(o.getTableName());
        h.tvItems.setText(countItems(o) + " món");
        h.tvTotal.setText(fmt.format(o.getTotal()) + "đ");
        h.tvStatus.setText(statusLabel(o.getStatus()));

        h.itemView.setOnClickListener(v -> listener.onClick(o));
    }

    private int countItems(OrderModel o) {
        if (o.getItems() == null) return 0;
        int c = 0;
        for (var item : o.getItems().values()) c += item.getQuantity();
        return c;
    }

    private String statusLabel(String status) {
        if (status == null) return "";
        switch (status) {
            case OrderModel.STATUS_PENDING:   return "Chờ xử lý";
            case OrderModel.STATUS_PREPARING: return "Đang pha";
            case OrderModel.STATUS_DONE:      return "Xong";
            default: return status;
        }
    }

    @Override public int getItemCount() { return orders.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTable, tvItems, tvTotal, tvStatus;
        VH(View v) {
            super(v);
            tvTable  = v.findViewById(R.id.tv_order_table);
            tvItems  = v.findViewById(R.id.tv_order_items);
            tvTotal  = v.findViewById(R.id.tv_order_total);
            tvStatus = v.findViewById(R.id.tv_order_status);
        }
    }
}
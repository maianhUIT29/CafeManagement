package com.example.cafemanagement.view.cashier.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.OrderItemModel;
import com.example.cafemanagement.model.OrderModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShiftOrderAdapter extends RecyclerView.Adapter<ShiftOrderAdapter.VH> {

    private final List<OrderModel> data;
    private final NumberFormat     fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ShiftOrderAdapter(List<OrderModel> data) { this.data = data; }

    public void updateData(List<OrderModel> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_list, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        OrderModel o = data.get(pos);

        if (h.tvTable != null) {
            if (OrderModel.TYPE_TAKE_AWAY.equals(o.getOrderType())
                    || o.getTableId() == null || o.getTableId().isEmpty()) {
                h.tvTable.setText("Mang về");
            } else {
                String numPart = o.getTableId().replaceAll("^[^0-9]+", "");
                h.tvTable.setText("Bàn " + (numPart.isEmpty() ? o.getTableId() : numPart));
            }
        }

        if (h.tvItemCount != null) {
            int totalQty = 0;
            if (o.getItems() != null)
                for (OrderItemModel item : o.getItems().values())
                    totalQty += item.getQuantity();
            h.tvItemCount.setText(totalQty + " món");
        }

        if (h.tvTime   != null) h.tvTime.setText(sdf.format(new Date(o.getCreatedAt())));
        if (h.tvTotal  != null) h.tvTotal.setText(fmt.format(o.getTotal()) + "đ");
        if (h.tvStatus != null) h.tvStatus.setText(
                OrderModel.STATUS_COMPLETE.equals(o.getStatus()) ? "Hoàn thành" : "Xong");
    }

    @Override public int getItemCount() { return data.size(); }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvTable, tvItemCount, tvTime, tvTotal, tvStatus;
        VH(@NonNull View v) {
            super(v);
            tvTable     = v.findViewById(R.id.tv_order_table);
            tvItemCount = v.findViewById(R.id.tv_order_item_count);
            tvTime      = v.findViewById(R.id.tv_order_time);
            tvTotal     = v.findViewById(R.id.tv_order_total);
            tvStatus    = v.findViewById(R.id.tv_order_status);
        }
    }
}
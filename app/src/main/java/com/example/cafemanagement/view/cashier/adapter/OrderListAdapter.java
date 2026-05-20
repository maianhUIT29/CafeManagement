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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.VH> {

    public interface OnItemClick { void onClick(int position); }

    private final List<OrderModel> data;
    private final List<String>     keys;
    private final OnItemClick      listener;

    public OrderListAdapter(List<OrderModel> data, List<String> keys, OnItemClick listener) {
        this.data = data; this.keys = keys; this.listener = listener;
    }

    public void updateData(List<OrderModel> newData, List<String> newKeys) {
        data.clear();
        data.addAll(newData);
        keys.clear();
        keys.addAll(newKeys);
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

        boolean isTakeAway = OrderModel.TYPE_TAKE_AWAY.equals(o.getOrderType());
        h.tvTable.setText(isTakeAway ? "🥤 Mang đi"
                : (o.getTableName() != null ? o.getTableName() : "—"));
        h.tvTotal.setText(
                NumberFormat.getInstance(new Locale("vi", "VN")).format(o.getTotal()) + "đ");
        h.tvTime.setText(
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(o.getCreatedAt())));
        h.tvItemCount.setText((o.getItems() != null ? o.getItems().size() : 0) + " món");

        String status = o.getStatus() != null ? o.getStatus() : "";
        switch (status) {
            case OrderModel.STATUS_PENDING:
                h.tvStatus.setText("Chờ xử lý");
                h.tvStatus.setTextColor(0xFFF07D28);
                break;
            case OrderModel.STATUS_PREPARING:
                h.tvStatus.setText("Đang pha");
                h.tvStatus.setTextColor(0xFF2196F3);
                break;
            case OrderModel.STATUS_DONE:
                h.tvStatus.setText("Hoàn thành");
                h.tvStatus.setTextColor(0xFF2D9B5A);
                break;
            case OrderModel.STATUS_PAID:
                if (isTakeAway) {
                    h.tvStatus.setText("Đã thanh toán ✓");
                    h.tvStatus.setTextColor(0xFF9E9E9E);
                } else {
                    h.tvStatus.setText("Đã TT — chờ trả bàn");
                    h.tvStatus.setTextColor(0xFF8D6E63);
                }
                break;
            case OrderModel.STATUS_COMPLETE:
                h.tvStatus.setText("Hoàn tất ✓");
                h.tvStatus.setTextColor(0xFF9E9E9E);
                break;
            default:
                h.tvStatus.setText(status);
                h.tvStatus.setTextColor(0xFF9E9E9E);
        }

        boolean isDone = OrderModel.STATUS_COMPLETE.equals(status)
                || (OrderModel.STATUS_PAID.equals(status) && isTakeAway);
        h.itemView.setOnClickListener(isDone ? null : v -> listener.onClick(h.getAdapterPosition()));
        h.itemView.setAlpha(OrderModel.STATUS_COMPLETE.equals(status) ? 0.45f : 1f);
    }

    @Override public int getItemCount() { return data.size(); }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvTable, tvTotal, tvTime, tvStatus, tvItemCount;
        VH(@NonNull View v) {
            super(v);
            tvTable     = v.findViewById(R.id.tv_order_table);
            tvTotal     = v.findViewById(R.id.tv_order_total);
            tvTime      = v.findViewById(R.id.tv_order_time);
            tvStatus    = v.findViewById(R.id.tv_order_status);
            tvItemCount = v.findViewById(R.id.tv_order_item_count);
        }
    }
}
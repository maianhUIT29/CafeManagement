package com.example.cafemanagement.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import java.util.Map;

public class BaristaOrderAdapter
        extends RecyclerView.Adapter<BaristaOrderAdapter.VH> {

    public interface OnCompleteListener {
        void onComplete(String orderId, OrderModel order);
    }

    /** Pair orderId + OrderModel */
    public static class OrderEntry {
        public final String     id;
        public final OrderModel order;
        public OrderEntry(String id, OrderModel order) {
            this.id = id; this.order = order;
        }
    }

    private final List<OrderEntry>    list;
    private final OnCompleteListener  listener;

    public BaristaOrderAdapter(List<OrderEntry> list, OnCompleteListener listener) {
        this.list     = list;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_barista_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        OrderEntry entry = list.get(position);
        OrderModel order = entry.order;

        // Order ID rút gọn
        String shortId = entry.id
                .substring(Math.max(0, entry.id.length() - 6))
                .toUpperCase();
        h.tvOrderId.setText("#" + shortId);

        // Badge ưu tiên: DINE_IN = cam, TAKE_AWAY = xanh
        boolean isDineIn = OrderModel.TYPE_DINE_IN.equals(order.getOrderType());
        if (isDineIn) {
            h.tvPriority.setText("🪑 Tại chỗ");
            h.tvPriority.setBackgroundResource(R.drawable.bg_status_chip);
            h.tvPriority.setTextColor(0xFFE65100);
        } else {
            h.tvPriority.setText("🥤 Mang đi");
            h.tvPriority.setBackgroundResource(R.drawable.bg_status_chip);
            h.tvPriority.setTextColor(0xFF1565C0);
        }

        // Tên bàn (nếu có)
        if (isDineIn && order.getTableName() != null) {
            h.tvTableName.setVisibility(View.VISIBLE);
            h.tvTableName.setText(order.getTableName());
        } else {
            h.tvTableName.setVisibility(View.GONE);
        }

        // Thời gian đặt
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(order.getCreatedAt()));
        h.tvTime.setText("🕐 " + time);

        // Danh sách món
        h.llItems.removeAllViews();
        Map<String, OrderItemModel> items = order.getItems();
        if (items != null) {
            for (OrderItemModel item : items.values()) {
                View itemView = LayoutInflater.from(h.itemView.getContext())
                        .inflate(R.layout.item_barista_order_row, h.llItems, false);

                TextView tvName   = itemView.findViewById(R.id.tv_row_name);
                TextView tvDetail = itemView.findViewById(R.id.tv_row_detail);

                tvName.setText(item.getProductName()
                        + (item.getSize() != null ? "  [" + item.getSize() + "]" : "")
                        + "  ×" + item.getQuantity());

                StringBuilder detail = new StringBuilder();
                detail.append("Đường ").append(item.getSugar())
                        .append("%  •  Đá ").append(item.getIce()).append("%");
                if (item.getToppings() != null && !item.getToppings().isEmpty()) {
                    detail.append("\nTopping: ");
                    for (String t : item.getToppings().keySet()) detail.append(t).append(", ");
                    detail.setLength(detail.length() - 2);
                }
                if (item.getNote() != null && !item.getNote().isEmpty()) {
                    detail.append("\nGhi chú: ").append(item.getNote());
                }
                tvDetail.setText(detail.toString());

                h.llItems.addView(itemView);
            }
        }

        // Tổng tiền
        h.tvTotal.setText(
                NumberFormat.getInstance(new Locale("vi", "VN")).format(order.getTotal()) + "đ");

        // Nút hoàn thành
        h.btnComplete.setOnClickListener(v -> listener.onComplete(entry.id, order));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView     tvOrderId, tvPriority, tvTableName, tvTime, tvTotal;
        LinearLayout llItems;
        Button       btnComplete;

        VH(@NonNull View v) {
            super(v);
            tvOrderId   = v.findViewById(R.id.tv_b_order_id);
            tvPriority  = v.findViewById(R.id.tv_b_priority);
            tvTableName = v.findViewById(R.id.tv_b_table_name);
            tvTime      = v.findViewById(R.id.tv_b_time);
            llItems     = v.findViewById(R.id.ll_b_items);
            tvTotal     = v.findViewById(R.id.tv_b_total);
            btnComplete = v.findViewById(R.id.btn_b_complete);
        }
    }
    public void updateData(List<OrderEntry> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }
}
package com.example.cafemanagement.view.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.OrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerOrderStatusFragment extends Fragment {

    private RecyclerView recyclerOrders;
    private LinearLayout layoutEmpty;
    private ValueEventListener ordersListener;

    // Simple adapter inline
    private OrderStatusAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerOrders = view.findViewById(R.id.recyclerOrders);
        layoutEmpty    = view.findViewById(R.id.layoutEmpty);

        adapter = new OrderStatusAdapter(new ArrayList<>());
        recyclerOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerOrders.setAdapter(adapter);

        listenMyOrders();
    }

    private void listenMyOrders() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String customerId = user.getUid();

        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<OrderEntry> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    OrderModel order = child.getValue(OrderModel.class);
                    if (order == null) continue;
                    // Chỉ lấy đơn của customer này
                    if (customerId.equals(order.getCustomerId())) {
                        list.add(new OrderEntry(child.getKey(), order));
                    }
                }
                // Sắp xếp mới nhất lên đầu
                list.sort((a, b) ->
                        Long.compare(b.order.getCreatedAt(), a.order.getCreatedAt()));

                adapter.updateData(list);
                layoutEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerOrders.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        FirebaseHelper.getOrdersRef().addValueEventListener(ordersListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ordersListener != null)
            FirebaseHelper.getOrdersRef().removeEventListener(ordersListener);
    }

    // ── Data class ────────────────────────────────────────────────────────────
    static class OrderEntry {
        String     orderId;
        OrderModel order;
        OrderEntry(String id, OrderModel o) { orderId = id; order = o; }
    }

    // ── Inline Adapter ────────────────────────────────────────────────────────
    static class OrderStatusAdapter
            extends RecyclerView.Adapter<OrderStatusAdapter.VH> {

        private List<OrderEntry> data;
        OrderStatusAdapter(List<OrderEntry> d) { data = d; }

        void updateData(List<OrderEntry> newData) {
            data = newData;
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate programmatically — không cần XML riêng
            LinearLayout card = buildCardLayout(parent);
            return new VH(card);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            OrderEntry entry = data.get(position);
            holder.bind(entry);
        }

        @Override public int getItemCount() { return data.size(); }

        private LinearLayout buildCardLayout(ViewGroup parent) {
            LinearLayout root = new LinearLayout(parent.getContext());
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(48, 36, 48, 36);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(32, 16, 32, 16);
            root.setLayoutParams(lp);
            root.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
            return root;
        }

        static class VH extends RecyclerView.ViewHolder {
            LinearLayout root;
            VH(LinearLayout v) { super(v); root = v; }

            void bind(OrderEntry entry) {
                root.removeAllViews();
                OrderModel o = entry.order;

                // ID + thời gian
                addText(root, "Đơn #" + entry.orderId.substring(
                                Math.max(0, entry.orderId.length() - 6)),
                        16, true, "#3E2723");

                SimpleDateFormat sdf =
                        new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                addText(root, sdf.format(new Date(o.getCreatedAt())),
                        12, false, "#9E9E9E");

                // Loại đơn + bàn
                String typeLabel = OrderModel.TYPE_DINE_IN.equals(o.getOrderType())
                        ? "🪑 Tại chỗ" + (o.getTableName() != null ? " — " + o.getTableName() : "")
                        : "🥡 Mang đi";
                addText(root, typeLabel, 13, false, "#5D4037");

                // Món
                if (o.getItems() != null) {
                    for (com.example.cafemanagement.model.OrderItemModel item
                            : o.getItems().values()) {
                        addText(root,
                                "  • " + item.getQuantity() + "x " + item.getProductName(),
                                13, false, "#424242");
                    }
                }

                // Tổng tiền
                addText(root, "Tổng: " +
                                new java.text.DecimalFormat("#,###").format(o.getTotal()) + "đ",
                        14, true, "#3E2723");

                // Status badge
                String statusLabel;
                String statusColor;
                switch (o.getStatus() != null ? o.getStatus() : "") {
                    case OrderModel.STATUS_PREPARING:
                        statusLabel = "⏳ Đang pha chế";
                        statusColor = "#FF8F00";
                        break;
                    case OrderModel.STATUS_DONE:
                        statusLabel = "✅ Hoàn thành — Vui lòng lấy đồ!";
                        statusColor = "#2E7D32";
                        break;
                    case OrderModel.STATUS_PAID:
                        statusLabel = "💳 Đã thanh toán";
                        statusColor = "#1565C0";
                        break;
                    default:
                        statusLabel = "🕐 Chờ xử lý";
                        statusColor = "#757575";
                }
                addText(root, statusLabel, 14, true, statusColor);
            }

            private void addText(LinearLayout parent, String text,
                                 int spSize, boolean bold, String hex) {
                TextView tv = new TextView(parent.getContext());
                tv.setText(text);
                tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, spSize);
                tv.setTextColor(android.graphics.Color.parseColor(hex));
                if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 4, 0, 4);
                tv.setLayoutParams(lp);
                parent.addView(tv);
            }
        }
    }
}
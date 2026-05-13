package com.example.cafemanagement.view.cashier;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.CartManager;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.OrderModel;
import com.example.cafemanagement.model.TableModel;
import com.example.cafemanagement.view.CashierActivity;
import com.example.cafemanagement.view.cashier.adapter.ActiveOrderAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView tvGreeting, tvRevenue, tvOrderCount, tvTableOccupied;
    private RecyclerView rvActiveOrders;
    private ActiveOrderAdapter activeOrderAdapter;
    private final List<OrderModel> activeOrders   = new ArrayList<>();
    private final List<String>     activeOrderIds = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGreeting      = view.findViewById(R.id.tv_greeting);
        tvRevenue       = view.findViewById(R.id.tv_revenue_today);
        tvOrderCount    = view.findViewById(R.id.tv_order_count);
        tvTableOccupied = view.findViewById(R.id.tv_table_occupied);
        rvActiveOrders  = view.findViewById(R.id.rv_active_orders);

        activeOrderAdapter = new ActiveOrderAdapter(activeOrders, activeOrderIds, order -> {
            // TODO: navigate to order detail / edit
        });
        rvActiveOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvActiveOrders.setAdapter(activeOrderAdapter);

        // ── Tạo đơn mới → OrderTypeFragment (chọn Tại chỗ / Mang đi) ────────
        view.findViewById(R.id.btn_new_order).setOnClickListener(v -> {
            CartManager.getInstance().clear(); // reset cart trước
            ((CashierActivity) requireActivity())
                    .navigateTo(CashierActivity.SCREEN_ORDER_TYPE, null, true);
        });

        loadGreeting();
        loadStats();
        loadActiveOrders();
    }

    private void loadGreeting() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user != null && user.getDisplayName() != null
                ? user.getDisplayName() : "Nhân viên";
        tvGreeting.setText("Xin chào, " + name + " 👋");
    }

    private void loadStats() {
        // Bàn đang dùng
        FirebaseHelper.getTablesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                int occupied = 0, total = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    total++;
                    TableModel t = ds.getValue(TableModel.class);
                    if (t != null && "OCCUPIED".equals(t.getStatus())) occupied++;
                }
                tvTableOccupied.setText(occupied + "/" + total);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        // Doanh thu hôm nay (PAID + COMPLETE)
        FirebaseHelper.getOrdersRef()
                .orderByChild("createdAt")
                .startAt((double) getTodayStartMillis())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        long revenue = 0;
                        int  count   = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            OrderModel o = ds.getValue(OrderModel.class);
                            if (o != null && (OrderModel.STATUS_PAID.equals(o.getStatus())
                                    || OrderModel.STATUS_COMPLETE.equals(o.getStatus()))) {
                                revenue += o.getTotal();
                                count++;
                            }
                        }
                        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
                        tvRevenue.setText(fmt.format(revenue) + "đ");
                        tvOrderCount.setText(String.valueOf(count));
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void loadActiveOrders() {
        // Hiện đơn PENDING + PREPARING trên dashboard
        FirebaseHelper.getOrdersRef()
                .orderByChild("createdAt")
                .startAt((double) getTodayStartMillis())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        activeOrders.clear();
                        activeOrderIds.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            OrderModel o = ds.getValue(OrderModel.class);
                            if (o != null && (
                                    OrderModel.STATUS_PENDING.equals(o.getStatus())
                                            || OrderModel.STATUS_PREPARING.equals(o.getStatus()))) {
                                activeOrders.add(o);
                                activeOrderIds.add(ds.getKey());
                            }
                        }
                        activeOrderAdapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private long getTodayStartMillis() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
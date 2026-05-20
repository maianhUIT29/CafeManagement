package com.example.cafemanagement.view.cashier;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.OrderModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShiftReportFragment extends Fragment {

    private TextView tvShiftTitle, tvCashierName;
    private TextView tvTimeRange, tvTotalRevenue, tvTotalOrders, tvAvgOrder;
    private RecyclerView rvOrders;
    private Button btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shift_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvShiftTitle   = view.findViewById(R.id.tv_report_shift_title);
        tvCashierName  = view.findViewById(R.id.tv_report_cashier);
        tvTimeRange    = view.findViewById(R.id.tv_report_time_range);
        tvTotalRevenue = view.findViewById(R.id.tv_report_revenue);
        tvTotalOrders  = view.findViewById(R.id.tv_report_orders);
        tvAvgOrder     = view.findViewById(R.id.tv_report_avg);
        rvOrders       = view.findViewById(R.id.rv_report_orders);
        btnBack        = view.findViewById(R.id.btn_report_back);

        Bundle args = getArguments();
        if (args == null) return;

        String shiftName   = args.getString("shiftName",   "Ca làm việc");
        String cashierName = args.getString("cashierName", "Nhân viên");
        String cashierId   = args.getString("cashierId",   "");
        long   openedAt    = args.getLong("openedAt",   0);
        long   closedAt    = args.getLong("closedAt",   0);
        boolean isLive     = args.getBoolean("isLive",  true);

        // Header
        tvShiftTitle.setText("Báo cáo — " + shiftName);
        tvCashierName.setText("Nhân viên: " + cashierName);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
        String openStr  = sdf.format(new Date(openedAt));
        String closeStr = isLive ? "Đang mở" : sdf.format(new Date(closedAt));
        tvTimeRange.setText(openStr + "  →  " + closeStr);

        if (!isLive) {
            // Dữ liệu đã được tính sẵn (sau đóng ca)
            long revenue = args.getLong("totalRevenue", 0);
            int  orders  = args.getInt("totalOrders",  0);
            bindSummary(revenue, orders);
        } else {
            // Live: query realtime
            loadOrdersForShift(cashierId, openedAt);
        }

        btnBack.setOnClickListener(v -> requireActivity()
                .getSupportFragmentManager().popBackStack());
    }

    private void loadOrdersForShift(String cashierId, long openedAt) {
        FirebaseHelper.getOrdersRef()
                .orderByChild("cashierId")
                .equalTo(cashierId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        long revenue = 0;
                        int  count   = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            OrderModel o = ds.getValue(OrderModel.class);
                            if (o != null
                                    && OrderModel.STATUS_PAID.equals(o.getStatus())
                                    && o.getCreatedAt() >= openedAt) {
                                revenue += o.getTotal();
                                count++;
                            }
                        }
                        bindSummary(revenue, count);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void bindSummary(long revenue, int orders) {
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotalRevenue.setText(fmt.format(revenue) + "đ");
        tvTotalOrders.setText(String.valueOf(orders));
        long avg = orders > 0 ? revenue / orders : 0;
        tvAvgOrder.setText(fmt.format(avg) + "đ");
    }
}
package com.example.cafemanagement.view.customer;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.adapter.PointHistoryAdapter;
import com.example.cafemanagement.model.OrderModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerPointsFragment extends Fragment {

    private TextView txtTotalPoints;
    private TextView txtProgressMessage;
    private LinearProgressIndicator progressBarPoints;
    private RecyclerView recyclerPointHistory;

    private PointHistoryAdapter pointAdapter;
    private List<OrderModel> orderList;
    private DatabaseReference ordersRef;
    private DatabaseReference userRef;

    // Biến lưu trữ mốc nhận mã giảm giá quy định
    private final int TARGET_POINTS = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_point, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtTotalPoints = view.findViewById(R.id.txtTotalPoints);
        txtProgressMessage = view.findViewById(R.id.txtProgressMessage);
        progressBarPoints = view.findViewById(R.id.progressBarPoints);
        recyclerPointHistory = view.findViewById(R.id.recyclerPointHistory);

        // Khởi tạo và liên kết bộ chuyển đổi với danh sách
        recyclerPointHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        orderList = new ArrayList<>();
        pointAdapter = new PointHistoryAdapter(orderList);
        recyclerPointHistory.setAdapter(pointAdapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String customerId = currentUser.getUid();
            ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(customerId);

            loadCurrentPoints();
            loadPointHistory(customerId);
        } else {
            Toast.makeText(requireContext(), "Lỗi hệ thống không tìm thấy phiên đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm truy vấn điểm số hiện hành và xử lý thanh tiến trình
    private void loadCurrentPoints() {
        userRef.child("points").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer currentPoints = snapshot.getValue(Integer.class);
                    if (currentPoints == null) currentPoints = 0;

                    txtTotalPoints.setText(String.valueOf(currentPoints));

                    if (currentPoints >= TARGET_POINTS) {
                        progressBarPoints.setProgress(100);
                        txtProgressMessage.setText("Chúc mừng bạn đã đủ điều kiện nhận mã giảm giá");
                    } else {
                        int progressPercentage = (currentPoints * 100) / TARGET_POINTS;
                        progressBarPoints.setProgress(progressPercentage);
                        int pointsNeeded = TARGET_POINTS - currentPoints;
                        txtProgressMessage.setText("Cần mua thêm " + pointsNeeded + " điểm để nhận phần thưởng");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Lỗi đồng bộ dữ liệu điểm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm truy vấn lịch sử hóa đơn dựa trên mã định danh khách hàng
    private void loadPointHistory(String customerId) {
        Query query = ordersRef.orderByChild("customerId").equalTo(customerId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    OrderModel order = dataSnapshot.getValue(OrderModel.class);

                    // Logic sàng lọc bắt buộc loại trừ các hóa đơn không phát sinh điểm
                    if (order != null && order.getPointsEarned() > 0) {
                        orderList.add(order);
                    }
                }

                // Đảo ngược danh sách để đưa các giao dịch mới nhất lên vị trí đầu tiên
                Collections.reverse(orderList);
                pointAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Lỗi đồng bộ lịch sử", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

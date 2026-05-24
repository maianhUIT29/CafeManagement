package com.example.cafemanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.OrderModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PointHistoryAdapter extends RecyclerView.Adapter<PointHistoryAdapter.PointViewHolder> {

    private List<OrderModel> orderList;

    public PointHistoryAdapter(List<OrderModel> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public PointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_point_history, parent, false);
        return new PointViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PointViewHolder holder, int position) {
        OrderModel order = orderList.get(position);

        // Chuyển đổi nhãn thời gian thực thành định dạng ngày giờ chuẩn xác
        if (order.getCreatedAt() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String dateString = sdf.format(new Date(order.getCreatedAt()));
            holder.txtOrderDate.setText(dateString);
        } else {
            holder.txtOrderDate.setText("Chưa xác định thời gian");
        }

        // Kiểm tra và hiển thị mã đơn hàng để chống lỗi rỗng dữ liệu
        if (order.getOrderId() != null) {
            holder.txtOrderId.setText("Đơn hàng: " + order.getOrderId());
        }

        // Hiển thị số điểm cộng thêm
        holder.txtEarnedPoints.setText("+" + order.getPointsEarned() + " Điểm");
    }

    @Override
    public int getItemCount() {
        if (orderList != null) {
            return orderList.size();
        }
        return 0;
    }

    public static class PointViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderDate;
        TextView txtOrderId;
        TextView txtEarnedPoints;

        public PointViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderDate = itemView.findViewById(R.id.txtOrderDate);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtEarnedPoints = itemView.findViewById(R.id.txtEarnedPoints);
        }
    }
}
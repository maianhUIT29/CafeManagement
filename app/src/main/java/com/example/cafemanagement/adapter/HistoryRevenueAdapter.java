package com.example.cafemanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.HistoryRevenueModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryRevenueAdapter extends RecyclerView.Adapter<HistoryRevenueAdapter.HistoryViewHolder> {

    private List<HistoryRevenueModel> historyList = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public void setHistoryList(List<HistoryRevenueModel> list) {
        this.historyList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Bạn cần tạo tệp item_admin_history.xml có các TextView tương ứng
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryRevenueModel item = historyList.get(position);

        holder.txtShiftName.setText(item.getShiftName() + " (" + item.getCashierName() + ")");

        String timeStr = timeFormat.format(new Date(item.getOpenedAt())) + " - " +
                (item.getClosedAt() > 0 ? timeFormat.format(new Date(item.getClosedAt())) : "Đang mở");
        holder.txtTime.setText(timeStr);

        holder.txtRevenue.setText("Doanh thu ca: " + com.example.cafemanagement.helper.PriceFormatter.formatPrice(item.getTotalRevenue()));

        // Logic hiển thị Dòng tiền
        if ("CLOSED".equals(item.getStatus())) {
            holder.layoutCashDetails.setVisibility(View.VISIBLE);
            holder.txtExpectedCash.setText("Két dự kiến: " + com.example.cafemanagement.helper.PriceFormatter.formatPrice(item.getExpectedCashInDrawer()));

            // Nếu là Ca Tối (hoặc ca có phát sinh depositAmount)
            if (item.getDepositAmount() > 0) {
                holder.txtDepositAmount.setVisibility(View.VISIBLE);
                holder.txtDepositAmount.setText("Tiền cần cất/nộp: " + com.example.cafemanagement.helper.PriceFormatter.formatPrice(item.getDepositAmount()));
            } else {
                holder.txtDepositAmount.setVisibility(View.GONE);
            }
        } else {
            // Ca đang mở, chưa có dữ liệu kết ca
            holder.layoutCashDetails.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtShiftName, txtTime, txtRevenue, txtExpectedCash, txtDepositAmount;
        LinearLayout layoutCashDetails;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtShiftName = itemView.findViewById(R.id.txtHistoryShiftName);
            txtTime = itemView.findViewById(R.id.txtHistoryTime);
            txtRevenue = itemView.findViewById(R.id.txtHistoryRevenue);

            layoutCashDetails = itemView.findViewById(R.id.layoutCashDetails);
            txtExpectedCash = itemView.findViewById(R.id.txtHistoryExpectedCash);
            txtDepositAmount = itemView.findViewById(R.id.txtHistoryDepositAmount);
        }
    }
}
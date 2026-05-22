package com.example.cafemanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafemanagement.R;
import com.example.cafemanagement.model.StaffSalaryModel;
import com.example.cafemanagement.helper.PriceFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminSalaryAdapter extends RecyclerView.Adapter<AdminSalaryAdapter.SalaryViewHolder> {

    private List<StaffSalaryModel> list = new ArrayList<>();

    public void setSalaryList(List<StaffSalaryModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SalaryViewHolder onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
        return new SalaryViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_admin_salary, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SalaryViewHolder h, int position) {
        StaffSalaryModel m = list.get(position);
        h.txtName.setText(m.getName());
        h.txtHours.setText(String.format(Locale.getDefault(), "Công: %.1f giờ", m.getTotalHours()));
        h.txtAmount.setText(PriceFormatter.formatPrice(m.getTotalSalary()));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class SalaryViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtHours, txtAmount;
        public SalaryViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtSalaryStaffName);
            txtHours = itemView.findViewById(R.id.txtSalaryHours);
            txtAmount = itemView.findViewById(R.id.txtSalaryAmount);
        }
    }
}
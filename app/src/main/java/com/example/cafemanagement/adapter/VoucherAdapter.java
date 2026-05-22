package com.example.cafemanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.VoucherModel;

import java.util.ArrayList;
import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private List<VoucherModel> voucherList = new ArrayList<>();
    private final OnVoucherActionListener listener;

    // Giao diện để giao tiếp với Activity
    public interface OnVoucherActionListener {
        void onEditClick(VoucherModel voucher);
        void onDeleteClick(VoucherModel voucher);
    }

    public VoucherAdapter(OnVoucherActionListener listener) {
        this.listener = listener;
    }

    // Hàm cập nhật danh sách và vẽ lại giao diện
    public void setVoucherList(List<VoucherModel> list) {
        this.voucherList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Cần tạo tệp item_voucher_admin.xml trong thư mục res/layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voucher_admin, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        VoucherModel voucher = voucherList.get(position);

        holder.txtCode.setText(voucher.getCode());
        holder.txtDiscount.setText("Giảm: " + voucher.getDiscount() + "%");
        holder.txtDescription.setText(voucher.getDescription());

        // Bắt sự kiện chỉnh sửa
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(voucher);
            }
        });

        // Bắt sự kiện xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(voucher);
            }
        });
    }

    @Override
    public int getItemCount() {
        return voucherList != null ? voucherList.size() : 0;
    }

    public static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView txtCode, txtDiscount, txtDescription;
        ImageView btnEdit, btnDelete;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCode = itemView.findViewById(R.id.txtVoucherCode);
            txtDiscount = itemView.findViewById(R.id.txtVoucherDiscount);
            txtDescription = itemView.findViewById(R.id.txtVoucherDesc);
            btnEdit = itemView.findViewById(R.id.btnEditVoucher);
            btnDelete = itemView.findViewById(R.id.btnDeleteVoucher);
        }
    }
}
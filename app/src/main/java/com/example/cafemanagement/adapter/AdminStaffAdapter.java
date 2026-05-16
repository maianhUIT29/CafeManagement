package com.example.cafemanagement.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.UserModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminStaffAdapter extends RecyclerView.Adapter<AdminStaffAdapter.ViewHolder> {

    public interface StaffActionListener {
        void onEdit(UserModel user);
        void onDelete(UserModel user);
    }

    private List<UserModel> items = new ArrayList<>();
    private final StaffActionListener listener;

    public AdminStaffAdapter(StaffActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<UserModel> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_staff, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = items.get(position);
        String role = user.getRole() != null ? user.getRole() : "";

        holder.txtName.setText(user.getName());
        holder.txtPhone.setText("📞 " + (user.getPhone() != null ? user.getPhone() : "—"));
        String email = user.getEmail();
        if (email != null && !email.isEmpty()) {
            holder.txtEmail.setVisibility(View.VISIBLE);
            holder.txtEmail.setText("✉ " + email);
        } else {
            holder.txtEmail.setVisibility(View.GONE);
        }
        holder.txtRole.setText(roleLabel(role));
        holder.txtSalary.setText(String.format(Locale.getDefault(), "%,.0f đ", user.getSalaryRate()));
        holder.txtInitial.setText(getInitial(user.getName()));

        applyRoleStyle(holder, role);
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(user));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));
    }

    private void applyRoleStyle(ViewHolder holder, String role) {
        switch (role) {
            case "cashier":
                holder.viewAccent.setBackgroundColor(Color.parseColor("#FFB300"));
                holder.frameAvatar.setBackgroundResource(R.drawable.bg_role_cashier);
                holder.txtInitial.setTextColor(Color.parseColor("#F57F17"));
                holder.txtRole.setBackgroundResource(R.drawable.bg_badge_role_cashier);
                holder.txtRole.setTextColor(Color.parseColor("#E65100"));
                break;
            case "barista":
                holder.viewAccent.setBackgroundColor(Color.parseColor("#42A5F5"));
                holder.frameAvatar.setBackgroundResource(R.drawable.bg_role_barista);
                holder.txtInitial.setTextColor(Color.parseColor("#1565C0"));
                holder.txtRole.setBackgroundResource(R.drawable.bg_badge_role_barista);
                holder.txtRole.setTextColor(Color.parseColor("#0D47A1"));
                break;
            case "admin":
            default:
                holder.viewAccent.setBackgroundColor(Color.parseColor("#5D4037"));
                holder.frameAvatar.setBackgroundResource(R.drawable.bg_role_admin);
                holder.txtInitial.setTextColor(Color.parseColor("#3E2723"));
                holder.txtRole.setBackgroundResource(R.drawable.bg_badge_role_admin);
                holder.txtRole.setTextColor(Color.parseColor("#3E2723"));
                break;
        }
    }

    private String getInitial(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        return String.valueOf(Character.toUpperCase(name.trim().charAt(0)));
    }

    private String roleLabel(String role) {
        if (role == null) return "—";
        switch (role) {
            case "admin": return "Quản trị";
            case "cashier": return "Thu ngân";
            case "barista": return "Pha chế";
            default: return role;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewAccent;
        FrameLayout frameAvatar;
        TextView txtInitial, txtName, txtPhone, txtEmail, txtRole, txtSalary;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewAccent = itemView.findViewById(R.id.viewRoleAccent);
            frameAvatar = itemView.findViewById(R.id.frameStaffAvatar);
            txtInitial = itemView.findViewById(R.id.txtStaffInitial);
            txtName = itemView.findViewById(R.id.txtStaffName);
            txtPhone = itemView.findViewById(R.id.txtStaffPhone);
            txtEmail = itemView.findViewById(R.id.txtStaffEmail);
            txtRole = itemView.findViewById(R.id.txtStaffRole);
            txtSalary = itemView.findViewById(R.id.txtStaffSalary);
            btnEdit = itemView.findViewById(R.id.btnEditStaff);
            btnDelete = itemView.findViewById(R.id.btnDeleteStaff);
        }
    }
}

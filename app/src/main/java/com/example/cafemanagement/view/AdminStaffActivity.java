package com.example.cafemanagement.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.adapter.AdminStaffAdapter;
import com.example.cafemanagement.model.UserModel;
import com.example.cafemanagement.viewmodel.AdminStaffViewModel;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminStaffActivity extends AdminBaseActivity {

    private static final String FILTER_ALL = "all";
    private static final String FILTER_ADMIN = "admin";
    private static final String FILTER_CASHIER = "cashier";
    private static final String FILTER_BARISTA = "barista";

    private static final String[] ROLES = {"cashier", "barista", "admin"};
    private static final String[] ROLE_LABELS = {"Thu ngân", "Pha chế", "Quản trị"};

    private AdminStaffViewModel viewModel;
    private AdminStaffAdapter adapter;
    private View layoutEmpty;
    private TextView txtEmptyTitle;

    private TextView txtStatTotal, txtStatAdmin, txtStatCashier, txtStatBarista;
    private List<UserModel> allStaff = new ArrayList<>();
    private String currentFilter = FILTER_ALL;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_admin_staff;
    }

    @Override
    protected int getNavItemId() {
        return R.id.nav_admin_staff;
    }

    @Override
    protected String getAdminTitle() {
        return "Quản lý nhân sự";
    }

    @Override
    protected void onAdminContentReady(Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AdminStaffViewModel.class);

        layoutEmpty = findViewById(R.id.layoutEmptyStaff);
        txtEmptyTitle = findViewById(R.id.txtEmptyStaff);
        txtStatTotal = findViewById(R.id.txtStaffStatTotal);
        txtStatAdmin = findViewById(R.id.txtStaffStatAdmin);
        txtStatCashier = findViewById(R.id.txtStaffStatCashier);
        txtStatBarista = findViewById(R.id.txtStaffStatBarista);

        RecyclerView recycler = findViewById(R.id.recyclerStaff);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminStaffAdapter(new AdminStaffAdapter.StaffActionListener() {
            @Override
            public void onEdit(UserModel user) {
                showStaffDialog(user);
            }

            @Override
            public void onDelete(UserModel user) {
                confirmDelete(user);
            }
        });
        recycler.setAdapter(adapter);

        findViewById(R.id.fabAddStaff).setOnClickListener(v -> showStaffDialog(null));
        setupFilterChips();
        viewModel.getStaff().observe(this, this::onStaffLoaded);
    }

    private void setupFilterChips() {
        ChipGroup chipGroup = findViewById(R.id.chipGroupStaffFilter);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = checkedIds.isEmpty() ? R.id.chipStaffAll : checkedIds.get(0);
            if (checkedId == R.id.chipStaffAdmin) {
                currentFilter = FILTER_ADMIN;
            } else if (checkedId == R.id.chipStaffCashier) {
                currentFilter = FILTER_CASHIER;
            } else if (checkedId == R.id.chipStaffBarista) {
                currentFilter = FILTER_BARISTA;
            } else {
                currentFilter = FILTER_ALL;
            }
            applyFilter();
        });
    }

    private void onStaffLoaded(List<UserModel> list) {
        allStaff = list != null ? list : new ArrayList<>();
        updateSummary();
        applyFilter();
    }

    private void updateSummary() {
        int admin = 0, cashier = 0, barista = 0;
        for (UserModel u : allStaff) {
            if (u.getRole() == null) continue;
            switch (u.getRole()) {
                case "admin": admin++; break;
                case "cashier": cashier++; break;
                case "barista": barista++; break;
                default: break;
            }
        }
        txtStatTotal.setText(String.valueOf(allStaff.size()));
        txtStatAdmin.setText(String.valueOf(admin));
        txtStatCashier.setText(String.valueOf(cashier));
        txtStatBarista.setText(String.valueOf(barista));
    }

    private void applyFilter() {
        List<UserModel> filtered = new ArrayList<>();
        for (UserModel u : allStaff) {
            if (FILTER_ALL.equals(currentFilter)) {
                filtered.add(u);
            } else if (currentFilter.equals(u.getRole())) {
                filtered.add(u);
            }
        }
        adapter.setItems(filtered);

        boolean noStaff = allStaff.isEmpty();
        boolean noFilterResult = !noStaff && filtered.isEmpty();
        if (noStaff) {
            layoutEmpty.setVisibility(View.VISIBLE);
            txtEmptyTitle.setText("Chưa có nhân viên");
        } else if (noFilterResult) {
            layoutEmpty.setVisibility(View.VISIBLE);
            txtEmptyTitle.setText("Không có nhân viên phù hợp bộ lọc");
        } else {
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showStaffDialog(UserModel existing) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_staff, null);
        TextInputEditText edtName = dialogView.findViewById(R.id.edtStaffName);
        TextInputEditText edtPhone = dialogView.findViewById(R.id.edtStaffPhone);
        TextInputEditText edtEmail = dialogView.findViewById(R.id.edtStaffEmail);
        TextInputEditText edtSalary = dialogView.findViewById(R.id.edtStaffSalary);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerStaffRole);
        spinnerRole.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, ROLE_LABELS));

        boolean isEdit = existing != null;
        if (isEdit) {
            edtName.setText(existing.getName());
            edtPhone.setText(existing.getPhone());
            edtEmail.setText(existing.getEmail());
            edtSalary.setText(String.valueOf(existing.getSalaryRate()));
            for (int i = 0; i < ROLES.length; i++) {
                if (ROLES[i].equals(existing.getRole())) {
                    spinnerRole.setSelection(i);
                    break;
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa nhân viên" : "Thêm nhân viên mới")
                .setView(dialogView)
                .setPositiveButton("Lưu", (d, w) -> {
                    String name = textOf(edtName);
                    String phone = textOf(edtPhone);
                    if (name.isEmpty() || phone.isEmpty()) {
                        adminToastInfo("Vui lòng nhập tên và SĐT");
                        return;
                    }
                    UserModel user = isEdit ? existing : new UserModel();
                    user.setName(name);
                    user.setPhone(phone);
                    user.setEmail(textOf(edtEmail));
                    user.setRole(ROLES[spinnerRole.getSelectedItemPosition()]);
                    try {
                        user.setSalaryRate(Double.parseDouble(textOf(edtSalary)));
                    } catch (NumberFormatException e) {
                        user.setSalaryRate(0);
                    }
                    if (!isEdit) {
                        user.setPoints(0);
                    }
                    viewModel.saveStaff(user, isEdit, adminRepositoryCallback("Đã lưu nhân viên"));
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void confirmDelete(UserModel user) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa nhân viên")
                .setMessage("Xóa \"" + user.getName() + "\"? Hành động không thể hoàn tác.")
                .setPositiveButton("Xóa", (d, w) ->
                        viewModel.deleteStaff(user.getUserId(), adminRepositoryCallback("Đã xóa nhân viên")))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}

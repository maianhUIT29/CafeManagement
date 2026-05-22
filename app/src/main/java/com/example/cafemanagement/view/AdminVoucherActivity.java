package com.example.cafemanagement.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.adapter.VoucherAdapter;
import com.example.cafemanagement.model.VoucherModel;
import com.example.cafemanagement.viewmodel.AdminVoucherViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AdminVoucherActivity extends AdminBaseActivity {

    private AdminVoucherViewModel viewModel;
    private VoucherAdapter adapter;

    private RecyclerView recyclerVouchers;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddVoucher;

    // Kế thừa các hàm từ AdminBaseActivity để khung giao diện tự động lắp ráp
    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_admin_voucher;
    }

    @Override
    protected int getNavItemId() {
        return R.id.nav_admin_voucher;
    }

    @Override
    protected String getAdminTitle() {
        return "Quản lý Voucher";
    }

    // Hàm này sẽ được AdminBaseActivity tự động gọi sau khi đã vẽ xong khung giao diện
    @Override
    protected void onAdminContentReady(Bundle savedInstanceState) {
        initViews();
        setupRecyclerView();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {
        recyclerVouchers = findViewById(R.id.recyclerVouchers);
        progressBar = findViewById(R.id.progressBarVouchers);
        fabAddVoucher = findViewById(R.id.fabAddVoucher);
    }

    private void setupRecyclerView() {
        recyclerVouchers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VoucherAdapter(new VoucherAdapter.OnVoucherActionListener() {
            @Override
            public void onEditClick(VoucherModel voucher) {
                showVoucherDialog(voucher);
            }

            @Override
            public void onDeleteClick(VoucherModel voucher) {
                showDeleteConfirmDialog(voucher.getCode());
            }
        });
        recyclerVouchers.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AdminVoucherViewModel.class);

        viewModel.getVoucherList().observe(this, vouchers -> {
            if (vouchers != null) {
                adapter.setVoucherList(vouchers);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getActionStatus().observe(this, status -> {
            if (status != null && !status.isEmpty()) {
                if (status.contains("Lỗi")) {
                    adminToastError(status); // Sử dụng hàm từ AdminBaseActivity
                } else {
                    adminToastSuccess(status);
                }
            }
        });
    }

    private void setupListeners() {
        fabAddVoucher.setOnClickListener(v -> {
            showVoucherDialog(null);
        });
    }

    private void showVoucherDialog(VoucherModel existingVoucher) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_admin_voucher, null);
        builder.setView(view);

        EditText edtCode = view.findViewById(R.id.edtVoucherCode);
        EditText edtDiscount = view.findViewById(R.id.edtVoucherDiscount);
        EditText edtDescription = view.findViewById(R.id.edtVoucherDesc);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveVoucher);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancelVoucher);

        TextView txtTitle = view.findViewById(R.id.txtDialogTitle);
        boolean isEditMode = (existingVoucher != null);

        if (isEditMode) {
            txtTitle.setText("Chỉnh sửa Voucher");
            edtCode.setText(existingVoucher.getCode());
            edtCode.setEnabled(false);
            edtDiscount.setText(String.valueOf(existingVoucher.getDiscount()));
            edtDescription.setText(existingVoucher.getDescription());
        } else {
            txtTitle.setText("Thêm Voucher mới");
        }

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String code = edtCode.getText().toString().trim();
            String discountStr = edtDiscount.getText().toString().trim();
            String desc = edtDescription.getText().toString().trim();

            if (code.isEmpty() || discountStr.isEmpty() || desc.isEmpty()) {
                adminToastError("Vui lòng nhập đầy đủ thông tin");
                return;
            }

            viewModel.saveVoucher(code, discountStr, desc);
            dialog.dismiss();
        });
    }

    private void showDeleteConfirmDialog(String code) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa Voucher")
                .setMessage("Bạn có chắc chắn muốn xóa mã '" + code + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteVoucher(code);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
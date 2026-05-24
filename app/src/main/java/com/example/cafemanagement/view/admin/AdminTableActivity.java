package com.example.cafemanagement.view.admin;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.cafemanagement.adapter.AdminTableManageAdapter;
import com.example.cafemanagement.model.TableModel;
import com.example.cafemanagement.viewmodel.AdminTableViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminTableActivity extends AdminBaseActivity {

    private static final String FILTER_ALL = "all";
    private static final String FILTER_AVAILABLE = "available";
    private static final String FILTER_OCCUPIED = "occupied";

    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_OCCUPIED = "OCCUPIED";

    private static final String[] STATUSES = {STATUS_AVAILABLE, STATUS_OCCUPIED};
    private static final String[] STATUS_LABELS = {"Trống", "Đang dùng"};

    private AdminTableViewModel viewModel;
    private AdminTableManageAdapter adapter;

    private View layoutEmpty;
    private TextView txtEmptyTitle;
    private TextView txtEmptyDesc;

    private TextView txtStatTotal;
    private TextView txtStatAvailable;
    private TextView txtStatOccupied;
    private TextView txtOccupancyPercent;
    private TextView txtTableSubtitle;

    private List<TableModel> allTables = new ArrayList<>();
    private String currentFilter = FILTER_ALL;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_admin_table;
    }

    @Override
    protected int getNavItemId() {
        return R.id.nav_admin_tables;
    }

    @Override
    protected String getAdminTitle() {
        return "Quản lý bàn";
    }

    @Override
    protected void onAdminContentReady(Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AdminTableViewModel.class);

        bindViews();
        setupRecyclerView();
        setupFilterChips();
        setupActions();

        viewModel.getTables().observe(this, this::onTablesLoaded);
    }

    private void bindViews() {
        layoutEmpty = findViewById(R.id.layoutEmptyTables);
        txtEmptyTitle = findViewById(R.id.txtEmptyTables);
        txtEmptyDesc = findViewById(R.id.txtEmptyDesc);

        txtStatTotal = findViewById(R.id.txtTableStatTotal);
        txtStatAvailable = findViewById(R.id.txtTableStatAvailable);
        txtStatOccupied = findViewById(R.id.txtTableStatOccupied);
        txtOccupancyPercent = findViewById(R.id.txtOccupancyPercent);
        txtTableSubtitle = findViewById(R.id.txtTableSubtitle);
    }

    private void setupRecyclerView() {
        RecyclerView recycler = findViewById(R.id.recyclerTables);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminTableManageAdapter(new AdminTableManageAdapter.TableActionListener() {
            @Override
            public void onEdit(TableModel table) {
                showTableDialog(table);
            }

            @Override
            public void onDelete(TableModel table) {
                confirmDelete(table);
            }
        });

        recycler.setAdapter(adapter);
    }

    private void setupActions() {
        findViewById(R.id.fabAddTable).setOnClickListener(v -> showTableDialog(null));

        MaterialButton btnEmptyAdd = findViewById(R.id.btnEmptyAddTable);
        btnEmptyAdd.setOnClickListener(v -> showTableDialog(null));
    }

    private void setupFilterChips() {
        ChipGroup chipGroup = findViewById(R.id.chipGroupTableFilter);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = checkedIds.isEmpty() ? R.id.chipFilterAll : checkedIds.get(0);

            if (checkedId == R.id.chipFilterAvailable) {
                currentFilter = FILTER_AVAILABLE;
            } else if (checkedId == R.id.chipFilterOccupied) {
                currentFilter = FILTER_OCCUPIED;
            } else {
                currentFilter = FILTER_ALL;
            }

            applyFilter();
        });
    }

    private void onTablesLoaded(List<TableModel> list) {
        allTables = list != null ? list : new ArrayList<>();
        updateSummary();
        applyFilter();
    }

    private void updateSummary() {
        int total = allTables.size();
        int available = 0;
        int occupied = 0;

        for (TableModel table : allTables) {
            if (isOccupied(table)) {
                occupied++;
            } else {
                available++;
            }
        }

        int occupancyPercent = total == 0 ? 0 : Math.round((occupied * 100f) / total);

        txtStatTotal.setText(String.valueOf(total));
        txtStatAvailable.setText(String.valueOf(available));
        txtStatOccupied.setText(String.valueOf(occupied));
        txtOccupancyPercent.setText(occupancyPercent + "%");

        txtTableSubtitle.setText(
                total == 0
                        ? "Chưa có dữ liệu bàn"
                        : "Có " + available + " bàn trống và " + occupied + " bàn đang dùng"
        );
    }

    private void applyFilter() {
        List<TableModel> filtered = new ArrayList<>();

        for (TableModel table : allTables) {
            boolean occupied = isOccupied(table);

            if (FILTER_AVAILABLE.equals(currentFilter) && !occupied) {
                filtered.add(table);
            } else if (FILTER_OCCUPIED.equals(currentFilter) && occupied) {
                filtered.add(table);
            } else if (FILTER_ALL.equals(currentFilter)) {
                filtered.add(table);
            }
        }

        adapter.setItems(filtered);

        boolean noTablesAtAll = allTables.isEmpty();
        boolean noFilterResult = !noTablesAtAll && filtered.isEmpty();

        if (noTablesAtAll) {
            showEmptyState(
                    "Chưa có bàn nào",
                    "Thêm bàn đầu tiên để bắt đầu quản lý khu vực phục vụ."
            );
        } else if (noFilterResult) {
            showEmptyState(
                    "Không có bàn phù hợp",
                    "Thử đổi bộ lọc để xem các bàn khác."
            );
        } else {
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showEmptyState(String title, String desc) {
        layoutEmpty.setVisibility(View.VISIBLE);
        txtEmptyTitle.setText(title);
        txtEmptyDesc.setText(desc);
    }

    private boolean isOccupied(TableModel table) {
        return table != null && STATUS_OCCUPIED.equalsIgnoreCase(table.getStatus());
    }

    private void showTableDialog(TableModel existing) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_table, null);

        TextInputEditText edtName = dialogView.findViewById(R.id.edtTableName);
        TextInputEditText edtZone = dialogView.findViewById(R.id.edtTableZone);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerTableStatus);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                STATUS_LABELS
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(spinnerAdapter);

        boolean isEdit = existing != null;

        if (isEdit) {
            edtName.setText(existing.getName());
            edtZone.setText(existing.getZone());

            int selectedIndex = isOccupied(existing) ? 1 : 0;
            spinnerStatus.setSelection(selectedIndex);
        } else {
            spinnerStatus.setSelection(0);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(isEdit ? "Cập nhật bàn" : "Thêm bàn mới")
                .setView(dialogView)
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = textOf(edtName);
                String zone = textOf(edtZone);

                if (TextUtils.isEmpty(name)) {
                    edtName.setError("Vui lòng nhập tên bàn");
                    edtName.requestFocus();
                    return;
                }

                TableModel table = isEdit ? existing : new TableModel();
                table.setName(name);
                table.setZone(zone);
                table.setStatus(STATUSES[spinnerStatus.getSelectedItemPosition()]);

                if (!isEdit) {
                    table.setCurrentOrderId("");
                }

                viewModel.saveTable(
                        table,
                        isEdit,
                        adminRepositoryCallback(isEdit ? "Đã cập nhật bàn" : "Đã thêm bàn")
                );

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void confirmDelete(TableModel table) {
        if (table == null) return;

        String tableName = TextUtils.isEmpty(table.getName()) ? "bàn này" : table.getName();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa bàn")
                .setMessage("Bạn có chắc muốn xóa \"" + tableName + "\"? Hành động này không thể hoàn tác.")
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Xóa", (dialog, which) ->
                        viewModel.deleteTable(
                                table.getTableId(),
                                adminRepositoryCallback("Đã xóa bàn")
                        )
                )
                .show();
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null
                ? editText.getText().toString().trim()
                : "";
    }
}
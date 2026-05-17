package com.example.cafemanagement.view.cashier;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.TableModel;
import com.example.cafemanagement.view.CashierActivity;
import com.example.cafemanagement.view.cashier.adapter.TableAdapter;
import com.example.cafemanagement.viewmodel.SelectTableViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class SelectTableFragment extends Fragment {

    private SelectTableViewModel viewModel;

    private ChipGroup    chipGroupZone;
    private RecyclerView rvTables;
    private Button       btnContinue;
    private TableAdapter tableAdapter;

    // UI state — chỉ dùng trong Fragment lifecycle
    private String  selectedTableId   = null;
    private String  selectedTableName = null;
    private boolean tableWasOccupied  = false;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SelectTableViewModel.class);

        chipGroupZone = view.findViewById(R.id.chip_group_zone);
        rvTables      = view.findViewById(R.id.rv_tables);
        btnContinue   = view.findViewById(R.id.btn_continue_to_menu);

        tableAdapter = new TableAdapter(
                new ArrayList<>(),
                new ArrayList<>(),
                // Short click
                (tableId, table) -> {
                    if ("OCCUPIED".equals(table.getStatus())) {
                        Toast.makeText(requireContext(),
                                "Bàn đang dùng — giữ để trả bàn", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedTableId   = tableId;
                    selectedTableName = table.getName();
                    tableAdapter.setSelectedId(tableId);
                    btnContinue.setEnabled(true);
                    btnContinue.setText("Tiếp tục — " + table.getName());
                },
                // Long click
                (tableId, table) -> {
                    if ("OCCUPIED".equals(table.getStatus()))
                        showReleaseTableDialog(tableId, table);
                }
        );

        rvTables.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvTables.setAdapter(tableAdapter);

        btnContinue.setEnabled(false);
        btnContinue.setOnClickListener(v -> {
            if (selectedTableId != null)
                viewModel.confirmTableSelection(selectedTableId, selectedTableName);
        });

        chipGroupZone.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) viewModel.filterByZone(chip.getText().toString());
            }
        });

        // Observe
        setupObservers();

        // Load
        if (savedInstanceState == null) viewModel.loadTables();

        // Back callback
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (tableWasOccupied && selectedTableId != null) {
                            viewModel.releaseTableOnBack(selectedTableId);
                            tableWasOccupied = false;
                        }
                        setEnabled(false);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });
    }

    private void setupObservers() {
        viewModel.getDisplayedTables().observe(getViewLifecycleOwner(), tables ->
                tableAdapter.updateData(tables,
                        viewModel.getDisplayedIds().getValue() != null
                                ? viewModel.getDisplayedIds().getValue() : new ArrayList<>()));

        viewModel.getDisplayedIds().observe(getViewLifecycleOwner(), ids ->
                tableAdapter.updateData(
                        viewModel.getDisplayedTables().getValue() != null
                                ? viewModel.getDisplayedTables().getValue() : new ArrayList<>(),
                        ids));

        viewModel.getZoneList().observe(getViewLifecycleOwner(), this::buildZoneChips);

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getSelectResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.state) {
                case LOADING:
                    btnContinue.setEnabled(false);
                    btnContinue.setText("Đang xử lý...");
                    break;
                case SUCCESS:
                    tableWasOccupied = true;
                    ((CashierActivity) requireActivity())
                            .navigateTo(CashierActivity.SCREEN_MENU, null, true);
                    break;
                case ERROR:
                    btnContinue.setEnabled(true);
                    btnContinue.setText("Tiếp tục — " + selectedTableName);
                    Toast.makeText(requireContext(),
                            "Lỗi: " + result.errorMessage, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    // --- UI helpers ---

    private void buildZoneChips(java.util.List<String> zones) {
        chipGroupZone.removeAllViews();
        for (String zone : zones) {
            Chip chip = new Chip(requireContext());
            chip.setText(zone);
            chip.setCheckable(true);
            chip.setChecked("Tất cả".equals(zone));
            chip.setChipBackgroundColorResource(R.color.chip_state_color);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_state_color));
            chipGroupZone.addView(chip);
        }
    }

    private void showReleaseTableDialog(String tableId, TableModel table) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Trả bàn — " + table.getName())
                .setMessage("Bàn đang được đánh dấu là đang dùng.\n"
                        + "Bạn có muốn trả bàn về trạng thái trống không?")
                .setPositiveButton("Trả bàn ✓",
                        (d, w) -> viewModel.releaseTable(tableId, table.getName()))
                .setNegativeButton("Hủy", null)
                .show();
    }
}
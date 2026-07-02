package com.example.cafemanagement.view.cashier;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.model.TableModel;
import com.example.cafemanagement.view.cashier.adapter.TableManagementAdapter;
import com.example.cafemanagement.viewmodel.TableManagementViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class TableManagementFragment extends Fragment {

    private TableManagementViewModel viewModel;

    private ChipGroup              chipGroupZone;
    private RecyclerView           rvTables;
    private TextView               tvSummary;
    private TableManagementAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_table_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TableManagementViewModel.class);

        chipGroupZone = view.findViewById(R.id.chip_group_zone);
        rvTables      = view.findViewById(R.id.rv_tables);
        tvSummary     = view.findViewById(R.id.tv_table_summary);

        adapter = new TableManagementAdapter(
                new ArrayList<>(), new ArrayList<>(),
                this::onReleaseTableClick);

        rvTables.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvTables.setAdapter(adapter);

        chipGroupZone.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) viewModel.filterByZone(chip.getText().toString());
            }
        });

        setupObservers();

        if (savedInstanceState == null) viewModel.loadTables();
    }

    private void setupObservers() {
        viewModel.getDisplayedTables().observe(getViewLifecycleOwner(), tables ->
                adapter.updateData(tables,
                        viewModel.getDisplayedIds().getValue() != null
                                ? viewModel.getDisplayedIds().getValue() : new ArrayList<>()));

        viewModel.getDisplayedIds().observe(getViewLifecycleOwner(), ids ->
                adapter.updateData(
                        viewModel.getDisplayedTables().getValue() != null
                                ? viewModel.getDisplayedTables().getValue() : new ArrayList<>(),
                        ids));

        viewModel.getZoneList().observe(getViewLifecycleOwner(), this::buildZoneChips);

        viewModel.getSummary().observe(getViewLifecycleOwner(),
                text -> tvSummary.setText(text));

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    // --- Dialog (UI only) ---

    private void onReleaseTableClick(String tableId, TableModel table) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Trả bàn — " + table.getName())
                .setMessage("Xác nhận trả " + table.getName() + " về trạng thái trống?\n\n"
                        + "Lưu ý: chỉ thực hiện khi khách đã rời bàn và đơn đã được thanh toán.")
                .setPositiveButton("Trả bàn ✓",
                        (d, w) -> viewModel.releaseTable(tableId, table))
                .setNegativeButton("Hủy", null)
                .show();
    }

    // --- UI helpers ---

    private void buildZoneChips(List<String> zones) {
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
}
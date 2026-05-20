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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.CartManager;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.TableModel;
import com.example.cafemanagement.view.CashierActivity;
import com.example.cafemanagement.view.cashier.adapter.TableAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SelectTableFragment extends Fragment {

    private ChipGroup    chipGroupZone;
    private RecyclerView rvTables;
    private Button       btnContinue;
    private TableAdapter tableAdapter;

    private final Map<String, TableModel> allTables       = new LinkedHashMap<>();
    private final List<String>            displayedIds    = new ArrayList<>();
    private final List<TableModel>        displayedTables = new ArrayList<>();

    private String  selectedTableId   = null;
    private String  selectedTableName = null;
    private String  currentZone       = "Tất cả";
    private boolean tableWasOccupied  = false; // đã set OCCUPIED chưa

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chipGroupZone = view.findViewById(R.id.chip_group_zone);
        rvTables      = view.findViewById(R.id.rv_tables);
        btnContinue   = view.findViewById(R.id.btn_continue_to_menu);

        // TableAdapter với cả short click và long click
        tableAdapter = new TableAdapter(
                displayedTables,
                displayedIds,
                // Short click
                (tableId, table) -> {
                    if ("OCCUPIED".equals(table.getStatus())) {
                        Toast.makeText(requireContext(),
                                "Bàn đang dùng — giữ để trả bàn",
                                Toast.LENGTH_SHORT).show();
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
                    if ("OCCUPIED".equals(table.getStatus())) {
                        showReleaseTableDialog(tableId, table);
                    }
                }
        );

        rvTables.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvTables.setAdapter(tableAdapter);

        btnContinue.setEnabled(false);
        btnContinue.setOnClickListener(v -> confirmTableSelection());

        loadTables();

        chipGroupZone.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    currentZone = chip.getText().toString();
                    filterByZone(currentZone);
                }
            }
        });

        // ── Fix back crash ────────────────────────────────────────────────────
        // Dùng OnBackPressedCallback thay vì onBackPressed() bị deprecated
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Nếu đã set OCCUPIED mà chưa sang menu → giải phóng bàn
                        if (tableWasOccupied && selectedTableId != null) {
                            FirebaseHelper.getTablesRef()
                                    .child(selectedTableId)
                                    .child("status").setValue("AVAILABLE");
                            tableWasOccupied = false;
                        }
                        setEnabled(false);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });
    }

    // ── Xác nhận chọn bàn → set OCCUPIED → navigate Menu ────────────────────

    private void confirmTableSelection() {
        if (selectedTableId == null) return;

        btnContinue.setEnabled(false);
        btnContinue.setText("Đang xử lý...");

        FirebaseHelper.getTablesRef()
                .child(selectedTableId)
                .child("status").setValue("OCCUPIED")
                .addOnSuccessListener(unused -> {
                    if (!isAdded()) return;
                    tableWasOccupied = true;
                    CartManager.getInstance().setTable(selectedTableId, selectedTableName);
                    ((CashierActivity) requireActivity())
                            .navigateTo(CashierActivity.SCREEN_MENU, null, true);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    btnContinue.setEnabled(true);
                    btnContinue.setText("Tiếp tục — " + selectedTableName);
                    Toast.makeText(requireContext(),
                            "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ── Long press: dialog trả bàn ────────────────────────────────────────────

    private void showReleaseTableDialog(String tableId, TableModel table) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Trả bàn — " + table.getName())
                .setMessage("Bàn đang được đánh dấu là đang dùng.\nBạn có muốn trả bàn về trạng thái trống không?")
                .setPositiveButton("Trả bàn ✓", (d, w) -> releaseTable(tableId, table.getName()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void releaseTable(String tableId, String tableName) {
        FirebaseHelper.getTablesRef().child(tableId)
                .child("status").setValue("AVAILABLE")
                .addOnSuccessListener(u -> {
                    if (!isAdded()) return;
                    FirebaseHelper.getTablesRef().child(tableId)
                            .child("currentOrderId").setValue(null);
                    Toast.makeText(requireContext(),
                            tableName + " đã trả về trống ✓", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ── Load & filter ─────────────────────────────────────────────────────────

    private void loadTables() {
        FirebaseHelper.getTablesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                allTables.clear();
                List<String> zones = new ArrayList<>();
                zones.add("Tất cả");

                for (DataSnapshot ds : snapshot.getChildren()) {
                    TableModel t = ds.getValue(TableModel.class);
                    if (t != null) {
                        allTables.put(ds.getKey(), t);
                        if (t.getZone() != null && !zones.contains(t.getZone()))
                            zones.add(t.getZone());
                    }
                }

                buildZoneChips(zones);
                filterByZone(currentZone);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void buildZoneChips(List<String> zones) {
        chipGroupZone.removeAllViews();
        for (String zone : zones) {
            Chip chip = new Chip(requireContext());
            chip.setText(zone);
            chip.setCheckable(true);
            chip.setChecked(zone.equals(currentZone));
            chip.setChipBackgroundColorResource(R.color.chip_state_color);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_state_color));
            chipGroupZone.addView(chip);
        }
    }

    private void filterByZone(String zone) {
        displayedIds.clear();
        displayedTables.clear();
        for (Map.Entry<String, TableModel> e : allTables.entrySet()) {
            if ("Tất cả".equals(zone) || zone.equals(e.getValue().getZone())) {
                displayedIds.add(e.getKey());
                displayedTables.add(e.getValue());
            }
        }
        tableAdapter.notifyDataSetChanged();
    }
}
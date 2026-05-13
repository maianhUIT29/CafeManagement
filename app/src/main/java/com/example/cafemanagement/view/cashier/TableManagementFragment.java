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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.TableModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TableManagementFragment extends Fragment {

    private ChipGroup chipGroupZone;
    private RecyclerView rvTables;
    private TextView tvSummary;

    private final Map<String, TableModel> allTables       = new LinkedHashMap<>();
    private final List<String>            displayedIds    = new ArrayList<>();
    private final List<TableModel>        displayedTables = new ArrayList<>();

    private String currentZone = "Tất cả";
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

        chipGroupZone = view.findViewById(R.id.chip_group_zone);
        rvTables      = view.findViewById(R.id.rv_tables);
        tvSummary     = view.findViewById(R.id.tv_table_summary);

        adapter = new TableManagementAdapter(
                displayedTables,
                displayedIds,
                this::onReleaseTableClick   // callback nút "Trả bàn"
        );

        rvTables.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvTables.setAdapter(adapter);

        chipGroupZone.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    currentZone = chip.getText().toString();
                    filterByZone(currentZone);
                }
            }
        });

        loadTables();
    }

    // ── Callback nút Trả bàn ─────────────────────────────────────────────────

    private void onReleaseTableClick(String tableId, TableModel table) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Trả bàn — " + table.getName())
                .setMessage("Xác nhận trả " + table.getName() + " về trạng thái trống?\n\n"
                        + "Lưu ý: chỉ thực hiện khi khách đã rời bàn và đơn đã được thanh toán.")
                .setPositiveButton("Trả bàn ✓", (d, w) -> releaseTable(tableId, table))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void releaseTable(String tableId, TableModel table) {
        // Tìm đơn PAID của bàn này và chuyển sang COMPLETE
        String currentOrderId = table.getCurrentOrderId();
        if (currentOrderId != null) {
            FirebaseHelper.getOrdersRef().child(currentOrderId)
                    .child("status").setValue("COMPLETE");
        }

        // Bàn → AVAILABLE
        FirebaseHelper.getTablesRef().child(tableId)
                .child("status").setValue("AVAILABLE")
                .addOnSuccessListener(u -> {
                    if (!isAdded()) return;
                    FirebaseHelper.getTablesRef().child(tableId)
                            .child("currentOrderId").setValue(null);
                    Toast.makeText(requireContext(),
                            "🪑 " + table.getName() + " đã trống ✓",
                            Toast.LENGTH_SHORT).show();
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
                updateSummary();
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
        adapter.notifyDataSetChanged();
    }

    private void updateSummary() {
        int total    = allTables.size();
        int occupied = 0;
        for (TableModel t : allTables.values()) {
            if ("OCCUPIED".equals(t.getStatus())) occupied++;
        }
        tvSummary.setText(occupied + "/" + total + " bàn đang dùng");
    }

    // ════════════════════════════════════════════════════════════════════════
    // Inner Adapter — mỗi ô bàn lớn hơn, hiện nút "Trả bàn" khi OCCUPIED
    // ════════════════════════════════════════════════════════════════════════

    public static class TableManagementAdapter
            extends RecyclerView.Adapter<TableManagementAdapter.VH> {

        public interface OnRelease { void onRelease(String tableId, TableModel table); }

        private final List<TableModel> tables;
        private final List<String>     ids;
        private final OnRelease        onRelease;

        public TableManagementAdapter(List<TableModel> tables, List<String> ids,
                                      OnRelease onRelease) {
            this.tables    = tables;
            this.ids       = ids;
            this.onRelease = onRelease;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_table_management, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            TableModel t  = tables.get(pos);
            String     id = ids.get(pos);

            boolean isOccupied = "OCCUPIED".equals(t.getStatus());

            h.tvName.setText(t.getName());
            h.tvZone.setText(t.getZone() != null ? t.getZone() : "");

            if (isOccupied) {
                h.tvStatus.setText("🔴 Đang dùng");
                h.tvStatus.setTextColor(0xFFE53935);
                h.itemView.setBackgroundResource(R.drawable.bg_table_occupied);
                h.btnRelease.setVisibility(View.VISIBLE);
                h.btnRelease.setOnClickListener(v -> onRelease.onRelease(id, t));
            } else {
                h.tvStatus.setText("🟢 Trống");
                h.tvStatus.setTextColor(0xFF2D9B5A);
                h.itemView.setBackgroundResource(R.drawable.bg_table_available);
                h.btnRelease.setVisibility(View.GONE);
            }
        }

        @Override public int getItemCount() { return tables.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvZone, tvStatus;
            android.widget.Button btnRelease;

            VH(View v) {
                super(v);
                tvName     = v.findViewById(R.id.tv_table_name);
                tvZone     = v.findViewById(R.id.tv_table_management_zone); // id mới
                tvStatus   = v.findViewById(R.id.tv_table_status);
                btnRelease = v.findViewById(R.id.btn_table_release);        // id mới
            }
        }
    }
}
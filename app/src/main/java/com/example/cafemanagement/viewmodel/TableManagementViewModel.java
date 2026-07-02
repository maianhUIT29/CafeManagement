package com.example.cafemanagement.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.TableModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TableManagementViewModel extends ViewModel {

    // --- LiveData ---
    private final MutableLiveData<List<TableModel>> displayedTables = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>>     displayedIds    = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>>     zoneList        = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String>           summary         = new MutableLiveData<>();
    private final MutableLiveData<String>           toastMessage    = new MutableLiveData<>();

    // --- State nội bộ ---
    private final Map<String, TableModel> allTables = new LinkedHashMap<>();
    private String currentZone = "Tất cả";
    private ValueEventListener tablesListener;

    // --- Getters ---
    public LiveData<List<TableModel>> getDisplayedTables() { return displayedTables; }
    public LiveData<List<String>>     getDisplayedIds()    { return displayedIds; }
    public LiveData<List<String>>     getZoneList()        { return zoneList; }
    public LiveData<String>           getSummary()         { return summary; }
    public LiveData<String>           getToastMessage()    { return toastMessage; }

    // --- Load tables (realtime) ---
    public void loadTables() {
        tablesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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

                zoneList.setValue(zones);
                filterByZone(currentZone);
                updateSummary();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        FirebaseHelper.getTablesRef().addValueEventListener(tablesListener);
    }

    // --- Filter ---
    public void filterByZone(String zone) {
        currentZone = zone;
        List<String>     ids    = new ArrayList<>();
        List<TableModel> tables = new ArrayList<>();
        for (Map.Entry<String, TableModel> e : allTables.entrySet()) {
            if ("Tất cả".equals(zone) || zone.equals(e.getValue().getZone())) {
                ids.add(e.getKey());
                tables.add(e.getValue());
            }
        }
        displayedIds.setValue(ids);
        displayedTables.setValue(tables);
    }

    private void updateSummary() {
        int total    = allTables.size();
        int occupied = 0;
        for (TableModel t : allTables.values())
            if ("OCCUPIED".equals(t.getStatus())) occupied++;
        summary.setValue(occupied + "/" + total + " bàn đang dùng");
    }

    // --- Trả bàn ---
    public void releaseTable(String tableId, TableModel table) {
        // Đơn PAID → COMPLETE
        String currentOrderId = table.getCurrentOrderId();
        if (currentOrderId != null) {
            FirebaseHelper.getOrdersRef().child(currentOrderId)
                    .child("status").setValue("COMPLETE");
        }

        // Bàn → AVAILABLE
        FirebaseHelper.getTablesRef().child(tableId)
                .child("status").setValue("AVAILABLE")
                .addOnSuccessListener(u -> {
                    FirebaseHelper.getTablesRef().child(tableId)
                            .child("currentOrderId").setValue(null);
                    toastMessage.setValue("🪑 " + table.getName() + " đã trống ✓");
                })
                .addOnFailureListener(e ->
                        toastMessage.setValue("Lỗi: " + e.getMessage()));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (tablesListener != null)
            FirebaseHelper.getTablesRef().removeEventListener(tablesListener);
    }
}
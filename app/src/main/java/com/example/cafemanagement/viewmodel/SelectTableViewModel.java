package com.example.cafemanagement.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.helper.CartManager;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.TableModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SelectTableViewModel extends ViewModel {

    // --- LiveData ---
    private final MutableLiveData<List<TableModel>> displayedTables = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>>     displayedIds    = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>>     zoneList        = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String>           toastMessage    = new MutableLiveData<>();
    private final MutableLiveData<TableSelectResult> selectResult   = new MutableLiveData<>();

    // --- State nội bộ ---
    private final Map<String, TableModel> allTables   = new LinkedHashMap<>();
    private String currentZone = "Tất cả";
    private ValueEventListener tablesListener;

    // --- Getters ---
    public LiveData<List<TableModel>>  getDisplayedTables() { return displayedTables; }
    public LiveData<List<String>>      getDisplayedIds()    { return displayedIds; }
    public LiveData<List<String>>      getZoneList()        { return zoneList; }
    public LiveData<String>            getToastMessage()    { return toastMessage; }
    public LiveData<TableSelectResult> getSelectResult()    { return selectResult; }

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

    // --- Xác nhận chọn bàn → set OCCUPIED ---
    public void confirmTableSelection(String tableId, String tableName) {
        selectResult.setValue(TableSelectResult.loading());
        FirebaseHelper.getTablesRef()
                .child(tableId).child("status").setValue("OCCUPIED")
                .addOnSuccessListener(unused -> {
                    CartManager.getInstance().setTable(tableId, tableName);
                    selectResult.setValue(TableSelectResult.success(tableId, tableName));
                })
                .addOnFailureListener(e ->
                        selectResult.setValue(TableSelectResult.error(e.getMessage())));
    }

    // --- Giải phóng bàn khi back (chưa sang Menu) ---
    public void releaseTableOnBack(String tableId) {
        FirebaseHelper.getTablesRef()
                .child(tableId).child("status").setValue("AVAILABLE");
    }

    // --- Long press: trả bàn về AVAILABLE ---
    public void releaseTable(String tableId, String tableName) {
        FirebaseHelper.getTablesRef().child(tableId)
                .child("status").setValue("AVAILABLE")
                .addOnSuccessListener(u -> {
                    FirebaseHelper.getTablesRef().child(tableId)
                            .child("currentOrderId").setValue(null);
                    toastMessage.setValue(tableName + " đã trả về trống ✓");
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

    // --- Result wrapper ---
    public static class TableSelectResult {
        public enum State { LOADING, SUCCESS, ERROR }

        public final State  state;
        public final String tableId;
        public final String tableName;
        public final String errorMessage;

        private TableSelectResult(State state, String tableId,
                                  String tableName, String errorMessage) {
            this.state        = state;
            this.tableId      = tableId;
            this.tableName    = tableName;
            this.errorMessage = errorMessage;
        }

        public static TableSelectResult loading() {
            return new TableSelectResult(State.LOADING, null, null, null);
        }
        public static TableSelectResult success(String tableId, String tableName) {
            return new TableSelectResult(State.SUCCESS, tableId, tableName, null);
        }
        public static TableSelectResult error(String message) {
            return new TableSelectResult(State.ERROR, null, null, message);
        }
    }
}
package com.example.cafemanagement.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.TableModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TableRepository {
    private final DatabaseReference tablesRef;

    public TableRepository() {
        this.tablesRef = FirebaseHelper.getDatabaseInstance().getReference("Tables");
    }

    public MutableLiveData<List<TableModel>> getAllTables() {
        MutableLiveData<List<TableModel>> tableLiveData = new MutableLiveData<>();

        tablesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<TableModel> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    TableModel table = ds.getValue(TableModel.class);
                    if (table != null) {
                        table.setTableId(ds.getKey());
                        list.add(table);
                    }
                }
                tableLiveData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tableLiveData.setValue(null);
            }
        });

        return tableLiveData;
    }
}
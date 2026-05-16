package com.example.cafemanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.model.TableModel;
import com.example.cafemanagement.repository.RepositoryCallback;
import com.example.cafemanagement.repository.TableRepository;

import java.util.List;

public class AdminTableViewModel extends ViewModel {
    private final TableRepository tableRepository = new TableRepository();

    public LiveData<List<TableModel>> getTables() {
        return tableRepository.getAllTables();
    }

    public void saveTable(TableModel table, boolean isEdit, RepositoryCallback callback) {
        if (isEdit) {
            tableRepository.updateTable(table, callback);
        } else {
            tableRepository.addTable(table, callback);
        }
    }

    public void deleteTable(String tableId, RepositoryCallback callback) {
        tableRepository.deleteTable(tableId, callback);
    }
}
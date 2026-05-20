package com.example.cafemanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.model.TableModel;
import com.example.cafemanagement.repository.TableRepository;

import java.util.List;

public class OrderSetupViewModel extends ViewModel {
    private final TableRepository repository;
    private final LiveData<List<TableModel>> tableList;

    private final MutableLiveData<Boolean> isDineIn = new MutableLiveData<>(false);
    private final MutableLiveData<TableModel> selectedTable = new MutableLiveData<>();

    public OrderSetupViewModel() {
        repository = new TableRepository();
        tableList = repository.getAllTables();
    }

    public LiveData<List<TableModel>> getTableList() {
        return tableList;
    }

    public LiveData<Boolean> getIsDineIn() {
        return isDineIn;
    }

    public void setServiceType(boolean dineIn) {
        isDineIn.setValue(dineIn);
        if (!dineIn) {
            selectedTable.setValue(null);
        }
    }

    public LiveData<TableModel> getSelectedTable() {
        return selectedTable;
    }

    public void setSelectedTable(TableModel table) {
        selectedTable.setValue(table);
    }
}

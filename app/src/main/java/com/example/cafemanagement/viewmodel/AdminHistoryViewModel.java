package com.example.cafemanagement.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.model.HistoryRevenueModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminHistoryViewModel extends ViewModel {

    private final DatabaseReference historyRef;
    private final MutableLiveData<List<HistoryRevenueModel>> historyListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public AdminHistoryViewModel() {
        historyRef = FirebaseDatabase.getInstance().getReference("HistoryRevenue");
    }

    public LiveData<List<HistoryRevenueModel>> getHistoryList() {
        return historyListLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void loadHistoryData() {
        isLoadingLiveData.setValue(true);

        historyRef.orderByChild("openedAt").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<HistoryRevenueModel> list = new ArrayList<>();
                for (DataSnapshot doc : snapshot.getChildren()) {
                    if (doc.hasChild("status")) {
                        HistoryRevenueModel model = doc.getValue(HistoryRevenueModel.class);
                        if (model != null) {
                            list.add(model);
                        }
                    }
                }

                Collections.reverse(list);

                historyListLiveData.postValue(list);
                isLoadingLiveData.postValue(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorLiveData.postValue(error.getMessage());
                isLoadingLiveData.postValue(false);
            }
        });
    }
}
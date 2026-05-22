package com.example.cafemanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.repository.ShiftRepository;

public class CashierShiftViewModel extends ViewModel {

    private final ShiftRepository repository;
    private final MutableLiveData<String> actionStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);

    public CashierShiftViewModel() {
        repository = new ShiftRepository();
    }

    public LiveData<String> getActionStatus() { return actionStatusLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoadingLiveData; }

    public void endShift(String historyId, String shiftConfigId, double currentShiftRevenue) {
        if (historyId == null || historyId.isEmpty()) {
            actionStatusLiveData.setValue("Lỗi: Không tìm thấy phiên làm việc");
            return;
        }

        isLoadingLiveData.setValue(true);
        repository.closeShiftWithCashLogic(historyId, shiftConfigId, currentShiftRevenue, new ShiftRepository.ShiftCloseCallback() {
            @Override
            public void onSuccess(String message) {
                isLoadingLiveData.setValue(false);
                actionStatusLiveData.setValue(message);
            }

            @Override
            public void onError(String error) {
                isLoadingLiveData.setValue(false);
                actionStatusLiveData.setValue(error);
            }
        });
    }
}
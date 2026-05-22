package com.example.cafemanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.model.StaffSalaryModel;
import com.example.cafemanagement.repository.SalaryRepository;

import java.util.List;

public class AdminSalaryViewModel extends ViewModel {

    private final SalaryRepository repository;
    private final MutableLiveData<List<StaffSalaryModel>> salaryListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public AdminSalaryViewModel() {
        repository = new SalaryRepository();
    }

    public LiveData<List<StaffSalaryModel>> getSalaryList() {
        return salaryListLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void loadMonthlySalary(int month, int year) {
        isLoadingLiveData.setValue(true);
        repository.calculateMonthlySalary(month, year, new SalaryRepository.SalaryCalculateCallback() {
            @Override
            public void onCalculated(List<StaffSalaryModel> salaryList) {
                salaryListLiveData.postValue(salaryList);
                isLoadingLiveData.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue(error);
                isLoadingLiveData.postValue(false);
            }
        });
    }
}
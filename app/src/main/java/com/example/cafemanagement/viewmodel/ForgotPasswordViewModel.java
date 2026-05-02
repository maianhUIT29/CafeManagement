package com.example.cafemanagement.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafemanagement.repository.AuthRepository;

public class ForgotPasswordViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<String> resetStatus = new MutableLiveData<>();

    public ForgotPasswordViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();
    }

    public LiveData<String> getResetStatus() {
        return resetStatus;
    }

    public void sendResetEmail(String email) {
        authRepository.forgotPassword(email, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String role) { // Cập nhật: Thêm String role
                resetStatus.setValue("SUCCESS");
            }

            @Override
            public void onError(String message) {
                resetStatus.setValue(message);
            }
        });
    }
}
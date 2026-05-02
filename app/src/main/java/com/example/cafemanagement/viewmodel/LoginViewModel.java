package com.example.cafemanagement.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafemanagement.repository.AuthRepository;

public class LoginViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<String> authStatus = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();
    }

    public LiveData<String> getAuthStatus() {
        return authStatus;
    }

    public void login(String phone, String password) {
        authRepository.login(phone, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String role) {
                // Trả về role (admin, cashier, barista, customer) để Activity điều hướng
                authStatus.setValue(role);
            }

            @Override
            public void onError(String message) {
                // Thêm tiền tố "ERROR: " để Activity nhận biết đây là thông báo lỗi
                authStatus.setValue("ERROR: " + message);
            }
        });
    }

    public void onGoogleAuthSuccess(String idToken) {
        authRepository.firebaseAuthWithGoogle(idToken, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String role) {
                // Đã cập nhật: Google Auth giờ cũng trả về role từ Database
                authStatus.setValue(role);
            }

            @Override
            public void onError(String message) {
                // Đồng bộ cấu trúc thông báo lỗi
                authStatus.setValue("ERROR: " + message);
            }
        });
    }
}
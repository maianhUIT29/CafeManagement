package com.example.cafemanagement.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafemanagement.model.UserModel;
import com.example.cafemanagement.repository.AuthRepository;

public class RegisterViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<String> registerStatus = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();
    }

    public LiveData<String> getRegisterStatus() {
        return registerStatus;
    }

    public void register(String name, String phone, String email, String password) {
        UserModel newUser = new UserModel();
        newUser.setName(name);
        newUser.setPhone(phone);
        newUser.setEmail(email);
        newUser.setRole("customer");
        newUser.setPoints(0);

        authRepository.registerUser(newUser, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String role) { // Cập nhật: Thêm String role
                registerStatus.setValue("SUCCESS");
            }

            @Override
            public void onError(String message) {
                registerStatus.setValue(message);
            }
        });
    }
}
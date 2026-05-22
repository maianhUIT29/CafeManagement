package com.example.cafemanagement.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafemanagement.repository.AuthRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
                authStatus.setValue(role);
            }

            @Override
            public void onError(String message) {
                authStatus.setValue("ERROR: " + message);
            }
        });
    }

    public void onGoogleAuthSuccess(String idToken) {
        authRepository.firebaseAuthWithGoogle(idToken, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String role) {
                authStatus.setValue(role);
            }

            @Override
            public void onError(String message) {
                authStatus.setValue("ERROR: " + message);
            }
        });
    }

    /**
     * Kiểm tra xem người dùng đã đăng nhập chưa và lấy Role
     */
    public void checkCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            authRepository.fetchUserRole(user.getUid(), new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(String role) {
                    authStatus.setValue(role);
                }

                @Override
                public void onError(String message) {
                    authStatus.setValue("ERROR: " + message);
                }
            });
        }
    }
}

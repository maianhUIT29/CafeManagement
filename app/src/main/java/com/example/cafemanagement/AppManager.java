package com.example.cafemanagement;

import android.app.Application;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;

public class AppManager extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Khởi tạo Firebase Persistence (cho phép dùng offline)
        FirebaseHelper.initPersistence();
        
        // Thiết lập ngôn ngữ để khử cảnh báo X-Firebase-Locale trong Logcat
        FirebaseAuth.getInstance().setLanguageCode("vi");
    }
}

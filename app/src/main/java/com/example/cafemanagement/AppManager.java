package com.example.cafemanagement;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class AppManager extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
        }
    }
}
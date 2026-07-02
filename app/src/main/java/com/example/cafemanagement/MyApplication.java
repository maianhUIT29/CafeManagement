package com.example.cafemanagement;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    private static final String DATABASE_URL =
            "https://cafemanagement-5e518-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance(DATABASE_URL).setPersistenceEnabled(true);
    }
}
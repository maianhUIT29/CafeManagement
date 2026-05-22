package com.example.cafemanagement.helper;

import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Kết nối Firebase Realtime Database thống nhất — mọi repository phải dùng lớp này.
 */
public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";

    /** URL phải trùng firebase_url trong google-services.json */
    public static final String DATABASE_URL =
            "https://cafemanagement-5e518-default-rtdb.asia-southeast1.firebasedatabase.app";

    private static FirebaseDatabase database;
    private static boolean persistenceConfigured;

    private FirebaseHelper() {
    }

    /**
     * Gọi một lần trong Application.onCreate() trước khi đọc/ghi dữ liệu.
     */
    public static void initPersistence() {
        if (persistenceConfigured) return;
        try {
            FirebaseDatabase.getInstance(DATABASE_URL).setPersistenceEnabled(true);
            Log.d(TAG, "Persistence enabled for " + DATABASE_URL);
        } catch (Exception e) {
            Log.w(TAG, "Persistence already enabled or unavailable: " + e.getMessage());
        }
        persistenceConfigured = true;
    }

    public static FirebaseDatabase getDatabaseInstance() {
        if (database == null) {
            database = FirebaseDatabase.getInstance(DATABASE_URL);
public class FirebaseHelper {

    private static final String DATABASE_URL =
            "https://cafemanagement-5e518-default-rtdb.asia-southeast1.firebasedatabase.app/";

    public static FirebaseDatabase getDatabaseInstance() {
        return FirebaseDatabase.getInstance(DATABASE_URL);
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance(DATABASE_URL);
            // ĐÃ XÓA LỆNH setPersistenceEnabled Ở ĐÂY
        }
        return database;
    }

    public static DatabaseReference getCategoriesRef() {
        return getDatabaseInstance().getReference("Categories");
    }

    public static DatabaseReference getProductsRef() {
        return getDatabaseInstance().getReference("Products");
    }

    public static DatabaseReference getTablesRef() {
        return getDatabaseInstance().getReference("Tables");
    }

    public static DatabaseReference getOrdersRef() {
        return getDatabaseInstance().getReference("Orders");
    }

    public static DatabaseReference getVouchersRef() {
        return getDatabaseInstance().getReference("Vouchers");
    }

    public static DatabaseReference getHistoryRevenueRef() {
        return getDatabaseInstance().getReference("HistoryRevenue");
    }

    public static DatabaseReference getUsersRef() {
        return getDatabaseInstance().getReference("Users");
    }

    public static DatabaseReference getTablesRef() {
        return getDatabaseInstance().getReference("Tables");
    }

    public static void logDatabaseError(String source, DatabaseError error) {
        if (error != null) {
            Log.e(TAG, source + " — " + error.getCode() + ": " + error.getMessage());
        }
    }
}
    public static DatabaseReference getShiftConfigRef() {
        return getDatabaseInstance().getReference("ShiftConfig");
    }

    // ── MỚI: Ca làm việc đang mở ──────────────────────────────────────────────
    // Key = cashierId, mỗi cashier chỉ có 1 ca tại 1 thời điểm
    public static DatabaseReference getActiveShiftsRef() {
        return getDatabaseInstance().getReference("ActiveShifts");
    }
}

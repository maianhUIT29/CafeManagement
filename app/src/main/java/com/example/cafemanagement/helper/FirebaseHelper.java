package com.example.cafemanagement.helper;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {

    private static final String DATABASE_URL =
            "https://cafemanagement-5e518-default-rtdb.asia-southeast1.firebasedatabase.app/";

    public static FirebaseDatabase getDatabaseInstance() {
        return FirebaseDatabase.getInstance(DATABASE_URL);
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

    public static DatabaseReference getShiftConfigRef() {
        return getDatabaseInstance().getReference("ShiftConfig");
    }

    // ── MỚI: Ca làm việc đang mở ──────────────────────────────────────────────
    // Key = cashierId, mỗi cashier chỉ có 1 ca tại 1 thời điểm
    public static DatabaseReference getActiveShiftsRef() {
        return getDatabaseInstance().getReference("ActiveShifts");
    }
}
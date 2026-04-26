package com.example.cafemanagement.helper;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Lớp hỗ trợ quản lý kết nối Firebase tập trung.
 * Việc tách biệt này giúp MainActivity không cần quan tâm đến URL hay cấu hình kỹ thuật của Database.
 */
public class FirebaseHelper {

    // Đường dẫn Realtime Database của bạn tại khu vực Singapore
    private static final String DATABASE_URL = "https://cafemanagement-5e518-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static FirebaseDatabase mDatabase;

    /**
     * Khởi tạo và trả về instance của FirebaseDatabase.
     * Sử dụng cơ chế kiểm tra đơn lẻ (Singleton) để tối ưu hiệu năng.
     */
    public static FirebaseDatabase getDatabaseInstance() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance(DATABASE_URL);
            // Cho phép lưu trữ dữ liệu ngoại tuyến để ứng dụng mượt mà hơn
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    /**
     * Trả về tham chiếu đến bảng Categories (Danh mục)
     */
    public static DatabaseReference getCategoriesRef() {
        return getDatabaseInstance().getReference("Categories");
    }

    /**
     * Trả về tham chiếu đến bảng Products (Sản phẩm/Món ăn)
     */
    public static DatabaseReference getProductsRef() {
        return getDatabaseInstance().getReference("Products");
    }
}
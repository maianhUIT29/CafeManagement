package com.example.cafemanagement.repository;

import androidx.annotation.NonNull;

import com.example.cafemanagement.model.UserModel;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AuthRepository {
    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");
    }

    // Cập nhật interface để trả về String role khi thành công
    public interface AuthCallback {
        void onSuccess(String role);
        void onError(String message);
    }

    /**
     * Đăng ký: Trả về role của user vừa tạo
     */
    public void registerUser(UserModel user, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String uid = task.getResult().getUser().getUid();
                        mDatabase.child(uid).setValue(user)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        // Đăng ký xong trả về role trong model (thường là customer)
                                        callback.onSuccess(user.getRole());
                                    } else {
                                        callback.onError("Lỗi lưu Database: " + dbTask.getException().getMessage());
                                    }
                                });
                    } else {
                        callback.onError(task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại");
                    }
                });
    }

    /**
     * Đăng nhập: Lấy UID -> Auth -> Lấy Role từ Database
     */
    public void login(String phone, String password, AuthCallback callback) {
        mDatabase.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String realEmail = "";
                    String userId = "";
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        realEmail = userSnap.child("email").getValue(String.class);
                        userId = userSnap.getKey(); // Lấy UID của user này
                    }

                    if (realEmail != null && !realEmail.isEmpty()) {
                        String finalUserId = userId;
                        mAuth.signInWithEmailAndPassword(realEmail, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Sau khi đăng nhập thành công, đi lấy role
                                        fetchUserRole(finalUserId, callback);
                                    } else {
                                        callback.onError("Mật khẩu không chính xác");
                                    }
                                });
                    }
                } else {
                    callback.onError("Số điện thoại này chưa được đăng ký");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Lỗi kết nối CSDL: " + error.getMessage());
            }
        });
    }

    /**
     * Hàm phụ: Truy cập trực tiếp vào node của User để lấy Role
     */
    private void fetchUserRole(String uid, AuthCallback callback) {
        mDatabase.child(uid).child("role").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String role = task.getResult().getValue(String.class);
                if (role == null) role = "customer"; // Mặc định nếu không tìm thấy
                callback.onSuccess(role);
            } else {
                callback.onError("Không thể lấy quyền hạn người dùng");
            }
        });
    }

    /**
     * Quên mật khẩu: Trả về null cho role vì không cần chuyển hướng role
     */
    public void forgotPassword(String email, AuthCallback callback) {
        if (email == null || email.isEmpty()) {
            callback.onError("Email không được để trống");
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(task.getException() != null ? task.getException().getMessage() : "Không thể gửi email");
                    }
                });
    }

    /**
     * Đăng nhập Google: Cũng cần lấy Role sau khi login thành công
     */
    public void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String uid = task.getResult().getUser().getUid();
                        fetchUserRole(uid, callback);
                    } else {
                        callback.onError("Lỗi xác thực Google");
                    }
                });
    }
}
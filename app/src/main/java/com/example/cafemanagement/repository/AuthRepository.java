package com.example.cafemanagement.repository;

import androidx.annotation.NonNull;

import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.UserModel;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class AuthRepository {

    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseHelper.getUsersRef();
    }

    public interface AuthCallback {
        void onSuccess(String role);
        void onError(String message);
    }
public void registerUser(UserModel user, String password, AuthCallback callback) {
    if (user == null) {
        callback.onError("Thông tin người dùng không hợp lệ");
        return;
    }

    String email = user.getEmail();

    if (email == null || email.trim().isEmpty()) {
        callback.onError("Email không được để trống");
        return;
    }

    if (password == null || password.trim().isEmpty()) {
        callback.onError("Mật khẩu không được để trống");
        return;
    }

    mAuth.createUserWithEmailAndPassword(email.trim(), password.trim())
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                    String uid = task.getResult().getUser().getUid();

                    user.setUserId(uid);

                    mDatabase.child(uid).setValue(user)
                            .addOnSuccessListener(unused -> callback.onSuccess(user.getRole()))
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                } else {
                    String message = "Đăng ký thất bại";

                    if (task.getException() != null) {
                        message = task.getException().getMessage();
                    }

                    callback.onError(message);
                }
            });
}

    public void login(String phone, String password, AuthCallback callback) {
        if (phone == null || phone.trim().isEmpty()) {
            callback.onError("Vui lòng nhập số điện thoại");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            callback.onError("Vui lòng nhập mật khẩu");
            return;
        }

        String normalizedPhone = phone.trim();

        mDatabase.orderByChild("phone")
                .equalTo(normalizedPhone)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onError("Số điện thoại này chưa được đăng ký");
                            return;
                        }

                        String realEmail = null;

                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            realEmail = userSnap.child("email").getValue(String.class);
                            break;
                        }

                        if (realEmail == null || realEmail.trim().isEmpty()) {
                            callback.onError("Tài khoản này chưa có email để đăng nhập");
                            return;
                        }

                        signInWithEmail(realEmail.trim(), password, callback);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        FirebaseHelper.logDatabaseError("AuthRepository.login", error);
                        callback.onError("Lỗi kết nối Firebase: " + error.getMessage());
                    }
                });
    }

    private void signInWithEmail(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Đăng nhập thất bại";
                        callback.onError(error);
                        return;
                    }

                    FirebaseUser currentUser = mAuth.getCurrentUser();

                    if (currentUser == null) {
                        callback.onError("Đăng nhập thành công nhưng không lấy được thông tin người dùng");
                        return;
                    }

                    fetchUserRole(currentUser.getUid(), callback);
                });
    }

    /**
     * Lấy role của user từ Realtime Database.
     */
    public void fetchUserRole(String uid, AuthCallback callback) {
        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("UID người dùng không hợp lệ");
            return;
        }

        mDatabase.child(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onError("Không thể lấy thông tin người dùng");
                        return;
                    }

                    DataSnapshot snapshot = task.getResult();

                    if (snapshot == null || !snapshot.exists()) {
                        callback.onError("Không tìm thấy thông tin người dùng trong Database");
                        return;
                    }

                    String role = snapshot.child("role").getValue(String.class);

                    if (role == null || role.trim().isEmpty()) {
                        role = "customer";
                    }

                    callback.onSuccess(role.trim());
                });
    }

    public void forgotPassword(String email, AuthCallback callback) {
        if (email == null || email.trim().isEmpty()) {
            callback.onError("Email không được để trống");
            return;
        }

        mAuth.sendPasswordResetEmail(email.trim())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Không thể gửi email";
                        callback.onError(error);
                    }
                });
    }

    public void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        if (idToken == null || idToken.trim().isEmpty()) {
            callback.onError("Google token không hợp lệ");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || task.getResult().getUser() == null) {
                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Lỗi xác thực Google";
                        callback.onError(error);
                        return;
                    }

                    FirebaseUser firebaseUser = task.getResult().getUser();
                    String uid = firebaseUser.getUid();

                    mDatabase.child(uid)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                if (snapshot.exists()) {
                                    fetchUserRole(uid, callback);
                                } else {
                                    UserModel newUser = new UserModel();
                                    newUser.setUserId(uid);
                                    newUser.setName(firebaseUser.getDisplayName());
                                    newUser.setEmail(firebaseUser.getEmail());
                                    newUser.setPhone("");
                                    newUser.setRole("customer");
                                    newUser.setPoints(0);
                                    newUser.setSalaryRate(0);

                                    mDatabase.child(uid)
                                            .setValue(newUser)
                                            .addOnSuccessListener(unused -> callback.onSuccess("customer"))
                                            .addOnFailureListener(e -> callback.onError("Không thể tạo user Google: " + e.getMessage()));
                                }
                            })
                            .addOnFailureListener(e -> callback.onError("Không thể kiểm tra user Google: " + e.getMessage()));
                });
    }
}
package com.example.cafemanagement.helper;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Hỗ trợ kiểm tra trạng thái xác thực Firebase.
 */
public final class FirebaseAuthHelper {

    public interface AuthReadyCallback {
        void onReady(@NonNull FirebaseUser user);
        void onFailed(@NonNull String message);
    }

    private FirebaseAuthHelper() {
    }

    /**
     * Kiểm tra nhanh xem người dùng đã đăng nhập hay chưa.
     */
    public static boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    /**
     * Đảm bảo người dùng đã đăng nhập. 
     * Nếu đã có phiên, gọi onReady. Nếu chưa, gọi onFailed.
     */
    public static void ensureSignedIn(@NonNull AuthReadyCallback callback) {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current != null) {
            callback.onReady(current);
        } else {
            callback.onFailed("Phiên đăng nhập đã hết hạn hoặc chưa đăng nhập.");
        }
    }
    
    /**
     * Lấy UID của người dùng hiện tại một cách an toàn.
     */
    public static String getCurrentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }
}

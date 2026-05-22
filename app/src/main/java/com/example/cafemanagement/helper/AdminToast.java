package com.example.cafemanagement.helper;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.example.cafemanagement.R;

/**
 * Toast tùy chỉnh cho khu Admin — giao diện thống nhất với theme quán cà phê.
 */
public final class AdminToast {

    public enum Type {
        SUCCESS("Thành công", "✓", R.drawable.bg_admin_toast_success),
        ERROR("Lỗi", "✕", R.drawable.bg_admin_toast_error),
        INFO("Thông báo", "ℹ", R.drawable.bg_admin_toast_info);

        final String title;
        final String icon;
        final int backgroundRes;

        Type(String title, String icon, int backgroundRes) {
            this.title = title;
            this.icon = icon;
            this.backgroundRes = backgroundRes;
        }
    }

    private AdminToast() {
    }

    public static void show(@NonNull Context context, @NonNull String message, @NonNull Type type) {
        show(context, message, type, Toast.LENGTH_SHORT);
    }

    public static void show(@NonNull Context context, @NonNull String message,
                            @NonNull Type type, int duration) {
        if (message.trim().isEmpty()) return;

        View layout = LayoutInflater.from(context).inflate(R.layout.view_admin_toast, null);
        View container = layout.findViewById(R.id.layoutAdminToast);
        TextView txtIcon = layout.findViewById(R.id.txtAdminToastIcon);
        TextView txtTitle = layout.findViewById(R.id.txtAdminToastTitle);
        TextView txtMessage = layout.findViewById(R.id.txtAdminToastMessage);

        container.setBackgroundResource(type.backgroundRes);
        txtIcon.setText(type.icon);
        txtTitle.setText(type.title);
        txtMessage.setText(message);

        Toast toast = new Toast(context.getApplicationContext());
        toast.setView(layout);
        toast.setDuration(duration);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, dp(context, 96));
        toast.show();
    }

    public static void success(@NonNull Context context, @NonNull String message) {
        show(context, message, Type.SUCCESS);
    }

    public static void success(@NonNull Context context, @StringRes int messageRes) {
        success(context, context.getString(messageRes));
    }

    public static void error(@NonNull Context context, @NonNull String message) {
        show(context, message, Type.ERROR, Toast.LENGTH_LONG);
    }

    public static void error(@NonNull Context context, @StringRes int messageRes) {
        error(context, context.getString(messageRes));
    }

    public static void info(@NonNull Context context, @NonNull String message) {
        show(context, message, Type.INFO);
    }

    public static void info(@NonNull Context context, @StringRes int messageRes) {
        info(context, context.getString(messageRes));
    }

    private static int dp(Context context, int value) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}

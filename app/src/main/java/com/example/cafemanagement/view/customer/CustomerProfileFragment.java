package com.example.cafemanagement.view.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cafemanagement.R;
import com.example.cafemanagement.view.LoginActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CustomerProfileFragment extends Fragment {

    private MaterialButton btnLogout;
    private TextView btnChangePassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);

        // Lắng nghe sự kiện nhấn nút Đăng xuất
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        // Lắng nghe sự kiện nhấn nút Đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> showChangePasswordBottomSheet());
    }

    // Hàm chức năng hiển thị hộp thoại xác nhận Đăng xuất
    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Thực thi lệnh đăng xuất khỏi hệ thống Firebase
                    FirebaseAuth.getInstance().signOut();

                    // Chuyển hướng người dùng về lại màn hình Đăng nhập
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Hàm chức năng hiển thị cửa sổ trượt Đổi mật khẩu
    private void showChangePasswordBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_password, null);
        bottomSheetDialog.setContentView(view);

        TextInputEditText edtOldPassword = view.findViewById(R.id.edtOldPassword);
        TextInputEditText edtNewPassword = view.findViewById(R.id.edtNewPassword);
        TextInputEditText edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        MaterialButton btnSavePassword = view.findViewById(R.id.btnSavePassword);

        btnSavePassword.setOnClickListener(v -> {
            String oldPass = edtOldPassword.getText().toString().trim();
            String newPass = edtNewPassword.getText().toString().trim();
            String confirmPass = edtConfirmPassword.getText().toString().trim();

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ các trường thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(requireContext(), "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(requireContext(), "Mật khẩu mới phải dài ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getEmail() != null) {
                // Theo quy chuẩn bảo mật của Firebase, hệ thống bắt buộc phải xác thực lại người dùng trước khi đổi mật khẩu
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);

                user.reauthenticate(credential).addOnSuccessListener(aVoid -> {
                    // Nếu xác thực mật khẩu cũ thành công, tiến hành cập nhật mật khẩu mới
                    user.updatePassword(newPass).addOnSuccessListener(aVoid1 -> {
                        Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Lỗi khi cập nhật mật khẩu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Mật khẩu hiện tại không chính xác", Toast.LENGTH_SHORT).show();
                });
            }
        });

        bottomSheetDialog.show();
    }
}
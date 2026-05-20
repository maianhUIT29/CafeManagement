package com.example.cafemanagement.view;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.cafemanagement.databinding.ActivityForgotPasswordBinding;
import com.example.cafemanagement.viewmodel.ForgotPasswordViewModel;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private ForgotPasswordViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng ViewBinding để quản lý các View
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        setupObservers();
        setupEvents();
    }

    private void setupObservers() {
        // Lắng nghe kết quả từ ViewModel
        viewModel.getResetStatus().observe(this, status -> {
            if ("SUCCESS".equals(status)) {
                Toast.makeText(this, "Link khôi phục đã được gửi! Vui lòng kiểm tra Email.", Toast.LENGTH_LONG).show();
                finish(); // Đóng màn hình và quay lại Login
            } else if (status != null) {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupEvents() {
        // Sự kiện nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Sự kiện nút Gửi link
        binding.btnSendResetLink.setOnClickListener(v -> {
            String email = binding.edtForgotEmail.getText().toString().trim();

            // Kiểm tra tính hợp lệ của Email
            if (email.isEmpty()) {
                binding.edtForgotEmail.setError("Vui lòng nhập Email");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.edtForgotEmail.setError("Định dạng Email không hợp lệ");
                return;
            }

            // Gọi ViewModel để gửi yêu cầu đến Repository
            Toast.makeText(this, "Đang xử lý...", Toast.LENGTH_SHORT).show();
            viewModel.sendResetEmail(email);
        });
    }
}
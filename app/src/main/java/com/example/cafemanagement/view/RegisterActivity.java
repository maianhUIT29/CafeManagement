package com.example.cafemanagement.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.cafemanagement.databinding.ActivityRegisterBinding;
import com.example.cafemanagement.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupObservers();
        setupEvents();
    }

    private void setupObservers() {
        // Lắng nghe kết quả trả về từ ViewModel
        viewModel.getRegisterStatus().observe(this, status -> {
            if ("SUCCESS".equals(status)) {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                finish(); // Đóng màn hình đăng ký, tự động trở về màn hình Đăng nhập
            } else if (status != null) {
                Toast.makeText(this, "Lỗi: " + status, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupEvents() {
        // 1. Xử lý nút Đăng ký
        binding.btnRegisterSubmit.setOnClickListener(v -> {
            // Lấy dữ liệu từ giao diện
            String name = binding.edtRegName.getText().toString().trim();
            String phone = binding.edtRegPhone.getText().toString().trim();
            String email = binding.edtRegEmail.getText().toString().trim();
            String pass = binding.edtRegPassword.getText().toString().trim();

            // Ràng buộc (Validation): Không được để trống trường bắt buộc
            if (name.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập Họ tên, SĐT và Mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ràng buộc (Validation): SĐT nên có 10 số (cơ bản)
            if (phone.length() < 10) {
                Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ràng buộc (Validation): Mật khẩu phải từ 6 ký tự trở lên (quy định của Firebase)
            if (pass.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải chứa ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            // Chuyển dữ liệu xuống ViewModel để xử lý
            viewModel.register(name, phone, email, pass);
        });

        // 2. Xử lý nút "Đã có tài khoản? Đăng nhập" (Trở về trang Login)
        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }
}
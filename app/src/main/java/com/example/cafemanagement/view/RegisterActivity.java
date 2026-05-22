package com.example.cafemanagement.view;

import android.os.Bundle;
import android.util.Patterns;
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
        viewModel.getRegisterStatus().observe(this, status -> {
            if (status == null) {
                return;
            }

            if ("SUCCESS".equals(status)) {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi: " + status, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupEvents() {
        binding.btnRegisterSubmit.setOnClickListener(v -> {
            String name = binding.edtRegName.getText().toString().trim();
            String phone = binding.edtRegPhone.getText().toString().trim();
            String email = binding.edtRegEmail.getText().toString().trim();
            String password = binding.edtRegPassword.getText().toString().trim();

            if (name.isEmpty()) {
                binding.edtRegName.setError("Vui lòng nhập họ tên");
                binding.edtRegName.requestFocus();
                return;
            }

            if (phone.isEmpty()) {
                binding.edtRegPhone.setError("Vui lòng nhập số điện thoại");
                binding.edtRegPhone.requestFocus();
                return;
            }

            if (phone.length() < 10) {
                binding.edtRegPhone.setError("Số điện thoại không hợp lệ");
                binding.edtRegPhone.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                binding.edtRegEmail.setError("Vui lòng nhập email");
                binding.edtRegEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.edtRegEmail.setError("Email không hợp lệ");
                binding.edtRegEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.edtRegPassword.setError("Vui lòng nhập mật khẩu");
                binding.edtRegPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                binding.edtRegPassword.setError("Mật khẩu phải chứa ít nhất 6 ký tự");
                binding.edtRegPassword.requestFocus();
                return;
            }

            viewModel.register(name, phone, email, password);
        });

        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }
}
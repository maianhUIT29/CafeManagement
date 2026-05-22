package com.example.cafemanagement.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.cafemanagement.R;
import com.example.cafemanagement.databinding.ActivityLoginBinding;
import com.example.cafemanagement.viewmodel.LoginViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel loginViewModel;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences sharedPreferences;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        sharedPreferences = getSharedPreferences("CafeVongPrefs", MODE_PRIVATE);

        initGoogleSignIn();
        initUI();
        setupObservers();
        setupEvents();

        // Kiểm tra xem đã đăng nhập chưa
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(this, "Tự động đăng nhập: " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.VISIBLE);
            loginViewModel.checkCurrentUser();
        }
    }

    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initUI() {
        if (sharedPreferences.getBoolean("remember", false)) {
            binding.edtPhone.setText(sharedPreferences.getString("phone", ""));
            binding.cbRememberMe.setChecked(true);
        }
    }

    private void setupObservers() {
        loginViewModel.getAuthStatus().observe(this, status -> {
            if (status == null) return;

            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);

            if (status.startsWith("ERROR: ")) {
                String errorMsg = status.replace("ERROR: ", "");
                Toast.makeText(this, "Lỗi: " + errorMsg, Toast.LENGTH_LONG).show();
            } else {
                navigateToDashboard(status);
            }
        });
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        switch (role.toLowerCase()) {
            case "admin":
                intent = new Intent(this, AdminActivity.class);
                break;
            case "cashier":
                intent = new Intent(this, CashierActivity.class);
                break;
            case "barista":
                intent = new Intent(this, BaristaActivity.class);
                break;
            default:
                intent = new Intent(this, OrderSetupActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }

    private void setupEvents() {
        binding.tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        binding.btnShowHide.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                binding.edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.btnShowHide.setImageResource(R.drawable.ic_visibility);
            } else {
                binding.edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.btnShowHide.setImageResource(R.drawable.ic_visibility_off);
            }
            binding.edtPassword.setSelection(binding.edtPassword.getText().length());
        });

        binding.btnLogin.setOnClickListener(v -> {
            String phone = binding.edtPhone.getText().toString().trim();
            String pass = binding.edtPassword.getText().toString().trim();

            if (phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnLogin.setEnabled(false);

            saveRememberMeStatus(phone);
            loginViewModel.login(phone, pass);
        });

        binding.layoutGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleLauncher.launch(signInIntent);
        });
    }

    private void saveRememberMeStatus(String phone) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (binding.cbRememberMe.isChecked()) {
            editor.putString("phone", phone);
            editor.putBoolean("remember", true);
        } else {
            editor.remove("phone");
            editor.putBoolean("remember", false);
        }
        editor.apply();
    }

    private final ActivityResultLauncher<Intent> googleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            binding.progressBar.setVisibility(View.VISIBLE);
                            loginViewModel.onGoogleAuthSuccess(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        Toast.makeText(this, "Không thể kết nối Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
}

package com.example.cafemanagement.view.customer;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cafemanagement.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        bottomNavCustomer = findViewById(R.id.bottomNavCustomer);

        // Lắng nghe sự kiện người dùng bấm vào thanh điều hướng
        bottomNavCustomer.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_menu) {
                replaceFragment(new CustomerMenuFragment());
                return true;
            } else if (itemId == R.id.nav_cart) {
                replaceFragment(new CustomerBasketFragment());
                return true;
            } else if (itemId == R.id.nav_history) {
                replaceFragment(new CustomerOrderStatusFragment());
                return true;
            } else if (itemId == R.id.nav_points) {
                replaceFragment(new CustomerPointsFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                replaceFragment(new CustomerProfileFragment());
                return true;
            }

            return false;
        });

        // Kiểm tra tín hiệu Intent để điều hướng nếu quay về từ màn hình thanh toán
        handleIntent(getIntent(), savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent, null);
    }

    private void handleIntent(Intent intent, Bundle savedInstanceState) {
        // Kiểm tra chính xác cờ tín hiệu điều hướng
        if (intent != null && "ORDER_STATUS".equals(intent.getStringExtra("NAVIGATE_TO"))) {
            String orderId = intent.getStringExtra("ORDER_ID");

            // Xóa cờ tín hiệu ngay lập tức để chống lỗi lặp vòng đời màn hình
            intent.removeExtra("NAVIGATE_TO");

            // BƯỚC 1: Cập nhật giao diện thanh điều hướng trước
            bottomNavCustomer.setSelectedItemId(R.id.nav_history);

            // BƯỚC 2: Khởi tạo mảnh ghép chứa dữ liệu và đè lên mảnh ghép rỗng
            CustomerOrderStatusFragment fragment = new CustomerOrderStatusFragment();
            Bundle args = new Bundle();
            args.putString("ORDER_ID", orderId);
            fragment.setArguments(args);

            replaceFragment(fragment);

        } else if (savedInstanceState == null) {
            // Khởi tạo mảnh ghép mặc định khi ứng dụng vừa khởi động
            bottomNavCustomer.setSelectedItemId(R.id.nav_menu);
        }
    }

    // Hàm chức năng cốt lõi hoán đổi mảnh ghép giao diện an toàn
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }
}
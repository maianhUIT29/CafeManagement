package com.example.cafemanagement.view.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.cafemanagement.Constant.AppInfo;
import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.AdminToast;
import com.example.cafemanagement.repository.RepositoryCallback;
import com.example.cafemanagement.view.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public abstract class AdminBaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected MaterialToolbar adminToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_base);

        drawerLayout = findViewById(R.id.adminDrawerLayout);
        navigationView = findViewById(R.id.adminNavView);
        adminToolbar = findViewById(R.id.adminToolbar);

        getLayoutInflater().inflate(getContentLayoutRes(),
                findViewById(R.id.adminContentContainer), true);

        setSupportActionBar(adminToolbar);
        adminToolbar.setTitle(getAdminTitle());

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                adminToolbar,
                R.string.admin_drawer_open,
                R.string.admin_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(
                ContextCompat.getColor(this, R.color.white));

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(getNavItemId());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        onAdminContentReady(savedInstanceState);
        showFirebaseWarningIfNeeded();
    }

    private void showFirebaseWarningIfNeeded() {
        String warning = getIntent().getStringExtra(AppInfo.EXTRA_FIREBASE_WARNING);
        if (warning != null && !warning.isEmpty()) {
            adminToastError(warning);
            getIntent().removeExtra(AppInfo.EXTRA_FIREBASE_WARNING);
        }
    }

    @LayoutRes
    protected abstract int getContentLayoutRes();

    @IdRes
    protected abstract int getNavItemId();

    protected abstract String getAdminTitle();

    protected abstract void onAdminContentReady(Bundle savedInstanceState);

    protected void adminToastSuccess(String message) {
        AdminToast.success(this, message);
    }

    protected void adminToastError(String message) {
        AdminToast.error(this, message);
    }

    protected void adminToastInfo(String message) {
        AdminToast.info(this, message);
    }

    protected RepositoryCallback adminRepositoryCallback(String successMessage) {
        return new RepositoryCallback() {
            @Override
            public void onSuccess() {
                adminToastSuccess(successMessage);
            }

            @Override
            public void onError(String message) {
                adminToastError(message != null ? message : "Đã xảy ra lỗi");
            }
        };
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_admin_logout) {
            logout();
            return true;
        }

        if (id == getNavItemId()) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        Class<?> target = null;
        if (id == R.id.nav_admin_dashboard) {
            target = AdminActivity.class;
        } else if (id == R.id.nav_admin_tables) {
            target = AdminTableActivity.class;
        } else if (id == R.id.nav_admin_menu) {
            target = AdminMenuActivity.class;
        } else if (id == R.id.nav_admin_staff) {
            target = AdminStaffActivity.class;
        } else if (id == R.id.nav_admin_voucher) {
            target = AdminVoucherActivity.class;
        }
        else if (id == R.id.nav_admin_history) {
            target = AdminHistoryActivity.class;
        } else if (id == R.id.nav_admin_salary) {
            target = AdminSalaryActivity.class;
        }

        if (target != null && !target.equals(getClass())) {
            Intent intent = new Intent(this, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        adminToastInfo("Đã đăng xuất");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

package com.example.cafemanagement.view.customer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cafemanagement.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CustomerMainActivity extends AppCompatActivity {

    // Thông tin bàn — được set sau khi user chọn xong trong Tab Đặt hàng
    private boolean isDineIn  = false;
    private String  tableId   = null;
    private String  tableName = null;

    public boolean isDineIn()     { return isDineIn; }
    public String  getTableId()   { return tableId; }
    public String  getTableName() { return tableName; }

    public void setOrderContext(boolean isDineIn, String tableId, String tableName) {
        this.isDineIn  = isDineIn;
        this.tableId   = tableId;
        this.tableName = tableName;
    }

    private ViewPager2 viewPager;

    public void navigateToTab(int index) {
        if (viewPager != null) viewPager.setCurrentItem(index, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        TabLayout tabLayout = findViewById(R.id.tabLayoutCustomer);
        viewPager = findViewById(R.id.viewPagerCustomer);

        viewPager.setAdapter(new CustomerPageAdapter(this));
        viewPager.setUserInputEnabled(false);

        new TabLayoutMediator(tabLayout, viewPager, (tab, pos) -> {
            if      (pos == 0) tab.setText("🛒 Đặt hàng");
            else if (pos == 1) tab.setText("📋 Đơn hàng");
            else               tab.setText("👤 Tài khoản");
        }).attach();

        // Nếu quay về từ checkout → chuyển sang tab Đơn hàng
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        String navigateTo = intent.getStringExtra("NAVIGATE_TO");
        if (navigateTo == null) return;

        switch (navigateTo) {
            case "ORDER_STATUS":
                intent.removeExtra("NAVIGATE_TO");
                viewPager.setCurrentItem(1, true);
                break;

            case "ORDER_SETUP":
                intent.removeExtra("NAVIGATE_TO");
                // Chuyển về tab Đặt hàng và reset về màn hình chọn bàn
                viewPager.setCurrentItem(0, true);
                viewPager.post(() -> {
                    CustomerOrderingTabFragment tab =
                            (CustomerOrderingTabFragment) getSupportFragmentManager()
                                    .findFragmentByTag("f0"); // ViewPager2 dùng tag "f{index}"
                    if (tab != null) tab.showSetup();
                });
                break;
        }
    }

    static class CustomerPageAdapter extends FragmentStateAdapter {
        CustomerPageAdapter(FragmentActivity fa) { super(fa); }

        @Override public int getItemCount() { return 3; }

        @NonNull @Override
        public Fragment createFragment(int position) {
            if (position == 0) return new CustomerOrderingTabFragment();
            if (position == 1) return new CustomerOrderStatusFragment();
            return new CustomerAccountTabFragment();
        }
    }
}
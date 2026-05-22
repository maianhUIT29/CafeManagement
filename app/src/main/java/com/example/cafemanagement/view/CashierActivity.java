package com.example.cafemanagement.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.cafemanagement.R;
import com.example.cafemanagement.helper.CartManager;
import com.example.cafemanagement.view.cashier.ConfirmFragment;
import com.example.cafemanagement.view.cashier.CustomizeFragment;
import com.example.cafemanagement.view.cashier.DashboardFragment;
import com.example.cafemanagement.view.cashier.MenuFragment;
import com.example.cafemanagement.view.cashier.OrderListFragment;
import com.example.cafemanagement.view.cashier.OrderTypeFragment;
import com.example.cafemanagement.view.cashier.PaymentFragment;
import com.example.cafemanagement.view.cashier.SelectTableFragment;
import com.example.cafemanagement.view.cashier.ShiftFragment;
import com.example.cafemanagement.view.cashier.ShiftReportFragment;
import com.example.cafemanagement.view.cashier.TableManagementFragment;

public class CashierActivity extends AppCompatActivity {

    // ── Screen constants ──────────────────────────────────────────────────────
    public static final String SCREEN_DASHBOARD        = "dashboard";
    public static final String SCREEN_ORDER_TYPE       = "order_type";
    public static final String SCREEN_SELECT_TABLE     = "select_table";
    public static final String SCREEN_MENU             = "menu";
    public static final String SCREEN_CUSTOMIZE        = "customize";
    public static final String SCREEN_PAYMENT          = "payment";
    public static final String SCREEN_CONFIRM          = "confirm";
    public static final String SCREEN_SHIFT            = "shift";
    public static final String SCREEN_SHIFT_REPORT     = "shift_report";   // MỚI
    public static final String SCREEN_ORDER_LIST       = "order_list";
    public static final String SCREEN_TABLE_MANAGEMENT = "table_management";

    // ── Tab constants ─────────────────────────────────────────────────────────
    private static final int TAB_POS    = 0;
    private static final int TAB_ORDERS = 1;
    private static final int TAB_TABLES = 2;
    private static final int TAB_SHIFT  = 3;

    private TextView     tvTitle;
    private ImageButton  btnBack;
    private View         toolbar;
    private LinearLayout bottomNav;

    private LinearLayout navTabPos, navTabOrders, navTabTables, navTabShift;
    private TextView     navLabelPos, navLabelOrders, navLabelTables, navLabelShift;

    private int currentTab = TAB_POS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashier);

        tvTitle   = findViewById(R.id.tv_toolbar_title);
        btnBack   = findViewById(R.id.btn_back);
        toolbar   = findViewById(R.id.cashier_toolbar);
        bottomNav = findViewById(R.id.bottom_nav);

        navTabPos    = findViewById(R.id.nav_tab_pos);
        navTabOrders = findViewById(R.id.nav_tab_orders);
        navTabTables = findViewById(R.id.nav_tab_tables);
        navTabShift  = findViewById(R.id.nav_tab_shift);

        navLabelPos    = findViewById(R.id.nav_label_pos);
        navLabelOrders = findViewById(R.id.nav_label_orders);
        navLabelTables = findViewById(R.id.nav_label_tables);
        navLabelShift  = findViewById(R.id.nav_label_shift);

        navTabPos.setOnClickListener(v    -> switchTab(TAB_POS));
        navTabOrders.setOnClickListener(v -> switchTab(TAB_ORDERS));
        navTabTables.setOnClickListener(v -> switchTab(TAB_TABLES));
        navTabShift.setOnClickListener(v  -> switchTab(TAB_SHIFT));

        // ── Back handler (AndroidX) ───────────────────────────────────────────
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int backStackCount = getSupportFragmentManager().getBackStackEntryCount();

                if (backStackCount <= 1) {
                    updateToolbar(SCREEN_DASHBOARD, "");
                    updateBottomNavVisibility(SCREEN_DASHBOARD);
                    highlightTab(currentTab);
                }

                if (backStackCount > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Nút back trên toolbar
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        if (savedInstanceState == null) {
            navigateTo(SCREEN_DASHBOARD, null, false);
            highlightTab(TAB_POS);
        }
    }

    // ── Tab switching ─────────────────────────────────────────────────────────

    private void switchTab(int tab) {
        if (currentTab == tab) return;
        currentTab = tab;
        highlightTab(tab);

        getSupportFragmentManager().popBackStack(null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);

        switch (tab) {
            case TAB_POS:    navigateTab(SCREEN_DASHBOARD);        break;
            case TAB_ORDERS: navigateTab(SCREEN_ORDER_LIST);       break;
            case TAB_TABLES: navigateTab(SCREEN_TABLE_MANAGEMENT); break;
            case TAB_SHIFT:  navigateTab(SCREEN_SHIFT);            break;
        }
    }

    private void navigateTab(String screen) {
        Fragment fragment;

        switch (screen) {
            case SCREEN_ORDER_LIST:       fragment = new OrderListFragment();       break;
            case SCREEN_TABLE_MANAGEMENT: fragment = new TableManagementFragment(); break;
            case SCREEN_SHIFT:            fragment = new ShiftFragment();           break;
            default:                      fragment = new DashboardFragment();       break;
        }

        updateToolbar(screen, "");
        updateBottomNavVisibility(screen);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.cashier_fragment_container, fragment)
                .commit();
    }

    private void highlightTab(int activeTab) {
        int activeColor   = getResources().getColor(R.color.accent_brown,  getTheme());
        int inactiveColor = getResources().getColor(R.color.text_secondary, getTheme());
        navLabelPos.setTextColor(   activeTab == TAB_POS    ? activeColor : inactiveColor);
        navLabelOrders.setTextColor(activeTab == TAB_ORDERS ? activeColor : inactiveColor);
        navLabelTables.setTextColor(activeTab == TAB_TABLES ? activeColor : inactiveColor);
        navLabelShift.setTextColor( activeTab == TAB_SHIFT  ? activeColor : inactiveColor);
    }

    // ── Fragment navigation ───────────────────────────────────────────────────

    public void navigateTo(String screen, Bundle args, boolean addToBackStack) {
        navigateTo(screen, args, addToBackStack, true);
    }

    public void navigateTo(String screen, Bundle args, boolean addToBackStack, boolean isForward) {
        Fragment fragment;
        String   title;

        switch (screen) {
            case SCREEN_ORDER_TYPE:
                fragment = new OrderTypeFragment();
                title    = "Tạo đơn mới";
                break;
            case SCREEN_SELECT_TABLE:
                fragment = new SelectTableFragment();
                title    = "Chọn bàn";
                break;
            case SCREEN_MENU:
                fragment = new MenuFragment();
                title    = CartManager.getInstance().getTableName() != null
                        ? CartManager.getInstance().getTableName() : "Chọn món";
                break;
            case SCREEN_CUSTOMIZE:
                fragment = new CustomizeFragment();
                title    = "Tuỳ chỉnh món";
                break;
            case SCREEN_PAYMENT:
                fragment = new PaymentFragment();
                title    = "Thanh toán";
                break;
            case SCREEN_CONFIRM:
                fragment = new ConfirmFragment();
                title    = "";
                break;
            case SCREEN_SHIFT:
                fragment = new ShiftFragment();
                title    = "";
                break;
            case SCREEN_SHIFT_REPORT:                   // MỚI
                fragment = new ShiftReportFragment();
                title    = "Báo cáo ca";
                break;
            case SCREEN_ORDER_LIST:
                fragment = new OrderListFragment();
                title    = "";
                break;
            case SCREEN_TABLE_MANAGEMENT:
                fragment = new TableManagementFragment();
                title    = "";
                break;
            default: // DASHBOARD
                fragment = new DashboardFragment();
                title    = "";
                break;
        }

        if (args != null) fragment.setArguments(args);

        updateToolbar(screen, title);
        updateBottomNavVisibility(screen);

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();

        if (isForward) {
            tx.setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left,  R.anim.slide_out_right
            );
        } else {
            tx.setCustomAnimations(
                    R.anim.slide_in_left,  R.anim.slide_out_right,
                    R.anim.slide_in_right, R.anim.slide_out_left
            );
        }

        tx.replace(R.id.cashier_fragment_container, fragment);
        if (addToBackStack) tx.addToBackStack(screen);
        tx.commit();
    }

    private void updateToolbar(String screen, String title) {
        boolean showToolbar = !screen.equals(SCREEN_DASHBOARD)
                && !screen.equals(SCREEN_CONFIRM)
                && !screen.equals(SCREEN_SHIFT)
                && !screen.equals(SCREEN_SHIFT_REPORT)   // report tự có toolbar riêng
                && !screen.equals(SCREEN_ORDER_LIST)
                && !screen.equals(SCREEN_TABLE_MANAGEMENT)
                && !screen.equals(SCREEN_ORDER_TYPE);
        toolbar.setVisibility(showToolbar ? View.VISIBLE : View.GONE);
        if (tvTitle != null) tvTitle.setText(title);
    }

    private void updateBottomNavVisibility(String screen) {
        boolean hideBottomNav = screen.equals(SCREEN_SELECT_TABLE)
                || screen.equals(SCREEN_MENU)
                || screen.equals(SCREEN_CUSTOMIZE)
                || screen.equals(SCREEN_PAYMENT)
                || screen.equals(SCREEN_CONFIRM)
                || screen.equals(SCREEN_SHIFT_REPORT);   // MỚI: ẩn bottom nav khi xem report
        bottomNav.setVisibility(hideBottomNav ? View.GONE : View.VISIBLE);
    }

    public void setToolbarTitle(String title) {
        if (tvTitle != null) tvTitle.setText(title);
    }
}
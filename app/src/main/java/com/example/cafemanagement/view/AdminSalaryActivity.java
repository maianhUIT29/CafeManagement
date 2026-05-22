package com.example.cafemanagement.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafemanagement.R;
import com.example.cafemanagement.adapter.AdminSalaryAdapter;
import com.example.cafemanagement.helper.PriceFormatter;
import com.example.cafemanagement.model.StaffSalaryModel;
import com.example.cafemanagement.viewmodel.AdminSalaryViewModel;
import java.util.Calendar;
import java.util.Locale;

public class AdminSalaryActivity extends AdminBaseActivity {

    private AdminSalaryViewModel viewModel;
    private AdminSalaryAdapter adapter;
    private TextView txtCurrentMonth, txtTotalBudget;
    private ImageButton btnPrevMonth, btnNextMonth;
    private ProgressBar progressBar;
    private Calendar currentCalendar;

    @Override
    protected int getContentLayoutRes() { return R.layout.activity_admin_salary; }

    @Override
    protected int getNavItemId() { return R.id.nav_admin_salary; }

    @Override
    protected String getAdminTitle() { return "Quản lý lương"; }

    @Override
    protected void onAdminContentReady(Bundle savedInstanceState) {
        initViews();
        setupRecyclerView();
        setupViewModel();

        currentCalendar = Calendar.getInstance();
        fetchData();

        btnPrevMonth.setOnClickListener(v -> { currentCalendar.add(Calendar.MONTH, -1); fetchData(); });
        btnNextMonth.setOnClickListener(v -> { currentCalendar.add(Calendar.MONTH, 1); fetchData(); });
    }

    private void initViews() {
        txtCurrentMonth = findViewById(R.id.txtCurrentMonth);
        txtTotalBudget = findViewById(R.id.txtTotalMonthlyBudget);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        progressBar = findViewById(R.id.progressBarSalary);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerSalary);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminSalaryAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AdminSalaryViewModel.class);

        viewModel.getSalaryList().observe(this, list -> {
            adapter.setSalaryList(list);

            // Tính tổng ngân sách lương tháng (PO logic)
            double total = 0;
            for (StaffSalaryModel s : list) total += s.getTotalSalary();
            txtTotalBudget.setText(PriceFormatter.formatPrice(total));
        });

        viewModel.getIsLoading().observe(this, loading ->
                progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));
    }

    private void fetchData() {
        int m = currentCalendar.get(Calendar.MONTH) + 1;
        int y = currentCalendar.get(Calendar.YEAR);
        txtCurrentMonth.setText(String.format(Locale.getDefault(), "Tháng %02d/%d", m, y));
        viewModel.loadMonthlySalary(m, y);
    }
}
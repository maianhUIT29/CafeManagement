package com.example.cafemanagement.view.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.adapter.HistoryRevenueAdapter;
import com.example.cafemanagement.viewmodel.AdminHistoryViewModel;

public class AdminHistoryActivity extends AdminBaseActivity {

    private AdminHistoryViewModel viewModel;
    private HistoryRevenueAdapter adapter;
    private RecyclerView recyclerHistory;
    private ProgressBar progressBar;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_admin_history;
    }

    @Override
    protected int getNavItemId() {
        return R.id.nav_admin_history;
    }

    @Override
    protected String getAdminTitle() {
        return "Nhật ký dòng tiền";
    }

    @Override
    protected void onAdminContentReady(Bundle savedInstanceState) {
        initViews();
        setupRecyclerView();
        setupViewModel();
    }

    private void initViews() {
        recyclerHistory = findViewById(R.id.recyclerHistory);
        progressBar = findViewById(R.id.progressBarHistory);
    }

    private void setupRecyclerView() {
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryRevenueAdapter();
        recyclerHistory.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AdminHistoryViewModel.class);

        viewModel.getHistoryList().observe(this, historyList -> {
            if (historyList != null) {
                adapter.setHistoryList(historyList);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                adminToastError(error);
            }
        });

        viewModel.loadHistoryData();
    }
}
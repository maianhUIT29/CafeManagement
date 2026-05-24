package com.example.cafemanagement.view.barista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.viewmodel.BaristaViewModel;

public class OrdersTabFragment extends Fragment {

    private BaristaViewModel    viewModel;
    private BaristaOrderAdapter adapter;
    private TextView            tvPending, tvDone, tvEmpty;
    private LinearLayout        layoutStats, layoutNoShift, layoutContent;
    private RecyclerView        rv;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             ViewGroup container, Bundle b) {
        return inf.inflate(R.layout.fragment_barista_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);

        // Dùng chung ViewModel với Activity
        viewModel = new ViewModelProvider(requireActivity()).get(BaristaViewModel.class);

        tvPending     = v.findViewById(R.id.tv_pending_count);
        tvDone        = v.findViewById(R.id.tv_done_count);
        tvEmpty       = v.findViewById(R.id.tv_barista_empty);
        layoutStats   = v.findViewById(R.id.layout_order_stats);
        layoutNoShift = v.findViewById(R.id.layout_no_shift_orders);
        layoutContent = v.findViewById(R.id.layout_orders_content);
        rv            = v.findViewById(R.id.rv_barista_orders);

        adapter = new BaristaOrderAdapter(
                new ArrayList<>(),
                (orderId, order) -> viewModel.completeOrder(orderId)  // bỏ order, chỉ dùng orderId
        );
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        setupObservers();
    }

    private void setupObservers() {
        viewModel.getShiftActive().observe(getViewLifecycleOwner(), active -> {
            layoutStats.setVisibility(active ? View.VISIBLE : View.GONE);
            layoutNoShift.setVisibility(active ? View.GONE : View.VISIBLE);
            layoutContent.setVisibility(active ? View.VISIBLE : View.GONE);
        });

        viewModel.getPendingOrders().observe(getViewLifecycleOwner(), orders -> {
            adapter.updateData(orders);
            tvPending.setText(String.valueOf(orders.size()));
            tvEmpty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
            rv.setVisibility(orders.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getDoneCount().observe(getViewLifecycleOwner(),
                count -> tvDone.setText(String.valueOf(count)));

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }
}
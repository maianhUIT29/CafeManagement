package com.example.cafemanagement.view.customer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.viewmodel.OrderSetupViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class CustomerOrderSetupFragment extends Fragment {

    private MaterialCardView cardTakeaway, cardDineIn;
    private ImageView imgTakeaway, imgDineIn;
    private TextView txtTakeaway, txtDineIn;
    private LinearLayout layoutTableSelection;
    private RecyclerView recyclerTables;
    private com.example.cafemanagement.adapter.TableAdapter tableAdapter;
    private MaterialButton btnContinue;
    private OrderSetupViewModel viewModel;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_order_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupViewModel();
        setupListeners();
    }

    private void initViews(View view) {
        cardTakeaway        = view.findViewById(R.id.cardTakeaway);
        cardDineIn          = view.findViewById(R.id.cardDineIn);
        imgTakeaway         = view.findViewById(R.id.imgTakeaway);
        txtTakeaway         = view.findViewById(R.id.txtTakeaway);
        imgDineIn           = view.findViewById(R.id.imgDineIn);
        txtDineIn           = view.findViewById(R.id.txtDineIn);
        layoutTableSelection= view.findViewById(R.id.layoutTableSelection);
        recyclerTables      = view.findViewById(R.id.recyclerTables);
        btnContinue         = view.findViewById(R.id.btnContinue);
    }

    private void setupRecyclerView() {
        tableAdapter = new com.example.cafemanagement.adapter.TableAdapter(
                table -> viewModel.setSelectedTable(table));
        recyclerTables.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        recyclerTables.setAdapter(tableAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(OrderSetupViewModel.class);

        viewModel.getTableList().observe(getViewLifecycleOwner(), tables -> {
            if (tables != null) tableAdapter.setTableList(tables);
        });

        viewModel.getIsDineIn().observe(getViewLifecycleOwner(), isDineIn -> {
            if (isDineIn) {
                setCardSelected(cardDineIn, imgDineIn, txtDineIn);
                setCardUnselected(cardTakeaway, imgTakeaway, txtTakeaway);
                layoutTableSelection.setVisibility(View.VISIBLE);
            } else {
                setCardSelected(cardTakeaway, imgTakeaway, txtTakeaway);
                setCardUnselected(cardDineIn, imgDineIn, txtDineIn);
                layoutTableSelection.setVisibility(View.GONE);
            }
        });

        viewModel.getSelectedTable().observe(getViewLifecycleOwner(),
                table -> tableAdapter.setSelectedTable(table));
    }

    private void setupListeners() {
        cardTakeaway.setOnClickListener(v -> viewModel.setServiceType(false));
        cardDineIn.setOnClickListener(v -> viewModel.setServiceType(true));

        btnContinue.setOnClickListener(v -> {
            boolean isDineIn = Boolean.TRUE.equals(viewModel.getIsDineIn().getValue());
            if (isDineIn && viewModel.getSelectedTable().getValue() == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn bàn",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Lưu context bàn vào CustomerMainActivity
            if (requireActivity() instanceof CustomerMainActivity) {
                String tid  = isDineIn && viewModel.getSelectedTable().getValue() != null
                        ? viewModel.getSelectedTable().getValue().getTableId() : null;
                String tname = isDineIn && viewModel.getSelectedTable().getValue() != null
                        ? viewModel.getSelectedTable().getValue().getName() : null;
                ((CustomerMainActivity) requireActivity())
                        .setOrderContext(isDineIn, tid, tname);
            }

            // Điều hướng sang Menu trong cùng tab
            if (getParentFragment() instanceof CustomerOrderingTabFragment) {
                ((CustomerOrderingTabFragment) getParentFragment()).showMenu();
            }
        });
    }

    private void setCardSelected(MaterialCardView card, ImageView icon, TextView text) {
        card.setCardBackgroundColor(Color.parseColor("#3E2723"));
        icon.setColorFilter(Color.parseColor("#FFD54F"));
        text.setTextColor(Color.parseColor("#FFD54F"));
    }

    private void setCardUnselected(MaterialCardView card, ImageView icon, TextView text) {
        card.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        icon.setColorFilter(Color.parseColor("#3E2723"));
        text.setTextColor(Color.parseColor("#3E2723"));
    }
}
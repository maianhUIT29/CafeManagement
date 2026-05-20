package com.example.cafemanagement.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.adapter.TableAdapter;
import com.example.cafemanagement.viewmodel.OrderSetupViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class OrderSetupActivity extends AppCompatActivity {

    // Khai báo biến một lần duy nhất, loại bỏ sự trùng lặp
    private MaterialCardView cardTakeaway, cardDineIn;
    private ImageView imgTakeaway, imgDineIn;
    private TextView txtTakeaway, txtDineIn;

    private LinearLayout layoutTableSelection;
    private RecyclerView recyclerTables;
    private TableAdapter adapter;
    private MaterialButton btnContinue;
    private OrderSetupViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_setup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {
        cardTakeaway = findViewById(R.id.cardTakeaway);
        cardDineIn = findViewById(R.id.cardDineIn);

        // Bổ sung ánh xạ cho Icon và Text để có thể đổi màu
        imgTakeaway = findViewById(R.id.imgTakeaway);
        txtTakeaway = findViewById(R.id.txtTakeaway);
        imgDineIn = findViewById(R.id.imgDineIn);
        txtDineIn = findViewById(R.id.txtDineIn);

        layoutTableSelection = findViewById(R.id.layoutTableSelection);
        recyclerTables = findViewById(R.id.recyclerTables);
        btnContinue = findViewById(R.id.btnContinue);
    }

    private void setupRecyclerView() {
        adapter = new TableAdapter(table -> viewModel.setSelectedTable(table));
        recyclerTables.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerTables.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(OrderSetupViewModel.class);

        viewModel.getTableList().observe(this, tables -> {
            if (tables != null) {
                adapter.setTableList(tables);
            }
        });

        // Tích hợp 2 hàm đổi màu sắc toàn diện vào logic của ViewModel
        viewModel.getIsDineIn().observe(this, isDineIn -> {
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

        viewModel.getSelectedTable().observe(this, table -> adapter.setSelectedTable(table));
    }

    private void setupListeners() {
        cardTakeaway.setOnClickListener(v -> viewModel.setServiceType(false));

        cardDineIn.setOnClickListener(v -> viewModel.setServiceType(true));

        btnContinue.setOnClickListener(v -> {
            boolean isDineIn = viewModel.getIsDineIn().getValue() != null && viewModel.getIsDineIn().getValue();
            if (isDineIn && viewModel.getSelectedTable().getValue() == null) {
                Toast.makeText(this, "Vui lòng chọn bàn", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(OrderSetupActivity.this, MenuActivity.class);
            intent.putExtra("IS_DINE_IN", isDineIn);
            if (isDineIn) {
                intent.putExtra("TABLE_ID", viewModel.getSelectedTable().getValue().getTableId());
                intent.putExtra("TABLE_NAME", viewModel.getSelectedTable().getValue().getName());
            }
            startActivity(intent);
        });
    }

    /**
     * Hàm thiết lập trạng thái ĐƯỢC CHỌN: Nền nâu, Icon vàng, Chữ vàng
     */
    private void setCardSelected(MaterialCardView card, ImageView icon, TextView text) {
        card.setCardBackgroundColor(Color.parseColor("#3E2723"));
        icon.setColorFilter(Color.parseColor("#FFD54F"));
        text.setTextColor(Color.parseColor("#FFD54F"));
    }

    /**
     * Hàm thiết lập trạng thái BỎ CHỌN: Nền trắng/xám, Icon đen/nâu, Chữ đen/nâu
     */
    private void setCardUnselected(MaterialCardView card, ImageView icon, TextView text) {
        card.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        icon.setColorFilter(Color.parseColor("#3E2723"));
        text.setTextColor(Color.parseColor("#3E2723"));
    }
}
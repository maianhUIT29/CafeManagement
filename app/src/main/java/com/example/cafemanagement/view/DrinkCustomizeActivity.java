package com.example.cafemanagement.view;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.cafemanagement.R;
import com.example.cafemanagement.BasketManager;
import com.example.cafemanagement.model.BasketItemModel;
import com.example.cafemanagement.viewmodel.DrinkCustomizeViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class DrinkCustomizeActivity extends AppCompatActivity {

    private DrinkCustomizeViewModel viewModel;

    private String productName = "Không rõ tên";
    private String productImageUrl = "";
    private double basePrice = 0.0;

    private String currentSize = "Size Nhỏ";
    private String currentSweetness = "50% Đường";

    private TextView txtName, txtSubDescription, txtPrice, txtQty;
    private ImageView imgProduct, btnPlus, btnMinus;
    private MaterialButton btnAddToCart;

    private MaterialCardView cardSizeSmall, cardSizeMedium, cardSizeLarge;
    private TextView txtSizeSmall, txtSizeMedium, txtSizeLarge;

    private MaterialCardView cardSweet0, cardSweet30, cardSweet50, cardSweet100;
    private TextView txtSweet0, txtSweet30, txtSweet50, txtSweet100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_custom);

        initViews();
        handleIntentData();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {
        txtName = findViewById(R.id.txtName);
        txtSubDescription = findViewById(R.id.txtSubDescription);
        txtPrice = findViewById(R.id.txtPriceHeader);
        txtQty = findViewById(R.id.txtQty);
        imgProduct = findViewById(R.id.imgProduct);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);

        cardSizeSmall = findViewById(R.id.cardSizeSmall);
        txtSizeSmall = findViewById(R.id.txtSizeSmall);
        cardSizeMedium = findViewById(R.id.cardSizeMedium);
        txtSizeMedium = findViewById(R.id.txtSizeMedium);
        cardSizeLarge = findViewById(R.id.cardSizeLarge);
        txtSizeLarge = findViewById(R.id.txtSizeLarge);

        cardSweet0 = findViewById(R.id.cardSweet0);
        txtSweet0 = findViewById(R.id.txtSweet0);
        cardSweet30 = findViewById(R.id.cardSweet30);
        txtSweet30 = findViewById(R.id.txtSweet30);
        cardSweet50 = findViewById(R.id.cardSweet50);
        txtSweet50 = findViewById(R.id.txtSweet50);
        cardSweet100 = findViewById(R.id.cardSweet100);
        txtSweet100 = findViewById(R.id.txtSweet100);
    }

    private void handleIntentData() {
        if (getIntent() != null) {
            productName = getIntent().getStringExtra("PRODUCT_NAME");
            productImageUrl = getIntent().getStringExtra("PRODUCT_IMAGE");

            basePrice = getIntent().getDoubleExtra("PRODUCT_PRICE", 0.0);
            if (basePrice == 0.0) {
                basePrice = (double) getIntent().getFloatExtra("PRODUCT_PRICE", 0.0f);
            }
            if (basePrice == 0.0) {
                basePrice = (double) getIntent().getIntExtra("PRODUCT_PRICE", 0);
            }

            if (productName != null) {
                txtName.setText(productName);
            }
            if (productImageUrl != null && !productImageUrl.isEmpty()) {
                Glide.with(this).load(productImageUrl).into(imgProduct);
            }

            // ĐÃ SỬA: Gọi lớp PriceFormatter dùng chung để định dạng giá gốc trên cùng
            txtPrice.setText(com.example.cafemanagement.helper.PriceFormatter.formatPrice(basePrice));
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DrinkCustomizeViewModel.class);

        viewModel.setBasePrice(basePrice);

        viewModel.getQuantity().observe(this, qty -> {
            txtQty.setText(String.valueOf(qty));
        });

        // Chỉ cố định chữ trên nút bấm, ẩn biến số giá tiền đi
        viewModel.getTotalPrice().observe(this, price -> {
            btnAddToCart.setText("Thêm vào giỏ");
        });
    }

    private void setupListeners() {
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        btnPlus.setOnClickListener(v -> viewModel.increaseQty());
        btnMinus.setOnClickListener(v -> viewModel.decreaseQty());

        cardSizeSmall.setOnClickListener(v -> updateSizeUI(1));
        cardSizeMedium.setOnClickListener(v -> updateSizeUI(2));
        cardSizeLarge.setOnClickListener(v -> updateSizeUI(3));

        cardSweet0.setOnClickListener(v -> updateSweetUI(0));
        cardSweet30.setOnClickListener(v -> updateSweetUI(30));
        cardSweet50.setOnClickListener(v -> updateSweetUI(50));
        cardSweet100.setOnClickListener(v -> updateSweetUI(100));

        btnAddToCart.setOnClickListener(v -> {
            saveItemToBasket();
        });
    }

    private void saveItemToBasket() {
        int quantity = viewModel.getQuantity().getValue() != null ? viewModel.getQuantity().getValue() : 1;
        double totalPrice = viewModel.getTotalPrice().getValue() != null ? viewModel.getTotalPrice().getValue() : basePrice;

        double unitPrice = totalPrice / quantity;

        String optionsDisplay = currentSize + ", " + currentSweetness;

        BasketItemModel customItem = new BasketItemModel(
                productName,
                productImageUrl,
                optionsDisplay,
                unitPrice,
                quantity
        );

        BasketManager.getInstance().addCustomItem(customItem);

        Toast.makeText(this, "Đã thêm thành công", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateSizeUI(int type) {
        resetUI(cardSizeSmall, txtSizeSmall, "#F5F5F5", "#424242");
        resetUI(cardSizeMedium, txtSizeMedium, "#F5F5F5", "#424242");
        resetUI(cardSizeLarge, txtSizeLarge, "#F5F5F5", "#424242");

        if (type == 1) {
            highlightUI(cardSizeSmall, txtSizeSmall, "#FFD54F", "#2D1A11");
            currentSize = "Size Nhỏ";
            viewModel.setSizeModifier(0.0);
        } else if (type == 2) {
            highlightUI(cardSizeMedium, txtSizeMedium, "#FFD54F", "#2D1A11");
            currentSize = "Size Vừa";
            viewModel.setSizeModifier(0.5);
        } else if (type == 3) {
            highlightUI(cardSizeLarge, txtSizeLarge, "#FFD54F", "#2D1A11");
            currentSize = "Size Lớn";
            viewModel.setSizeModifier(1.0);
        }
    }

    private void updateSweetUI(int percent) {
        resetUI(cardSweet0, txtSweet0, "#F5F5F5", "#424242");
        resetUI(cardSweet30, txtSweet30, "#F5F5F5", "#424242");
        resetUI(cardSweet50, txtSweet50, "#F5F5F5", "#424242");
        resetUI(cardSweet100, txtSweet100, "#F5F5F5", "#424242");

        if (percent == 0) {
            highlightUI(cardSweet0, txtSweet0, "#2D1A11", "#FFFFFF");
            currentSweetness = "0% Đường";
        } else if (percent == 30) {
            highlightUI(cardSweet30, txtSweet30, "#2D1A11", "#FFFFFF");
            currentSweetness = "30% Đường";
        } else if (percent == 50) {
            highlightUI(cardSweet50, txtSweet50, "#2D1A11", "#FFFFFF");
            currentSweetness = "50% Đường";
        } else if (percent == 100) {
            highlightUI(cardSweet100, txtSweet100, "#2D1A11", "#FFFFFF");
            currentSweetness = "100% Đường";
        }
    }

    private void resetUI(MaterialCardView card, TextView text, String bgColor, String textColor) {
        card.setCardBackgroundColor(Color.parseColor(bgColor));
        text.setTextColor(Color.parseColor(textColor));
    }

    private void highlightUI(MaterialCardView card, TextView text, String bgColor, String textColor) {
        card.setCardBackgroundColor(Color.parseColor(bgColor));
        text.setTextColor(Color.parseColor(textColor));
    }
}
package com.example.cafemanagement.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagement.R;
import com.example.cafemanagement.adapter.CartItemAdapter;
import com.example.cafemanagement.api.CreateOrder;
import com.example.cafemanagement.model.CartItemModel;
import com.example.cafemanagement.viewmodel.CheckoutViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// IMPORT ZALOPAY
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

// IMPORT VNPAY (Bắt buộc)
import com.vnpay.authentication.VNP_AuthenticationActivity;
import com.vnpay.authentication.VNP_SdkCompletedCallback;

public class CheckoutActivity extends AppCompatActivity {

    private CheckoutViewModel viewModel;
    private CartItemAdapter adapter;

    private TextView txtSubtotal, txtTotal;
    private MaterialCardView cardCash, cardVnpay, cardZaloPay;
    private MaterialButton btnConfirmPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Cấp quyền mạng đồng bộ (Bắt buộc cho ZaloPay)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Khởi tạo ZaloPay Sandbox
        ZaloPaySDK.init(2553, Environment.SANDBOX);

        initViews();
        setupRecyclerView();

        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);
        setupEvents();
        observeViewModel();

        // KÍCH HOẠT HÀM LẤY DỮ LIỆU THẬT THAY VÌ DỮ LIỆU ẢO
        loadRealCart();
    }

    private void initViews() {
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtTotal = findViewById(R.id.txtTotal);
        cardCash = findViewById(R.id.cardCash);
        cardVnpay = findViewById(R.id.cardMoMo);
        cardZaloPay = findViewById(R.id.cardZaloPay);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        RecyclerView recyclerCart = findViewById(R.id.recyclerCart);
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartItemAdapter();
        recyclerCart.setAdapter(adapter);
    }

    private void setupEvents() {
        cardCash.setOnClickListener(v -> viewModel.setPaymentMethod("CASH"));
        cardVnpay.setOnClickListener(v -> viewModel.setPaymentMethod("VNPAY"));
        cardZaloPay.setOnClickListener(v -> viewModel.setPaymentMethod("ZALOPAY"));

        btnConfirmPayment.setOnClickListener(v -> {
            String method = viewModel.getPaymentMethod().getValue();
            double total = viewModel.getTotal().getValue() != null ? viewModel.getTotal().getValue() : 0;
            int finalAmount = (int) Math.round(total);

            if ("VNPAY".equals(method)) {
                requestVNPAYPayment(String.valueOf(finalAmount));
            } else if ("ZALOPAY".equals(method)) {
                requestZaloPayPayment(String.valueOf(finalAmount));
            } else {
                viewModel.processPayment();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getCartItems().observe(this, items -> adapter.setCartList(items));

        // ĐÃ SỬA: Gọi lớp PriceFormatter dùng chung để định dạng tiền
        viewModel.getSubtotal().observe(this, val -> txtSubtotal.setText(com.example.cafemanagement.helper.PriceFormatter.formatPrice(val)));
        viewModel.getTotal().observe(this, val -> txtTotal.setText(com.example.cafemanagement.helper.PriceFormatter.formatPrice(val)));

        viewModel.getPaymentMethod().observe(this, method -> {
            resetPaymentCards();
            if ("CASH".equals(method)) highlightCard(cardCash);
            else if ("VNPAY".equals(method)) highlightCard(cardVnpay);
            else if ("ZALOPAY".equals(method)) highlightCard(cardZaloPay);
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                btnConfirmPayment.setText("Đang xử lý...");
                btnConfirmPayment.setEnabled(false);
            } else {
                btnConfirmPayment.setText("Xác nhận & Thanh toán →");
                btnConfirmPayment.setEnabled(true);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getPaymentUrl().observe(this, url -> {
            if (url != null && url.equals("SUCCESS_CASH")) {
                String randomId = "CASH" + System.currentTimeMillis();
                Intent intent = new Intent(CheckoutActivity.this, OrderStatusActivity.class);
                intent.putExtra("ORDER_ID", randomId);
                startActivity(intent);
                finish();
            }
        });
    }

    private void resetPaymentCards() {
        cardCash.setStrokeWidth(0);
        cardCash.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        cardVnpay.setStrokeWidth(0);
        cardVnpay.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        cardZaloPay.setStrokeWidth(0);
        cardZaloPay.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
    }

    private void highlightCard(MaterialCardView card) {
        card.setStrokeWidth(4);
        card.setStrokeColor(Color.parseColor("#BCAAA4"));
        card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
    }

    /**
     * Hàm lấy dữ liệu giỏ hàng thực tế từ hệ thống và ánh xạ vào Checkout.
     */
    private void loadRealCart() {
        // Lấy danh sách gốc từ BasketManager giống hệt cách BasketActivity đang làm
        List<com.example.cafemanagement.model.BasketItemModel> realBasketList =
                com.example.cafemanagement.BasketManager.getInstance().getBasketItems();

        if (realBasketList != null && !realBasketList.isEmpty()) {
            List<CartItemModel> mappedCartList = new ArrayList<>();

            for (int i = 0; i < realBasketList.size(); i++) {
                com.example.cafemanagement.model.BasketItemModel bItem = realBasketList.get(i);

                // Tính tổng tiền của món này (Giá x Số lượng)
                double itemTotal = bItem.getPrice() * bItem.getQuantity();

                mappedCartList.add(new CartItemModel(
                        String.valueOf(i),
                        "Món trong giỏ " + (i + 1), // Hãy sửa lại thành hàm get tên thực tế ở đây
                        bItem.getPrice(),
                        bItem.getQuantity(),
                        "Mặc định",
                        "",
                        "",
                        itemTotal
                ));
            }
            // Đưa toàn bộ danh sách đã ánh xạ vào ViewModel.
            // Hệ thống sẽ tự động vòng lặp và cộng dồn ra TỔNG TIỀN thanh toán chính xác.
            viewModel.loadCartData(mappedCartList);
        } else {
            // Giữ lại hàm dữ liệu ảo để dự phòng lỗi crash nếu giỏ hàng rỗng
            createDummyCart();
        }
    }

    private void createDummyCart() {
        List<CartItemModel> list = new ArrayList<>();
        list.add(new CartItemModel("1", "Ethiopian Pour Over", 5.50, 1, "Medium", "", "", 5.50));
        viewModel.loadCartData(list);
    }

    // ==========================================
    // KHU VỰC TÍCH HỢP VNPAY PAYMENT
    // ==========================================
    private void requestVNPAYPayment(String totalAmount) {
        String txnRef = String.valueOf(System.currentTimeMillis());
        String paymentUrl = com.example.cafemanagement.helper.VNPAYHelper.getPaymentUrl(totalAmount, txnRef);

        Intent intent = new Intent(this, VNP_AuthenticationActivity.class);
        intent.putExtra("url", paymentUrl);
        intent.putExtra("tmn_code", "P18EMF3O");
        intent.putExtra("scheme", "vnpay");
        intent.putExtra("is_sandbox", true);

        VNP_AuthenticationActivity.setSdkCompletedCallback(new VNP_SdkCompletedCallback() {
            @Override
            public void sdkAction(String action) {
                // ĐÃ SỬA: Xóa bỏ dòng runOnUiThread bị gọi lồng 2 lần và chỉnh lại dấu ngoặc cho chuẩn xác
                runOnUiThread(() -> {
                    if ("SuccessBackAction".equals(action)) {
                        // CHUYỂN HƯỚNG SANG MÀN HÌNH TRẠNG THÁI
                        Intent intentStatus = new Intent(CheckoutActivity.this, OrderStatusActivity.class);
                        intentStatus.putExtra("ORDER_ID", "VNP" + txnRef);
                        startActivity(intentStatus);
                        finish(); // Đóng màn hình Checkout lại
                    } else if ("FaildBackAction".equals(action)) {
                        Toast.makeText(CheckoutActivity.this, "Giao dịch thất bại", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CheckoutActivity.this, "Trạng thái: " + action, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        startActivity(intent);
    }

    // ==========================================
    // KHU VỰC TÍCH HỢP ZALOPAY PAYMENT
    // ==========================================
    private void requestZaloPayPayment(String totalAmount) {
        CreateOrder orderApi = new CreateOrder();
        try {
            JSONObject data = orderApi.createOrder(totalAmount);
            String code = data.getString("returncode");

            if (code.equals("1")) {
                String token = data.getString("zptranstoken");
                ZaloPaySDK.getInstance().payOrder(CheckoutActivity.this, token, "demozpdk://app", new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(final String transactionId, final String transToken, final String appTransID) {
                        runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Thanh toán ZaloPay thành công!", Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onPaymentCanceled(String zpTransToken, String appTransID) {
                        runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Bạn đã hủy giao dịch ZaloPay", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                        runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Lỗi thanh toán ZaloPay", Toast.LENGTH_LONG).show());
                    }
                });
            } else {
                Toast.makeText(this, "Lỗi tạo đơn ZaloPay", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // HỨNG KẾT QUẢ TỪ APP NGÂN HÀNG GỌI VỀ
    // ==========================================
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null) {
            String scheme = uri.getScheme();

            if ("demozpdk".equals(scheme)) {
                ZaloPaySDK.getInstance().onResult(intent);
            }
            else if ("vnpay".equals(scheme)) {
                Toast.makeText(this, "Đã quay lại từ App Ngân hàng (VNPAY)", Toast.LENGTH_LONG).show();
            }
        }
    }
}
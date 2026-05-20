package com.example.cafemanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.model.CartItemModel;

import java.util.List;

public class CheckoutViewModel extends ViewModel {

    private final MutableLiveData<List<CartItemModel>> cartItemsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Double> subtotalLiveData = new MutableLiveData<>(0.0);
    // ĐÃ XÓA: vatLiveData theo yêu cầu loại bỏ thuế VAT khỏi ứng dụng
    private final MutableLiveData<Double> totalLiveData = new MutableLiveData<>(0.0);
    private final MutableLiveData<String> paymentMethodLiveData = new MutableLiveData<>("CASH");

    // LiveData để quản lý trạng thái UI và chuyển hướng
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> paymentUrl = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * Hàm nạp dữ liệu giỏ hàng và tính toán tiền.
     * Chức năng: Tính tổng tiền trực tiếp không bao gồm VAT.
     */
    public void loadCartData(List<CartItemModel> items) {
        cartItemsLiveData.setValue(items);
        double subtotal = 0;
        for (CartItemModel item : items) {
            subtotal += item.getTotalPrice();
        }

        // Đã bỏ tính VAT, tổng cộng bằng chính giá tạm tính
        subtotalLiveData.setValue(subtotal);
        totalLiveData.setValue(subtotal);
    }

    /**
     * Hàm cập nhật phương thức thanh toán do người dùng chọn trên giao diện.
     */
    public void setPaymentMethod(String method) {
        paymentMethodLiveData.setValue(method);
    }

    /**
     * Hàm xử lý luồng thanh toán.
     * Chức năng: Vì ZaloPay và MoMo đã được bắt sự kiện và xử lý bằng SDK trực tiếp
     * bên trong CheckoutActivity, ViewModel giờ đây chỉ phụ trách việc xử lý đơn hàng Tiền mặt.
     */
    public void processPayment() {
        String method = paymentMethodLiveData.getValue();

        if ("CASH".equals(method)) {
            isLoading.setValue(true);

            // TODO: (Tương lai) Gọi Repository để lưu đơn hàng vào Firebase Realtime Database tại đây

            // Trả về tín hiệu thành công để Activity biết và chuyển màn hình
            paymentUrl.setValue("SUCCESS_CASH");
            isLoading.setValue(false);
        }

        // Không xử lý MoMo và ZaloPay ở đây nữa để tránh xung đột với SDK bên Activity
    }

    // ==========================================
    // KHU VỰC GETTERS ĐỂ ACTIVITY QUAN SÁT (OBSERVE) DỮ LIỆU
    // ==========================================
    public LiveData<List<CartItemModel>> getCartItems() { return cartItemsLiveData; }
    public LiveData<Double> getSubtotal() { return subtotalLiveData; }
    public LiveData<Double> getTotal() { return totalLiveData; }
    public LiveData<String> getPaymentMethod() { return paymentMethodLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getPaymentUrl() { return paymentUrl; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
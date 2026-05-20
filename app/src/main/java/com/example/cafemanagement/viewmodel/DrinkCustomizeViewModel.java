package com.example.cafemanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DrinkCustomizeViewModel extends ViewModel {

    // Biến lưu trữ giá trị gốc của món ăn (ví dụ: Cà phê đen giá 10$)
    private double basePrice = 0.0;

    // Biến lưu trữ số tiền cộng thêm khi người dùng đổi kích cỡ (ví dụ: Size Lớn cộng 1$)
    private double sizeModifier = 0.0;

    // LiveData để tự động cập nhật giao diện ngay khi dữ liệu thay đổi
    private final MutableLiveData<Integer> quantity = new MutableLiveData<>(1);
    private final MutableLiveData<Double> totalPrice = new MutableLiveData<>(0.0);

    /**
     * Hàm setBasePrice: Nhận mức giá gốc từ màn hình trước truyền sang.
     * Sau khi nhận giá, nó tự động gọi hàm updateTotalPrice() để tính lại tổng tiền.
     */
    public void setBasePrice(double price) {
        this.basePrice = price;
        updateTotalPrice();
    }

    /**
     * Hàm setSizeModifier: Nhận mức tiền phụ phí khi người dùng chọn Size.
     * Lưu phụ phí này vào biến sizeModifier và gọi hàm tính lại tổng tiền.
     */
    public void setSizeModifier(double modifier) {
        this.sizeModifier = modifier;
        updateTotalPrice();
    }

    /**
     * Hàm getQuantity: Cung cấp dữ liệu số lượng ly hiện tại ra bên ngoài.
     * Màn hình giao diện sẽ dùng hàm này để theo dõi và in con số lên màn hình.
     */
    public LiveData<Integer> getQuantity() {
        return quantity;
    }

    /**
     * Hàm getTotalPrice: Cung cấp dữ liệu tổng tiền hiện tại ra bên ngoài.
     * Màn hình giao diện sẽ dùng hàm này để in số tiền lên nút Thêm vào giỏ.
     */
    public LiveData<Double> getTotalPrice() {
        return totalPrice;
    }

    /**
     * Hàm increaseQty: Xử lý sự kiện khi người dùng bấm nút dấu cộng (+).
     * Nó lấy số lượng hiện tại, cộng thêm 1, sau đó gọi hàm tính lại tổng tiền.
     */
    public void increaseQty() {
        int currentQty = quantity.getValue() != null ? quantity.getValue() : 1;
        quantity.setValue(currentQty + 1);
        updateTotalPrice();
    }

    /**
     * Hàm decreaseQty: Xử lý sự kiện khi người dùng bấm nút dấu trừ (-).
     * Nó kiểm tra nếu số lượng lớn hơn 1 thì mới cho trừ (để tránh số lượng bị âm hoặc bằng 0),
     * sau đó gọi hàm tính lại tổng tiền.
     */
    public void decreaseQty() {
        int currentQty = quantity.getValue() != null ? quantity.getValue() : 1;
        if (currentQty > 1) {
            quantity.setValue(currentQty - 1);
            updateTotalPrice();
        }
    }

    /**
     * Hàm updateTotalPrice: Đây là hàm quan trọng nhất để tính nhẩm số tiền.
     * Công thức: (Giá gốc + Phụ phí kích cỡ) * Số lượng.
     * Kết quả tính được sẽ lưu vào LiveData để giao diện tự động cập nhật.
     */
    private void updateTotalPrice() {
        int currentQty = quantity.getValue() != null ? quantity.getValue() : 1;
        double total = (basePrice + sizeModifier) * currentQty;
        totalPrice.setValue(total);
    }
}
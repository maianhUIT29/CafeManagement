package com.example.cafemanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.model.VoucherModel;
import com.example.cafemanagement.repository.VoucherRepository;

import java.util.List;

public class AdminVoucherViewModel extends ViewModel {

    private final VoucherRepository repository;
    private final MutableLiveData<List<VoucherModel>> voucherListLiveData;
    private final MutableLiveData<String> actionStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);

    public AdminVoucherViewModel() {
        repository = new VoucherRepository();
        voucherListLiveData = repository.getVouchers();
    }

    public LiveData<List<VoucherModel>> getVoucherList() {
        return voucherListLiveData;
    }

    public LiveData<String> getActionStatus() {
        return actionStatusLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    // Hàm gọi Thêm/Sửa từ Repository
    public void saveVoucher(String code, String discountStr, String description) {
        isLoadingLiveData.setValue(true);

        try {
            int discount = Integer.parseInt(discountStr);
            if (discount < 1 || discount > 100) {
                actionStatusLiveData.setValue("Lỗi: Mức giảm giá phải từ 1 đến 100");
                isLoadingLiveData.setValue(false);
                return;
            }

            VoucherModel voucher = new VoucherModel();
            voucher.setCode(code);
            voucher.setDiscount(discount);
            voucher.setDescription(description);

            repository.saveVoucher(voucher, new VoucherRepository.RepoCallback() {
                @Override
                public void onSuccess(String message) {
                    actionStatusLiveData.setValue(message);
                    isLoadingLiveData.setValue(false);
                }

                @Override
                public void onError(String error) {
                    actionStatusLiveData.setValue(error);
                    isLoadingLiveData.setValue(false);
                }
            });

        } catch (NumberFormatException e) {
            actionStatusLiveData.setValue("Lỗi: Định dạng mức giảm giá không hợp lệ");
            isLoadingLiveData.setValue(false);
        }
    }

    // Hàm gọi Xóa từ Repository
    public void deleteVoucher(String code) {
        isLoadingLiveData.setValue(true);
        repository.deleteVoucher(code, new VoucherRepository.RepoCallback() {
            @Override
            public void onSuccess(String message) {
                actionStatusLiveData.setValue(message);
                isLoadingLiveData.setValue(false);
            }

            @Override
            public void onError(String error) {
                actionStatusLiveData.setValue(error);
                isLoadingLiveData.setValue(false);
            }
        });
    }
}
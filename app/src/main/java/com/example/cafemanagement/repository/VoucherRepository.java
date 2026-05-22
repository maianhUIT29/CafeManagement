package com.example.cafemanagement.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.cafemanagement.model.VoucherModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VoucherRepository {

    private final DatabaseReference databaseReference;

    public VoucherRepository() {
        // Khởi tạo kết nối tới nhánh "Vouchers" trên Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Vouchers");
    }

    // READ: Lấy danh sách Voucher theo thời gian thực
    public MutableLiveData<List<VoucherModel>> getVouchers() {
        MutableLiveData<List<VoucherModel>> liveData = new MutableLiveData<>();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<VoucherModel> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    VoucherModel voucher = dataSnapshot.getValue(VoucherModel.class);
                    if (voucher != null) {
                        list.add(voucher);
                    }
                }
                liveData.postValue(list);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                liveData.postValue(null);
            }
        });

        return liveData;
    }

    // CREATE / UPDATE: Thêm hoặc Cập nhật Voucher
    // Do cấu trúc Voucher của bạn dùng "Code" làm định danh, ta sẽ dùng Code làm Key trên Firebase
    public void saveVoucher(VoucherModel voucher, final RepoCallback callback) {
        if (voucher.getCode() == null || voucher.getCode().trim().isEmpty()) {
            callback.onError("Mã Voucher không hợp lệ");
            return;
        }

        String key = voucher.getCode().toUpperCase().trim();
        voucher.setCode(key); // Ép kiểu in hoa

        databaseReference.child(key).setValue(voucher)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Lưu Voucher thành công"))
                .addOnFailureListener(e -> callback.onError("Lỗi khi lưu: " + e.getMessage()));
    }

    // DELETE: Xóa Voucher
    public void deleteVoucher(String voucherCode, final RepoCallback callback) {
        databaseReference.child(voucherCode).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess("Xóa Voucher thành công"))
                .addOnFailureListener(e -> callback.onError("Lỗi khi xóa: " + e.getMessage()));
    }

    // Interface để gửi kết quả phản hồi ngược lại cho ViewModel
    public interface RepoCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}
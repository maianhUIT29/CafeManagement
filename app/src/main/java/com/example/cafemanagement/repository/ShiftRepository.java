package com.example.cafemanagement.repository;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

public class ShiftRepository {

    private final DatabaseReference historyRef;
    private final double FIXED_PETTY_CASH = 1000000.0; // Quỹ đầu ngày cố định 1 triệu

    public ShiftRepository() {
        historyRef = FirebaseDatabase.getInstance().getReference("HistoryRevenue");
    }

    public interface ShiftCloseCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    /**
     * Hàm xử lý khi Thu ngân bấm kết ca
     */
    public void closeShiftWithCashLogic(String historyId, String shiftConfigId, double currentShiftRevenue, ShiftCloseCallback callback) {

        // 1. Lấy mốc thời gian 00:00:00 và 23:59:59 của ngày hôm nay
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endOfDay = calendar.getTimeInMillis();

        // 2. Truy vấn Firebase để tính tổng doanh thu của các ca ĐÃ ĐÓNG trong hôm nay
        historyRef.orderByChild("openedAt").startAt(startOfDay).endAt(endOfDay)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double previousRevenueToday = 0.0;

                        for (DataSnapshot doc : snapshot.getChildren()) {
                            // Chỉ cộng tiền của các ca khác đã đóng thành công (không cộng chính ca đang mở này)
                            String status = doc.child("status").getValue(String.class);
                            String docId = doc.getKey();

                            if ("CLOSED".equals(status) && docId != null && !docId.equals(historyId)) {
                                Double rev = doc.child("totalRevenue").getValue(Double.class);
                                if (rev != null) {
                                    previousRevenueToday += rev;
                                }
                            }
                        }

                        // 3. Tính toán Tiền dự kiến trong két
                        double expectedCash = FIXED_PETTY_CASH + previousRevenueToday + currentShiftRevenue;
                        double depositAmount = 0.0;

                        // 4. Nếu là ca Tối (Đóng sổ), tự động tính số tiền cần cất đi
                        if ("SH_EVENING".equals(shiftConfigId)) {
                            // Tiền nộp = Toàn bộ doanh thu trong ngày (Chỉ để lại 1 triệu)
                            depositAmount = previousRevenueToday + currentShiftRevenue;
                        }

                        // 5. Cập nhật dữ liệu lên Firebase
                        HashMap<String, Object> updates = new HashMap<>();
                        updates.put("status", "CLOSED");
                        updates.put("closedAt", System.currentTimeMillis());
                        updates.put("expectedCashInDrawer", expectedCash);
                        updates.put("depositAmount", depositAmount);

                        historyRef.child(historyId).updateChildren(updates)
                                .addOnSuccessListener(aVoid -> callback.onSuccess("Kết ca thành công!"))
                                .addOnFailureListener(e -> callback.onError("Lỗi lưu dữ liệu: " + e.getMessage()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError("Lỗi mạng: " + error.getMessage());
                    }
                });
    }
}
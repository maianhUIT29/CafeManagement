package com.example.cafemanagement.repository;

import androidx.annotation.NonNull;

import com.example.cafemanagement.model.StaffSalaryModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SalaryRepository {

    private final DatabaseReference dbRef;

    public SalaryRepository() {
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public interface SalaryCalculateCallback {
        void onCalculated(List<StaffSalaryModel> salaryList);
        void onError(String error);
    }

    public void calculateMonthlySalary(int targetMonth, int targetYear, SalaryCalculateCallback callback) {
        dbRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                List<StaffSalaryModel> resultList = new ArrayList<>();
                long totalUsers = usersSnapshot.getChildrenCount();

                if (totalUsers == 0) {
                    callback.onCalculated(resultList);
                    return;
                }

                final int[] processedCount = {0};

                for (DataSnapshot userDoc : usersSnapshot.getChildren()) {
                    String userId = userDoc.getKey();
                    String name = userDoc.child("name").getValue(String.class);
                    String role = userDoc.child("role").getValue(String.class);
                    Double salaryRateVal = userDoc.child("salaryRate").getValue(Double.class);
                    double salaryRate = salaryRateVal != null ? salaryRateVal : 0;

                    // Bỏ qua khách hàng hoặc nhân viên chưa cài đặt mức lương
                    if (role == null || role.equals("customer") || role.equals("admin") || salaryRate == 0) {
                        processedCount[0]++;
                        if (processedCount[0] == totalUsers) {
                            callback.onCalculated(resultList);
                        }
                        continue;
                    }

                    // Tách luồng xử lý riêng cho Thu ngân và Pha chế
                    if (role.equals("cashier")) {
                        fetchCashierHours(userId, targetMonth, targetYear, new HoursCallback() {
                            @Override
                            public void onHoursRetrieved(double totalHours) {
                                double totalSalary = totalHours * salaryRate;
                                resultList.add(new StaffSalaryModel(userId, name, role, salaryRate, totalHours, totalSalary));
                                processedCount[0]++;
                                if (processedCount[0] == totalUsers) callback.onCalculated(resultList);
                            }
                        });
                    } else if (role.equals("barista")) {
                        fetchBaristaHours(userId, targetMonth, targetYear, new HoursCallback() {
                            @Override
                            public void onHoursRetrieved(double totalHours) {
                                double totalSalary = totalHours * salaryRate;
                                resultList.add(new StaffSalaryModel(userId, name, role, salaryRate, totalHours, totalSalary));
                                processedCount[0]++;
                                if (processedCount[0] == totalUsers) callback.onCalculated(resultList);
                            }
                        });
                    } else {
                        processedCount[0]++;
                        if (processedCount[0] == totalUsers) callback.onCalculated(resultList);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private interface HoursCallback {
        void onHoursRetrieved(double totalHours);
    }

    private void fetchCashierHours(String cashierId, int targetMonth, int targetYear, HoursCallback callback) {
        dbRef.child("ShiftSessions").orderByChild("cashierId").equalTo(cashierId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double totalHours = 0;
                        Calendar calendar = Calendar.getInstance();

                        for (DataSnapshot sessionDoc : snapshot.getChildren()) {
                            String status = sessionDoc.child("status").getValue(String.class);
                            Long openedAt = sessionDoc.child("openedAt").getValue(Long.class);
                            Long closedAt = sessionDoc.child("closedAt").getValue(Long.class);

                            if (status != null && status.equals("CLOSED") && openedAt != null && closedAt != null) {
                                calendar.setTimeInMillis(openedAt);
                                int month = calendar.get(Calendar.MONTH) + 1;
                                int year = calendar.get(Calendar.YEAR);

                                if (month == targetMonth && year == targetYear) {
                                    long durationMs = closedAt - openedAt;
                                    totalHours += (double) durationMs / (1000.0 * 60.0 * 60.0);
                                }
                            }
                        }
                        callback.onHoursRetrieved(totalHours);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onHoursRetrieved(0);
                    }
                });
    }

    private void fetchBaristaHours(String baristaId, int targetMonth, int targetYear, HoursCallback callback) {
        dbRef.child("BaristaShifts").orderByChild("baristaId").equalTo(baristaId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double totalHours = 0;
                        Calendar calendar = Calendar.getInstance();

                        for (DataSnapshot shiftDoc : snapshot.getChildren()) {
                            Long startTime = shiftDoc.child("startTime").getValue(Long.class);
                            Long endTime = shiftDoc.child("endTime").getValue(Long.class);

                            if (startTime != null && endTime != null) {
                                calendar.setTimeInMillis(startTime);
                                int month = calendar.get(Calendar.MONTH) + 1;
                                int year = calendar.get(Calendar.YEAR);

                                if (month == targetMonth && year == targetYear) {
                                    long durationMs = endTime - startTime;
                                    totalHours += (double) durationMs / (1000.0 * 60.0 * 60.0);
                                }
                            }
                        }
                        callback.onHoursRetrieved(totalHours);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onHoursRetrieved(0);
                    }
                });
    }
}
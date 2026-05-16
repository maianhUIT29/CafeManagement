package com.example.cafemanagement.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserRepository {
    private static final Set<String> STAFF_ROLES = new HashSet<>(Arrays.asList("admin", "cashier", "barista"));

    private final DatabaseReference usersRef;

    public UserRepository() {
        usersRef = FirebaseHelper.getUsersRef();
    }

    public MutableLiveData<List<UserModel>> getStaffUsers() {
        MutableLiveData<List<UserModel>> liveData = new MutableLiveData<>();
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<UserModel> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    UserModel user = ds.getValue(UserModel.class);
                    if (user != null && user.getRole() != null && STAFF_ROLES.contains(user.getRole())) {
                        user.setUserId(ds.getKey());
                        list.add(user);
                    }
                }
                liveData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseHelper.logDatabaseError("UserRepository.getStaffUsers", error);
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    public void addStaff(UserModel user, RepositoryCallback callback) {
        String key = usersRef.push().getKey();
        if (key == null) {
            callback.onError("Không tạo được ID nhân viên");
            return;
        }
        user.setUserId(key);
        usersRef.child(key).setValue(user)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateStaff(UserModel user, RepositoryCallback callback) {
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            callback.onError("Thiếu ID nhân viên");
            return;
        }
        usersRef.child(user.getUserId()).setValue(user)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteStaff(String userId, RepositoryCallback callback) {
        usersRef.child(userId).removeValue()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}

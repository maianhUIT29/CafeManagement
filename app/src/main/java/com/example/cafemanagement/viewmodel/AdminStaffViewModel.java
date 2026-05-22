package com.example.cafemanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.model.UserModel;
import com.example.cafemanagement.repository.RepositoryCallback;
import com.example.cafemanagement.repository.UserRepository;

import java.util.List;

public class AdminStaffViewModel extends ViewModel {
    private final UserRepository userRepository = new UserRepository();

    public LiveData<List<UserModel>> getStaff() {
        return userRepository.getStaffUsers();
    }

    public void saveStaff(UserModel user, boolean isEdit, RepositoryCallback callback) {
        if (isEdit) {
            userRepository.updateStaff(user, callback);
        } else {
            userRepository.addStaff(user, callback);
        }
    }

    public void deleteStaff(String userId, RepositoryCallback callback) {
        userRepository.deleteStaff(userId, callback);
    }
}

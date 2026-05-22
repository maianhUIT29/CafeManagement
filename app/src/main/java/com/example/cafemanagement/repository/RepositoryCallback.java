package com.example.cafemanagement.repository;

public interface RepositoryCallback {
    void onSuccess();
    void onError(String message);
}

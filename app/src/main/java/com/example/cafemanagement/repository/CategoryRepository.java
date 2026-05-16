package com.example.cafemanagement.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.CategoryModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {
    private final DatabaseReference categoriesRef;

    public CategoryRepository() {
        this.categoriesRef = FirebaseHelper.getDatabaseInstance().getReference("Categories");
    }

    public MutableLiveData<List<CategoryModel>> getCategories() {
        MutableLiveData<List<CategoryModel>> data = new MutableLiveData<>();
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CategoryModel> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    CategoryModel cat = ds.getValue(CategoryModel.class);
                    if (cat != null) {
                        cat.setId(ds.getKey());
                        list.add(cat);
                    }
                }
                data.setValue(list);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseHelper.logDatabaseError("CategoryRepository.getCategories", error);
                data.setValue(null);
            }
        });
        return data;
    }

    public void addCategory(CategoryModel category, RepositoryCallback callback) {
        String key = categoriesRef.push().getKey();
        if (key == null) {
            callback.onError("Không tạo được ID danh mục");
            return;
        }
        category.setId(key);
        categoriesRef.child(key).setValue(category)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateCategory(CategoryModel category, RepositoryCallback callback) {
        if (category.getId() == null || category.getId().isEmpty()) {
            callback.onError("Thiếu ID danh mục");
            return;
        }
        categoriesRef.child(category.getId()).setValue(category)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteCategory(String categoryId, RepositoryCallback callback) {
        categoriesRef.child(categoryId).removeValue()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
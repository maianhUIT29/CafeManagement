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
            public void onCancelled(@NonNull DatabaseError error) { data.setValue(null); }
        });
        return data;
    }
}
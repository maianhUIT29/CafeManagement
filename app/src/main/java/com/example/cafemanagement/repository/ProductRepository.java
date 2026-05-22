package com.example.cafemanagement.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.ProductModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    private final DatabaseReference productsRef;

    public ProductRepository() {
        this.productsRef = FirebaseHelper.getProductsRef();
    }

    public MutableLiveData<List<ProductModel>> getAllProducts() {
        MutableLiveData<List<ProductModel>> data = new MutableLiveData<>();
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ProductModel> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ProductModel product = ds.getValue(ProductModel.class);
                    if (product != null) {
                        product.setProductId(ds.getKey());
                        list.add(product);
                    }
                }
                data.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseHelper.logDatabaseError("ProductRepository.getAllProducts", error);
                data.setValue(null);
            }
        });
        return data;
    }

    public void addProduct(ProductModel product, RepositoryCallback callback) {
        String key = productsRef.push().getKey();
        if (key == null) {
            callback.onError("Không tạo được ID món");
            return;
        }
        product.setProductId(key);
        productsRef.child(key).setValue(product)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateProduct(ProductModel product, RepositoryCallback callback) {
        if (product.getProductId() == null || product.getProductId().isEmpty()) {
            callback.onError("Thiếu ID món");
            return;
        }
        productsRef.child(product.getProductId()).setValue(product)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteProduct(String productId, RepositoryCallback callback) {
        productsRef.child(productId).removeValue()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
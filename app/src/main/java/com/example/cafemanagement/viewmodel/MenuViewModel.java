package com.example.cafemanagement.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.helper.FirebaseHelper;
import com.example.cafemanagement.model.CategoryModel;
import com.example.cafemanagement.model.ProductModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MenuViewModel extends ViewModel {

    // --- LiveData cho Fragment observe ---
    private final MutableLiveData<List<CategoryModel>> catList  = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>>        catIds   = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ProductModel>>  prodList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>>        prodIds  = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String>              selectedCatId = new MutableLiveData<>("all");

    // --- State nội bộ ---
    private final Map<String, CategoryModel> categoriesMap = new LinkedHashMap<>();
    private final Map<String, ProductModel>  productsMap   = new LinkedHashMap<>();

    // --- Getters ---
    public LiveData<List<CategoryModel>> getCatList()     { return catList; }
    public LiveData<List<String>>        getCatIds()      { return catIds; }
    public LiveData<List<ProductModel>>  getProdList()    { return prodList; }
    public LiveData<List<String>>        getProdIds()     { return prodIds; }
    public LiveData<String>              getSelectedCatId() { return selectedCatId; }

    // --- Load data (chỉ gọi 1 lần) ---
    public void loadData() {
        loadCategories();
        loadProducts();
    }

    private void loadCategories() {
        FirebaseHelper.getCategoriesRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        categoriesMap.clear();
                        List<String>        ids  = new ArrayList<>();
                        List<CategoryModel> list = new ArrayList<>();

                        // Tab "Tất cả" luôn đứng đầu
                        ids.add("all");
                        list.add(makeCatAll());

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            CategoryModel c = ds.getValue(CategoryModel.class);
                            if (c != null) {
                                categoriesMap.put(ds.getKey(), c);
                                ids.add(ds.getKey());
                                list.add(c);
                            }
                        }
                        catIds.setValue(ids);
                        catList.setValue(list);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void loadProducts() {
        FirebaseHelper.getProductsRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productsMap.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ProductModel p = ds.getValue(ProductModel.class);
                            if (p != null) productsMap.put(ds.getKey(), p);
                        }
                        // Filter theo category đang chọn
                        filterProducts(selectedCatId.getValue() != null
                                ? selectedCatId.getValue() : "all");
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    public void selectCategory(String catId) {
        selectedCatId.setValue(catId);
        filterProducts(catId);
    }

    private void filterProducts(String catId) {
        List<String>       ids  = new ArrayList<>();
        List<ProductModel> list = new ArrayList<>();
        for (Map.Entry<String, ProductModel> e : productsMap.entrySet()) {
            if ("all".equals(catId) || catId.equals(e.getValue().getCategoryId())) {
                ids.add(e.getKey());
                list.add(e.getValue());
            }
        }
        prodIds.setValue(ids);
        prodList.setValue(list);
    }

    private CategoryModel makeCatAll() {
        CategoryModel c = new CategoryModel();
        c.setName("Tất cả");
        return c;
    }
}
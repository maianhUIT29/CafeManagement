package com.example.cafemanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.cafemanagement.model.CategoryModel;
import com.example.cafemanagement.model.ProductModel;
import com.example.cafemanagement.repository.CategoryRepository;
import com.example.cafemanagement.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;

public class MenuViewModel extends ViewModel {
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    private final LiveData<List<ProductModel>> allProducts;
    private final MutableLiveData<List<ProductModel>> filteredProducts = new MutableLiveData<>();
    private final LiveData<List<CategoryModel>> categories;
    private final MutableLiveData<String> selectedCategoryId = new MutableLiveData<>("cat_01");

    public MenuViewModel() {
        productRepo = new ProductRepository();
        categoryRepo = new CategoryRepository();
        allProducts = productRepo.getAllProducts();
        categories = categoryRepo.getCategories();
    }

    public LiveData<List<CategoryModel>> getCategories() { return categories; }
    public LiveData<List<ProductModel>> getFilteredProducts() { return filteredProducts; }

    public void filterByCategory(String categoryId) {
        selectedCategoryId.setValue(categoryId);
        List<ProductModel> all = allProducts.getValue();
        if (all != null) {
            List<ProductModel> filtered = new ArrayList<>();
            for (ProductModel p : all) {
                if (p.getCategoryId().equals(categoryId)) filtered.add(p);
            }
            filteredProducts.setValue(filtered);
        }
    }
}
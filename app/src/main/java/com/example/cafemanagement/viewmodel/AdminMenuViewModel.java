package com.example.cafemanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.model.CategoryModel;
import com.example.cafemanagement.model.ProductModel;
import com.example.cafemanagement.repository.CategoryRepository;
import com.example.cafemanagement.repository.ProductRepository;
import com.example.cafemanagement.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class AdminMenuViewModel extends ViewModel {

    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final ProductRepository productRepository = new ProductRepository();

    private final MutableLiveData<List<ProductModel>> filteredProducts = new MutableLiveData<>();

    private String selectedCategoryId = null;

    public LiveData<List<CategoryModel>> getCategories() {
        return categoryRepository.getCategories();
    }

    public LiveData<List<ProductModel>> getAllProducts() {
        return productRepository.getAllProducts();
    }

    public LiveData<List<ProductModel>> getFilteredProducts() {
        return filteredProducts;
    }

    public void setCategoryFilter(String categoryId) {
        selectedCategoryId = categoryId;
    }

    public void applyFilter(List<ProductModel> products) {
        if (products == null) {
            filteredProducts.setValue(new ArrayList<>());
            return;
        }

        if (selectedCategoryId == null || selectedCategoryId.trim().isEmpty()) {
            filteredProducts.setValue(products);
            return;
        }

        List<ProductModel> list = new ArrayList<>();

        for (ProductModel product : products) {
            if (product != null && selectedCategoryId.equals(product.getCategoryId())) {
                list.add(product);
            }
        }

        filteredProducts.setValue(list);
    }

    public void saveCategory(CategoryModel category, boolean isEdit, RepositoryCallback callback) {
        if (isEdit) {
            categoryRepository.updateCategory(category, callback);
        } else {
            categoryRepository.addCategory(category, callback);
        }
    }

    public void deleteCategory(String categoryId, RepositoryCallback callback) {
        categoryRepository.deleteCategory(categoryId, callback);
    }

    public void saveProduct(ProductModel product, boolean isEdit, RepositoryCallback callback) {
        if (isEdit) {
            productRepository.updateProduct(product, callback);
        } else {
            productRepository.addProduct(product, callback);
        }
    }

    public void deleteProduct(String productId, RepositoryCallback callback) {
        productRepository.deleteProduct(productId, callback);
    }
}
package com.example.cafemanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.cafemanagement.model.ProductModel;
import com.example.cafemanagement.repository.ProductRepository;
import java.util.List;

public class ProductViewModel extends ViewModel {
    private final ProductRepository repository;
    private final LiveData<List<ProductModel>> productList;

    public ProductViewModel() {
        repository = new ProductRepository();
        productList = repository.getAllProducts();
    }

    public LiveData<List<ProductModel>> getProductList() {
        return productList;
    }
}
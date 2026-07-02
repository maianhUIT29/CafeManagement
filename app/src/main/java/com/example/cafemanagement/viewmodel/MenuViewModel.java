package com.example.cafemanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cafemanagement.model.CategoryModel;
import com.example.cafemanagement.model.ProductModel;
import com.example.cafemanagement.repository.CategoryRepository;
import com.example.cafemanagement.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;

public class MenuViewModel extends ViewModel {

    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;

    // --- LiveData for UI ---
    private final MediatorLiveData<List<CategoryModel>> catList = new MediatorLiveData<>();
    private final MutableLiveData<List<String>> catIds = new MutableLiveData<>(new ArrayList<>());
    private final MediatorLiveData<List<ProductModel>> prodList = new MediatorLiveData<>();
    private final MutableLiveData<List<String>> prodIds = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> selectedCatId = new MutableLiveData<>("all");

    public MenuViewModel() {
        categoryRepo = new CategoryRepository();
        productRepo = new ProductRepository();

        // Observe categories from repository and add "All" option
        LiveData<List<CategoryModel>> repoCategories = categoryRepo.getCategories();
        catList.addSource(repoCategories, categories -> {
            List<CategoryModel> list = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            
            list.add(makeCatAll());
            ids.add("all");
            
            if (categories != null) {
                list.addAll(categories);
                for (CategoryModel c : categories) {
                    if (c.getId() != null) ids.add(c.getId());
                }
            }
            catList.setValue(list);
            catIds.setValue(ids);
        });

        // Observe products and filter based on selected category
        LiveData<List<ProductModel>> repoProducts = productRepo.getAllProducts();
        prodList.addSource(repoProducts, products -> filterProducts(products, selectedCatId.getValue()));
        prodList.addSource(selectedCatId, catId -> filterProducts(repoProducts.getValue(), catId));
    }

    // --- Getters ---
    public LiveData<List<CategoryModel>> getCatList() { return catList; }
    public LiveData<List<String>> getCatIds() { return catIds; }
    public LiveData<List<ProductModel>> getProdList() { return prodList; }
    public LiveData<List<String>> getProdIds() { return prodIds; }
    public LiveData<String> getSelectedCatId() { return selectedCatId; }

    // Compatibility getters for MenuActivity
    public LiveData<List<CategoryModel>> getCategories() { return catList; }
    public LiveData<List<ProductModel>> getFilteredProducts() { return prodList; }

    /**
     * Called to trigger initial data load if needed. 
     * In this reactive setup, repositories start loading via ValueEventListeners automatically.
     */
    public void loadData() {
        // Data flow is handled by MediatorLiveData sources
    }

    public void selectCategory(String catId) {
        selectedCatId.setValue(catId);
    }

    // Compatibility method for MenuActivity
    public void filterByCategory(String catId) {
        selectCategory(catId);
    }

    private void filterProducts(List<ProductModel> all, String catId) {
        if (all == null) {
            prodList.setValue(new ArrayList<>());
            prodIds.setValue(new ArrayList<>());
            return;
        }

        List<ProductModel> filteredList = new ArrayList<>();
        List<String> filteredIds = new ArrayList<>();

        for (ProductModel p : all) {
            if ("all".equals(catId) || (p.getCategoryId() != null && p.getCategoryId().equals(catId))) {
                filteredList.add(p);
                filteredIds.add(p.getProductId());
            }
        }
        prodList.setValue(filteredList);
        prodIds.setValue(filteredIds);
    }

    private CategoryModel makeCatAll() {
        CategoryModel c = new CategoryModel();
        c.setName("Tất cả");
        c.setId("all");
        return c;
    }
}

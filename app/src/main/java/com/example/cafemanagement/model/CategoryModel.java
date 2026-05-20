package com.example.cafemanagement.model;

public class CategoryModel {
    private String name;
    private int sortOrder;

    // Constructor mặc định cho Firebase
    public CategoryModel() {
    }

    public CategoryModel(String name, int sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
    }

    // Getter và Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
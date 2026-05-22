package com.example.cafemanagement.model;

public class CategoryModel {
    private String id;
    private String name;
    private String icon; // optional emoji hoặc icon URL

    public CategoryModel() {}

    public CategoryModel(String name, int sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    // Getter và Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
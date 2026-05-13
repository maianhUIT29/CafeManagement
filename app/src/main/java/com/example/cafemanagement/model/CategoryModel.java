package com.example.cafemanagement.model;

public class CategoryModel {
    private String name;
    private String icon; // optional emoji hoặc icon URL

    public CategoryModel() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
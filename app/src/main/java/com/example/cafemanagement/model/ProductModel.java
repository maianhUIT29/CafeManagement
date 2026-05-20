package com.example.cafemanagement.model;

import java.util.List;
import java.util.Map;

public class ProductModel {
    private String categoryId;
    private String name;
    private int price;
    private Options options; // Class lồng nhau để khớp với JSON

    public ProductModel() {
    }

    // Inner class để mapping object "options" trong JSON
    public static class Options {
        private Map<String, Integer> sizes; // Ví dụ: {"M": 0, "L": 5000}
        private List<Integer> sugar;        // Ví dụ: [0, 50, 100]

        public Options() {}

        public Map<String, Integer> getSizes() { return sizes; }
        public void setSizes(Map<String, Integer> sizes) { this.sizes = sizes; }

        public List<Integer> getSugar() { return sugar; }
        public void setSugar(List<Integer> sugar) { this.sugar = sugar; }
    }

    // Getter và Setter cho Product
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public Options getOptions() { return options; }
    public void setOptions(Options options) { this.options = options; }
}
package com.example.cafemanagement.model;

import java.util.List;
import java.util.Map;

public class ProductModel {
    private String productId;
    private String categoryId;
    private String name;
    private String description;
    private String imageUrl;
    private int price;
    private boolean available;
    private String imageUrl;
    private String description;
    private Options options;

    public ProductModel() {}

    public static class Options {
        private Map<String, Integer> sizes;
        private List<Integer> sugar;
        private List<Integer> ice;
        private Map<String, Integer> toppings;

        public Options() {}

        // Getter và Setter cho các trường bên trong Options
        public Map<String, Integer> getSizes() { return sizes; }
        public void setSizes(Map<String, Integer> sizes) { this.sizes = sizes; }
        public List<Integer> getSugar() { return sugar; }
        public void setSugar(List<Integer> sugar) { this.sugar = sugar; }

        public List<Integer> getIce() { return ice; }
        public void setIce(List<Integer> ice) { this.ice = ice; }

        public List<Integer> getIce() { return ice; }
        public void setIce(List<Integer> ice) { this.ice = ice; }
        public Map<String, Integer> getToppings() { return toppings; }
        public void setToppings(Map<String, Integer> toppings) { this.toppings = toppings; }
    }

    // Getter và Setter cho ProductModel
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Options getOptions() { return options; }
    public void setOptions(Options options) { this.options = options; }
}
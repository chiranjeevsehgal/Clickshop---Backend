package com.clickshop.dto;

public class ProductSummary {
    private int id;
    private String name;
    private String category;
    private String description;
    private float price;

   

    public ProductSummary(int id, String name, String category, String description, float price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
    }

    @Override
    public String toString() {
        return "ProductSummary [id=" + id + ", name=" + name + ", category=" + category + ", description=" + description
                + ", price=" + price + "]";
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public float getPrice() {
        return price;
    }
}

package com.clickshop.dto;

import com.clickshop.entity.Product;

public class GeminiRequest {
    private String prompt;
    private Product products[];

    public GeminiRequest() {
    }

    public Product[] getProducts() {
        return products;
    }

    public void setProducts(Product[] products) {
        this.products = products;
    }

    public GeminiRequest(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

}

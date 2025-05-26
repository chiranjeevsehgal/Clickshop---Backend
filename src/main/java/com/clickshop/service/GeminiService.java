package com.clickshop.service;

import java.util.List;

import com.clickshop.dto.ProductSummary;
import com.clickshop.entity.Product;

public interface GeminiService {
    
    public String generateAiResponse(String prompt);
    
    public String generateAiResponse_withProducts(String prompt, List<ProductSummary> productSummaries);
}

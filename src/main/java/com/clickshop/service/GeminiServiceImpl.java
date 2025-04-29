package com.clickshop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.clickshop.dto.ProductSummary;
import com.clickshop.entity.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiServiceImpl implements GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.model}")
    private String model;
    
    private final String API_URL = "https://generativelanguage.googleapis.com/v1/models/";

    @Override
    public String generateAiResponse(String prompt, List<ProductSummary> productSummaries) {
        try {
            System.out.println(productSummaries);
            System.out.println("Sending request to Gemini API for prompt: ");
            String enhancedPrompt = "You are an e-commerce shopping assistant. Provide concise, relevant product information. Only answer questions related to shopping, products, and e-commerce, if a question is unrelated to e-commerce, politely say that I am not allowed to help on this, keep responses concise and to the point without unnecessary explanations, use bullet points where appropriate, avoid lengthy conclusions and summaries, focus on practical, actionable product recommendations. I am giving all the products existing in our store, give the response in context to that. \n"+ productSummaries.toString() + "\nIf the request is for a product, give the id for all the resulting products from the database as response and all the response should be from this data, nothing out of this. And if there is some condition like price limit, then the response should abide by that condition. Give in plain string not markdown or bold and all." + prompt;
            
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> contentMap = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", enhancedPrompt);
            parts.add(textPart);
            contentMap.put("parts", parts);
            
            List<Map<String, Object>> contents = new ArrayList<>();
            contents.add(contentMap);
            requestBody.put("contents", contents);
            
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 1024);
            generationConfig.put("topP", 0.8);
            generationConfig.put("topK", 40);
            requestBody.put("generationConfig", generationConfig);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = API_URL + model + ":generateContent?key=" + apiKey;
            System.out.println("Request URL: " + url.replace(apiKey, "API_KEY_REDACTED"));
            
            System.out.println("Request body: " + requestBody);
            
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );
            
            Map<String, Object> responseBody = responseEntity.getBody();
            System.out.println("Response status: " + responseEntity.getStatusCode());
            
            System.out.println("Response body: " + responseBody);
            
            System.out.println("Response class: " + (responseBody != null ? responseBody.getClass().getName() : "null"));
            
            if (responseBody != null) {
                System.out.println("Response keys: ");
                for (String key : responseBody.keySet()) {
                    Object value = responseBody.get(key);
                    System.out.println("  - " + key + ": " + (value != null ? value.getClass().getName() : "null"));
                }
            }
            
            if (responseBody != null && responseBody.containsKey("candidates")) {
                Object candidatesObj = responseBody.get("candidates");
                System.out.println("Candidates object type: " + candidatesObj.getClass().getName());
                
                if (candidatesObj instanceof List) {
                    List<?> candidates = (List<?>) candidatesObj;
                    System.out.println("Candidates size: " + candidates.size());
                    
                    if (!candidates.isEmpty()) {
                        Object candidateObj = candidates.get(0);
                        System.out.println("First candidate type: " + candidateObj.getClass().getName());
                        
                        if (candidateObj instanceof Map) {
                            Map<?, ?> candidate = (Map<?, ?>) candidateObj;
                            System.out.println("Candidate keys: " + candidate.keySet());
                            
                            if (candidate.containsKey("content")) {
                                Object contentObj = candidate.get("content");
                                System.out.println("Content object type: " + contentObj.getClass().getName());
                                
                                if (contentObj instanceof Map) {
                                    Map<?, ?> content = (Map<?, ?>) contentObj;
                                    System.out.println("Content map keys: " + content.keySet());
                                    
                                    if (content.containsKey("parts")) {
                                        Object partsObj = content.get("parts");
                                        System.out.println("Parts object type: " + partsObj.getClass().getName());
                                        
                                        if (partsObj instanceof List) {
                                            List<?> parts1 = (List<?>) partsObj;
                                            System.out.println("Parts size: " + parts1.size());
                                            
                                            if (!parts1.isEmpty()) {
                                                Object partObj = parts1.get(0);
                                                System.out.println("First part type: " + partObj.getClass().getName());
                                                
                                                if (partObj instanceof Map) {
                                                    Map<?, ?> part1 = (Map<?, ?>) partObj;
                                                    System.out.println("Part keys: " + part1.keySet());
                                                    
                                                    if (part1.containsKey("text")) {
                                                        Object textObj = part1.get("text");
                                                        System.out.println("Text object type: " + textObj.getClass().getName());
                                                        
                                                        if (textObj instanceof String) {
                                                            return (String) textObj;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (responseBody != null && responseBody.containsKey("content")) {
                Object contentObj = responseBody.get("content");
                System.out.println("Direct content object type: " + contentObj.getClass().getName());
                
                if (contentObj instanceof Map) {
                    Map<?, ?> contentMap1 = (Map<?, ?>) contentObj;
                    System.out.println("Direct content map keys: " + contentMap1.keySet());
                    
                    if (contentMap1.containsKey("parts")) {
                        Object partsObj = contentMap1.get("parts");
                        System.out.println("Direct parts object type: " + partsObj.getClass().getName());
                        
                        if (partsObj instanceof List) {
                            List<?> parts1 = (List<?>) partsObj;
                            System.out.println("Direct parts size: " + parts1.size());
                            
                            if (!parts1.isEmpty()) {
                                Object partObj = parts1.get(0);
                                System.out.println("Direct first part type: " + partObj.getClass().getName());
                                
                                if (partObj instanceof Map) {
                                    Map<?, ?> part1 = (Map<?, ?>) partObj;
                                    System.out.println("Direct part keys: " + part1.keySet());
                                    
                                    if (part1.containsKey("text")) {
                                        Object textObj = part1.get("text");
                                        System.out.println("Direct text object type: " + textObj.getClass().getName());
                                        
                                        if (textObj instanceof String) {
                                            return (String) textObj;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            System.out.println("Could not extract text from response: " + responseBody);
            return "I received a response but couldn't extract the content. Please try again.";
            
        } catch (Exception e) {
            System.out.println("Error in Gemini service: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
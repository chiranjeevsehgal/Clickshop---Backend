package com.clickshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clickshop.dto.GeminiRequest;
import com.clickshop.dto.GeminiResponse;
import com.clickshop.service.GeminiService;
import com.clickshop.service.ProductService;

@RestController
@RequestMapping("/gemini")
public class GeminiController {

	@Autowired
	private GeminiService geminiService;
	
	@Autowired
	private ProductService productService;

	@PostMapping()	
	public ResponseEntity<GeminiResponse> getAiResponse(@RequestBody GeminiRequest request) {
		var productSummaries = productService.getProductSummaries();
		String response = geminiService.generateAiResponse(request.getPrompt(), productSummaries);
		return ResponseEntity.ok(new GeminiResponse(response));
	}

}

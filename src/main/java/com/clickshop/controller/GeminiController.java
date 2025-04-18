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

@RestController
@RequestMapping("/gemini")
public class GeminiController {

	@Autowired
	private GeminiService geminiService;

	@PostMapping()	
	public ResponseEntity<GeminiResponse> getAiResponse(@RequestBody GeminiRequest request) {
		String response = geminiService.generateAiResponse(request.getPrompt());
		return ResponseEntity.ok(new GeminiResponse(response));
	}

}

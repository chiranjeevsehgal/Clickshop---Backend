package com.clickshop.dto;

public class GeminiRequest {
    private String prompt;

    public GeminiRequest() {
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

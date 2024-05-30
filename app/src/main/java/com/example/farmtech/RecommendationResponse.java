package com.example.farmtech;

public class RecommendationResponse {
    private String recommended_crop;

    public String getRecommendation() {
        return recommended_crop;
    }

    public void setRecommendation(String recommended_crop) {
        this.recommended_crop = recommended_crop;
    }
}
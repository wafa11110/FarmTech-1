package com.example.farmtech;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RecommendationApi {
    @POST("/api/predict")
    Call<RecommendationResponse> getRecommendations(@Body Map<String, String> requestData);
}

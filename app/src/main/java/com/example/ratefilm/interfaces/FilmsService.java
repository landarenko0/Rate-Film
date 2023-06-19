package com.example.ratefilm.interfaces;

import com.example.ratefilm.data_response.FilmSearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface FilmsService {
    @GET("/api/v2.1/films/search-by-keyword")
    Call<FilmSearchResponse> searchByKeywords(@Header("X-API-KEY") String apiKey, @Query("keyword") String keyword);
}

package com.example.ratefilm;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface FilmsService {
    @GET("/api/v2.2/films/{id}")
    Call<Film> getFilmById(@Header("X-API-KEY") String apiKey, @Path("id") String id);
}

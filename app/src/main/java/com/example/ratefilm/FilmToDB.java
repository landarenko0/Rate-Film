package com.example.ratefilm;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FilmToDB {
    @SerializedName("id")
    private int id;
    @SerializedName("description")
    private String description;
    @SerializedName("nameOriginal")
    private String nameOriginal;
    @SerializedName("nameRu")
    private String nameRu;
    @SerializedName("posterUrl")
    private String posterUrl;
    @SerializedName("rating")
    private float rating;
    @SerializedName("reviews")
    private HashMap<String, Review> reviews = new HashMap<>();
    private Bitmap bitmap;

    public FilmToDB() {

    }

    public FilmToDB(int id, String description, String nameOriginal, String nameRu, String posterUrl, float rating, HashMap<String, Review> reviews) {
        this.id = id;
        this.description = description;
        this.nameOriginal = nameOriginal;
        this.nameRu = nameRu;
        this.posterUrl = posterUrl;
        this.rating = rating;
        if (reviews != null) this.reviews = reviews;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNameOriginal() {
        return nameOriginal;
    }

    public void setNameOriginal(String nameOriginal) {
        this.nameOriginal = nameOriginal;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public HashMap<String, Review> getReviews() {
        return reviews;
    }

    public List<Review> getReviewsList() {
        return new ArrayList<>(reviews.values());
    }

    public void setReviews(HashMap<String, Review> reviews) {
        this.reviews = reviews;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void addReview(String userName, Review review) {
        reviews.put(userName, review);
    }

    public void deleteReview(Review review) {
        reviews.remove(review);
    }
}
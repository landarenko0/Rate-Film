package com.example.ratefilm;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class FilmToDB implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("description")
    private String description;
    @SerializedName("nameEn")
    private String nameEn;
    @SerializedName("nameRu")
    private String nameRu;
    @SerializedName("posterUrl")
    private String posterUrl;
    @SerializedName("rating")
    private float rating;
    @SerializedName("reviewsCount")
    private int reviewsCount;
    @SerializedName("reviews")
    private List<Review> reviews;
    private Bitmap bitmap;

    public FilmToDB() {

    }

    public FilmToDB(int id, String description, String nameEn, String nameRu, String posterUrl, float rating, int reviewsCount, List<Review> reviews) {
        this.id = id;
        this.description = description;
        this.nameEn = nameEn;
        this.nameRu = nameRu;
        this.posterUrl = posterUrl;
        this.rating = rating;
        this.reviewsCount = reviewsCount;
        this.reviews = reviews;
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

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
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

    public int getReviewsCount() {
        return reviewsCount;
    }

    public void setReviewsCount(int reviewsCount) {
        this.reviewsCount = reviewsCount;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}

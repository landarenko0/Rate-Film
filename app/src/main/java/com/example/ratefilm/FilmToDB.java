package com.example.ratefilm;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

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
    private Bitmap bitmap;

    public FilmToDB() {

    }

    public FilmToDB(int id, String description, String nameOriginal, String nameRu, String posterUrl) {
        this.id = id;
        this.description = description;
        this.nameOriginal = nameOriginal;
        this.nameRu = nameRu;
        this.posterUrl = posterUrl;
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

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
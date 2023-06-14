package com.example.ratefilm;

import java.util.List;

public class FilmsByKeyword {
    private int filmId;
    private String nameRu;
    private String nameEn;
    private String type;
    private String year;
    private String description;
    private String filmLength;
    private List<Country> countries;
    private List<Genre> genres;
    private String rating;
    private int ratingVoteCount;
    private String posterUrl;
    private String posterUrlPreview;

    public FilmsByKeyword() {

    }

    public FilmsByKeyword(int filmId, String nameRu, String nameEn, String type, String year, String description, String filmLength, List<Country> countries, List<Genre> genres, String rating, int ratingVoteCount, String posterUrl, String posterUrlPreview) {
        this.filmId = filmId;
        this.nameRu = nameRu;
        this.nameEn = nameEn;
        this.type = type;
        this.year = year;
        this.description = description;
        this.filmLength = filmLength;
        this.countries = countries;
        this.genres = genres;
        this.rating = rating;
        this.ratingVoteCount = ratingVoteCount;
        this.posterUrl = posterUrl;
        this.posterUrlPreview = posterUrlPreview;
    }

    public int getFilmId() {
        return filmId;
    }

    public void setFilmId(int filmId) {
        this.filmId = filmId;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilmLength() {
        return filmLength;
    }

    public void setFilmLength(String filmLength) {
        this.filmLength = filmLength;
    }

    public List<Country> getCountries() {
        return countries;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public int getRatingVoteCount() {
        return ratingVoteCount;
    }

    public void setRatingVoteCount(int ratingVoteCount) {
        this.ratingVoteCount = ratingVoteCount;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getPosterUrlPreview() {
        return posterUrlPreview;
    }

    public void setPosterUrlPreview(String posterUrlPreview) {
        this.posterUrlPreview = posterUrlPreview;
    }
}

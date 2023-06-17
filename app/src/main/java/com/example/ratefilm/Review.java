package com.example.ratefilm;

public class Review {
    private String review;
    private float rating;
    private String username;
    private FilmToDB film;

    public Review() {

    }

    public Review(String review, float rating, String username, FilmToDB film) {
        this.review = review;
        this.rating = rating;
        this.username = username;
        this.film = film;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public FilmToDB getFilm() {
        return film;
    }

    public void setFilm(FilmToDB film) {
        this.film = film;
    }
}

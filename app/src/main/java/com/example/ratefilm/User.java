package com.example.ratefilm;

import java.util.List;

public class User {
    private List<Review> reviews;
    private String username;
    private List<FilmToDB> likedFilms;

    public User() {

    }

    public User(String username) {
        this.username = username;
    }

    public User(List<Review> reviews) {
        this.reviews = reviews;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview(Review review) {
        reviews.add(review);
    }

    public void deleteReview(Review review) {
        reviews.remove(review);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<FilmToDB> getLikedFilms() {
        return likedFilms;
    }

    public void setLikedFilms(List<FilmToDB> likedFilms) {
        this.likedFilms = likedFilms;
    }
}

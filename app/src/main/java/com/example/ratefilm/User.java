package com.example.ratefilm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User {
    private HashMap<String, Review> reviews = new HashMap<>();
    private String username;
    private String email;
    private List<FilmToDB> likedFilms = new ArrayList<>();

    public User() {

    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public User(HashMap<String, Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview(String userName, Review review) {
        reviews.put(userName, review);
    }

//    public void deleteReview(Review review) {
//        reviews.remove(review);
//    }

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

    public HashMap<String, Review> getReviews() {
        return reviews;
    }

    public void setReviews(HashMap<String, Review> reviews) {
        this.reviews = reviews;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

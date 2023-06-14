package com.example.ratefilm;

public class Review {
    private String review;
    private float rating;
    private String username;
    private String filmName;

    public Review() {

    }

    public Review(String review, float rating, String filmName, String username) {
        this.review = review;
        this.rating = rating;
        this.filmName = filmName;
        this.username = username;
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

//    public User getUser() {
//        return user;
//    }

//    public void setUser(User user) {
//        this.user = user;
//    }

    public String getFilmName() {
        return filmName;
    }

    public void setFilmName(String filmName) {
        this.filmName = filmName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

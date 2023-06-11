package com.example.ratefilm;

public class Review {
    private String review;
    private float rating;
    private User user;

    public Review() {

    }

    public Review(String review, float rating, User user) {
        this.review = review;
        this.rating = rating;
        this.user = user;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

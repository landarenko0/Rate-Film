package com.example.ratefilm.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ratefilm.data_response.FilmToDB;
import com.example.ratefilm.data_response.Review;
import com.example.ratefilm.data_response.User;
import com.example.ratefilm.adapters.ReviewListAdapter;
import com.example.ratefilm.databinding.FilmDetailsBinding;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class FilmDetailsActivity extends AppCompatActivity {

    private FilmDetailsBinding binding;
    private FilmToDB film;
    private User user;
    private Review userReview;
    private DatabaseReference database;
    private ArrayList<Review> reviews;
    private boolean reviewsLoaded = false;
    private boolean likedFilm = false;
    private boolean likedFilmResultIsLoaded = false;
    private Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        init();

        setContentView(binding.getRoot());

        setFilmDetails();
    }

    private void init() {
        binding = FilmDetailsBinding.inflate(getLayoutInflater());

        reviews = new ArrayList<>();

        database = FirebaseDatabase.getInstance().getReference();

        gson = new Gson();

        film = gson.fromJson(getIntent().getStringExtra("filmJson"), FilmToDB.class);
        user = gson.fromJson(getIntent().getStringExtra("userJson"), User.class);

        DownloadFilmReviewsThread thread = new DownloadFilmReviewsThread();
        thread.start();

        binding.currentUserReview.setOnClickListener(view -> {
            if (reviewsLoaded) toCreateReviewActivity();
        });

        binding.likedFilm.setOnClickListener(view -> {
            if (likedFilmResultIsLoaded) {
                if (likedFilm) {
                    DeleteFilmFromLikedFilmsThread deleteThread = new DeleteFilmFromLikedFilmsThread();
                    deleteThread.start();
                } else {
                    AddFilmToLikedFilmsThread addThread = new AddFilmToLikedFilmsThread();
                    addThread.start();
                }
            } else {
                Toast.makeText(FilmDetailsActivity.this, "Пожалуйста, подождите", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFilmDetails() {
        if (film.getBitmap() == null) {
            DownloadPosterThread thread = new DownloadPosterThread();
            thread.start();
        } else {
            binding.detailsPosterImage.setImageBitmap(film.getBitmap());
        }

        binding.detailsFilmName.setText(film.getNameRu());
        binding.detailsFilmDescription.setText(film.getDescription());
    }

    private boolean hasCurrentUserLeftReview() {
        String username = user.getUsername();

        for (int i = 0; i < reviews.size(); i++) {
            Review review = reviews.get(i);

            if (review.getUsername().equals(username)) {
                userReview = review;

                return true;
            }
        }

        return false;
    }

    private void toCreateReviewActivity() {
        Intent intent = new Intent(FilmDetailsActivity.this, AddReviewActivity.class);

        intent.putExtra("reviewJson", gson.toJson(userReview));
        intent.putExtra("filmJson", gson.toJson(film));
        intent.putExtra("userJson", gson.toJson(user));

        startActivity(intent);
    }

    class DownloadPosterThread extends Thread {
        @Override
        public void run() {
            super.run();

            try {
                URL url = new URL(film.getPosterUrl());

                InputStream stream = (InputStream) url.getContent();

                if (stream != null) {
                    runOnUiThread(() -> binding.detailsPosterImage.setImageBitmap(BitmapFactory.decodeStream(stream)));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class DownloadFilmReviewsThread extends Thread {
        @Override
        public void run() {
            super.run();

            database.child("Users").child(user.getEmail().split("@")[0]).child("likedFilms").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().hasChild(film.getNameOriginal())) {
                        binding.likedFilm.setText("Удалить из избранного");
                        likedFilm = true;
                    } else {
                        binding.likedFilm.setText("Добавить в избранное");
                    }

                    likedFilmResultIsLoaded = true;
                }
            });

            database.child("Films").child("Other").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();

                    if (snapshot.hasChild(film.getNameOriginal())) {
                        snapshot = snapshot.child(film.getNameOriginal());

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Review review = dataSnapshot.getValue(Review.class);

                            reviews.add(review);
                        }
                    }

                    ReviewListAdapter adapter = new ReviewListAdapter(reviews);
                    binding.rvReviews.setAdapter(adapter);
                    binding.rvReviews.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                    if (reviews.size() == 0) {
                        runOnUiThread(() -> binding.detailsRating.setText("Нет оценок"));
                    } else {
                        float sum = 0f;

                        for (Review review : reviews) {
                            sum += review.getRating();
                        }

                        float finalSum = sum;
                        runOnUiThread(() -> binding.detailsRating.setText(String.format(Locale.US, "%.1f", finalSum/reviews.size())));
                    }

                    runOnUiThread(() -> {
                        binding.currentUserReview.setText(hasCurrentUserLeftReview() ? "Редактировать отзыв" : "Добавить отзыв");
                        reviewsLoaded = true;
                    });
                }
            });
        }
    }

    class AddFilmToLikedFilmsThread extends Thread {
        @Override
        public void run() {
            super.run();

            likedFilmResultIsLoaded = false;

            FilmToDB tmp = film;

            tmp.setBitmap(null);

            database.child("Users").child(user.getEmail().split("@")[0]).child("likedFilms").child(film.getNameOriginal()).setValue(tmp).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    binding.likedFilm.setText("Удалить из избранного");
                    likedFilm = true;
                    likedFilmResultIsLoaded = true;
                }
            });
        }
    }

    class DeleteFilmFromLikedFilmsThread extends Thread {
        @Override
        public void run() {
            super.run();

            likedFilmResultIsLoaded = false;

            database.child("Users").child(user.getEmail().split("@")[0]).child("likedFilms").child(film.getNameOriginal()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    binding.likedFilm.setText("Добавить в избранное");
                    likedFilm = false;
                    likedFilmResultIsLoaded = true;
                }
            });
        }
    }
}

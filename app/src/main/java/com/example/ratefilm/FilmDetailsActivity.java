package com.example.ratefilm;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ratefilm.databinding.FilmDetailsBinding;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class FilmDetailsActivity extends AppCompatActivity {

    private FilmDetailsBinding binding;
    private FilmToDB film;
    private TextView filmName;
    private TextView filmDescription;
    private TextView rating;
    private TextView currentUserReview;
    private ImageView poster;
    private RecyclerView recyclerView;
    private User user;
    private Review userReview;

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

        Gson gson = new Gson();

        String filmJson = getIntent().getStringExtra("filmJson");
        film = gson.fromJson(filmJson, FilmToDB.class);

        String userJson = getIntent().getStringExtra("userJson");
        user = gson.fromJson(userJson, User.class);

        filmName = binding.detailsFilmName;
        filmDescription = binding.detailsFilmDescription;
        rating = binding.detailsRating;
        currentUserReview = binding.currentUserReview;
        poster = binding.detailsPosterImage;
        recyclerView = binding.rvReviews;

        currentUserReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toCreateReviewActivity();
            }
        });
    }

    private void setFilmDetails() {
        if (film.getBitmap() == null) {
            DownloadPosterThread thread = new DownloadPosterThread();
            thread.start();
        } else {
            poster.setImageBitmap(film.getBitmap());
        }

        filmName.setText(film.getNameRu());
        filmDescription.setText(film.getDescription());

        rating.setText(film.getRating() == 0f ? "Нет оценок" : String.valueOf(film.getRating()));

        currentUserReview.setText(hasCurrentUserLeftReview() ? "Редактировать отзыв" : "Добавить отзыв");

        ReviewListAdapter adapter = new ReviewListAdapter(film.getReviewsList());

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private boolean hasCurrentUserLeftReview() {
        String username = user.getUsername();

        List<Review> reviews = film.getReviewsList();

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

        Gson gson = new Gson();

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            poster.setImageBitmap(BitmapFactory.decodeStream(stream));
                        }
                    });
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

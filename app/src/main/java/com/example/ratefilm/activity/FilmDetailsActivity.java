package com.example.ratefilm.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ratefilm.R;
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
    private static final String USER_JSON = "userJson";
    private static final String FILM_JSON = "filmJson";
    private static final String REVIEW_JSON = "reviewJson";
    private static final String TAG = "posterDownloadError";
    private static final String ERROR_MESSAGE = "Cannot download film poster";
    private static final String USERS = "Users";
    private static final String LIKED_FILMS = "likedFilms";
    private static final String FILMS = "Films";
    private static final String OTHER = "Other";

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

        film = gson.fromJson(getIntent().getStringExtra(FILM_JSON), FilmToDB.class);
        user = gson.fromJson(getIntent().getStringExtra(USER_JSON), User.class);

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
                Toast.makeText(FilmDetailsActivity.this, getResources().getText(R.string.please_wait), Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvReviews.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
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

    private void toCreateReviewActivity() {
        Intent intent = new Intent(FilmDetailsActivity.this, AddReviewActivity.class);

        intent.putExtra(REVIEW_JSON, gson.toJson(userReview));
        intent.putExtra(FILM_JSON, getIntent().getStringExtra(FILM_JSON));
        intent.putExtra(USER_JSON, getIntent().getStringExtra(USER_JSON));

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
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);

                    runOnUiThread(() -> binding.detailsPosterImage.setImageBitmap(bitmap));
                }
            } catch (IOException e) {
                Log.e(TAG, ERROR_MESSAGE);
            }
        }
    }

    class DownloadFilmReviewsThread extends Thread {
        @Override
        public void run() {
            super.run();

            database.child(USERS).child(user.getEmail().split("@")[0]).child(LIKED_FILMS).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().hasChild(String.valueOf(film.getId()))) {
                        binding.likedFilm.setText(getResources().getText(R.string.delete_from_favorite));
                        likedFilm = true;
                    } else {
                        binding.likedFilm.setText(getResources().getText(R.string.add_to_favorite));
                    }

                    likedFilmResultIsLoaded = true;
                }
            });

            database.child(FILMS).child(OTHER).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();

                    if (snapshot.hasChild(String.valueOf(film.getId()))) {
                        snapshot = snapshot.child(String.valueOf(film.getId()));

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Review review = dataSnapshot.getValue(Review.class);

                            if (review != null && review.getUsername().equals(user.getUsername())) {
                                userReview = review;
                            }

                            reviews.add(review);
                        }
                    }

                    ReviewListAdapter adapter = new ReviewListAdapter(reviews);
                    binding.rvReviews.setAdapter(adapter);

                    if (reviews.size() == 0) {
                        runOnUiThread(() -> binding.detailsRating.setText(getResources().getText(R.string.no_reviews)));
                    } else {
                        float sum = 0f;

                        for (Review review : reviews) {
                            sum += review.getRating();
                        }

                        float finalSum = sum;
                        runOnUiThread(() -> binding.detailsRating.setText(String.format(Locale.US, "%.1f", finalSum/reviews.size())));
                    }

                    runOnUiThread(() -> {
                        binding.currentUserReview.setText(userReview == null ? getResources().getText(R.string.add_review) : getResources().getText(R.string.edit_review));
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

            database.child(USERS).child(user.getEmail().split("@")[0]).child(LIKED_FILMS).child(String.valueOf(film.getId())).setValue(tmp).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    binding.likedFilm.setText(getResources().getText(R.string.delete_from_favorite));
                    likedFilm = true;
                    likedFilmResultIsLoaded = true;

                    runOnUiThread(() -> Toast.makeText(FilmDetailsActivity.this, getResources().getText(R.string.film_added_success), Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    class DeleteFilmFromLikedFilmsThread extends Thread {
        @Override
        public void run() {
            super.run();

            likedFilmResultIsLoaded = false;

            database.child(USERS).child(user.getEmail().split("@")[0]).child(LIKED_FILMS).child(String.valueOf(film.getId())).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    binding.likedFilm.setText(getResources().getText(R.string.add_to_favorite));
                    likedFilm = false;
                    likedFilmResultIsLoaded = true;

                    runOnUiThread(() -> Toast.makeText(FilmDetailsActivity.this, getResources().getText(R.string.film_deleted_success), Toast.LENGTH_SHORT).show());
                }
            });
        }
    }
}

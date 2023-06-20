package com.example.ratefilm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ratefilm.R;
import com.example.ratefilm.data_response.FilmToDB;
import com.example.ratefilm.data_response.Review;
import com.example.ratefilm.data_response.User;
import com.example.ratefilm.databinding.AddReviewLayoutBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

public class AddReviewActivity extends AppCompatActivity {
    private AddReviewLayoutBinding binding;
    private Gson gson;
    private Review oldReview;
    private FilmToDB film;
    private User user;
    private DatabaseReference database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        init();

        setContentView(binding.getRoot());

        setInformation();
    }

    private void init() {
        gson = new Gson();

        binding = AddReviewLayoutBinding.inflate(getLayoutInflater());

        oldReview = gson.fromJson(getIntent().getStringExtra("reviewJson"), Review.class);
        film = gson.fromJson(getIntent().getStringExtra("filmJson"), FilmToDB.class);
        user = gson.fromJson(getIntent().getStringExtra("userJson"), User.class);

        if (oldReview == null)  binding.deleteReview.setVisibility(View.GONE);

        binding.deleteReview.setOnClickListener(view -> deleteReview());

        database = FirebaseDatabase.getInstance().getReference();

        binding.saveBtn.setOnClickListener(view -> saveReview());
    }

    private void setInformation() {
        binding.filmNameAddReview.setText(film.getNameRu());

        if (oldReview != null) {
            binding.addReviewText.setText(oldReview.getReview());
            binding.rating.setRating(oldReview.getRating());
        }
    }

    private void deleteReview() {
        DeleteReviewThread thread = new DeleteReviewThread();
        thread.start();

        toFilmDetailsActivity();
    }

    private void saveReview() {
        if (binding.rating.getRating() == 0f) {
            Toast.makeText(this, getResources().getText(R.string.enter_rate), Toast.LENGTH_SHORT).show();

            return;
        }

        String reviewText = binding.addReviewText.getText().toString();

        User user = gson.fromJson(getIntent().getStringExtra("userJson"), User.class);

        film.setBitmap(null);

        Review newReview = new Review(reviewText, binding.rating.getRating(), user.getUsername(), film);

        PublishReviewThread thread = new PublishReviewThread(newReview);
        thread.start();

        toFilmDetailsActivity();
    }

    private void toFilmDetailsActivity() {
        Intent intent = new Intent(AddReviewActivity.this, FilmDetailsActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("userJson", gson.toJson(user));
        intent.putExtra("filmJson", getIntent().getStringExtra("filmJson"));

        startActivity(intent);

        finish();
    }

    class PublishReviewThread extends Thread {
        private final Review review;

        public PublishReviewThread(Review review) {
            this.review = review;
        }

        @Override
        public void run() {
            super.run();

            database.child("Users").child(user.getEmail().split("@")[0]).child("reviews").child(String.valueOf(film.getId())).setValue(review);
            database.child("Films").child("Other").child(String.valueOf(film.getId())).child(user.getEmail().split("@")[0]).setValue(review);

            runOnUiThread(() -> Toast.makeText(getApplicationContext(), getResources().getText(R.string.review_added_success), Toast.LENGTH_SHORT).show());
        }
    }

    class DeleteReviewThread extends Thread {
        @Override
        public void run() {
            super.run();

            database.child("Users").child(user.getEmail().split("@")[0]).child("reviews").child(String.valueOf(film.getId())).removeValue();
            database.child("Films").child("Other").child(String.valueOf(film.getId())).child(user.getEmail().split("@")[0]).removeValue();

            runOnUiThread(() -> Toast.makeText(getApplicationContext(), getResources().getText(R.string.review_deleted_success), Toast.LENGTH_SHORT).show());
        }
    }
}

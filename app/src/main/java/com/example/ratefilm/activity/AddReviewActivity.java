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
    private Review oldReview;
    private FilmToDB film;
    private User user;
    private DatabaseReference database;
    private static final String REVIEW_JSON = "reviewJson";
    private static final String FILM_JSON = "filmJson";
    private static final String USER_JSON = "userJson";
    private static final String USERS = "Users";
    private static final String REVIEWS = "reviews";
    private static final String FILMS = "Films";
    private static final String OTHER = "Other";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        init();

        setContentView(binding.getRoot());

        setInformation();
    }

    private void init() {
        binding = AddReviewLayoutBinding.inflate(getLayoutInflater());

        Gson gson = new Gson();
        oldReview = gson.fromJson(getIntent().getStringExtra(REVIEW_JSON), Review.class);
        film = gson.fromJson(getIntent().getStringExtra(FILM_JSON), FilmToDB.class);
        user = gson.fromJson(getIntent().getStringExtra(USER_JSON), User.class);

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

        film.setBitmap(null);

        Review newReview = new Review(binding.addReviewText.getText().toString(), binding.rating.getRating(), user.getUsername(), film);

        PublishReviewThread thread = new PublishReviewThread(newReview);
        thread.start();

        toFilmDetailsActivity();
    }

    private void toFilmDetailsActivity() {
        Intent intent = new Intent(AddReviewActivity.this, FilmDetailsActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(USER_JSON, getIntent().getStringExtra(USER_JSON));
        intent.putExtra(FILM_JSON, getIntent().getStringExtra(FILM_JSON));

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

            database.child(USERS).child(user.getEmail().split("@")[0]).child(REVIEWS).child(String.valueOf(film.getId())).setValue(review);
            database.child(FILMS).child(OTHER).child(String.valueOf(film.getId())).child(user.getEmail().split("@")[0]).setValue(review);

            runOnUiThread(() -> Toast.makeText(getApplicationContext(), getResources().getText(R.string.review_added_success), Toast.LENGTH_SHORT).show());
        }
    }

    class DeleteReviewThread extends Thread {
        @Override
        public void run() {
            super.run();

            database.child(USERS).child(user.getEmail().split("@")[0]).child(REVIEWS).child(String.valueOf(film.getId())).removeValue();
            database.child(FILMS).child(OTHER).child(String.valueOf(film.getId())).child(user.getEmail().split("@")[0]).removeValue();

            runOnUiThread(() -> Toast.makeText(getApplicationContext(), getResources().getText(R.string.review_deleted_success), Toast.LENGTH_SHORT).show());
        }
    }
}

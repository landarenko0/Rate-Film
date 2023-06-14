package com.example.ratefilm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ratefilm.databinding.AddReviewLayoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

public class AddReviewActivity extends AppCompatActivity {

    private TextView filmName_tv;
    private EditText reviewText_et;
    private RatingBar ratingBar;
    private AddReviewLayoutBinding binding;
    private Gson gson;
    private Review oldReview;
    private FilmToDB film;
    private DatabaseReference database;
    private String currentUserName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        setContentView(binding.getRoot());

        setInformation();
    }

    private void init() {
        gson = new Gson();

        binding = AddReviewLayoutBinding.inflate(getLayoutInflater());

        filmName_tv = binding.filmNameAddReview;
        reviewText_et = binding.addReviewText;
        ratingBar = binding.rating;

        oldReview = gson.fromJson(getIntent().getStringExtra("reviewJson"), Review.class);
        film = gson.fromJson(getIntent().getStringExtra("filmJson"), FilmToDB.class);

        database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveReview();
            }
        });

        assert currentUser != null;
        assert currentUser.getEmail() != null;

        currentUserName = currentUser.getEmail().split("@")[0];
    }

    private void setInformation() {
        filmName_tv.setText(film.getNameRu());

        if (oldReview != null) {
            reviewText_et.setText(oldReview.getReview());
            ratingBar.setRating(oldReview.getRating());
        }
    }

    private void saveReview() {
        if (ratingBar.getRating() == 0f) {
            Toast.makeText(this, "Введите оценку фильму", Toast.LENGTH_SHORT).show();

            return;
        }

        String reviewText = reviewText_et.getText().toString();

        User user = gson.fromJson(getIntent().getStringExtra("userJson"), User.class);

        Review newReview = new Review(reviewText, ratingBar.getRating(), film.getNameOriginal(), user.getUsername());

        user.addReview(currentUserName, newReview);
        film.addReview(currentUserName, newReview);

        PublishReviewThread thread = new PublishReviewThread(newReview);
        thread.start();

        Intent intent = new Intent(AddReviewActivity.this, FilmDetailsActivity.class);

        intent.putExtra("userJson", gson.toJson(user));
        intent.putExtra("filmJson", gson.toJson(film));

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

            database.child("Users").child(currentUserName).child("reviews").child(film.getNameOriginal()).setValue(review);
            database.child("Films").child(film.getNameOriginal()).child("reviews").child(currentUserName).setValue(review);

            // TODO: Добавить изменение средней оценки фильма
        }
    }
}

package com.example.ratefilm.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ratefilm.data_response.FilmToDB;
import com.example.ratefilm.interfaces.RecyclerViewOnClickListener;
import com.example.ratefilm.data_response.Review;
import com.example.ratefilm.data_response.User;
import com.example.ratefilm.adapters.FilmsListAdapter;
import com.example.ratefilm.databinding.AccountLayoutBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends AppCompatActivity implements RecyclerViewOnClickListener {

    private AccountLayoutBinding binding;
    private User user;
    private ArrayList<FilmToDB> likedFilms;
    private ArrayList<FilmToDB> filmsWithReview;
    private DatabaseReference database;
    private Gson gson;
    private static final String USER_JSON = "userJson";
    private static final String FILM_JSON = "filmJson";
    private static final String USERS = "Users";
    private static final String LIKED_FILMS = "likedFilms";
    private static final String REVIEWS = "reviews";
    private static final String TAG = "posterDownloadError";
    private static final String ERROR_MESSAGE = "Cannot download film poster";
    private boolean activityJustCreated = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        init();

        setContentView(binding.getRoot());

        DownloadUserFilmsThread thread = new DownloadUserFilmsThread();
        thread.start();
    }

    private void init() {
        binding = AccountLayoutBinding.inflate(getLayoutInflater());

        database = FirebaseDatabase.getInstance().getReference();

        likedFilms = new ArrayList<>();
        filmsWithReview = new ArrayList<>();

        gson = new Gson();

        user = gson.fromJson(getIntent().getStringExtra(USER_JSON), User.class);

        binding.accountUsername.setText(user.getUsername());
        binding.accountEmail.setText(user.getEmail());

        binding.likedFilms.setLayoutManager(new LinearLayoutManager(AccountActivity.this, LinearLayoutManager.HORIZONTAL, false));
        binding.filmsWithReview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    public void onItemClick(List<FilmToDB> films, int position) {
        FilmToDB film = films.get(position);

        Intent intent = new Intent(AccountActivity.this, FilmDetailsActivity.class);

        intent.putExtra(FILM_JSON, gson.toJson(film));
        intent.putExtra(USER_JSON, getIntent().getStringExtra(USER_JSON));

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (activityJustCreated) {
            activityJustCreated = false;
            return;
        }

        DownloadUserFilmsThread thread = new DownloadUserFilmsThread();
        thread.start();
    }

    private class DownloadUserFilmsThread extends Thread {

        @Override
        public void run() {
            super.run();

            database.child(USERS).child(user.getEmail().split("@")[0]).child(LIKED_FILMS).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();

                    ArrayList<FilmToDB> checkLikedFilms = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        FilmToDB film = dataSnapshot.getValue(FilmToDB.class);

                        checkLikedFilms.add(film);
                    }

                    if (checkLikedFilms.size() != likedFilms.size()) {
                        likedFilms = checkLikedFilms;

                        DownloadPosterThread thread = new DownloadPosterThread(likedFilms, binding.likedFilms);
                        thread.start();

                        FilmsListAdapter adapter = new FilmsListAdapter(likedFilms, AccountActivity.this);
                        binding.likedFilms.setAdapter(adapter);
                    }
                }
            });

            database.child(USERS).child(user.getEmail().split("@")[0]).child(REVIEWS).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();

                    ArrayList<FilmToDB> checkFilmsWithReview = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Review review = dataSnapshot.getValue(Review.class);

                        if (review == null) return;

                        FilmToDB film = review.getFilm();

                        checkFilmsWithReview.add(film);
                    }

                    if (checkFilmsWithReview.size() != filmsWithReview.size()) {
                        filmsWithReview = checkFilmsWithReview;

                        DownloadPosterThread thread = new DownloadPosterThread(filmsWithReview, binding.filmsWithReview);
                        thread.start();

                        FilmsListAdapter adapter = new FilmsListAdapter(filmsWithReview, AccountActivity.this);
                        binding.filmsWithReview.setAdapter(adapter);
                    }
                }
            });
        }
    }

    private class DownloadPosterThread extends Thread {

        private final ArrayList<FilmToDB> films;
        private final RecyclerView recyclerView;

        DownloadPosterThread(ArrayList<FilmToDB> films, RecyclerView recyclerView) {
            this.films = films;
            this.recyclerView = recyclerView;
        }

        @Override
        public void run() {
            super.run();

            for (int i = 0; i < films.size(); i++) {
                FilmToDB film = films.get(i);

                try {
                    URL url = new URL(film.getPosterUrl());
                    InputStream stream = (InputStream) url.getContent();

                    if (stream != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(stream);

                        film.setBitmap(bitmap);

                        if (recyclerView.getAdapter() == null) return;

                        int finalI = i;
                        runOnUiThread(() -> recyclerView.getAdapter().notifyItemChanged(finalI));
                    }
                } catch (IOException e) {
                    Log.e(TAG, ERROR_MESSAGE);
                }
            }
        }
    }
}

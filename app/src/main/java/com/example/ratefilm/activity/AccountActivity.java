package com.example.ratefilm.activity;

import android.content.Intent;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        init();

        setContentView(binding.getRoot());
    }

    private void init() {
        binding = AccountLayoutBinding.inflate(getLayoutInflater());

        database = FirebaseDatabase.getInstance().getReference();

        likedFilms = new ArrayList<>();
        filmsWithReview = new ArrayList<>();

        gson = new Gson();

        user = gson.fromJson(getIntent().getStringExtra("userJson"), User.class);

        binding.accountUsername.setText(user.getUsername());
        binding.accountEmail.setText(user.getEmail());
    }

    @Override
    public void onItemClick(List<FilmToDB> films, int position) {
        FilmToDB film = films.get(position);

        Intent intent = new Intent(AccountActivity.this, FilmDetailsActivity.class);

        intent.putExtra("filmJson", gson.toJson(film));
        intent.putExtra("userJson", gson.toJson(user));

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        likedFilms.clear();
        filmsWithReview.clear();

        DownloadUserLikedFilmsThread thread = new DownloadUserLikedFilmsThread();
        thread.start();
    }

    private class DownloadUserLikedFilmsThread extends Thread {

        @Override
        public void run() {
            super.run();

            database.child("Users").child(user.getEmail().split("@")[0]).child("likedFilms").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        FilmToDB film = dataSnapshot.getValue(FilmToDB.class);

                        likedFilms.add(film);
                    }

                    DownloadPosterThread thread = new DownloadPosterThread(likedFilms, binding.likedFilms);
                    thread.start();

                    FilmsListAdapter adapter = new FilmsListAdapter(likedFilms, AccountActivity.this);
                    binding.likedFilms.setAdapter(adapter);
                    binding.likedFilms.setLayoutManager(new LinearLayoutManager(AccountActivity.this, LinearLayoutManager.HORIZONTAL, false));
                }
            });

            database.child("Users").child(user.getEmail().split("@")[0]).child("reviews").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Review review = dataSnapshot.getValue(Review.class);

                        assert review != null;

                        FilmToDB film = review.getFilm();

                        filmsWithReview.add(film);
                    }

                    DownloadPosterThread thread = new DownloadPosterThread(filmsWithReview, binding.filmsWithReview);
                    thread.start();

                    FilmsListAdapter adapter = new FilmsListAdapter(filmsWithReview, AccountActivity.this);
                    binding.filmsWithReview.setAdapter(adapter);
                    binding.filmsWithReview.setLayoutManager(new LinearLayoutManager(AccountActivity.this, LinearLayoutManager.HORIZONTAL, false));
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
                        film.setBitmap(BitmapFactory.decodeStream(stream));

                        assert recyclerView.getAdapter() != null;

                        int finalI = i;
                        runOnUiThread(() -> recyclerView.getAdapter().notifyItemChanged(finalI));
                    }
                } catch (IOException e) {
                    Log.e("poster", "Cannot download film poster");
                }
            }
        }
    }
}

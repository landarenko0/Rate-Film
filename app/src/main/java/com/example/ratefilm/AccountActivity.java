package com.example.ratefilm;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ratefilm.databinding.AccountLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class AccountActivity extends AppCompatActivity {

    private AccountLayoutBinding binding;
    private User user;
    private ArrayList<FilmToDB> likedFilms;
    private ArrayList<FilmToDB> filmsWithReview;
    private DatabaseReference database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        init();

        setContentView(binding.getRoot());

        DownloadUserLikedFilmsThread thread = new DownloadUserLikedFilmsThread();
        thread.start();
    }

    private void init() {
        binding = AccountLayoutBinding.inflate(getLayoutInflater());

        database = FirebaseDatabase.getInstance().getReference();

        likedFilms = new ArrayList<>();
        filmsWithReview = new ArrayList<>();

        Gson gson = new Gson();

        user = gson.fromJson(getIntent().getStringExtra("userJson"), User.class);

        binding.accountUsername.setText(user.getUsername());
        binding.accountEmail.setText(user.getEmail());
    }

    private class DownloadUserLikedFilmsThread extends Thread {

        @Override
        public void run() {
            super.run();

            database.child("Users").child(user.getEmail().split("@")[0]).child("likedFilms").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            FilmToDB film = dataSnapshot.getValue(FilmToDB.class);

                            likedFilms.add(film);
                        }

                        DownloadPosterThread thread = new DownloadPosterThread(likedFilms, binding.likedFilms);
                        thread.start();

                        FilmsListAdapter adapter = new FilmsListAdapter(likedFilms, user, AccountActivity.this);
                        binding.likedFilms.setAdapter(adapter);
                        binding.likedFilms.setLayoutManager(new LinearLayoutManager(AccountActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    }
                }
            });

            database.child("Users").child(user.getEmail().split("@")[0]).child("reviews").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
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

                        FilmsListAdapter adapter = new FilmsListAdapter(filmsWithReview, user, AccountActivity.this);
                        binding.filmsWithReview.setAdapter(adapter);
                        binding.filmsWithReview.setLayoutManager(new LinearLayoutManager(AccountActivity.this, LinearLayoutManager.HORIZONTAL, false));
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
                        film.setBitmap(BitmapFactory.decodeStream(stream));

                        assert recyclerView.getAdapter() != null;

                        int finalI = i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.getAdapter().notifyItemChanged(finalI);
                            }
                        });
                    }
                } catch (IOException e) {
                    Log.e("poster", "Cannot download film poster");
                }
            }
        }
    }
}

package com.example.ratefilm;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ratefilm.databinding.FilmListLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FilmsListActivity extends AppCompatActivity {

    private FilmListLayoutBinding binding;
    private FilmsService service;
    private static final String API_KEY = "343b5488-f493-4cce-ae84-9e0f0fa4ff97";
    private static final String BASE_URL = "https://kinopoiskapiunofficial.tech";
    private DatabaseReference database;
    private ArrayList<FilmToDB> fantasyFilms;
    private ArrayList<FilmToDB> actionFilms;
    private ArrayList<FilmToDB> thrillerFilms;
    private RecyclerView fantasyRecyclerView;
    private RecyclerView actionRecyclerView;
    private RecyclerView thrillerRecyclerView;
    private User user;
    private Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        init();

        setContentView(binding.getRoot());

        DownloadThread thread = new DownloadThread();
        thread.start();
    }

    private void init() {
        database = FirebaseDatabase.getInstance().getReference();

        DownloadUser thread = new DownloadUser();
        thread.start();

        binding = FilmListLayoutBinding.inflate(getLayoutInflater());
        fantasyRecyclerView = binding.rvFantasyFilms;
        actionRecyclerView = binding.rvActionFilms;
        thrillerRecyclerView = binding.rvThrillerFilms;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(FilmsService.class);

        binding.svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchByKeywords(s);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.item_account) {
                    Intent intent = new Intent(FilmsListActivity.this, AccountActivity.class);

                    intent.putExtra("userJson", gson.toJson(user));

                    startActivity(intent);

                    binding.drawer.close();
                } else {
                    FirebaseAuth.getInstance().signOut();

                    binding.drawer.close();

                    finish();
                }

                return true;
            }
        });

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.drawer.open();
            }
        });

        fantasyFilms = new ArrayList<>();
        actionFilms = new ArrayList<>();
        thrillerFilms = new ArrayList<>();

        gson = new Gson();
    }

    private void searchByKeywords(String keywords) {
        Call<FilmSearchResponse> call = service.searchByKeywords(API_KEY, keywords);

        call.enqueue(new Callback<FilmSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<FilmSearchResponse> call, @NonNull Response<FilmSearchResponse> response) {
                assert response.body() != null;

                List<FilmsByKeyword> responseFilms = response.body().getFilms();

                List<FilmToDB> filmsList = new ArrayList<>();

                for (FilmsByKeyword keywordFilm : responseFilms) {
                    FilmToDB film = new FilmToDB(keywordFilm.getFilmId(), keywordFilm.getDescription(), keywordFilm.getNameEn(), keywordFilm.getNameRu(), keywordFilm.getPosterUrl(),
                            0f);

                    if (film.getNameOriginal() == null) film.setNameOriginal(film.getNameRu());

                    filmsList.add(film);
                }

                Intent intent = new Intent(FilmsListActivity.this, SearchActivity.class);

                intent.putExtra("filmsJson", gson.toJson(filmsList));
                intent.putExtra("query", keywords);
                intent.putExtra("userJson", gson.toJson(user));

                startActivity(intent);
            }

            @Override
            public void onFailure(@NonNull Call<FilmSearchResponse> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), "Не удалось выполнить запрос", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class DownloadUser extends Thread {
        @Override
        public void run() {
            super.run();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            assert currentUser != null;

            String userEmail = currentUser.getEmail();

            assert userEmail != null;

            database.child("Users").child(userEmail.split("@")[0]).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        user = task.getResult().getValue(User.class);
                    } else {
                        Toast.makeText(getApplicationContext(), "Не удалось выполнить запрос", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private class DownloadThread extends Thread {
        @Override
        public void run() {
            super.run();

            database.child("Films").child("Fantasy").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DataSnapshot snapshot : task.getResult().getChildren()) {
                            FilmToDB film = snapshot.getValue(FilmToDB.class);

                            fantasyFilms.add(film);
                        }

                        FilmsListAdapter adapter = new FilmsListAdapter(fantasyFilms, user, FilmsListActivity.this);
                        fantasyRecyclerView.setAdapter(adapter);
                        fantasyRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                        DownloadPosterThread thread = new DownloadPosterThread(fantasyFilms, fantasyRecyclerView);
                        thread.start();
                    }
                }
            });

            database.child("Films").child("Action").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DataSnapshot snapshot : task.getResult().getChildren()) {
                            FilmToDB film = snapshot.getValue(FilmToDB.class);

                            actionFilms.add(film);
                        }

                        FilmsListAdapter adapter = new FilmsListAdapter(actionFilms, user, FilmsListActivity.this);
                        actionRecyclerView.setAdapter(adapter);
                        actionRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                        DownloadPosterThread thread = new DownloadPosterThread(actionFilms, actionRecyclerView);
                        thread.start();
                    }
                }
            });

            database.child("Films").child("Thriller").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DataSnapshot snapshot : task.getResult().getChildren()) {
                            FilmToDB film = snapshot.getValue(FilmToDB.class);

                            thrillerFilms.add(film);
                        }

                        FilmsListAdapter adapter = new FilmsListAdapter(thrillerFilms, user, FilmsListActivity.this);
                        thrillerRecyclerView.setAdapter(adapter);
                        thrillerRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                        DownloadPosterThread thread = new DownloadPosterThread(thrillerFilms, thrillerRecyclerView);
                        thread.start();
                    }
                }
            });
        }
    }

    private class DownloadPosterThread extends Thread {

        private final List<FilmToDB> films;
        private final RecyclerView recyclerView;

        public DownloadPosterThread(List<FilmToDB> films, RecyclerView recyclerView) {
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
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

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

public class FilmsListActivity extends AppCompatActivity implements RecyclerViewOnClickListener {

    private FilmListLayoutBinding binding;
    private FilmsService service;
    private static final String API_KEY = "343b5488-f493-4cce-ae84-9e0f0fa4ff97";
    private static final String BASE_URL = "https://kinopoiskapiunofficial.tech";
    private DatabaseReference database;
    private ArrayList<FilmToDB> fantasyFilms;
    private ArrayList<FilmToDB> actionFilms;
    private ArrayList<FilmToDB> thrillerFilms;
    private User user;
    private Gson gson;
    private boolean userDownloaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        init();

        setContentView(binding.getRoot());
    }

    private void init() {
        database = FirebaseDatabase.getInstance().getReference();

        gson = new Gson();

        fantasyFilms = new ArrayList<>();
        actionFilms = new ArrayList<>();
        thrillerFilms = new ArrayList<>();

        DownloadThread thread = new DownloadThread();
        thread.start();

        binding = FilmListLayoutBinding.inflate(getLayoutInflater());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(FilmsService.class);

        binding.svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (userDownloaded) searchByKeywords(s);
                else Toast.makeText(FilmsListActivity.this, "Пожалуйста, подождите", Toast.LENGTH_SHORT).show();

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
                    FilmToDB film = new FilmToDB(keywordFilm.getFilmId(), keywordFilm.getDescription(), keywordFilm.getNameEn(), keywordFilm.getNameRu(), keywordFilm.getPosterUrl());

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
                Toast.makeText(FilmsListActivity.this, "Не удалось выполнить запрос", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(List<FilmToDB> films, int position) {
        if (!userDownloaded) {
            Toast.makeText(FilmsListActivity.this, "Пожалуйста, подождите", Toast.LENGTH_SHORT).show();
            return;
        }

        FilmToDB film = films.get(position);

        Intent intent = new Intent(FilmsListActivity.this, FilmDetailsActivity.class);

        intent.putExtra("filmJson", gson.toJson(film));
        intent.putExtra("userJson", gson.toJson(user));

        startActivity(intent);
    }


    private class DownloadThread extends Thread {
        @Override
        public void run() {
            super.run();

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            assert firebaseUser != null;
            assert firebaseUser.getEmail() != null;

            database.child("Users").child(firebaseUser.getEmail().split("@")[0]).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        user = task.getResult().getValue(User.class);
                        userDownloaded = true;
                    } else {
                        Toast.makeText(getApplicationContext(), "Не удалось выполнить запрос", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            database.child("Films").child("Fantasy").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DataSnapshot snapshot : task.getResult().getChildren()) {
                            FilmToDB film = snapshot.getValue(FilmToDB.class);

                            fantasyFilms.add(film);
                        }

                        FilmsListAdapter adapter = new FilmsListAdapter(fantasyFilms, FilmsListActivity.this);
                        binding.rvFantasyFilms.setAdapter(adapter);
                        binding.rvFantasyFilms.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                        DownloadPosterThread thread = new DownloadPosterThread(fantasyFilms, binding.rvFantasyFilms);
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

                        FilmsListAdapter adapter = new FilmsListAdapter(actionFilms, FilmsListActivity.this);
                        binding.rvActionFilms.setAdapter(adapter);
                        binding.rvActionFilms.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                        DownloadPosterThread thread = new DownloadPosterThread(actionFilms, binding.rvActionFilms);
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

                        FilmsListAdapter adapter = new FilmsListAdapter(thrillerFilms, FilmsListActivity.this);
                        binding.rvThrillerFilms.setAdapter(adapter);
                        binding.rvThrillerFilms.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                        DownloadPosterThread thread = new DownloadPosterThread(thrillerFilms, binding.rvThrillerFilms);
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

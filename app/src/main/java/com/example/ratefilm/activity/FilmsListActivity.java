package com.example.ratefilm.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ratefilm.data_response.FilmSearchResponse;
import com.example.ratefilm.data_response.FilmToDB;
import com.example.ratefilm.data_response.FilmsByKeyword;
import com.example.ratefilm.interfaces.FilmsService;
import com.example.ratefilm.R;
import com.example.ratefilm.interfaces.RecyclerViewOnClickListener;
import com.example.ratefilm.data_response.User;
import com.example.ratefilm.adapters.FilmsListAdapter;
import com.example.ratefilm.databinding.FilmListLayoutBinding;
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
    private DatabaseReference database;
    private ArrayList<FilmToDB> fantasyFilms;
    private ArrayList<FilmToDB> actionFilms;
    private ArrayList<FilmToDB> thrillerFilms;
    private User user;
    private Gson gson;
    private boolean userDownloaded = false;
    private static final String API_KEY = "343b5488-f493-4cce-ae84-9e0f0fa4ff97";
    private static final String BASE_URL = "https://kinopoiskapiunofficial.tech";
    private static final String USER_JSON = "userJson";
    private static final String FILMS_JSON = "filmsJson";
    private static final String FILM_JSON = "filmJson";
    private static final String QUERY = "query";
    private static final String USERS = "Users";
    private static final String FILMS = "Films";
    private static final String FANTASY = "Fantasy";
    private static final String ACTION = "Action";
    private static final String THRILLER = "Thriller";
    private static final String TAG = "posterDownloadError";
    private static final String ERROR_MESSAGE = "Cannot download film poster";

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

        binding.rvFantasyFilms.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvActionFilms.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvThrillerFilms.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

        binding.svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (userDownloaded) searchByKeywords(s);
                else Toast.makeText(FilmsListActivity.this, getResources().getText(R.string.please_wait), Toast.LENGTH_SHORT).show();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        binding.navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.item_account) {
                if (!userDownloaded) {
                    Toast.makeText(FilmsListActivity.this, getResources().getText(R.string.please_wait), Toast.LENGTH_SHORT).show();

                    return false;
                }

                Intent intent = new Intent(FilmsListActivity.this, AccountActivity.class);

                intent.putExtra(USER_JSON, gson.toJson(user));

                startActivity(intent);

                binding.drawer.close();
            } else {
                FirebaseAuth.getInstance().signOut();

                binding.drawer.close();

                toLoginActivity();

                finish();
            }

            return true;
        });

        binding.button.setOnClickListener(view -> binding.drawer.open());
    }

    private void searchByKeywords(String keywords) {
        Call<FilmSearchResponse> call = service.searchByKeywords(API_KEY, keywords);

        call.enqueue(new Callback<FilmSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<FilmSearchResponse> call, @NonNull Response<FilmSearchResponse> response) {
                if (response.body() == null) return;

                List<FilmsByKeyword> responseFilms = response.body().getFilms();

                List<FilmToDB> filmsList = new ArrayList<>();

                for (FilmsByKeyword keywordFilm : responseFilms) {
                    FilmToDB film = new FilmToDB(keywordFilm.getFilmId(), keywordFilm.getDescription(), keywordFilm.getNameEn(), keywordFilm.getNameRu(), keywordFilm.getPosterUrl());

                    filmsList.add(film);
                }

                Intent intent = new Intent(FilmsListActivity.this, SearchActivity.class);

                intent.putExtra(FILMS_JSON, gson.toJson(filmsList));
                intent.putExtra(QUERY, keywords);
                intent.putExtra(USER_JSON, gson.toJson(user));

                startActivity(intent);
            }

            @Override
            public void onFailure(@NonNull Call<FilmSearchResponse> call, @NonNull Throwable t) {
                Toast.makeText(FilmsListActivity.this, getResources().getText(R.string.request_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(List<FilmToDB> films, int position) {
        if (!userDownloaded) {
            Toast.makeText(FilmsListActivity.this, getResources().getText(R.string.please_wait), Toast.LENGTH_SHORT).show();
            return;
        }

        FilmToDB film = films.get(position);

        Intent intent = new Intent(FilmsListActivity.this, FilmDetailsActivity.class);

        intent.putExtra(FILM_JSON, gson.toJson(film));
        intent.putExtra(USER_JSON, gson.toJson(user));

        startActivity(intent);
    }

    private void toLoginActivity() {
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
    }


    private class DownloadThread extends Thread {
        @Override
        public void run() {
            super.run();

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if (firebaseUser == null) return;
            if (firebaseUser.getEmail() == null) return;

            database.child(USERS).child(firebaseUser.getEmail().split("@")[0]).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user = task.getResult().getValue(User.class);
                    userDownloaded = true;
                } else {
                    runOnUiThread(() -> Toast.makeText(FilmsListActivity.this, getResources().getText(R.string.request_fail), Toast.LENGTH_SHORT).show());
                }
            });

            database.child(FILMS).child(FANTASY).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DataSnapshot snapshot : task.getResult().getChildren()) {
                        FilmToDB film = snapshot.getValue(FilmToDB.class);

                        fantasyFilms.add(film);
                    }

                    FilmsListAdapter adapter = new FilmsListAdapter(fantasyFilms, FilmsListActivity.this);
                    binding.rvFantasyFilms.setAdapter(adapter);

                    DownloadPosterThread thread = new DownloadPosterThread(fantasyFilms, binding.rvFantasyFilms);
                    thread.start();
                }
            });

            database.child(FILMS).child(ACTION).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DataSnapshot snapshot : task.getResult().getChildren()) {
                        FilmToDB film = snapshot.getValue(FilmToDB.class);

                        actionFilms.add(film);
                    }

                    FilmsListAdapter adapter = new FilmsListAdapter(actionFilms, FilmsListActivity.this);
                    binding.rvActionFilms.setAdapter(adapter);

                    DownloadPosterThread thread = new DownloadPosterThread(actionFilms, binding.rvActionFilms);
                    thread.start();
                }
            });

            database.child(FILMS).child(THRILLER).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DataSnapshot snapshot : task.getResult().getChildren()) {
                        FilmToDB film = snapshot.getValue(FilmToDB.class);

                        thrillerFilms.add(film);
                    }

                    FilmsListAdapter adapter = new FilmsListAdapter(thrillerFilms, FilmsListActivity.this);
                    binding.rvThrillerFilms.setAdapter(adapter);

                    DownloadPosterThread thread = new DownloadPosterThread(thrillerFilms, binding.rvThrillerFilms);
                    thread.start();
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

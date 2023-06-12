package com.example.ratefilm;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ratefilm.databinding.FilmListLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FilmsListActivity extends AppCompatActivity {

    private FilmListLayoutBinding binding;
    private Retrofit retrofit;
    private static final String API_KEY = "343b5488-f493-4cce-ae84-9e0f0fa4ff97";
    private static final String BASE_URL = "https://kinopoiskapiunofficial.tech";
    private DatabaseReference database;
    private ArrayList<FilmToDB> films;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        init();

        setContentView(binding.getRoot());

        DownloadThread thread = new DownloadThread();
        thread.start();
        ////////////////////

//        RecyclerView recyclerView = binding.rvFilmsList;
//
//        MyAdapter adapter = new MyAdapter(films);
//        recyclerView.setAdapter(adapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        binding.button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                retrofit = new Retrofit.Builder()
//                        .baseUrl(BASE_URL)
//                        .addConverterFactory(GsonConverterFactory.create())
//                        .build();
//
//                FilmsService service = retrofit.create(FilmsService.class);
//
//                Call<Film> call = service.getFilmById(API_KEY, binding.etId.getText().toString());
//
//                call.enqueue(new Callback<Film>() {
//                    @Override
//                    public void onResponse(@NonNull Call<Film> call, @NonNull Response<Film> response) {
//                        if (response.isSuccessful()) {
//                            Film film = response.body();
//
//                            assert film != null;
//
//                            binding.tvResponse.setText(film.getNameRu());
//
//                            ArrayList<Review> arrayList = new ArrayList<>();
//                            arrayList.add(new Review("Классный фильм!", 5.0f));
//
//                            FilmToDB filmToDB = new FilmToDB(film.getKinopoiskId(),
//                                    film.getDescription(),
//                                    film.getNameEn(),
//                                    film.getNameRu(),
//                                    film.getPosterUrl(),
//                                    5.0f,
//                                    0,
//                                    arrayList);
//
//                            database.child("Films").push().setValue(filmToDB);
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NonNull Call<Film> call, @NonNull Throwable t) {
//                        Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
        //////////////////
    }

    private void init() {
        binding = FilmListLayoutBinding.inflate(getLayoutInflater());
        database = FirebaseDatabase.getInstance().getReference();
        recyclerView = binding.rvFilmsList;
        films = new ArrayList<>();
    }

    private class DownloadThread extends Thread {
        @Override
        public void run() {
            super.run();

            database.child("Films").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DataSnapshot snapshot : task.getResult().getChildren()) {
                            FilmToDB film = snapshot.getValue(FilmToDB.class);

                            films.add(film);
                        }

                        FilmsListAdapter adapter = new FilmsListAdapter(films, FilmsListActivity.this);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                        DownloadPoster thread = new DownloadPoster();
                        thread.start();
                    }
                }
            });
        }
    }

    private class DownloadPoster extends Thread {
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

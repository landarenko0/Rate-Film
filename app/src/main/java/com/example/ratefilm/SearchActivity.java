package com.example.ratefilm;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ratefilm.databinding.SearchLayoutBinding;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private SearchLayoutBinding binding;
    private List<FilmToDB> films;
    private String query;
    private User user;
    private Gson gson;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        init();

        setContentView(binding.getRoot());
    }

    private void init() {
        binding = SearchLayoutBinding.inflate(getLayoutInflater());

        gson = new Gson();

        Type type = new TypeToken<List<FilmToDB>>(){}.getType();

        films = gson.fromJson(getIntent().getStringExtra("filmsJson"), type);
        query = getIntent().getStringExtra("query");
        user = gson.fromJson(getIntent().getStringExtra("userJson"), User.class);
        recyclerView = binding.requestFilms;

        SearchAdapter adapter = new SearchAdapter(films, user, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        binding.queryText.setText("Результат поиска по запросу: " + query);

        DownloadPosterThread thread = new DownloadPosterThread();
        thread.start();
    }

    private class DownloadPosterThread extends Thread {
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

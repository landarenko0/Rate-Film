package com.example.ratefilm;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ratefilm.databinding.SearchLayoutBinding;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements RecyclerViewOnClickListener {

    private SearchLayoutBinding binding;
    private List<FilmToDB> films;
    private Gson gson;

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
        String query = getIntent().getStringExtra("query");

        SearchAdapter adapter = new SearchAdapter(films, SearchActivity.this);
        binding.requestFilms.setAdapter(adapter);
        binding.requestFilms.setLayoutManager(new LinearLayoutManager(this));

        binding.queryText.setText("Результат поиска по запросу: " + query);

        DownloadPosterThread thread = new DownloadPosterThread();
        thread.start();
    }

    @Override
    public void onItemClick(List<FilmToDB> films, int position) {
        FilmToDB film = films.get(position);

        Intent intent = new Intent(SearchActivity.this, FilmDetailsActivity.class);

        intent.putExtra("filmJson", gson.toJson(film));
        intent.putExtra("uesrJson", getIntent().getStringExtra("userJson"));

        startActivity(intent);
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

                        assert binding.requestFilms.getAdapter() != null;

                        int finalI = i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.requestFilms.getAdapter().notifyItemChanged(finalI);
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

package com.example.ratefilm.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ratefilm.data_response.FilmToDB;
import com.example.ratefilm.R;
import com.example.ratefilm.interfaces.RecyclerViewOnClickListener;
import com.example.ratefilm.adapters.SearchAdapter;
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
    private static final String FILMS_JSON = "filmsJson";
    private static final String QUERY = "query";
    private static final String FILM_JSON = "filmJson";
    private static final String USER_JSON = "userJson";
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
        binding = SearchLayoutBinding.inflate(getLayoutInflater());

        gson = new Gson();

        Type type = new TypeToken<List<FilmToDB>>(){}.getType();

        films = gson.fromJson(getIntent().getStringExtra(FILMS_JSON), type);
        String query = getIntent().getStringExtra(QUERY);

        SearchAdapter adapter = new SearchAdapter(films, SearchActivity.this);
        binding.requestFilms.setAdapter(adapter);
        binding.requestFilms.setLayoutManager(new LinearLayoutManager(this));

        binding.queryText.setText(getString(R.string.result_of_search, query));

        DownloadPosterThread thread = new DownloadPosterThread();
        thread.start();
    }

    @Override
    public void onItemClick(List<FilmToDB> films, int position) {
        FilmToDB film = films.get(position);

        Intent intent = new Intent(SearchActivity.this, FilmDetailsActivity.class);

        intent.putExtra(FILM_JSON, gson.toJson(film));
        intent.putExtra(USER_JSON, getIntent().getStringExtra(USER_JSON));

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

                        if (binding.requestFilms.getAdapter() == null) return;

                        int finalI = i;
                        runOnUiThread(() -> binding.requestFilms.getAdapter().notifyItemChanged(finalI));
                    }
                } catch (IOException e) {
                    Log.e(TAG, ERROR_MESSAGE);
                }
            }
        }
    }
}

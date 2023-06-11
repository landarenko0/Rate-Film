package com.example.ratefilm;

import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ratefilm.databinding.FilmDetailsBinding;
import com.google.gson.Gson;

public class FilmDetailsActivity extends AppCompatActivity {

    private FilmDetailsBinding binding;
    private TextView filmName;
    private TextView filmDescription;
    private TextView rating;
    private ImageView poster;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        init();

        setContentView(binding.getRoot());

        setFilmDetails();
    }

    private void init() {
        binding = FilmDetailsBinding.inflate(getLayoutInflater());

        filmName = binding.detailsFilmName;
        filmDescription = binding.detailsFilmDescription;
        rating = binding.detailsRating;
        poster = binding.detailsPosterImage;
        recyclerView = binding.rvReviews;
    }

    private void setFilmDetails() {
        Gson gson = new Gson();

        String filmJson = getIntent().getStringExtra("filmJson");
        FilmToDB film = gson.fromJson(filmJson, FilmToDB.class);

        poster.setImageBitmap(film.getBitmap());
        filmName.setText(film.getNameRu());
        filmDescription.setText(film.getDescription());
        rating.setText(String.valueOf(film.getRating()));

        ReviewListAdapter adapter = new ReviewListAdapter(film.getReviews());

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}

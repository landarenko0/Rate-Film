package com.example.ratefilm;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

public class FilmsListAdapter extends RecyclerView.Adapter<FilmsListAdapter.ViewHolder> {

    private final List<FilmToDB> films;
    private final Context parentContext;

    public FilmsListAdapter(List<FilmToDB> films, Context parentContext) {
        this.films = films;
        this.parentContext = parentContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.film, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FilmToDB film = films.get(position);

        holder.imageView.setImageBitmap(film.getBitmap());
        holder.textView.setText(film.getNameRu());
    }

    @Override
    public int getItemCount() {
        return films.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.poster_image);
            textView = itemView.findViewById(R.id.film_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showFilmDetails(getAdapterPosition());
                }
            });
        }

        private void showFilmDetails(int index) {
            Intent intent = new Intent(parentContext, FilmDetailsActivity.class);

            FilmToDB film = films.get(index);

            Gson gson = new Gson();
            String filmJson = gson.toJson(film);

            intent.putExtra("filmJson", filmJson);

            parentContext.startActivity(intent);
        }
    }
}

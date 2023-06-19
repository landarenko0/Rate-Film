package com.example.ratefilm.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ratefilm.data_response.FilmToDB;
import com.example.ratefilm.interfaces.RecyclerViewOnClickListener;
import com.example.ratefilm.databinding.SearchFilmBinding;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private final List<FilmToDB> films;
    private final RecyclerViewOnClickListener listener;

    public SearchAdapter(List<FilmToDB> films, RecyclerViewOnClickListener listener) {
        this.films = films;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SearchFilmBinding binding = SearchFilmBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        return new SearchAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FilmToDB film = films.get(position);

        holder.binding.searchPosterImage.setImageBitmap(film.getBitmap());
        holder.binding.searchFilmName.setText(film.getNameRu());
    }

    @Override
    public int getItemCount() {
        return films.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final SearchFilmBinding binding;

        public ViewHolder(@NonNull SearchFilmBinding binding) {
            super(binding.getRoot());

            this.binding = binding;

            binding.getRoot().setOnClickListener(view -> listener.onItemClick(films, getAdapterPosition()));
        }
    }
}

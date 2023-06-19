package com.example.ratefilm.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ratefilm.data_response.FilmToDB;
import com.example.ratefilm.intefaces.RecyclerViewOnClickListener;
import com.example.ratefilm.databinding.FilmBinding;

import java.util.List;

public class FilmsListAdapter extends RecyclerView.Adapter<FilmsListAdapter.ViewHolder> {

    private final List<FilmToDB> films;
    private final RecyclerViewOnClickListener listener;

    public FilmsListAdapter(List<FilmToDB> films, RecyclerViewOnClickListener listener) {
        this.films = films;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FilmBinding binding = FilmBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FilmToDB film = films.get(position);

        holder.binding.posterImage.setImageBitmap(film.getBitmap());
        holder.binding.filmName.setText(film.getNameRu());
    }

    @Override
    public int getItemCount() {
        return films.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final FilmBinding binding;

        public ViewHolder(@NonNull FilmBinding binding) {
            super(binding.getRoot());

            this.binding = binding;

            binding.getRoot().setOnClickListener(view -> listener.onItemClick(films, getAdapterPosition()));
        }
    }
}

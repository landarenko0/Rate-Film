package com.example.ratefilm;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ratefilm.databinding.ReviewBinding;

import java.util.List;

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ViewHolder> {

    private final List<Review> reviews;

    public ReviewListAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ReviewBinding binding = ReviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);

        holder.binding.username.setText(review.getUsername());
        holder.binding.rate.setText(String.valueOf(review.getRating()));
        holder.binding.reviewText.setText(review.getReview());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ReviewBinding binding;

        public ViewHolder(@NonNull ReviewBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }
}

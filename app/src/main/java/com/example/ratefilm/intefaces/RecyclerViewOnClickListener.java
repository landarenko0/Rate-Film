package com.example.ratefilm.intefaces;

import com.example.ratefilm.data_response.FilmToDB;

import java.util.List;

public interface RecyclerViewOnClickListener {
    void onItemClick(List<FilmToDB> films, int position);
}

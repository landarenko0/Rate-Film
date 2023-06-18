package com.example.ratefilm;

import java.util.List;

public interface RecyclerViewOnClickListener {
    void onItemClick(List<FilmToDB> films, int position);
}

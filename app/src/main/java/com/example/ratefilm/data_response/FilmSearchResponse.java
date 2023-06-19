package com.example.ratefilm.data_response;

import java.util.List;

public class FilmSearchResponse {
    private String keyword;
    private int pagesCount;
    private int searchFilmsCountResult;
    private List<FilmsByKeyword> films;

    public FilmSearchResponse() {

    }

    public FilmSearchResponse(String keyword, int pagesCount, int searchFilmsCountResult, List<FilmsByKeyword> films) {
        this.keyword = keyword;
        this.pagesCount = pagesCount;
        this.searchFilmsCountResult = searchFilmsCountResult;
        this.films = films;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(int pagesCount) {
        this.pagesCount = pagesCount;
    }

    public int getSearchFilmsCountResult() {
        return searchFilmsCountResult;
    }

    public void setSearchFilmsCountResult(int searchFilmsCountResult) {
        this.searchFilmsCountResult = searchFilmsCountResult;
    }

    public List<FilmsByKeyword> getFilms() {
        return films;
    }

    public void setFilms(List<FilmsByKeyword> films) {
        this.films = films;
    }
}

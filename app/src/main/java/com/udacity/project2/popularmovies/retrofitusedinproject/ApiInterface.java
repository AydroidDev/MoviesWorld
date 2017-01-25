package com.udacity.project2.popularmovies.retrofitusedinproject;

import com.udacity.project2.popularmovies.retrofitusedinproject.MovieResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by Dell on 12/22/2016.
 */
public interface ApiInterface {
    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(@Query("api_key") String apiKey);

    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(@QueryMap Map<String, String> options);

    @GET("movie/{id}")
    Call<MovieResponse> getMovieDetails(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/videos")
    Call<MovieTrailerResponse> getMovieTrailers(@Path("id") String id,@Query("api_key") String apiKey);
}
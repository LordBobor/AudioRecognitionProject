package ru.ekozoch.audiorcognitionproject;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by ekozoch on 01.06.15.
 */
public interface LastFMService {
        @GET("/2.0/?method=track.search&limit=1&page=1&api_key=270783472fd32b5af9bfe6b986a4d012&format=json")
        void listRepos(@Query("track") String user, @Query("artist") String artist, Callback<Response> cb);

        @GET("/2.0/?method=artist.search&limit=1&api_key=270783472fd32b5af9bfe6b986a4d012&format=json")
        void getArtist(@Query("artist") String artist, Callback<Response> cb);
}

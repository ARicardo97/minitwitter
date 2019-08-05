package com.ame.minitwitter.retrofit;

import com.ame.minitwitter.retrofit.request.RequestCreateTweet;
import com.ame.minitwitter.retrofit.request.RequestUserProfile;
import com.ame.minitwitter.retrofit.response.ResponseUploadPhoto;
import com.ame.minitwitter.retrofit.response.ResponseUserProfile;
import com.ame.minitwitter.retrofit.response.Tweet;
import com.ame.minitwitter.retrofit.response.TweetDeleted;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface AuthTwitterService {


    //Tweets
    @GET("tweets/all")
    Call<List<Tweet>> getAllTweets();

    @POST("tweets/create")
    Call<Tweet> createTweet(@Body RequestCreateTweet requestCreateTweet);


    @POST("tweets/like/{idTweet}")
                       //indicandole el nombre del parametro se hace con @Path
    Call<Tweet> likeTweet(@Path("idTweet") int idTweet);

    @DELETE("tweets/{idTweet}")
    Call<TweetDeleted> deleteTweet(@Path("idTweet")int idTweet);

    //Users
    @GET("users/profile")
    Call<ResponseUserProfile> getProfile();

    //Para modificar datos Existentes se usa PUT actualizando datos
    @PUT("users/profile")
    Call<ResponseUserProfile> updateProfile(@Body RequestUserProfile requestUserProfile);

    @Multipart //para enviar ficheros por partes
    @POST("users/uploadprofilephoto")
    Call<ResponseUploadPhoto> uploadProfilePhoto(@Part("file\"; filename=\"photo.jpg\" ")RequestBody file);

}

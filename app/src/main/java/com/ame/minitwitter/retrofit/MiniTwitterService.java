package com.ame.minitwitter.retrofit;

import com.ame.minitwitter.retrofit.request.RequestLogin;
import com.ame.minitwitter.retrofit.request.RequestSignup;
import com.ame.minitwitter.retrofit.response.ResponseAuth;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MiniTwitterService {

    //Petición del Login
    @POST("auth/login")
    Call<ResponseAuth> doLogin(@Body RequestLogin requestLogin);

    //Petición del Registro
    @POST("auth/signup")
    Call<ResponseAuth> doSignUp(@Body RequestSignup requestSignup);
}

package com.ame.minitwitter.retrofit.response;

import com.ame.minitwitter.common.Constantes;
import com.ame.minitwitter.retrofit.AuthInterceptor;
import com.ame.minitwitter.retrofit.AuthTwitterService;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthTwitterClient {
    private  static AuthTwitterClient instance = null;
    private AuthTwitterService miniTwitterService;
    private Retrofit retrofit;

    public AuthTwitterClient(){
        //incluir en la cabezera de la peticion el TOKEN que autoriza al usuario
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.addInterceptor(new AuthInterceptor());
        OkHttpClient cliente = okHttpClientBuilder.build();

       retrofit = new Retrofit.Builder()
               .baseUrl(Constantes.API_MINITWITTER_BASE_URL)
               .addConverterFactory(GsonConverterFactory.create())
               .client(cliente) //decirle que tenemos un cliente
               .build();
       //Instanciamos
       miniTwitterService = retrofit.create(AuthTwitterService.class);

    }

    //PATRON SINGLETON la instancia solo se crea una vez si es nula
    public static AuthTwitterClient getInstance(){
        if (instance == null){
            instance = new AuthTwitterClient();
        }
        return instance;
    }

    public AuthTwitterService getAuthTwitterService(){
        return miniTwitterService;
    }
}

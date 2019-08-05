package com.ame.minitwitter.retrofit;

import com.ame.minitwitter.common.Constantes;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MiniTwitterClient {
    private  static MiniTwitterClient instance = null;
    private MiniTwitterService miniTwitterService;
    private Retrofit retrofit;

    public MiniTwitterClient(){
       retrofit = new Retrofit.Builder()
               .baseUrl(Constantes.API_MINITWITTER_BASE_URL)
               .addConverterFactory(GsonConverterFactory.create())
               .build();
       //Instanciamos
       miniTwitterService = retrofit.create(MiniTwitterService.class);

    }

    //PATRON SINGLETON la instancia solo se crea una vez si es nula
    public static MiniTwitterClient getInstance(){
        if (instance == null){
            instance = new MiniTwitterClient();
        }
        return instance;
    }

    public MiniTwitterService getMiniTwitterService(){
        return miniTwitterService;
    }
}

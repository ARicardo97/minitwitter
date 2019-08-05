package com.ame.minitwitter.data;

import android.arch.lifecycle.MutableLiveData;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.ame.minitwitter.common.Constantes;
import com.ame.minitwitter.common.MyApp;
import com.ame.minitwitter.common.SharedPreferencesManager;
import com.ame.minitwitter.retrofit.AuthTwitterService;
import com.ame.minitwitter.retrofit.request.RequestUserProfile;
import com.ame.minitwitter.retrofit.response.AuthTwitterClient;
import com.ame.minitwitter.retrofit.response.ResponseUploadPhoto;
import com.ame.minitwitter.retrofit.response.ResponseUserProfile;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {
    AuthTwitterService authTwitterService;
    AuthTwitterClient authTwitterClient;
    MutableLiveData<ResponseUserProfile> userProfile; //se utiliza MutableLiveData para actualizar lista
    MutableLiveData<String> photoProfile; //para la subida de foto

    //Constructor
    ProfileRepository(){
        //inicializamos
        authTwitterClient = AuthTwitterClient.getInstance();
        authTwitterService = authTwitterClient.getAuthTwitterService();
        userProfile = getProfile();

        if (photoProfile == null) {
            photoProfile = new MutableLiveData<>();
        }
    }

    public MutableLiveData<String> getPhotoProfile(){
        return photoProfile;
    }

    //codigo que se conecta a nuestra API y nos devuelve los Tweets
    public MutableLiveData<ResponseUserProfile> getProfile(){

        if (userProfile == null){
            userProfile = new MutableLiveData<>();
        }

        Call<ResponseUserProfile> call = authTwitterService.getProfile();
        call.enqueue(new Callback<ResponseUserProfile>() {
            @Override
            public void onResponse(Call<ResponseUserProfile> call, Response<ResponseUserProfile> response) {
                if (response.isSuccessful()){
                    userProfile.setValue(response.body());
                }else {
                    Toast.makeText(MyApp.getContext(),"Algo Salió Mal. Intente de Nuevo",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseUserProfile> call, Throwable t) {
                Toast.makeText(MyApp.getContext(),"Error de Conexión. Intente de Nuevo",Toast.LENGTH_SHORT).show();
            }
        });

        return userProfile;
    }
           //Void
    public void updateProfile(RequestUserProfile requestUserProfile){
        //peticion
        Call<ResponseUserProfile> call = authTwitterService.updateProfile(requestUserProfile);
        call.enqueue(new Callback<ResponseUserProfile>() {
            @Override
            public void onResponse(Call<ResponseUserProfile> call, Response<ResponseUserProfile> response) {
              if (response.isSuccessful()){
                  userProfile.setValue(response.body()); //seteamos...y la razon de no devolver nada con return xq se devuelve en el setValue
              }else {
                  Toast.makeText(MyApp.getContext(),"Algo Salió Mal",Toast.LENGTH_SHORT).show();
              }
            }

            @Override
            public void onFailure(Call<ResponseUserProfile> call, Throwable t) {
              Toast.makeText(MyApp.getContext(),"Error de Conexión",Toast.LENGTH_SHORT).show();
            }
        });

        //cuando no kieres que se devuelva nada...se usa el Void al principio
    }

    public void uploadPhoto(String photoPath){
        File file = new File(photoPath);

        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpg"), file); //le pasamos el fichero

        Call<ResponseUploadPhoto> call = authTwitterService.uploadProfilePhoto(requestBody);
        call.enqueue(new Callback<ResponseUploadPhoto>() {
            @Override
            public void onResponse(Call<ResponseUploadPhoto> call, Response<ResponseUploadPhoto> response) {
                if (response.isSuccessful()){
                    SharedPreferencesManager.setSomeStringValue(Constantes.PREF_PHOTOURL, response.body().getFilename()); //para guardar los cambios
                    photoProfile.setValue(response.body().getFilename()); //tenemos un nuevo valor...00
                }else {
                    Toast.makeText(MyApp.getContext(),"Algo sucedio. Intenta de Nuevo",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseUploadPhoto> call, Throwable t) {
                Toast.makeText(MyApp.getContext(),"Error de Conexión",Toast.LENGTH_SHORT).show();
            }
        });
    }

}

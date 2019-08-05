package com.ame.minitwitter.data;

import android.arch.lifecycle.MutableLiveData;
import android.widget.Toast;

import com.ame.minitwitter.common.Constantes;
import com.ame.minitwitter.common.MyApp;
import com.ame.minitwitter.common.SharedPreferencesManager;
import com.ame.minitwitter.retrofit.request.RequestCreateTweet;
import com.ame.minitwitter.retrofit.response.AuthTwitterClient;
import com.ame.minitwitter.retrofit.AuthTwitterService;
import com.ame.minitwitter.retrofit.response.Like;
import com.ame.minitwitter.retrofit.response.Tweet;
import com.ame.minitwitter.retrofit.response.TweetDeleted;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TweetRepository  {
    AuthTwitterService authTwitterService;
    AuthTwitterClient authTwitterClient;
    MutableLiveData<List<Tweet>> allTweets; //se utiliza MutableLiveData para actualizar lista
    MutableLiveData<List<Tweet>> favTweets;
    String userName;

    TweetRepository(){
        //inicializamos
        authTwitterClient = AuthTwitterClient.getInstance();
        authTwitterService = authTwitterClient.getAuthTwitterService();
        allTweets = getAllTweets();
        userName = SharedPreferencesManager.getSomeStringValue(Constantes.PREF_USERNAME);
    }
    //codigo que se conecta a nuestra API y nos devuelve los Tweets
    public MutableLiveData<List<Tweet>> getAllTweets(){

        if (allTweets == null){
            allTweets = new MutableLiveData<>();
        }

        Call<List<Tweet>> call = authTwitterService.getAllTweets();  //obtenemos la lista de tweets
        //metodo que nos permite ejecutar en segundo plano independiente del hilo principal  la peticion del servidor
        call.enqueue(new Callback<List<Tweet>>() {
            @Override                                //Llamamos al servidor...
            public void onResponse(Call<List<Tweet>> call, Response<List<Tweet>> response) {
                //si la respuesta es correcta
                if (response.isSuccessful()){
                    allTweets.setValue(response.body()); //obtenemos toda la lista de Tweets
                }else {
                    Toast.makeText(MyApp.getContext(),"Algo Salió Mal. Intentelo de Nuevo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Tweet>> call, Throwable t) {
                Toast.makeText(MyApp.getContext(),"Error en la Conexión. Intentelo de Nuevo", Toast.LENGTH_SHORT).show();
            }
        });
        //Actualizando la lista de Tweets
        return allTweets;
    }

    public MutableLiveData<List<Tweet>> getFavsTweets(){
        if (favTweets == null){
            favTweets = new MutableLiveData<>();
        }
        List<Tweet> newFavList = new ArrayList<>();
        //Iterator nos permite recorrer la lista
        Iterator itTweets = allTweets.getValue().iterator();

        //mientras que haya un elemento de la lista de tweets, "hasNext" mientas k aya un siguente elemento
        while (itTweets.hasNext()){
            //obtenemos el elemento actual del tweet
                           //casteamos el obejto q nos devuelve el iterator next
            Tweet current = (Tweet) itTweets.next();
            Iterator itLikes = current.getLikes().iterator();

            boolean enc = false;
            //mientras q aya elementos de likes y ademas no hayamos encontrado el usuario logueado en la lista de likes
            //seguimos recorriendo la lista de likes
            while (itLikes.hasNext() && !enc){
                Like like = (Like)itLikes.next();
                //si el like actual..teniendo ese like el nombre del usuario es = al nombre de usuario logueado
                if (like.getUsername().equals(userName)){
                    enc = true; //hemos encontrado el like...dejamos de recorrer el tweet
                    newFavList.add(current);//añadimos el nuevo tweet favorito a al lista...y ya tenemos un nuevo tweet
                }
            }
        }
        //le comunicamos a cualquier observador que este pendiente de esta lista...que hay nuevo conjunto de datos
        favTweets.setValue(newFavList); //el observador va poder recibir la nueva lista de datos

        return favTweets;
    }

    public void createTweet(String mensaje){
        RequestCreateTweet requestCreateTweet = new RequestCreateTweet(mensaje); //instanciamos le pasamos el mensaje
        Call<Tweet> call = authTwitterService.createTweet(requestCreateTweet); //Invocamos la Petición

        //Una llamada al API
        call.enqueue(new Callback<Tweet>() {
            @Override
            public void onResponse(Call<Tweet> call, Response<Tweet> response) {
                //Recibimos la respuesta correcta o incorrecta
                if (response.isSuccessful()){
                    //correcta...se actualiza nuestra lista de tweets
                    List<Tweet> listaClonada = new ArrayList<>();
                    //Añadimos en primer lugar el nuevo tweet que nos llega del servidor
                    listaClonada.add(response.body());
                                                                  //i++ es por cada elemento vamos hacer una copia
                    for (int i=0; i < allTweets.getValue().size(); i++){
                        listaClonada.add(new Tweet(allTweets.getValue().get(i)));
                    }
                    allTweets.setValue(listaClonada); //Seteamos sobre la variable allTweets

                }else {
                    Toast.makeText(MyApp.getContext(),"Algo Salió Mal. Inténtelo de Nuevo",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Tweet> call, Throwable t) {
                //Nos indica Errores pero de Conexión
                Toast.makeText(MyApp.getContext(),"Error en la Conexión. Inténtelo de Nuevo",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Metodo para eliminar un tweet
    public void deleteTweet(final int idTweet){
        Call<TweetDeleted> call = authTwitterService.deleteTweet(idTweet);

        call.enqueue(new Callback<TweetDeleted>() {
            @Override
            public void onResponse(Call<TweetDeleted> call, Response<TweetDeleted> response) {
                //recibimos la respuesta del servidor
                if (response.isSuccessful()){
                    //Realizar una copia de todos los tweets excepto del tweet q ya se ha eliminado
                    //Lista clonada
                    List<Tweet> clonedTweets = new ArrayList<>();
                    //Recoremos la lista
                    for (int i=0; i < allTweets.getValue().size(); i++){
                        //   obtener lista. posicion actual. identificador != distinto del tweet q se acaba de eliminar
                        if (allTweets.getValue().get(i).getId() != idTweet){
                            //kiere decir que ese tweet debe conservarse en la lista
                            clonedTweets.add(new Tweet(allTweets.getValue().get(i)));
                        }
                    }
                    //le indicamos a los Observadores una nueva actualizacion de datos
                    //y q contiene todos los tweets menos los eliminados
                      allTweets.setValue(clonedTweets);
                      getFavsTweets(); //actualizamos la lista de los tweets favoritos tambien
                }else {
                    Toast.makeText(MyApp.getContext(),"Algo Salió Mal. Intentelo de Nuevo",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TweetDeleted> call, Throwable t) {
               //fallas de conexion
                Toast.makeText(MyApp.getContext(),"Error en la Conexión. Intente de Nuevo",Toast.LENGTH_SHORT).show();
            }
        });
    }


    //metodo para marcar el tweet
    public void likeTweet(final int idTweet){
        Call<Tweet> call = authTwitterService.likeTweet(idTweet); //solo le pasamos el idTweet

        //Una llamada al API
        call.enqueue(new Callback<Tweet>() {
            @Override
            public void onResponse(Call<Tweet> call, Response<Tweet> response) {
                //Recibimos la respuesta correcta o incorrecta
                if (response.isSuccessful()){
                    //correcta...se actualiza nuestra lista de tweets
                    List<Tweet> listaClonada = new ArrayList<>();

                    //i++  por cada elemento vamos hacer una copia
                    for (int i=0; i < allTweets.getValue().size(); i++){
                        //vamos a estar buscando si el tweet = al idTweet
                        if (allTweets.getValue().get(i).getId() == idTweet){
                            //si hemos encontrado en la lista original
                            //el elemento que le hemos hecho like
                            //introducimos el elemento que nos ha llegado del servidor
                            listaClonada.add(response.body());
                        }else {
                            listaClonada.add(new Tweet(allTweets.getValue().get(i)));
                        }
                    }
                    allTweets.setValue(listaClonada); //Seteamos sobre la variable allTweets

                    //refrescando la lista de favoritos
                    getFavsTweets(); //getFavsTweets vuelve a recorrer la lista

                }else {
                    Toast.makeText(MyApp.getContext(),"Algo Salió Mal. Inténtelo de Nuevo",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Tweet> call, Throwable t) {
                //Nos indica Errores pero de Conexión
                Toast.makeText(MyApp.getContext(),"Error en la Conexión. Inténtelo de Nuevo",Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.ame.minitwitter.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.ame.minitwitter.retrofit.response.Tweet;
import com.ame.minitwitter.ui.tweets.BottomModalTweetFragment;

import java.util.List;

public class TweetViewModel extends AndroidViewModel {

    private TweetRepository tweetRepository; //webService variable
    private LiveData<List<Tweet>> tweets;
    private LiveData<List<Tweet>> favTweets;

    public TweetViewModel(@NonNull Application application) {
        super(application);

        tweetRepository = new TweetRepository(); //Instanciamos nuestro repository
        tweets = tweetRepository.getAllTweets();   //para poder inicializar esata variable...
    }
    //inicializamos nuestra lista de Tweets
    public  LiveData<List<Tweet>> getTweets(){
        return tweets;
    }

    //Metodo para mostrar el cuadro de dialogo
    public void openDialogTweetMenu(Context ctx, int idTweet){
        BottomModalTweetFragment dialogTweet = BottomModalTweetFragment.newInstance(idTweet);
                   //casteamos
        dialogTweet.show(((AppCompatActivity)ctx).getSupportFragmentManager(),"BottomModalTweetFragment"); //Tag = etiqueta del dialogo
    }

    //invocamos el metodo getFavTweets a nuestro repository
    public  LiveData<List<Tweet>> getFavTweets(){
        favTweets = tweetRepository.getFavsTweets();
        return favTweets;
    }

    //para actualizar con el esniper
    public  LiveData<List<Tweet>> getNewTweets(){
        tweets = tweetRepository.getAllTweets(); //la llamanda de tweets de nuestro ¡¡Servidor!!
        return tweets;
    }

    //refrescando la nueva lista de tweets desde el servidor
    public  LiveData<List<Tweet>> getNewFavTweets(){
       getNewTweets();
       return getFavTweets();
    }

    public void deleteTweet(int idTweet){
        //llamanos al repository
        tweetRepository.deleteTweet(idTweet); //actualizamos la lista de tweets de nuestro adapter
    }

    //metodo que nos permite invocar al repository
    public void insertTweet(String mensaje){
        tweetRepository.createTweet(mensaje);
    }

    //metodo que invoque al metodo de like en el repositorio
    public void likeTweet(int idTweet){
        tweetRepository.likeTweet(idTweet);
    }
}

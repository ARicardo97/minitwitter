package com.ame.minitwitter.ui.tweets;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ame.minitwitter.R;
import com.ame.minitwitter.common.Constantes;
import com.ame.minitwitter.data.TweetViewModel;
import com.ame.minitwitter.retrofit.response.Tweet;

import java.util.List;


public class TweetListFragment extends Fragment {

    private int tweetListType = 1;
    RecyclerView recyclerView;
    MyTweetRecyclerViewAdapter adapter;
    List<Tweet> tweeList;
    SwipeRefreshLayout swipeRefreshLayout;
    TweetViewModel tweetViewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TweetListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TweetListFragment newInstance(int tweetListType) {
        TweetListFragment fragment = new TweetListFragment();
        Bundle args = new Bundle();
        args.putInt(Constantes.TWEET_LIST_TYPE, tweetListType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tweetViewModel = ViewModelProviders.of(getActivity())
                .get(TweetViewModel.class);

        if (getArguments() != null) {
            tweetListType = getArguments().getInt(Constantes.TWEET_LIST_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tweet_list, container, false);

            Context context = view.getContext();
             recyclerView = view.findViewById(R.id.list);
             swipeRefreshLayout = view.findViewById(R.id.swiperefreshlayout);
             swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorGreen));

             swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                 @Override
                 public void onRefresh() {
                     //metodo que se va a lanzar cuando se hace el gesto
                     swipeRefreshLayout.setRefreshing(true);//se activa cuando se quiere actualizar la lista de tweets

                     if (tweetListType == Constantes.TWEET_LIST_ALL){
                         loadNewData(); //lista de tweets todas
                     }else if (tweetListType == Constantes.TWEET_LIST_FAVS){
                         loadNewFavData();
                     }

                 }
             });
                recyclerView.setLayoutManager(new LinearLayoutManager(context));

            adapter = new MyTweetRecyclerViewAdapter(
                    getActivity(),
                    tweeList
            );
            recyclerView.setAdapter(adapter);

           if (tweetListType == Constantes.TWEET_LIST_ALL){
               loadTweetData(); //Carga de Datos cuando se ejecuta por primera vez
           }else if (tweetListType == Constantes.TWEET_LIST_FAVS){
               loadFavTweetData();
           }

        return view;
    }

    private void loadNewFavData() {
        //carga de nuevos tweets desde el servidor pero favoritos
       tweetViewModel.getNewFavTweets().observe(getActivity(), new Observer<List<Tweet>>() {
           @Override
           public void onChanged(@Nullable List<Tweet> tweets) {
               tweeList = tweets; //guardamos la lista de tweets
               //paramos el swiper porque ya se refresco
               swipeRefreshLayout.setRefreshing(false);
               adapter.setData(tweeList); //actualizamos la lista de datos que hemos recibido y q hemos guardado en nuestro tweet
               //eliminamos el observador sobre la lista de tweets
               tweetViewModel.getNewFavTweets().removeObserver(this);
           }
       });
    }

    private void loadFavTweetData() {
        //cargando la lista de tweet favoritos
        tweetViewModel.getFavTweets().observe(getActivity(), new Observer<List<Tweet>>() {
            @Override
            public void onChanged(@Nullable List<Tweet> tweets) {
                tweeList = tweets; //guardamos
                adapter.setData(tweeList);
            }
        });
    }

    //carga la lista de tweets
    private void loadTweetData() {
        tweetViewModel.getTweets().observe(getActivity(), new Observer<List<Tweet>>() {
            @Override
            public void onChanged(@Nullable List<Tweet> tweets) {
                tweeList = tweets;
                adapter.setData(tweeList); //invocamos el metodo setData para refrescar lista
            }
        });
    }

    //ejecuta el metodo que carga la lista de datos desde el ¡¡Servidor!! [SE EJECUTA DESPUES DE LA PRIMERA VEZ]
    private void loadNewData() {
        tweetViewModel.getNewTweets().observe(getActivity(), new Observer<List<Tweet>>() {
            @Override
            public void onChanged(@Nullable List<Tweet> tweets) {
                tweeList = tweets;
                swipeRefreshLayout.setRefreshing(false); //se desactiva cuando ya se obtuvo la lista de tweets actualizada
                adapter.setData(tweeList); //invocamos el metodo setData para refrescar lista
                //Desactivar el observer para que no se vuelva lanzar cuando se crea un nuevo tweet
                tweetViewModel.getNewTweets().removeObserver(this );
            }
        });
    }
}

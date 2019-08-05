package com.ame.minitwitter.ui.tweets;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ame.minitwitter.R;
import com.ame.minitwitter.common.Constantes;
import com.ame.minitwitter.common.SharedPreferencesManager;
import com.ame.minitwitter.data.TweetViewModel;
import com.ame.minitwitter.retrofit.response.Like;
import com.ame.minitwitter.retrofit.response.Tweet;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class MyTweetRecyclerViewAdapter extends RecyclerView.Adapter<MyTweetRecyclerViewAdapter.ViewHolder> {

    private Context ctx;
    private List<Tweet> mValues;
    String username;
    TweetViewModel tweetViewModel;

    //hacemos una copia para poder tener disponible en otra clase
    public MyTweetRecyclerViewAdapter(Context contexto, List<Tweet> items) {
        mValues = items;
        ctx = contexto;
        username = SharedPreferencesManager.getSomeStringValue(Constantes.PREF_USERNAME);
        tweetViewModel = ViewModelProviders.of((FragmentActivity) ctx).get(TweetViewModel.class); //instanciamos el viewModel
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_tweet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mValues != null) {
            holder.mItem = mValues.get(position);

            holder.tvUsername.setText("@" + holder.mItem.getUser().getUsername());
            holder.tvMessage.setText(holder.mItem.getMensaje());
            //para saber num de likes se usa size() xq es un array
            holder.tvLikesCount.setText(String.valueOf(holder.mItem.getLikes().size()));

            //cargando la imagen del usuario como avatar
            String photo = holder.mItem.getUser().getPhotoUrl();
            if (!photo.equals("")) {
                Glide.with(ctx)
                        .load("https://www.minitwitter.com/apiv1/uploads/photos/" + photo)
                        .diskCacheStrategy(DiskCacheStrategy.NONE) //no se almacene cache
                        .centerCrop()
                        .skipMemoryCache(true)
                        .into(holder.ivAvatar);
            }
            //Vamos a setear
            Glide.with(ctx)
                    .load(R.drawable.ic_likes)
                    .into(holder.ivLike);
            holder.tvLikesCount.setTextColor(ctx.getResources().getColor(android.R.color.black));//pintando de color rosa el like
            //letras negritas
            holder.tvLikesCount.setTypeface(null, Typeface.NORMAL);

            holder.ivShowMenu.setVisibility(View.GONE); //GONE = que desaparesca completamnete del layout por defecto
            if (holder.mItem.getUser().getUsername().equals(username)){    //equals significa es igual al..username
                holder.ivShowMenu.setVisibility(View.VISIBLE);
            }

            //Evento click para mostrar el menu eliminar
            holder.ivShowMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //llamar al tweetViewModel
                    tweetViewModel.openDialogTweetMenu(ctx,holder.mItem.getId());
                }
            });

            holder.ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //invocamos el metodo likeTweet
                    tweetViewModel.likeTweet(holder.mItem.getId());
                }
            });


            //vamos a navegar sobre el array del Like
            for (Like like : holder.mItem.getLikes()) { //obtenemos el like
                if (like.getUsername().equals(username)) {
                    Glide.with(ctx)
                            .load(R.drawable.ic_like_pink)
                            .into(holder.ivLike);
                    holder.tvLikesCount.setTextColor(ctx.getResources().getColor(R.color.pink));//pintando de color rosa el like
                    //letras negritas
                    holder.tvLikesCount.setTypeface(null, Typeface.BOLD);
                    break; //rompemos el bucle
                }
            }
        }
    }

    public void setData(List<Tweet> tweetList){
        this.mValues = tweetList;
        notifyDataSetChanged(); //Refrescando el adapter
    }

    @Override
    public int getItemCount() {
        if (mValues != null) //si mValues es distinto de null...
        return mValues.size();
        else return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView ivAvatar;
        public final ImageView ivLike;
        public final ImageView ivShowMenu;
        public final TextView tvUsername;
        public final TextView tvMessage;
        public final TextView tvLikesCount;
        public Tweet mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ivAvatar = view.findViewById(R.id.imageViewAvatar);
            ivLike = view.findViewById(R.id.imageViewLike);
            ivShowMenu = view.findViewById(R.id.imageViewShowMenu);
            tvUsername = view.findViewById(R.id.textViewUsername);
            tvMessage = view.findViewById(R.id.textViewMessage);
            tvLikesCount = view.findViewById(R.id.textViewLikes);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + tvUsername.getText() + "'";
        }
    }
}

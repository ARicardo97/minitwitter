package com.ame.minitwitter.ui.tweets;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ame.minitwitter.R;
import com.ame.minitwitter.common.Constantes;
import com.ame.minitwitter.common.SharedPreferencesManager;
import com.ame.minitwitter.data.TweetViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class NuevoTweetDialogFragment extends DialogFragment implements View.OnClickListener {
    ImageView ivClose, ivAvatar;
    Button btnTwittear;
    EditText etMensaje;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.nuevo_tweet_full_dialog, container, false);
        ivClose = view.findViewById(R.id.imageViewClose);
        ivAvatar = view.findViewById(R.id.imageViewAvatar);
        etMensaje = view.findViewById(R.id.editTextMensaje);
        btnTwittear = view.findViewById(R.id.buttonTwittear);

        //Eventos
        btnTwittear.setOnClickListener(this);
        ivClose.setOnClickListener(this);

        //seteamos la imagen del Usuario
        String photoUrl = SharedPreferencesManager.getSomeStringValue(Constantes.PREF_PHOTOURL);

        //si la foto que se sube es vacia...comprobación
        if (!photoUrl.isEmpty()) {
            Glide.with(getActivity())
                    .load(Constantes.API_MINITWITTER_FILES_URL + photoUrl)  //Cargamos la foto de perfil
                    .diskCacheStrategy(DiskCacheStrategy.NONE) //no se almacene cache
                    .centerCrop()
                    .skipMemoryCache(true)
                    .into(ivAvatar);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        String mensaje = etMensaje.getText().toString();

        if (id == R.id.buttonTwittear){
            //Comprobación de la existencia de un mensaje
            if (mensaje.isEmpty()){
                Toast.makeText(getActivity(),"Debe Escribir un Mensaje",Toast.LENGTH_SHORT).show();
            }else {
                TweetViewModel tweetViewModel = ViewModelProviders
                        .of(getActivity()).get(TweetViewModel.class);
                tweetViewModel.insertTweet(mensaje); //Insertando el Tweet y lo Guardamos
               getDialog().dismiss();
            }
        }else  if (id == R.id.imageViewClose){
               //En Caso de que el mensaje no sea vacio lo mostramos
            if (!mensaje.isEmpty()){
                showDialogConfirm(); //metodo para dialogo de confirmacion
            }else {
                getDialog().dismiss();
            }
        }
    }

    private void showDialogConfirm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("¿Desea Realmente Eliminar el Tweet? El Mensaje se Borrará")
                .setTitle("Cancelar Tweet");

        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getDialog().dismiss(); //para cerrar
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

         AlertDialog dialog = builder.create();
         dialog.show(); //mostramos el Dialogo
    }
}

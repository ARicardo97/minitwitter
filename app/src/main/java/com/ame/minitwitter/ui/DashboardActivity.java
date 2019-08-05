package com.ame.minitwitter.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ame.minitwitter.R;
import com.ame.minitwitter.common.Constantes;
import com.ame.minitwitter.common.SharedPreferencesManager;
import com.ame.minitwitter.data.ProfileViewModel;
import com.ame.minitwitter.retrofit.response.ResponseUserProfile;
import com.ame.minitwitter.ui.profile.ProfileFragment;
import com.ame.minitwitter.ui.tweets.NuevoTweetDialogFragment;
import com.ame.minitwitter.ui.tweets.TweetListFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class DashboardActivity extends AppCompatActivity implements PermissionListener {
    FloatingActionButton fab;
    ImageView ivAvatar;
    ProfileViewModel profileViewModel;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment f = null;

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    f = TweetListFragment.newInstance(Constantes.TWEET_LIST_ALL); //pasamos toda la lista de Tweets
                    fab.show();
                    break;
                case R.id.navigation_tweets_like:
                    f = TweetListFragment.newInstance(Constantes.TWEET_LIST_FAVS);
                    fab.hide();
                    break;
                case R.id.navigation_profile:
                    f = new ProfileFragment();
                    fab.hide();
                    break;
            }
            // si f es distinto de null...kiere decir que se ha cargado algun fragmento
            if (f != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, f)
                        .commit();
                return true;
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);

        fab = findViewById(R.id.fab);
        ivAvatar = findViewById(R.id.imageViewToolbarPhoto);

        getSupportActionBar().hide();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainer, TweetListFragment.newInstance(Constantes.TWEET_LIST_ALL))
                .commit();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NuevoTweetDialogFragment dialog = new NuevoTweetDialogFragment();
                dialog.show(getSupportFragmentManager(), " NuevoTweetDialogFragment"); //Lanzamiento de cuadro de dialogo
            }
        });

        //seteamos la imagen del Usuario
        String photoUrl = SharedPreferencesManager.getSomeStringValue(Constantes.PREF_PHOTOURL);

        //si la foto que se sube es vacia...comprobación
        if (!photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(Constantes.API_MINITWITTER_FILES_URL + photoUrl)  //Cargamos la foto de perfil
                    .diskCacheStrategy(DiskCacheStrategy.NONE) //no se almacene cache
                    .centerCrop()
                    .skipMemoryCache(true)
                    .into(ivAvatar);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_CANCELED){
            if (requestCode == Constantes.SELECT_PHOTO_GALLERY){
                //kiere decir que nos esta llegando la respuesta
                if (data != null){
                    //kiere decir que nos esta llegando una imagen
                    //seleccion de foto basados en Url
                    Uri imagenSeleccionada = data.getData(); //nos llega la informacion de la imagen que hemos seleccionado
                    String[] filePathColumn = {MediaStore.Images.Media.DATA}; //separamos todas esas partes que definen ese FilePathColumn
                    Cursor cursor = getContentResolver().query(imagenSeleccionada,
                            filePathColumn, null, null, null);
                    if (cursor != null){
                        cursor.moveToFirst(); //movemos el cursor a la primera posicion de esa consulta
                        //"filename" = filePathColumn
                        int imagenIndex = cursor.getColumnIndex(filePathColumn[0]); //nos devuelve el nombre enque num de columna se encuentra
                        String fotoPath = cursor.getString(imagenIndex);
                        profileViewModel.uploadPhoto(fotoPath); //se conecta para subir la foto
                        cursor.close();
                    }
                }
            }
        }
    }

    @Override
    public void onPermissionGranted(PermissionGrantedResponse response) {
        //INVOCAMOS La seleccion de fotos de la galeria
        Intent seleccionarFoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //Peticion abrir galeria
        startActivityForResult(seleccionarFoto, Constantes.SELECT_PHOTO_GALLERY);
    }

    @Override
    public void onPermissionDenied(PermissionDeniedResponse response) {
      //en caso de que no nos hayan dado permiso
        Toast.makeText(this,"No se puede Seleccionar la Foto",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

    }
}

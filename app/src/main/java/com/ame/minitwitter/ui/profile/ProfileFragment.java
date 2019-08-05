package com.ame.minitwitter.ui.profile;

import android.Manifest;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ame.minitwitter.common.Constantes;
import com.ame.minitwitter.data.ProfileViewModel;
import com.ame.minitwitter.R;
import com.ame.minitwitter.retrofit.request.RequestUserProfile;
import com.ame.minitwitter.retrofit.response.ResponseUserProfile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    ImageView ivAvatar;
    EditText etUsername, etEmail, etPassword, etWebsite, etDescripcion;
    Button btnSave, btnChangePassword;
    boolean loadingData = true;
    PermissionListener allPermissionsListener;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Importante en un view model usar el getActivity()...en lugar de this a la hora de instanciar
        profileViewModel = ViewModelProviders.of(getActivity()).get(ProfileViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_fragment, container, false);

        ivAvatar = v.findViewById(R.id.imageViewAvatar);
        etUsername = v.findViewById(R.id.editTextUsername);
        etPassword = v.findViewById(R.id.editTextCurrentPassword);
        etEmail = v.findViewById(R.id.editTextEmail);
        etWebsite = v.findViewById(R.id.editTextWebsite);
        etDescripcion = v.findViewById(R.id.editTextDescripcion);

        btnSave = v.findViewById(R.id.buttonSave);
        btnChangePassword = v.findViewById(R.id.buttonChangePassword);

        //Eventos                  (View ->{}   =  aplicando metodo lamba
        btnSave.setOnClickListener(view -> {
                String username = etUsername.getText().toString(); //asignamos valor
                String email = etEmail.getText().toString();
                String descripcion = etDescripcion.getText().toString();
                String website = etWebsite.getText().toString();
                String password = etPassword.getText().toString();
                if (username.isEmpty()){
                    etUsername.setError("Nombre de Usuario es Requerido"); //Validando los campos
                }else if (email.isEmpty()){
                    etEmail.setError("Email es Requerido");
                }else if (password.isEmpty()){
                    etPassword.setError("Contraseña es Requerida");
                }else {
                    RequestUserProfile requestUserProfile = new RequestUserProfile(username, email, descripcion, website, password);
                    profileViewModel.updateProfile(requestUserProfile);
                    Toast.makeText(getActivity(),"Actualizando Datos...Espere", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(false);
                }
        });

        btnChangePassword.setOnClickListener(view -> {
            Toast.makeText(getActivity(),"click on save", Toast.LENGTH_SHORT).show();
        });

        ivAvatar.setOnClickListener(view -> {
            //invocamos la seleccion de foto
            //invocamos al metodo de comprobacion de permisos
            //permisos para acceder a la galeria
            checkPermissions();
        });

        //viewModel
        profileViewModel.userProfile.observe(getActivity(), new Observer<ResponseUserProfile>() {
            @Override
            public void onChanged(@Nullable ResponseUserProfile responseUserProfile) {
                loadingData = false;
                etUsername.setText(responseUserProfile.getUsername());
                etEmail.setText(responseUserProfile.getEmail());
                etDescripcion.setText(responseUserProfile.getDescripcion());
                etWebsite.setText(responseUserProfile.getWebsite());
                //si la imagen que llega..
                if (!responseUserProfile.getPhotoUrl().isEmpty()){
                    //seteamos
                    Glide.with(getActivity())
                            .load(Constantes.API_MINITWITTER_FILES_URL + responseUserProfile.getPhotoUrl())
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.NONE) //no se almacene cache
                            .centerCrop()
                            .skipMemoryCache(true)
                            .into(ivAvatar);
                }
                if (!loadingData) {
                    btnSave.setEnabled(true);
                    Toast.makeText(getActivity(),"Datos Guardados Correctamente",Toast.LENGTH_SHORT).show();
                }
            }
        });

        profileViewModel.photoProfile.observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String photo) {
                if (!photo.isEmpty()){
                    Glide.with(getActivity())
                            .load(Constantes.API_MINITWITTER_FILES_URL + photo)
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .centerCrop()
                            .skipMemoryCache(true)
                            .into(ivAvatar);
                }
            }
        });

        return v;
    }

    private void checkPermissions() {
        PermissionListener dialogOnDeniedPermissionListener =
                DialogOnDeniedPermissionListener.Builder.withContext(getActivity())
                .withTitle("Permiso")
                .withMessage("Permisos son Necesarios para poder Seleccionar Foto")
                .withButtonText("Aceptar")
                .withIcon(R.mipmap.ic_launcher)
                .build(); //construyendo el dialogo se usa .build();

        allPermissionsListener = new CompositePermissionListener(
               (PermissionListener)  getActivity(),
                dialogOnDeniedPermissionListener
        );
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(allPermissionsListener) //cheque los permisos
                .check();
    }
}

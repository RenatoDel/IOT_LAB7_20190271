package com.example.iot_lab07_20190271.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.iot_lab07_20190271.R;
import com.example.iot_lab07_20190271.activity.LoginActivity;
import com.example.iot_lab07_20190271.services.FirebaseManager;
import com.example.iot_lab07_20190271.services.SocialAuthService;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private ImageView ivProviderIcon;
    private TextView tvUserEmail, tvUserName, tvUserProvider;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        loadUserData();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        ivProviderIcon = view.findViewById(R.id.iv_provider_icon);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserProvider = view.findViewById(R.id.tv_user_provider);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseManager.getCurrentUser();
        if (currentUser != null) {
            // Email
            tvUserEmail.setText(currentUser.getEmail());

            // Nombre
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvUserName.setText(displayName);
            } else {
                // Si no tiene displayName, extraer nombre del email
                String email = currentUser.getEmail();
                if (email != null) {
                    String userName = email.substring(0, email.indexOf("@"));
                    tvUserName.setText(userName);
                } else {
                    tvUserName.setText("Usuario");
                }
            }

            // Determinar proveedor y configurar ícono
            String provider = determineProvider(currentUser);
            tvUserProvider.setText("Método de acceso: " + getProviderDisplayName(provider));
            setProviderIcon(provider);
        }
    }

    private String determineProvider(FirebaseUser user) {
        if (user.getProviderData().size() > 1) {
            String providerId = user.getProviderData().get(1).getProviderId();
            if (providerId.contains("google")) {
                return "google";
            } else if (providerId.contains("facebook")) {
                return "facebook";
            }
        }
        return "email";
    }

    private String getProviderDisplayName(String provider) {
        switch (provider) {
            case "google":
                return "Google";
            case "facebook":
                return "Facebook";
            case "email":
            default:
                return "Email";
        }
    }

    private void setProviderIcon(String provider) {
        switch (provider) {
            case "google":
                ivProviderIcon.setImageResource(R.drawable.ic_google);
                break;
            case "facebook":
                ivProviderIcon.setImageResource(R.drawable.ic_facebook);
                break;
            case "email":
            default:
                ivProviderIcon.setImageResource(R.drawable.ic_person);
                break;
        }
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        // Cerrar sesión de Firebase
        FirebaseManager.signOut();

        // Cerrar sesión de servicios sociales
        SocialAuthService.signOutGoogle();
        SocialAuthService.signOutFacebook();

        // Ir a login
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
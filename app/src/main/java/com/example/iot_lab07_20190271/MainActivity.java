package com.example.iot_lab07_20190271;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.iot_lab07_20190271.activity.LoginActivity;
import com.example.iot_lab07_20190271.fragments.Lab7Fragment;
import com.example.iot_lab07_20190271.fragments.Linea1Fragment;
import com.example.iot_lab07_20190271.fragments.LimaPassFragment;
import com.example.iot_lab07_20190271.fragments.ProfileFragment;
import com.example.iot_lab07_20190271.fragments.SummaryFragment;
import com.example.iot_lab07_20190271.services.FirebaseManager;
import com.example.iot_lab07_20190271.services.SocialAuthService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firebase
        FirebaseManager.initialize();

        // Verificar si usuario está logueado
        if (!FirebaseManager.isUserLoggedIn()) {
            goToLogin();
            return;
        }

        // Inicializar servicios sociales para logout
        SocialAuthService.initializeGoogle(this);
        SocialAuthService.initializeFacebook();

        setupToolbar();
        setupBottomNavigation();

        // Cargar fragment inicial
        if (savedInstanceState == null) {
            loadFragment(new Linea1Fragment());
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_linea1) {
                selectedFragment = new Linea1Fragment();
            } else if (itemId == R.id.nav_limapass) {
                selectedFragment = new LimaPassFragment();
            } else if (itemId == R.id.nav_summary) {
                selectedFragment = new SummaryFragment();
            } else if (itemId == R.id.nav_lab7) {
                selectedFragment = new Lab7Fragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }


            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Cerrar sesión de Firebase
        FirebaseManager.signOut();

        // Cerrar sesión de servicios sociales
        SocialAuthService.signOutGoogle();
        SocialAuthService.signOutFacebook();

        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
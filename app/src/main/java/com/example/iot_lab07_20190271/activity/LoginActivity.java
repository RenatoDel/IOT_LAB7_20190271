package com.example.iot_lab07_20190271.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.iot_lab07_20190271.MainActivity;
import com.example.iot_lab07_20190271.R;
import com.example.iot_lab07_20190271.services.FirebaseManager;
import com.example.iot_lab07_20190271.services.SocialAuthService;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin, btnRegister, btnGoogleSignin, btnFacebookSignin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        setContentView(R.layout.activity_login);

        // Inicializar Firebase
        FirebaseManager.initialize();

        // Verificar si usuario ya está logueado
        if (FirebaseManager.isUserLoggedIn()) {
            goToMainActivity();
            return;
        }

        initViews();
        setupSocialAuth();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnGoogleSignin = findViewById(R.id.btn_google_signin);
        btnFacebookSignin = findViewById(R.id.btn_facebook_signin);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupSocialAuth() {
        // Inicializar servicios de autenticación social
        SocialAuthService.initializeGoogle(this);
        SocialAuthService.initializeFacebook();
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> registerUser());
        btnGoogleSignin.setOnClickListener(v -> signInWithGoogle());
        btnFacebookSignin.setOnClickListener(v -> signInWithFacebook());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (validateInputs(email, password)) {
            showProgress(true);

            FirebaseManager.getAuth()
                    .signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        showProgress(false);

                        if (task.isSuccessful()) {
                            Log.d(TAG, "Login exitoso");
                            Toast.makeText(this, "¡Bienvenido de vuelta!", Toast.LENGTH_SHORT).show();
                            goToMainActivity();
                        } else {
                            Log.w(TAG, "Error en login", task.getException());
                            String errorMessage = getErrorMessage(task.getException());
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (validateInputs(email, password)) {
            showProgress(true);

            FirebaseManager.getAuth()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        showProgress(false);

                        if (task.isSuccessful()) {
                            Log.d(TAG, "Registro exitoso");
                            Toast.makeText(this, "¡Cuenta creada exitosamente!", Toast.LENGTH_SHORT).show();

                            // Guardar usuario en Firestore
                            FirebaseManager.saveUserToFirestore("email");

                            goToMainActivity();
                        } else {
                            Log.w(TAG, "Error en registro", task.getException());
                            String errorMessage = getErrorMessage(task.getException());
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void signInWithGoogle() {
        showProgress(true);
        Intent signInIntent = SocialAuthService.getGoogleSignInIntent();
        startActivityForResult(signInIntent, SocialAuthService.RC_SIGN_IN);
    }

    private void signInWithFacebook() {
        showProgress(true);
        SocialAuthService.loginWithFacebook(this, new SocialAuthService.SocialAuthCallback() {
            @Override
            public void onSuccess(String provider) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this, "¡Login con Facebook exitoso!", Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this, "Error en Facebook: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Manejar resultado de Google Sign-In
        if (requestCode == SocialAuthService.RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            SocialAuthService.handleGoogleSignInResult(task, new SocialAuthService.SocialAuthCallback() {
                @Override
                public void onSuccess(String provider) {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this, "¡Login con Google exitoso!", Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                }

                @Override
                public void onError(Exception e) {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this, "Error en Google: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Manejar resultado de Facebook
            SocialAuthService.handleFacebookResult(requestCode, resultCode, data);
        }
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Ingresa tu correo electrónico");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingresa un correo válido");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Ingresa tu contraseña");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnRegister.setEnabled(!show);
        btnGoogleSignin.setEnabled(!show);
        btnFacebookSignin.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
    }

    private String getErrorMessage(Exception exception) {
        if (exception == null) return "Error desconocido";

        String errorCode = exception.getMessage();
        if (errorCode != null) {
            if (errorCode.contains("email-already-in-use")) {
                return "Este correo ya está registrado. Intenta iniciar sesión.";
            } else if (errorCode.contains("weak-password")) {
                return "La contraseña es muy débil. Usa al menos 6 caracteres.";
            } else if (errorCode.contains("invalid-email")) {
                return "Formato de correo inválido.";
            } else if (errorCode.contains("user-not-found")) {
                return "No existe una cuenta con este correo.";
            } else if (errorCode.contains("wrong-password")) {
                return "Contraseña incorrecta.";
            } else if (errorCode.contains("invalid-credential")) {
                return "Credenciales inválidas. Verifica tu correo y contraseña.";
            }
        }

        return "Error: " + errorCode;
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
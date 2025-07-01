package com.example.iot_lab07_20190271.services;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.iot_lab07_20190271.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class SocialAuthService {
    private static final String TAG = "SocialAuthService";
    public static final int RC_SIGN_IN = 9001;

    private static GoogleSignInClient googleSignInClient;
    private static CallbackManager callbackManager;

    // Interface para callbacks
    public interface SocialAuthCallback {
        void onSuccess(String provider);
        void onError(Exception e);
    }

    // Inicializar Google Sign-In
    public static void initializeGoogle(Activity activity) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    // Inicializar Facebook Login
    public static void initializeFacebook() {
        callbackManager = CallbackManager.Factory.create();
    }

    // Google Sign-In
    public static Intent getGoogleSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    // Manejar resultado de Google Sign-In
    public static void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask, SocialAuthCallback callback) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account, callback);
            }
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            callback.onError(new Exception("Google Sign-In failed: " + e.getMessage()));
        }
    }

    // Autenticar con Firebase usando Google
    private static void firebaseAuthWithGoogle(GoogleSignInAccount account, SocialAuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        FirebaseManager.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Google sign in successful");
                        FirebaseManager.saveUserToFirestore("google");
                        callback.onSuccess("google");
                    } else {
                        Log.w(TAG, "Google sign in failed", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    // Facebook Login
    public static void loginWithFacebook(Activity activity, SocialAuthCallback callback) {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Facebook login success");
                firebaseAuthWithFacebook(loginResult.getAccessToken(), callback);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Facebook login cancelled");
                callback.onError(new Exception("Facebook login cancelled"));
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "Facebook login error", error);
                callback.onError(error);
            }
        });

        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("email", "public_profile"));
    }

    // Autenticar con Firebase usando Facebook
    private static void firebaseAuthWithFacebook(AccessToken token, SocialAuthCallback callback) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        FirebaseManager.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Facebook sign in successful");
                        FirebaseManager.saveUserToFirestore("facebook");
                        callback.onSuccess("facebook");
                    } else {
                        Log.w(TAG, "Facebook sign in failed", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    // Manejar resultado de Facebook
    public static void handleFacebookResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Cerrar sesión de Google
    public static void signOutGoogle() {
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        }
    }

    // Cerrar sesión de Facebook
    public static void signOutFacebook() {
        LoginManager.getInstance().logOut();
    }
}
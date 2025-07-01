package com.example.iot_lab07_20190271.services;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.iot_lab07_20190271.models.User;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseAuth mAuth;
    private static FirebaseFirestore db;

    // Inicializar Firebase
    public static void initialize() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "Firebase inicializado correctamente");
    }

    // Getters
    public static FirebaseAuth getAuth() {
        return mAuth;
    }

    public static FirebaseFirestore getFirestore() {
        return db;
    }

    public static boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    // Obtener usuario actual
    public static FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // Guardar usuario en Firestore
    public static void saveUserToFirestore(String provider) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser != null) {
            Map<String, Object> user = new HashMap<>();
            user.put("email", currentUser.getEmail());
            user.put("displayName", currentUser.getDisplayName());
            user.put("provider", provider);
            user.put("createdAt", new Date());

            db.collection("users")
                    .document(currentUser.getUid())
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Usuario guardado en Firestore");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error guardando usuario", e);
                    });
        }
    }

    // Cerrar sesión
    public static void signOut() {
        mAuth.signOut();
        Log.d(TAG, "Usuario deslogueado");
    }
}
package com.example.iot_lab07_20190271.services;


import android.net.Uri;
import android.util.Log;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;

public class CloudStorage {
    private static final String TAG = "CloudStorage";
    private static FirebaseStorage storage;
    private static StorageReference storageRef;

    // Interface para callbacks
    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onError(Exception e);
        void onProgress(double progress);
    }

    public interface DownloadCallback {
        void onSuccess(Uri localFileUri);
        void onError(Exception e);
        void onProgress(double progress);
    }

    // Método de conexión al servicio de almacenamiento
    public static void initialize() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        Log.d(TAG, "Firebase Storage inicializado correctamente");
    }

    // Método para guardar archivo
    public static void uploadFile(Uri fileUri, String fileName, UploadCallback callback) {
        if (storage == null) {
            initialize();
        }

        StorageReference fileRef = storageRef.child("images/" + fileName);

        UploadTask uploadTask = fileRef.putFile(fileUri);

        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            callback.onProgress(progress);
            Log.d(TAG, "Upload is " + progress + "% done");
        }).addOnSuccessListener(taskSnapshot -> {
            // Obtener URL de descarga
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                Log.d(TAG, "Upload successful. Download URL: " + downloadUrl);
                callback.onSuccess(downloadUrl);
            }).addOnFailureListener(callback::onError);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Upload failed", e);
            callback.onError(e);
        });
    }

    // Método para obtener archivo
    public static void downloadFile(String fileName, File localFile, DownloadCallback callback) {
        if (storage == null) {
            initialize();
        }

        StorageReference fileRef = storageRef.child("images/" + fileName);

        fileRef.getFile(localFile).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            callback.onProgress(progress);
            Log.d(TAG, "Download is " + progress + "% done");
        }).addOnSuccessListener(taskSnapshot -> {
            Log.d(TAG, "Download successful");
            callback.onSuccess(Uri.fromFile(localFile));
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Download failed", e);
            callback.onError(e);
        });
    }

    // Método para obtener URL de descarga directa
    public static void getDownloadUrl(String fileName, DownloadUrlCallback callback) {
        if (storage == null) {
            initialize();
        }

        StorageReference fileRef = storageRef.child("images/" + fileName);
        fileRef.getDownloadUrl()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onError);
    }

    public interface DownloadUrlCallback {
        void onSuccess(Uri downloadUri);
        void onError(Exception e);
    }
}
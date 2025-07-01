package com.example.iot_lab07_20190271.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.iot_lab07_20190271.R;
import com.example.iot_lab07_20190271.services.CloudStorage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Lab7Fragment extends Fragment {
    private static final String TAG = "Lab7Fragment";
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_IMAGE_PICK = 1002;
    private static final int REQUEST_PERMISSIONS = 1003;

    // SharedPreferences keys
    private static final String PREFS_NAME = "Lab7Prefs";
    private static final String KEY_LAST_IMAGE_URL = "lastImageUrl";
    private static final String KEY_LAST_LOCAL_PATH = "lastLocalPath";
    private static final String KEY_LAST_FILENAME = "lastFilename";

    private ImageView ivSelectedImage;
    private Button btnSelectImage, btnUploadImage, btnDownloadImage;
    private TextView tvUploadStatus, tvDownloadUrl;
    private ProgressBar progressBarUpload, progressBarDownload;

    private Uri selectedImageUri;
    private String lastUploadedFileName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lab7, container, false);

        initViews(view);
        setupListeners();
        CloudStorage.initialize();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cargar imagen guardada si existe
        loadSavedImage();
    }

    private void initViews(View view) {
        ivSelectedImage = view.findViewById(R.id.iv_selected_image);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        btnUploadImage = view.findViewById(R.id.btn_upload_image);
        btnDownloadImage = view.findViewById(R.id.btn_download_image);
        tvUploadStatus = view.findViewById(R.id.tv_upload_status);
        tvDownloadUrl = view.findViewById(R.id.tv_download_url);
        progressBarUpload = view.findViewById(R.id.progress_bar_upload);
        progressBarDownload = view.findViewById(R.id.progress_bar_download);

        btnUploadImage.setEnabled(false);
        btnDownloadImage.setEnabled(false);
    }

    private void loadSavedImage() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedImagePath = prefs.getString(KEY_LAST_LOCAL_PATH, null);
        String savedUrl = prefs.getString(KEY_LAST_IMAGE_URL, "");
        String savedFilename = prefs.getString(KEY_LAST_FILENAME, "");

        if (savedImagePath != null) {
            File imageFile = new File(savedImagePath);
            if (imageFile.exists()) {
                // Cargar imagen desde archivo local
                Glide.with(this)
                        .load(imageFile)
                        .into(ivSelectedImage);

                // Habilitar botón de descarga si hay una imagen
                btnDownloadImage.setEnabled(true);

                // Restaurar información de estado
                tvUploadStatus.setText("Imagen cargada desde almacenamiento local");

                if (!savedUrl.isEmpty()) {
                    tvDownloadUrl.setText("URL: " + savedUrl);
                }

                if (!savedFilename.isEmpty()) {
                    lastUploadedFileName = savedFilename;
                }

                Log.d(TAG, "Imagen restaurada desde: " + savedImagePath);
            } else {
                // El archivo no existe, limpiar preferencias
                clearSavedData();
            }
        }
    }

    private void clearSavedData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_LAST_IMAGE_URL)
                .remove(KEY_LAST_LOCAL_PATH)
                .remove(KEY_LAST_FILENAME)
                .apply();
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> showImagePickerDialog());
        btnUploadImage.setOnClickListener(v -> uploadImage());
        btnDownloadImage.setOnClickListener(v -> downloadImage());
    }

    private void showImagePickerDialog() {
        String[] options = {"Tomar foto", "Seleccionar de galería"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Seleccionar imagen")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        if (checkPermissions()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            requestPermissions();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSIONS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    if (data != null && data.getExtras() != null) {
                        Toast.makeText(getContext(), "Foto capturada. Selecciona de galería para esta demo.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_IMAGE_PICK:
                    if (data != null) {
                        selectedImageUri = data.getData();
                        displaySelectedImage();
                    }
                    break;
            }
        }
    }

    private void displaySelectedImage() {
        if (selectedImageUri != null) {
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(ivSelectedImage);

            btnUploadImage.setEnabled(true);
            tvUploadStatus.setText("Imagen seleccionada. Lista para subir.");
        }
    }

    private void uploadImage() {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Selecciona una imagen primero", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generar nombre único para el archivo
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";
        lastUploadedFileName = fileName;

        progressBarUpload.setVisibility(View.VISIBLE);
        btnUploadImage.setEnabled(false);

        CloudStorage.uploadFile(selectedImageUri, fileName, new CloudStorage.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                progressBarUpload.setVisibility(View.GONE);
                btnUploadImage.setEnabled(true);
                btnDownloadImage.setEnabled(true);

                tvUploadStatus.setText("¡Imagen subida exitosamente!");
                tvDownloadUrl.setText("URL: " + downloadUrl);

                // GUARDAR: URL y nombre de archivo en SharedPreferences
                SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit()
                        .putString(KEY_LAST_IMAGE_URL, downloadUrl)
                        .putString(KEY_LAST_FILENAME, fileName)
                        .apply();

                Toast.makeText(getContext(), "Link: " + downloadUrl, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Upload successful: " + downloadUrl);
            }

            @Override
            public void onError(Exception e) {
                progressBarUpload.setVisibility(View.GONE);
                btnUploadImage.setEnabled(true);

                tvUploadStatus.setText("Error al subir imagen: " + e.getMessage());
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Upload failed", e);
            }

            @Override
            public void onProgress(double progress) {
                tvUploadStatus.setText("Subiendo... " + (int) progress + "%");
            }
        });
    }

    private void downloadImage() {
        if (lastUploadedFileName == null) {
            Toast.makeText(getContext(), "No hay imagen para descargar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear archivo local en el directorio de descargas
        File localFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), lastUploadedFileName);

        progressBarDownload.setVisibility(View.VISIBLE);
        btnDownloadImage.setEnabled(false);

        CloudStorage.downloadFile(lastUploadedFileName, localFile, new CloudStorage.DownloadCallback() {
            @Override
            public void onSuccess(Uri localFileUri) {
                progressBarDownload.setVisibility(View.GONE);
                btnDownloadImage.setEnabled(true);

                // Mostrar la imagen descargada
                Glide.with(Lab7Fragment.this)
                        .load(localFileUri)
                        .into(ivSelectedImage);

                // GUARDAR: Ruta local en SharedPreferences
                SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit()
                        .putString(KEY_LAST_LOCAL_PATH, localFile.getAbsolutePath())
                        .apply();

                Toast.makeText(getContext(), "Imagen descargada: " + localFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                Log.d(TAG, "Download successful: " + localFileUri);
            }

            @Override
            public void onError(Exception e) {
                progressBarDownload.setVisibility(View.GONE);
                btnDownloadImage.setEnabled(true);

                Toast.makeText(getContext(), "Error descargando: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Download failed", e);
            }

            @Override
            public void onProgress(double progress) {
                // Actualizar progreso si es necesario
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Permisos requeridos para usar la cámara", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
package com.example.iot_lab07_20190271.services;

import android.util.Log;

import com.example.iot_lab07_20190271.models.Movement;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovementService {
    private static final String TAG = "MovementService";
    private static final String COLLECTION_MOVEMENTS = "movements";

    // Interface para callbacks
    public interface MovementCallback {
        void onSuccess(List<Movement> movements);
        void onError(Exception e);
    }

    public interface SingleMovementCallback {
        void onSuccess(Movement movement);
        void onError(Exception e);
    }

    public interface OperationCallback {
        void onSuccess(String message);
        void onError(Exception e);
    }

    // Crear movimiento
    public static void createMovement(Movement movement, OperationCallback callback) {
        String userId = FirebaseManager.getCurrentUser().getUid();
        movement.setUserId(userId);
        movement.setCreatedAt(new Date());

        Map<String, Object> movementData = new HashMap<>();
        movementData.put("userId", movement.getUserId());
        movementData.put("cardType", movement.getCardType());
        movementData.put("cardId", movement.getCardId());
        movementData.put("date", movement.getDate());
        movementData.put("entryStation", movement.getEntryStation());
        movementData.put("exitStation", movement.getExitStation());
        movementData.put("travelTime", movement.getTravelTime());
        movementData.put("createdAt", movement.getCreatedAt());

        FirebaseManager.getFirestore()
                .collection(COLLECTION_MOVEMENTS)
                .add(movementData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Movimiento creado con ID: " + documentReference.getId());
                    callback.onSuccess("Movimiento registrado exitosamente");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creando movimiento", e);
                    callback.onError(e);
                });
    }

    // Obtener movimientos por tipo de tarjeta
    public static void getMovementsByCardType(String cardType, MovementCallback callback) {
        String userId = FirebaseManager.getCurrentUser().getUid();

        FirebaseManager.getFirestore()
                .collection(COLLECTION_MOVEMENTS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("cardType", cardType)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Movement> movements = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Movement movement = documentToMovement(document);
                        if (movement != null) {
                            movements.add(movement);
                        }
                    }
                    Log.d(TAG, "Obtenidos " + movements.size() + " movimientos de " + cardType);
                    callback.onSuccess(movements);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo movimientos", e);
                    callback.onError(e);
                });
    }

    // Obtener todos los movimientos del usuario
    public static void getAllMovements(MovementCallback callback) {
        String userId = FirebaseManager.getCurrentUser().getUid();

        FirebaseManager.getFirestore()
                .collection(COLLECTION_MOVEMENTS)
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Movement> movements = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Movement movement = documentToMovement(document);
                        if (movement != null) {
                            movements.add(movement);
                        }
                    }
                    Log.d(TAG, "Obtenidos " + movements.size() + " movimientos totales");
                    callback.onSuccess(movements);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo movimientos", e);
                    callback.onError(e);
                });
    }

    // Actualizar movimiento
    public static void updateMovement(String movementId, Movement movement, OperationCallback callback) {
        Map<String, Object> movementData = new HashMap<>();
        movementData.put("cardId", movement.getCardId());
        movementData.put("date", movement.getDate());
        movementData.put("entryStation", movement.getEntryStation());
        movementData.put("exitStation", movement.getExitStation());
        movementData.put("travelTime", movement.getTravelTime());

        FirebaseManager.getFirestore()
                .collection(COLLECTION_MOVEMENTS)
                .document(movementId)
                .update(movementData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Movimiento actualizado: " + movementId);
                    callback.onSuccess("Movimiento actualizado exitosamente");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando movimiento", e);
                    callback.onError(e);
                });
    }

    // Eliminar movimiento
    public static void deleteMovement(String movementId, OperationCallback callback) {
        FirebaseManager.getFirestore()
                .collection(COLLECTION_MOVEMENTS)
                .document(movementId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Movimiento eliminado: " + movementId);
                    callback.onSuccess("Movimiento eliminado exitosamente");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error eliminando movimiento", e);
                    callback.onError(e);
                });
    }

    // Convertir DocumentSnapshot a Movement
    private static Movement documentToMovement(DocumentSnapshot document) {
        try {
            Movement movement = new Movement();
            movement.setMovementId(document.getId());
            movement.setUserId(document.getString("userId"));
            movement.setCardType(document.getString("cardType"));
            movement.setCardId(document.getString("cardId"));
            movement.setDate(document.getDate("date"));
            movement.setEntryStation(document.getString("entryStation"));
            movement.setExitStation(document.getString("exitStation"));

            Long travelTimeLong = document.getLong("travelTime");
            movement.setTravelTime(travelTimeLong != null ? travelTimeLong.intValue() : 0);

            movement.setCreatedAt(document.getDate("createdAt"));
            return movement;
        } catch (Exception e) {
            Log.e(TAG, "Error convirtiendo documento a Movement", e);
            return null;
        }
    }
}
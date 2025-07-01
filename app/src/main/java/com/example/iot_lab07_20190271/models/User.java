package com.example.iot_lab07_20190271.models;

import java.util.Date;

public class User {
    private String userId;
    private String email;
    private String displayName;
    private String provider;
    private Date createdAt;

    // Constructor vacío (requerido por Firestore)
    public User() {}

    // Constructor con parámetros
    public User(String userId, String email, String displayName, String provider, Date createdAt) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.provider = provider;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
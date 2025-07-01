package com.example.iot_lab07_20190271.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Movement implements Parcelable {
    private String movementId;
    private String userId;
    private String cardType;
    private String cardId;
    private Date date;
    private String entryStation;
    private String exitStation;
    private int travelTime;
    private Date createdAt;

    // Constructor vacío
    public Movement() {}

    // Constructor completo
    public Movement(String movementId, String userId, String cardType, String cardId,
                    Date date, String entryStation, String exitStation, int travelTime) {
        this.movementId = movementId;
        this.userId = userId;
        this.cardType = cardType;
        this.cardId = cardId;
        this.date = date;
        this.entryStation = entryStation;
        this.exitStation = exitStation;
        this.travelTime = travelTime;
        this.createdAt = new Date();
    }

    // Constructor Parcel
    protected Movement(Parcel in) {
        movementId = in.readString();
        userId = in.readString();
        cardType = in.readString();
        cardId = in.readString();
        long dateTime = in.readLong();
        date = dateTime != -1 ? new Date(dateTime) : null;
        entryStation = in.readString();
        exitStation = in.readString();
        travelTime = in.readInt();
        long createdTime = in.readLong();
        createdAt = createdTime != -1 ? new Date(createdTime) : null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movementId);
        dest.writeString(userId);
        dest.writeString(cardType);
        dest.writeString(cardId);
        dest.writeLong(date != null ? date.getTime() : -1);
        dest.writeString(entryStation);
        dest.writeString(exitStation);
        dest.writeInt(travelTime);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Movement> CREATOR = new Creator<Movement>() {
        @Override
        public Movement createFromParcel(Parcel in) {
            return new Movement(in);
        }

        @Override
        public Movement[] newArray(int size) {
            return new Movement[size];
        }
    };

    // Todos los getters y setters (iguales que antes)
    public String getMovementId() { return movementId; }
    public void setMovementId(String movementId) { this.movementId = movementId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getEntryStation() { return entryStation; }
    public void setEntryStation(String entryStation) { this.entryStation = entryStation; }

    public String getExitStation() { return exitStation; }
    public void setExitStation(String exitStation) { this.exitStation = exitStation; }

    public int getTravelTime() { return travelTime; }
    public void setTravelTime(int travelTime) { this.travelTime = travelTime; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
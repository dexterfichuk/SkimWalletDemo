package com.simplytapp.demo.json;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class Card implements Parcelable {
    private final String TAG = Card.class.getSimpleName();

    @SerializedName("ID")
    private String id;

    @SerializedName("EXP")
    private String expiration;

    @SerializedName("DISABLED")
    private String disabled;

    @SerializedName("PAN")
    private String pan;

    @SerializedName("BRAND")
    private String brand;

    @SerializedName("LOGO")
    private String logo;

    @SerializedName("HASH")
    private String hash;

    @SerializedName("SPEC_VER")
    private String specVersion;

    public String getId() {
        return id;
    }

    public String getExpiration() {
        return expiration;
    }

    public String getPan() {
        return pan;
    }

    public String getBrand() {
        return brand;
    }

    public String getLogo() {
        return logo;
    }

    public String getHash() {
        return hash;
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public boolean isDisabled() {

        if ("N".equals(disabled)) {
            return false;
        } else if ("Y".equals(disabled)) {
            return true;
        }

        Log.e(TAG, "missing or invalid DISABLED field in card data");
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        if (brand != null ? !brand.equals(card.brand) : card.brand != null) return false;
        if (disabled != null ? !disabled.equals(card.disabled) : card.disabled != null)
            return false;
        if (expiration != null ? !expiration.equals(card.expiration) : card.expiration != null)
            return false;
        if (hash != null ? !hash.equals(card.hash) : card.hash != null) return false;
        if (id != null ? !id.equals(card.id) : card.id != null) return false;
        if (logo != null ? !logo.equals(card.logo) : card.logo != null) return false;
        if (pan != null ? !pan.equals(card.pan) : card.pan != null) return false;
        return !(specVersion != null ? !specVersion.equals(card.specVersion) : card.specVersion != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (expiration != null ? expiration.hashCode() : 0);
        result = 31 * result + (disabled != null ? disabled.hashCode() : 0);
        result = 31 * result + (pan != null ? pan.hashCode() : 0);
        result = 31 * result + (brand != null ? brand.hashCode() : 0);
        result = 31 * result + (logo != null ? logo.hashCode() : 0);
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        result = 31 * result + (specVersion != null ? specVersion.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Card{" +
                "id='" + id + '\'' +
                ", expiration='" + expiration + '\'' +
                ", disabled='" + disabled + '\'' +
                ", pan='" + pan + '\'' +
                ", brand='" + brand + '\'' +
                ", logo='" + logo + '\'' +
                ", hash='" + hash + '\'' +
                ", specVersion='" + specVersion + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.expiration);
        dest.writeString(this.disabled);
        dest.writeString(this.pan);
        dest.writeString(this.brand);
        dest.writeString(this.logo);
        dest.writeString(this.hash);
        dest.writeString(this.specVersion);
    }

    private Card(Parcel in) {
        this.id = in.readString();
        this.expiration = in.readString();
        this.disabled = in.readString();
        this.pan = in.readString();
        this.brand = in.readString();
        this.logo = in.readString();
        this.hash = in.readString();
        this.specVersion = in.readString();
    }

    public static final Parcelable.Creator<Card> CREATOR = new Parcelable.Creator<Card>() {
        public Card createFromParcel(Parcel source) {
            return new Card(source);
        }

        public Card[] newArray(int size) {
            return new Card[size];
        }
    };
}
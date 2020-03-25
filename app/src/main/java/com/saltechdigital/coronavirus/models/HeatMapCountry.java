package com.saltechdigital.coronavirus.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class HeatMapCountry implements Parcelable {

    private String stateName;
    private String countryName;
    private String lastUpdate;
    private int confirmed;
    private int deaths;
    private int recovered;
    private LatLng latLng;

    public HeatMapCountry() {

    }

    protected HeatMapCountry(Parcel in) {
        stateName = in.readString();
        countryName = in.readString();
        lastUpdate = in.readString();
        confirmed = in.readInt();
        deaths = in.readInt();
        recovered = in.readInt();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<HeatMapCountry> CREATOR = new Creator<HeatMapCountry>() {
        @Override
        public HeatMapCountry createFromParcel(Parcel in) {
            return new HeatMapCountry(in);
        }

        @Override
        public HeatMapCountry[] newArray(int size) {
            return new HeatMapCountry[size];
        }
    };

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(int confirmed) {
        this.confirmed = confirmed;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getRecovered() {
        return recovered;
    }

    public void setRecovered(int recovered) {
        this.recovered = recovered;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(stateName);
        dest.writeString(countryName);
        dest.writeString(lastUpdate);
        dest.writeInt(confirmed);
        dest.writeInt(deaths);
        dest.writeInt(recovered);
        dest.writeParcelable(latLng, flags);
    }
}

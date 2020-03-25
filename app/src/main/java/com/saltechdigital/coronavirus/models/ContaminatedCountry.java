package com.saltechdigital.coronavirus.models;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.saltechdigital.coronavirus.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ContaminatedCountry implements Parcelable, Comparable<ContaminatedCountry> {

    private String name;
    private String date;

    private int infection;
    private int death;
    private int healing;
    private double deathRate;
    private double healingRate;
    private double infectionRate;

    public ContaminatedCountry() {
    }

    protected ContaminatedCountry(Parcel in) {
        name = in.readString();
        date = in.readString();
        infection = in.readInt();
        death = in.readInt();
        healing = in.readInt();
        deathRate = in.readDouble();
        healingRate = in.readDouble();
        infectionRate = in.readDouble();
    }

    public static final Creator<ContaminatedCountry> CREATOR = new Creator<ContaminatedCountry>() {
        @Override
        public ContaminatedCountry createFromParcel(Parcel in) {
            return new ContaminatedCountry(in);
        }

        @Override
        public ContaminatedCountry[] newArray(int size) {
            return new ContaminatedCountry[size];
        }
    };

    public ArrayList<ContaminatedCountry> forStat(ContaminatedCountry country, JSONArray jsonArray) {
        ArrayList<ContaminatedCountry> countries = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.optJSONObject(i);
            Log.d("JEANPAUL", "forStat: beurk");
            if (object.has("Pays") && object.optString("Pays").equals(country.getName())) {
                ContaminatedCountry cc = new ContaminatedCountry();
                country.setDate(object.optString("Date"));
                country.setName(object.optString("Pays"));
                country.setInfection(object.optInt("Infection"));
                country.setDeath(object.optInt("Deces"));
                country.setHealing(object.optInt("Guerisons"));
                country.setDeathRate(object.optDouble("TauxDeces"));
                country.setHealingRate(object.optDouble("TauxGuerison"));
                country.setInfectionRate(object.optDouble("TauxInfection"));
                countries.add(cc);
            }
        }

        return countries;
    }

    public List<ContaminatedCountry> populate(JSONArray jsonArray) {
        List<ContaminatedCountry> countries = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject object = jsonArray.optJSONObject(i);
            if (object.has("Date") && object.optString("Date").contains(MainActivity.dataTodayDate)) {
                ContaminatedCountry country = new ContaminatedCountry();
                country.setDate(object.optString("Date"));
                country.setName(object.optString("Pays"));
                country.setInfection(object.optInt("Infection"));
                country.setDeath(object.optInt("Deces"));
                country.setHealing(object.optInt("Guerisons"));
                country.setDeathRate(object.optDouble("TauxDeces"));
                country.setHealingRate(object.optDouble("TauxGuerison"));
                country.setInfectionRate(object.optDouble("TauxInfection"));
                countries.add(country);
            } else if (object.has("Date") && object.optString("Date").contains(MainActivity.dataYesterdayDate)) {
                ContaminatedCountry country = new ContaminatedCountry();
                country.setDate(object.optString("Date"));
                country.setName(object.optString("Pays"));
                country.setInfection(object.optInt("Infection"));
                country.setDeath(object.optInt("Deces"));
                country.setHealing(object.optInt("Guerisons"));
                country.setDeathRate(object.optDouble("TauxDeces"));
                country.setHealingRate(object.optDouble("TauxGuerison"));
                country.setInfectionRate(object.optDouble("TauxInfection"));
                countries.add(country);
            }
        }
        return countries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInfection() {
        return infection;
    }

    public void setInfection(int infection) {
        this.infection = infection;
    }

    public int getDeath() {
        return death;
    }

    public void setDeath(int death) {
        this.death = death;
    }

    public int getHealing() {
        return healing;
    }

    public void setHealing(int healing) {
        this.healing = healing;
    }

    public double getDeathRate() {
        return deathRate;
    }

    public void setDeathRate(double deathRate) {
        this.deathRate = deathRate;
    }

    public double getHealingRate() {
        return healingRate;
    }

    public void setHealingRate(double healingRate) {
        this.healingRate = healingRate;
    }

    public double getInfectionRate() {
        return infectionRate;
    }

    public void setInfectionRate(double infectionRate) {
        this.infectionRate = infectionRate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(date);
        dest.writeInt(infection);
        dest.writeInt(death);
        dest.writeInt(healing);
        dest.writeDouble(deathRate);
        dest.writeDouble(healingRate);
        dest.writeDouble(infectionRate);
    }

    @Override
    public int compareTo(ContaminatedCountry o) {
        return this.infection > o.infection ? -1 : 1;
    }
}

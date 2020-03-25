package com.saltechdigital.coronavirus;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.saltechdigital.coronavirus.models.ContaminatedCountry;

public class MainModel extends ViewModel {

    private MutableLiveData<ContaminatedCountry> countryMutableLiveData;

    public MainModel() {
        countryMutableLiveData = new MutableLiveData<>();
    }

    public LiveData<ContaminatedCountry> getCountryMutableLiveData() {
        return countryMutableLiveData;
    }

    public void setCountryMutableLiveData(MutableLiveData<ContaminatedCountry> countryMutableLiveData) {
        this.countryMutableLiveData = countryMutableLiveData;
    }

    public void post(ContaminatedCountry country){
        this.countryMutableLiveData.setValue(country);
    }
}

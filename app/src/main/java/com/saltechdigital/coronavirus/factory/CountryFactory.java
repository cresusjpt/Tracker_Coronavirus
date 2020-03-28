package com.saltechdigital.coronavirus.factory;

import com.saltechdigital.coronavirus.models.ContaminatedCountry;
import com.saltechdigital.coronavirus.models.HeatMapCountry;
import com.saltechdigital.coronavirus.utils.Final;

public class CountryFactory {

    private String name;
    private String date;
    private int infected;
    private int death;
    private int recovered;

    public CountryFactory(String name, String date, int infected, int death, int recovered) {
        this.name = name;
        this.date = date;
        this.infected = infected;
        this.death = death;
        this.recovered = recovered;
    }

    public Country getCountry(String type){
        if (type.equals(Final.HEAT)){
            HeatMapCountry heatMapCountry = new HeatMapCountry();
            heatMapCountry.name(this.name);
            heatMapCountry.date(this.date);
            heatMapCountry.infection(this.infected);
            heatMapCountry.death(this.death);
            heatMapCountry.recoverd(this.recovered);
            return heatMapCountry;
        }else if (type.equals(Final.CONTAMINATED)){
            ContaminatedCountry country = new ContaminatedCountry();
            country.name(this.name);
            country.date(this.date);
            country.infection(this.infected);
            country.death(this.death);
            country.recoverd(this.recovered);
            return country;
        }

        return null;
    }
}

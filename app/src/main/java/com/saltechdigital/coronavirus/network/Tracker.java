package com.saltechdigital.coronavirus.network;


import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

/**
 * interface permettant de faire les requêtes pour récuperer les différentes informations
 * api utilisé : RETROFIT
 */
public interface Tracker {

   // String ENDPOINT = "http://10.0.2.2/deliver/web/api/";
    String ENDPOINT = "https://coronavirus.politologue.com/data/coronavirus/";
    String GITHUB_ENDPOINT = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/";
    String SERIE_CONFIRMED_ENDPOINT = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/";

    @GET("coronacsv.aspx?format=json")
    Call<ResponseBody> downloadFileWithFixedUrl();

    @GET("{dailyReport}.csv")
    Call<ResponseBody> dailyReports(@Path("dailyReport") String dailyDate);


    @GET("time_series_covid19_confirmed_global.csv")
    Call<ResponseBody> serieConfirmedReports();

    @GET("time_series_covid19_deaths_global.csv")
    Call<ResponseBody> serieDeathReports();

    @GET("time_series_covid19_recovered_global.csv")
    Call<ResponseBody> serieRecoveredReports();

    @Streaming
    @GET("coronacsv.aspx?format=json")
    Single covidData(Callback callback);
}

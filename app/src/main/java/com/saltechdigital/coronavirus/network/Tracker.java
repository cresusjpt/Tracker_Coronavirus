package com.saltechdigital.coronavirus.network;


import com.saltechdigital.coronavirus.models.POJO;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface Tracker {

   // String ENDPOINT = "http://10.0.2.2/deliver/web/api/";
    String ENDPOINT = "https://coronavirus.politologue.com/data/coronavirus/";
    String GITHUB_ENDPOINT = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/";
    String SERIE_CONFIRMED_ENDPOINT = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/";

    @GET("coronacsv.aspx?format=json")
    Call<ResponseBody> downloadFileWithFixedUrl();

    @GET("{dailyReport}.csv")
    Call<ResponseBody> dailyReports(@Path("dailyReport") String dailyDate);

    @GET("time_series_19-covid-Confirmed.csv")
    Call<ResponseBody> serieConfirmedReports();

    @GET("time_series_19-covid-Deaths.csv")
    Call<ResponseBody> serieDeathReports();

    @GET("time_series_19-covid-Recovered.csv")
    Call<ResponseBody> serieRecoveredReports();

    @Streaming
    @GET("coronacsv.aspx?format=json")
    Single covidData(Callback callback);
}

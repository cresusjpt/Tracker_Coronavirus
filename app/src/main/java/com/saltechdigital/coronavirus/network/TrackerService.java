package com.saltechdigital.coronavirus.network;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrackerService {

    public static Tracker createService(String endpoint, Context context) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //final String token = new SessionManager(context).getToken();
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                //.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                //.registerTypeAdapter(Address.class,new AddressDeserializer())
                .create();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                //.retryOnConnectionFailure(true)
                //.addInterceptor(interceptor)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Request.Builder builder = request.newBuilder().header("Accept", "application/json");
                    Request newRequest = builder.build();
                    return chain.proceed(newRequest);
                }).addInterceptor(chain -> {
                    Request request = chain.request();
                    Request.Builder builder = request.newBuilder().header("Content-Type", "application/json; charset=utf-8");
                    Request newRequest = builder.build();
                    return chain.proceed(newRequest);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endpoint)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(Tracker.class);
    }
}

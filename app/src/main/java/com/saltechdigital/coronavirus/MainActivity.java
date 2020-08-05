package com.saltechdigital.coronavirus;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.saltechdigital.coronavirus.models.ContaminatedCountry;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Nous avons utilisé des fragments
 * ceci est la classe principale dans lequel les 3 premiers fragments seront appelés
 */
public class MainActivity extends AppCompatActivity {

    //Nous n'avons pas utilisé de base de données localement donc une variable static stock certaines valeurs durant toute la durée de vie de l'application
    public static String COUNTRY = "FRANCE";
    public static JSONObject jsonObject = null;
    public static String data = null;
    public static List<ContaminatedCountry> contaminatedCountries = new ArrayList<>();
    public static ContaminatedCountry contaminatedOverview = null;
    public static String dataTodayDate;
    public static String dataYesterdayDate;

    private InterstitialAd mInterstitialAd;
    private ScheduledExecutorService scheduler;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void prepareAd(){
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6002058442934755/6779659716");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    protected void onDestroy() {
        //scheduler.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contaminatedOverview = new ContaminatedCountry();
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_dashboard,
                R.id.navigation_home, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        prepareAd();
        /*scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> runOnUiThread(() -> {
            if (mInterstitialAd.isLoaded()){
                mInterstitialAd.show();
            }
            prepareAd();
        }),120,180, TimeUnit.SECONDS);*/
        Date c = Calendar.getInstance().getTime();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("gmt"));
        SimpleDateFormat today = new SimpleDateFormat("dd", Locale.getDefault());
        today.setTimeZone(TimeZone.getTimeZone("gmt"));

        String yesterday = String.format(Locale.getDefault(),"%02d",Integer.parseInt(today.format(c)) - 1);
        String now = String.format(Locale.getDefault(),"%02d",Integer.parseInt(today.format(c)));

        dataYesterdayDate = df.format(c) + "-" + yesterday;
        dataTodayDate = df.format(c) + "-" + now;

        MobileAds.initialize(this, initializationStatus -> {
        });

        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //scheduler.shutdown();
        super.onBackPressed();
    }
}

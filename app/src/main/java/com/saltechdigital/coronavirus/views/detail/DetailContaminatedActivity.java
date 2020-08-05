package com.saltechdigital.coronavirus.views.detail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.saltechdigital.coronavirus.R;
import com.saltechdigital.coronavirus.decorator.ConfirmedDataDecorator;
import com.saltechdigital.coronavirus.decorator.DataSourceDecorator;
import com.saltechdigital.coronavirus.decorator.DeathDataDecorator;
import com.saltechdigital.coronavirus.decorator.FileDataSource;
import com.saltechdigital.coronavirus.decorator.RecoverDataDecorator;
import com.saltechdigital.coronavirus.models.ContaminatedCountry;
import com.saltechdigital.coronavirus.network.Tracker;
import com.saltechdigital.coronavirus.network.TrackerService;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.saltechdigital.coronavirus.adapter.CountryListAdapter.EXTRA_CONTAMINED;

public class DetailContaminatedActivity extends AppCompatActivity {

    private LinearLayout line;
    private Tracker tracker;
    private ImageView imageView;
    public ContaminatedCountry contaminatedCountry;
    private GraphView recoveredGraph, deathGraph, infectedGraph;
    private Context context;


    //private InterstitialAd interstitialAd;

    private String translatedName = "";

    //the country name is in english but in the jhu data file the name is english, so we need to translate
    private void translateCountryName() {
        String country = contaminatedCountry.getName();
        Locale outLocale = Locale.ENGLISH;
        Locale inLocale = Locale.FRENCH;
        for (Locale l : Locale.getAvailableLocales()) {
            if (l.getDisplayCountry(inLocale).equals(country)) {
                translatedName = l.getDisplayCountry(outLocale);
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_first);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;

        tracker = TrackerService.createService(Tracker.SERIE_CONFIRMED_ENDPOINT, this);

        //on recupere l'objet envoyé depuis l'intent de la précedente activité
        Intent intent = getIntent();
        contaminatedCountry = intent.getParcelableExtra(EXTRA_CONTAMINED);

        //si l'on a pu bien récupére il faut changer le titre de la barre et trouver la correspondance de ce nom en anglais
        //parce que pour réaliser les graphiques nous avons besoins de ce nom en anglais.
        if (contaminatedCountry != null) {
            setTitle(contaminatedCountry.getName());
            translateCountryName();
        }

        findById();
        //les données sont dans certains cas comme la france et l'amérique dans un format non ascendant. ce qui ne nous permettait pas de réaliser correctement le graphique
        //donc pour ces pays les méthodes des graphiques ne seront pas appelés
        if (!contaminatedCountry.getName().equals("France") && !contaminatedCountry.getName().equals("Chine") && !contaminatedCountry.getName().equals("Pays-Bas") && !contaminatedCountry.getName().equals("Royaume-Uni")) {
            confirmed();
            recovered();
            dead();
        }

        //pour la france nous remplaçons les graphiques par une image représentants le nombres des cas par régions et par départements
        if (contaminatedCountry.getName().equals("France")) {
            String uri = "https://static.data.gouv.fr/resources/cas-confirmes-dinfection-au-covid-19-par-region/20200315-084505/covid19.svg";
            Uri u = Uri.parse(uri);
            imageView.setVisibility(View.VISIBLE);
            line.setVisibility(View.INVISIBLE);
            GlideToVectorYou.justLoadImage(this, u, imageView);
        }

        /*interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-6002058442934755/6779659716");
        interstitialAd.loadAd(new AdRequest.Builder().build());

        MobileAds.initialize(this, initializationStatus -> {
        });*/
    }

    /*private void showInterstitial(){
        if (interstitialAd.isLoaded()){
            interstitialAd.show();
            interstitialAd.loadAd(new AdRequest.Builder().build());
        }

        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                interstitialAd.loadAd(new AdRequest.Builder().build());
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
    */

    //une permet qui permet d'inflater les view
    //utiliser pour rendre moins longue la méthodes create
    private void findById() {
        line = findViewById(R.id.line);
        imageView = findViewById(R.id.image);
        TextView infected = findViewById(R.id.tv_infected);
        TextView infectedRate = findViewById(R.id.tv_infected_rate);
        TextView death = findViewById(R.id.tv_death);
        TextView deathRate = findViewById(R.id.tv_death_rate);
        TextView recovered = findViewById(R.id.tv_recovered);
        TextView recoveredRate = findViewById(R.id.tv_recovered_rate);

        recoveredGraph = findViewById(R.id.recover);
        deathGraph = findViewById(R.id.dea);
        infectedGraph = findViewById(R.id.infec);


        infected.setText(MessageFormat.format("{0}", String.valueOf(contaminatedCountry.getInfection())));
        infectedRate.setText(MessageFormat.format("{0}%", String.valueOf(contaminatedCountry.getInfectionRate())));
        death.setText(MessageFormat.format("{0}", String.valueOf(contaminatedCountry.getDeath())));
        deathRate.setText(MessageFormat.format("{0}%", String.valueOf(contaminatedCountry.getDeathRate())));
        recovered.setText(MessageFormat.format("{0}", String.valueOf(contaminatedCountry.getHealing())));
        recoveredRate.setText(MessageFormat.format("{0}%", String.valueOf(contaminatedCountry.getHealingRate())));
    }

    //permet de réaliser le graphique des guérris par pays
    private void recovered() {
        //on fait un appel serveur vers l'api pour récuperer les informations
        Call<ResponseBody> call = tracker.serieRecoveredReports();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                    //si la requete a réussie
                    //Log.d(Final.TAG, "onResponse: " + translatedName);
                    try {
                        assert response.body() != null;
                        String message = response.body().string();
                        //on utilise ici le decorateur pour creer une source basique avec des comportements de recoverdata
                        DataSourceDecorator recoverData = new RecoverDataDecorator(new FileDataSource(context), context, translatedName);
                        recoverData.writeData(message);
                        //datapoint est un type de données renvoyé par le graphview
                        DataPoint[] points = recoverData.readData();
                        if (points != null) {
                            LineGraphSeries<DataPoint> seriere = new LineGraphSeries<>(points);
                            seriere.setColor(getColor(R.color.colorRecover));
                            seriere.setTitle(getString(R.string.heal));
                            recoveredGraph.addSeries(seriere);
                            recoveredGraph.setVisibility(View.VISIBLE);
                            recoveredGraph.setTitle(getString(R.string.heal));
                            recoveredGraph.setTitleColor(getColor(R.color.colorRecover));
                            recoveredGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(DetailContaminatedActivity.this));
                            recoveredGraph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
                            recoveredGraph.getViewport().setXAxisBoundsManual(true);
                            recoveredGraph.getGridLabelRenderer().setHumanRounding(false);
                        }
                    } catch (IOException e) {
                        //Log.d(Final.TAG, "onError: ", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                //Log.d(Final.TAG, "onFailure: ", t);
            }
        });
    }

    //permet de réaliser le graphique des mort par pays
    //sensiblement pareil que la précédente à part l'ajout du comportement particulier.
    private void dead() {
        Call<ResponseBody> call = tracker.serieDeathReports();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    //Log.d(Final.TAG, "onResponse: " + translatedName);
                    try {
                        assert response.body() != null;
                        String source = response.body().string();
                        DataSourceDecorator deathData = new DeathDataDecorator(new FileDataSource(context), context, translatedName);
                        deathData.writeData(source);
                        DataPoint[] points = deathData.readData();
                        if (points != null) {
                            LineGraphSeries<DataPoint> serieDeath = new LineGraphSeries<>(points);
                            serieDeath.setColor(getColor(R.color.colorDeath));
                            serieDeath.setTitle(getString(R.string.heal));
                            deathGraph.addSeries(serieDeath);
                            deathGraph.setVisibility(View.VISIBLE);
                            deathGraph.setTitle(getString(R.string.death));
                            deathGraph.setTitleColor(getColor(R.color.colorDeath));
                            deathGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(DetailContaminatedActivity.this));
                            deathGraph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
                            deathGraph.getViewport().setXAxisBoundsManual(true);

                            deathGraph.getGridLabelRenderer().setHumanRounding(false);
                        }
                    } catch (IOException e) {
                        //Log.d(Final.TAG, "ERROT 1: ");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

            }
        });
    }

    //permet de réaliser le graphique des infections par pays
    //sensiblement pareil que la précédente à part l'ajout du comportement particulier.
    private void confirmed() {
        //on fait un appel serveur vers l'api pour récuperer les informations
        Call<ResponseBody> call = tracker.serieConfirmedReports();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    //Log.d(Final.TAG, "onResponse: " + translatedName);
                    try {
                        assert response.body() != null;
                        String source = response.body().string();
                        DataSourceDecorator confirmedData = new ConfirmedDataDecorator(new FileDataSource(context), context, translatedName);
                        confirmedData.writeData(source);
                        DataPoint[] points = confirmedData.readData();
                        if (points != null) {
                            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);
                            series.setColor(getColor(R.color.colorInfect));
                            series.setTitle(getString(R.string.heal));
                            infectedGraph.addSeries(series);
                            infectedGraph.setVisibility(View.VISIBLE);
                            infectedGraph.setTitle(getString(R.string.infections));
                            infectedGraph.setTitleColor(getColor(R.color.colorInfect));
                            infectedGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(DetailContaminatedActivity.this));
                            infectedGraph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
                            infectedGraph.getViewport().setXAxisBoundsManual(true);
                            infectedGraph.getGridLabelRenderer().setHumanRounding(false);
                        }

                    } catch (IOException e) {
                        //Log.d(Final.TAG, "ERROT 1: ");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

            }
        });
    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.detail_contaminated_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_ads_dowload){
            showInterstitial();
        }
        return super.onOptionsItemSelected(item);
    }*/
}

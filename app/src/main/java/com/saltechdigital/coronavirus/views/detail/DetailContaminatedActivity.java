package com.saltechdigital.coronavirus.views.detail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.saltechdigital.coronavirus.decorator.DataSourceDecorator;
import com.saltechdigital.coronavirus.decorator.DeathDataDecorator;
import com.saltechdigital.coronavirus.decorator.FileDataSource;
import com.saltechdigital.coronavirus.decorator.RecoverDataDecorator;
import com.saltechdigital.coronavirus.models.ContaminatedCountry;
import com.saltechdigital.coronavirus.network.Tracker;
import com.saltechdigital.coronavirus.network.TrackerService;
import com.saltechdigital.coronavirus.utils.Final;

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

        Intent intent = getIntent();
        contaminatedCountry = intent.getParcelableExtra(EXTRA_CONTAMINED);

        if (contaminatedCountry != null) {
            setTitle(contaminatedCountry.getName());
            translateCountryName();
        }


        findById();
        if (!contaminatedCountry.getName().equals("France") && !contaminatedCountry.getName().equals("Chine") && !contaminatedCountry.getName().equals("Pays-Bas") && !contaminatedCountry.getName().equals("Royaume-Uni")) {
            confirmed();
            recovered();
            dead();
        }

        if (contaminatedCountry.getName().equals("France")) {
            String uri = "https://static.data.gouv.fr/resources/cas-confirmes-dinfection-au-covid-19-par-region/20200315-084505/covid19.svg";
            Uri u = Uri.parse(uri);
            imageView.setVisibility(View.VISIBLE);
            line.setVisibility(View.INVISIBLE);
            GlideToVectorYou.justLoadImage(this, u, imageView);
        }
    }

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


        infected.setText(MessageFormat.format("{0}%", String.valueOf(contaminatedCountry.getInfection())));
        infectedRate.setText(MessageFormat.format("{0}%", String.valueOf(contaminatedCountry.getInfectionRate())));
        death.setText(MessageFormat.format("{0}%", String.valueOf(contaminatedCountry.getDeath())));
        deathRate.setText(MessageFormat.format("{0}%", String.valueOf(contaminatedCountry.getDeathRate())));
        recovered.setText(MessageFormat.format("{0}%", String.valueOf(contaminatedCountry.getHealing())));
        recoveredRate.setText(MessageFormat.format("{0}%", String.valueOf(contaminatedCountry.getHealingRate())));
    }

    private void recovered() {
        Call<ResponseBody> call = tracker.serieRecoveredReports();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                    Log.d(Final.TAG, "onResponse: " + translatedName);
                    try {
                        assert response.body() != null;
                        String message = response.body().string();
                        DataSourceDecorator recoverData = new RecoverDataDecorator(new FileDataSource(context, message), context, translatedName);
                        recoverData.writeData(message);
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
                        Log.d(Final.TAG, "onError: ", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.d(Final.TAG, "onFailure: ", t);
            }
        });
    }

    private void dead() {
        Call<ResponseBody> call = tracker.serieDeathReports();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(Final.TAG, "onResponse: " + translatedName);
                    try {
                        assert response.body() != null;
                        String source = response.body().string();
                        DataSourceDecorator deathData = new DeathDataDecorator(new FileDataSource(context, source), context, translatedName);
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
                        Log.d(Final.TAG, "ERROT 1: ");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

            }
        });
    }

    private void confirmed() {
        Call<ResponseBody> call = tracker.serieConfirmedReports();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(Final.TAG, "onResponse: " + translatedName);
                    try {
                        assert response.body() != null;
                        String source = response.body().string();
                        DataSourceDecorator deathData = new DeathDataDecorator(new FileDataSource(context, source), context, translatedName);
                        deathData.writeData(source);
                        DataPoint[] points = deathData.readData();
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
                        Log.d(Final.TAG, "ERROT 1: ");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

            }
        });
    }
}

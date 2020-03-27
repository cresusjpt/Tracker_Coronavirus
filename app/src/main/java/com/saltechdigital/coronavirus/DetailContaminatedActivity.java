package com.saltechdigital.coronavirus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.saltechdigital.coronavirus.models.ContaminatedCountry;
import com.saltechdigital.coronavirus.network.Tracker;
import com.saltechdigital.coronavirus.network.TrackerService;
import com.saltechdigital.coronavirus.utils.CSVParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_first);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tracker = TrackerService.createService(Tracker.SERIE_CONFIRMED_ENDPOINT, this);

        Intent intent = getIntent();
        contaminatedCountry = new ContaminatedCountry();
        contaminatedCountry = intent.getParcelableExtra(EXTRA_CONTAMINED);

        if (contaminatedCountry != null)
            setTitle(contaminatedCountry.getName());

        findById();
        if (!contaminatedCountry.getName().equals("France") && !contaminatedCountry.getName().equals("Chine") && !contaminatedCountry.getName().equals("Pays-Bas") && !contaminatedCountry.getName().equals("Royaume-Uni")) {
            confirmed();
            recovered();
            dead();
        }

        if (contaminatedCountry.getName().equals("France")){
            String uri = "https://static.data.gouv.fr/resources/cas-confirmes-dinfection-au-covid-19-par-region/20200315-084505/covid19.svg";
            Uri u = Uri.parse(uri);
            imageView.setVisibility(View.VISIBLE);
            line.setVisibility(View.INVISIBLE);
            GlideToVectorYou.justLoadImage(this,u,imageView);
        }
    }

    private void findById() {
        line = findViewById(R.id.line);
        imageView = findViewById(R.id.image);
        TextView infected = findViewById(R.id.tv_infected);
        TextView death = findViewById(R.id.tv_death);
        TextView recovered = findViewById(R.id.tv_recovered);

        infected.setText(String.valueOf(contaminatedCountry.getInfection()));
        death.setText(String.valueOf(contaminatedCountry.getDeath()));
        recovered.setText(String.valueOf(contaminatedCountry.getHealing()));

        recoveredGraph = findViewById(R.id.recover);
        deathGraph = findViewById(R.id.dea);
        infectedGraph = findViewById(R.id.infec);
    }

    private void recovered() {
        Call<ResponseBody> call = tracker.serieRecoveredReports();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                    String country = contaminatedCountry.getName();
                    Locale outLocale = Locale.ENGLISH;
                    Locale inLocale = Locale.FRENCH;
                    String translatedName = "";
                    for (Locale l : Locale.getAvailableLocales()) {
                        if (l.getDisplayCountry(inLocale).equals(country)) {
                            translatedName = l.getDisplayCountry(outLocale);
                            break;
                        }
                    }

                    Log.d("JEANPAUL", "onResponse: " + translatedName);
                    try {
                        assert response.body() != null;
                        String message = response.body().string();
                        File outputDir = getCacheDir();
                        File outputFile = File.createTempFile("yesterday", "json", outputDir);

                        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                        writer.write(message);
                        writer.close();

                        CSVParser parser = new CSVParser(DetailContaminatedActivity.this);
                        List csv = parser.read(outputFile);
                        ArrayList<ContaminatedCountry> countryArrayList = new ArrayList<>();

                        if (csv.size() > 0) {
                            String[] header_row = (String[]) csv.get(0);
                            LineGraphSeries<DataPoint> seriere = new LineGraphSeries<>();
                            DataPoint[] points = new DataPoint[csv.size()];
                            for (int j = 1; j < csv.size(); j++) {
                                String[] data = (String[]) csv.get(j);
                                if (data[1].equals(translatedName)) {
                                    for (int i = 0; i < header_row.length; i++) {
                                        if (i > 3) {
                                            Log.d("JEANPAUL", "Pays : " + data[1] + " Ensuite" + header_row[i] + "onResponse: " + data[i]);
                                            ContaminatedCountry ntry = new ContaminatedCountry();
                                            ntry.setName(header_row[i]);
                                            ntry.setHealing(Integer.parseInt(data[i]));
                                            Log.d("JEANPAUL", ntry.getName() + " = : " + ntry.getHealing());


                                            String dateInString = ntry.getName();
                                            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
                                            Date parsedDate = formatter.parse(dateInString);


                                            if (parsedDate != null && ntry.getHealing() != 0) {
                                                DataPoint point = new DataPoint(parsedDate, ntry.getHealing());
                                                points[j-1] = point;
                                                seriere.appendData(point, false, csv.size());
                                                seriere.setColor(getColor(R.color.colorRecover));
                                                seriere.setTitle(getString(R.string.heal));
                                            }

                                            countryArrayList.add(ntry);
                                        }
                                    }
                                }
                            }
                            recoveredGraph.addSeries(seriere);
                            recoveredGraph.setVisibility(View.VISIBLE);
                            recoveredGraph.setTitle(getString(R.string.heal));
                            recoveredGraph.setTitleColor(getColor(R.color.colorRecover));

                            recoveredGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(DetailContaminatedActivity.this));
                            recoveredGraph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space
                            recoveredGraph.getViewport().setXAxisBoundsManual(true);

                            recoveredGraph.getGridLabelRenderer().setHumanRounding(false);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void dead() {
        Call<ResponseBody> call = tracker.serieDeathReports();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                    String country = contaminatedCountry.getName();
                    Locale outLocale = Locale.ENGLISH;
                    Locale inLocale = Locale.FRENCH;
                    String translatedName = "";
                    for (Locale l : Locale.getAvailableLocales()) {
                        if (l.getDisplayCountry(inLocale).equals(country)) {
                            translatedName = l.getDisplayCountry(outLocale);
                            break;
                        }
                    }

                    Log.d("JEANPAUL", "onResponse: " + translatedName);
                    try {
                        assert response.body() != null;
                        String message = response.body().string();
                        File outputDir = getCacheDir();
                        File outputFile = File.createTempFile("yesterday", "json", outputDir);

                        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                        writer.write(message);
                        writer.close();

                        CSVParser parser = new CSVParser(DetailContaminatedActivity.this);
                        List csv = parser.read(outputFile);
                        ArrayList<ContaminatedCountry> countryArrayList = new ArrayList<>();

                        if (csv.size() > 0) {
                            String[] header_row = (String[]) csv.get(0);
                            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                            for (int j = 1; j < csv.size(); j++) {
                                String[] data = (String[]) csv.get(j);
                                if (data[1].equals(translatedName)) {
                                    for (int i = 0; i < header_row.length; i++) {
                                        if (i > 3) {
                                            Log.d("JEANPAUL", "Pays : " + data[1] + " Ensuite" + header_row[i] + "onResponse: " + data[i]);
                                            //Province/State,Country/Region,Lat,Long,1/22/20,1/23/20,1/24/20,1/25/20,1/26/20,1/27/20,1/28/20,1/29/20,1/30/20,1/31/20,2/1/20,2/2/20,2/3/20,2/4/20,2/5/20,2/6/20,2/7/20,2/8/20,2/9/20,2/10/20,2/11/20,2/12/20,2/13
                                            ContaminatedCountry ntry = new ContaminatedCountry();
                                            ntry.setName(header_row[i]);
                                            ntry.setDeath(Integer.parseInt(data[i]));

                                            String dateInString = ntry.getName();
                                            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
                                            Date parsedDate = formatter.parse(dateInString);

                                            if (parsedDate != null && ntry.getDeath() != 0) {
                                                DataPoint point = new DataPoint(parsedDate, ntry.getDeath());
                                                series.appendData(point, false, 10000);
                                                series.setColor(getColor(R.color.colorDeath));
                                                series.setTitle(getString(R.string.death));
                                            }
                                            countryArrayList.add(ntry);
                                        }
                                    }
                                }
                            }
                            deathGraph.addSeries(series);
                            deathGraph.setVisibility(View.VISIBLE);
                            deathGraph.setTitle(getString(R.string.death));
                            deathGraph.setTitleColor(getColor(R.color.colorDeath));


                            deathGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(DetailContaminatedActivity.this));
                            deathGraph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space
                            deathGraph.getViewport().setXAxisBoundsManual(true);

                            deathGraph.getGridLabelRenderer().setHumanRounding(false);
                        }
                    } catch (IOException e) {
                        Log.d("JEANPAUL", "ERROT 1: ");
                        e.printStackTrace();
                    } catch (ParseException e) {
                        Log.d("JEANPAUL", "ERROT 2: ");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void confirmed() {
        Call<ResponseBody> call = tracker.serieConfirmedReports();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                    String country = contaminatedCountry.getName();
                    Locale outLocale = Locale.ENGLISH;
                    Locale inLocale = Locale.FRENCH;
                    String translatedName = "";
                    for (Locale l : Locale.getAvailableLocales()) {
                        if (l.getDisplayCountry(inLocale).equals(country)) {
                            translatedName = l.getDisplayCountry(outLocale);
                            break;
                        }
                    }

                    Log.d("JEANPAUL", "onResponse: " + translatedName);
                    try {
                        assert response.body() != null;
                        String message = response.body().string();
                        File outputDir = getCacheDir();
                        File outputFile = File.createTempFile("yesterday", "json", outputDir);

                        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                        writer.write(message);
                        writer.close();

                        CSVParser parser = new CSVParser(DetailContaminatedActivity.this);
                        List csv = parser.read(outputFile);
                        ArrayList<ContaminatedCountry> countryArrayList = new ArrayList<>();

                        if (csv.size() > 0) {
                            String[] header_row = (String[]) csv.get(0);
                            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                            for (int j = 1; j < csv.size(); j++) {
                                String[] data = (String[]) csv.get(j);
                                if (data[1].equals(translatedName)) {
                                    for (int i = 0; i < header_row.length; i++) {
                                        if (i > 3) {
                                            Log.d("JEANPAUL", "Pays : " + data[1] + " Ensuite" + header_row[i] + "onResponse: " + data[i]);
                                            //Province/State,Country/Region,Lat,Long,1/22/20,1/23/20,1/24/20,1/25/20,1/26/20,1/27/20,1/28/20,1/29/20,1/30/20,1/31/20,2/1/20,2/2/20,2/3/20,2/4/20,2/5/20,2/6/20,2/7/20,2/8/20,2/9/20,2/10/20,2/11/20,2/12/20,2/13
                                            ContaminatedCountry ntry = new ContaminatedCountry();
                                            ntry.setName(header_row[i]);
                                            ntry.setInfection(Integer.parseInt(data[i]));


                                            String dateInString = ntry.getName();
                                            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
                                            Date parsedDate = formatter.parse(dateInString);

                                            if (parsedDate != null && ntry.getInfection() != 0) {
                                                DataPoint point = new DataPoint(parsedDate, ntry.getInfection());
                                                series.appendData(point, true, 10000);
                                                series.setColor(getColor(R.color.colorInfect));
                                                series.setTitle(getString(R.string.infections));
                                            }

                                            countryArrayList.add(ntry);
                                        }
                                    }
                                }
                            }
                            infectedGraph.addSeries(series);
                            infectedGraph.setVisibility(View.VISIBLE);
                            infectedGraph.setTitle(getString(R.string.infections));
                            infectedGraph.setTitleColor(getColor(R.color.colorInfect));


                            infectedGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(DetailContaminatedActivity.this));
                            infectedGraph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space
                            infectedGraph.getViewport().setXAxisBoundsManual(true);

                            infectedGraph.getGridLabelRenderer().setHumanRounding(false);
                        }

                    } catch (IOException e) {
                        Log.d("JEANPAUL", "ERROT 1: ");
                        e.printStackTrace();
                    } catch (ParseException e) {
                        Log.d("JEANPAUL", "ERROT 2: ");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}

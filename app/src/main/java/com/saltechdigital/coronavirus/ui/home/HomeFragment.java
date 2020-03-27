package com.saltechdigital.coronavirus.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.saltechdigital.coronavirus.MainActivity;
import com.saltechdigital.coronavirus.R;
import com.saltechdigital.coronavirus.models.ContaminatedCountry;
import com.saltechdigital.coronavirus.models.HeatMapCountry;
import com.saltechdigital.coronavirus.network.Tracker;
import com.saltechdigital.coronavirus.network.TrackerService;
import com.saltechdigital.coronavirus.utils.CSVParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FragmentActivity fragmentActivity;
    private Context context;
    private Tracker tracker;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private LinearLayout bottomLinear;
    private TextView infected, death, recovered, count, pull;
    private ContaminatedCountry country;

    private ArrayList<HeatMapCountry> heatMapCountries = null;
    private ArrayList<LatLng> latLngList = null;
    private ArrayList<WeightedLatLng> weightedLatLng = null;
    private HeatmapTileProvider provider = null;

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        fragmentActivity = childFragment.getActivity();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomLinear);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        if (country != null)
                            pull.setText(getString(R.string.data_update, country.getDate().split("T")[0]));
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        if (country != null)
                            pull.setText(getString(R.string.pull_me_up));
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        if (country == null) {
            tracker = TrackerService.createService(Tracker.ENDPOINT, getContext());
            Call<ResponseBody> call = tracker.downloadFileWithFixedUrl();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            Log.d("JEANPAUL", "onResponse: success");
                            String data = Objects.requireNonNull(response.body()).string();
                            JSONObject jsonObject = new JSONObject(data);
                            JSONArray jsonArray = jsonObject.getJSONArray("GlobalData");
                            JSONObject object = jsonArray.optJSONObject(0);

                            country = new ContaminatedCountry();

                            country.setDate(object.optString("Date"));
                            country.setInfection(object.optInt("Infection"));
                            country.setDeath(object.optInt("Deces"));
                            country.setHealing(object.optInt("Guerisons"));
                            country.setDeathRate(object.optDouble("TauxDeces"));
                            country.setHealingRate(object.optDouble("TauxGuerison"));
                            country.setInfectionRate(object.optDouble("TauxInfection"));

                            MainActivity.contaminatedOverview = country;
                            infected.setText(String.valueOf(country.getInfection()));
                            death.setText(String.valueOf(country.getDeath()));
                            recovered.setText(String.valueOf(country.getHealing()));
                            if (MainActivity.contaminatedCountries.size() > 0) {
                                count.setText(getString(R.string.count_country, String.valueOf(MainActivity.contaminatedCountries.size())));
                            }

                            List<ContaminatedCountry> countries = new ContaminatedCountry().populate(jsonArray);
                            MainActivity.jsonObject = jsonObject;
                            MainActivity.contaminatedCountries = countries;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e("JEANPAUL", "onFailure: " + t.getMessage(), t);
                }
            });
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            country = savedInstanceState.getParcelable("COUNTRY_DATA");
            heatMapCountries = savedInstanceState.getParcelableArrayList("HEATMAP");
            latLngList = savedInstanceState.getParcelableArrayList("LATLNGLIST");
        }
        context = getContext();
        tracker = TrackerService.createService(Tracker.GITHUB_ENDPOINT, getContext());
        bottomLinear = Objects.requireNonNull(getActivity()).findViewById(R.id.bottom_sheet);

        infected = getActivity().findViewById(R.id.tv_infected);
        death = getActivity().findViewById(R.id.tv_death);
        recovered = getActivity().findViewById(R.id.tv_recovered);
        count = getActivity().findViewById(R.id.tv_count);
        pull = getActivity().findViewById(R.id.tv_pull);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        initBottomSheet();
    }

    private void heatmap() {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date c = calendar.getTime();

        SimpleDateFormat yesterdayFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        yesterdayFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
        String finalYesterday = yesterdayFormat.format(c);

        tracker = TrackerService.createService(Tracker.GITHUB_ENDPOINT, getContext());
        tracker.dailyReports(finalYesterday);
        Call<ResponseBody> call = tracker.dailyReports(finalYesterday);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    String message = null;
                    try {
                        assert response.body() != null;
                        message = response.body().string();
                        //Log.d("JEANPAUL", "TAGE: "+message);

                        File outputDir = context.getCacheDir();
                        File outputFile = File.createTempFile(finalYesterday, "json", outputDir);
                        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                        writer.write(message);
                        writer.close();

                        CSVParser parser = new CSVParser(getContext());
                        List csv = parser.read(outputFile);
                        if (csv.size() > 0) {
                            String[] header_row = (String[]) csv.get(0);
                            latLngList = new ArrayList<>();
                            weightedLatLng = new ArrayList<>();
                            heatMapCountries = new ArrayList<>();
                            for (int j = 1; j < csv.size(); j++) {
                                String[] data = (String[]) csv.get(j);
                                //FIPS 0,Admin2 1,Province_State 2,Country_Region 3,Last_Update 4,Lat 5,Long_ 6,Confirmed 7,Deaths 8,Recovered 9,Active 10,Combined_Key
                                HeatMapCountry heatMapCountry = new HeatMapCountry();
                                heatMapCountry.setStateName(data[2]);
                                heatMapCountry.setCountryName(data[3]);
                                heatMapCountry.setLastUpdate(data[4]);
                                heatMapCountry.setConfirmed(Integer.parseInt(data[7]));
                                heatMapCountry.setDeaths(Integer.parseInt(data[8]));
                                heatMapCountry.setRecovered(Integer.parseInt(data[9]));
                                LatLng latLng = new LatLng(Double.parseDouble(data[5]), Double.parseDouble(data[6]));
                                heatMapCountry.setLatLng(latLng);
                                latLngList.add(latLng);
                                heatMapCountries.add(heatMapCountry);
                                int death = heatMapCountry.getDeaths();
                                if (death <= 25) {
                                    WeightedLatLng wl = new WeightedLatLng(latLng, 2d);
                                    weightedLatLng.add(wl);
                                } else if (death <= 50) {
                                    WeightedLatLng wl = new WeightedLatLng(latLng, 4d);
                                    weightedLatLng.add(wl);
                                } else if (death <= 150) {
                                    WeightedLatLng wl = new WeightedLatLng(latLng, 8d);
                                    weightedLatLng.add(wl);
                                } else if (death <= 200) {
                                    WeightedLatLng wl = new WeightedLatLng(latLng, 10d);
                                    weightedLatLng.add(wl);
                                } else if (death <= 250) {
                                    WeightedLatLng wl = new WeightedLatLng(latLng, 100d);
                                    weightedLatLng.add(wl);
                                }
                            }
                            Log.d("JEANPAUL", "latlnglist size: " + latLngList.size());
                            provider = new HeatmapTileProvider
                                    .Builder()
                                    .weightedData(weightedLatLng)
                                    .radius(45)
                                    .build();
                            if (provider != null) {
                                TileOverlay tileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("JEANPAUL", "onFailure:heatmap " + t.getMessage());
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setRotateGesturesEnabled(true);

        try {
            zoomOnCountry();
            GeoJsonLayer geoJsonLayer = new GeoJsonLayer(mMap, R.raw.france_region, context);
            GeoJsonPolygonStyle polygonStyle = geoJsonLayer.getDefaultPolygonStyle();
            polygonStyle.setStrokeWidth(1f);
            polygonStyle.setStrokeColor(Color.BLUE);

            geoJsonLayer.addLayerToMap();

        } catch (IOException e) {
            Log.d("JEANPAUL", "onMapReady: ", e);
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (heatMapCountries == null || latLngList == null)
            heatmap();
    }

    public void zoomOnCountry() throws IOException {
        Geocoder geocoder = new Geocoder(getContext());
        List<Address> addresses;
        addresses = geocoder.getFromLocationName(MainActivity.COUNTRY, 1);
        if (addresses.size() > 0) {
            double latitude = addresses.get(0).getLatitude();
            double longitude = addresses.get(0).getLongitude();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 1f));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}

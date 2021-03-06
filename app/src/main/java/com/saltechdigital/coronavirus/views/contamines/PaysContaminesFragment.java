package com.saltechdigital.coronavirus.views.contamines;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.saltechdigital.coronavirus.MainActivity;
import com.saltechdigital.coronavirus.R;
import com.saltechdigital.coronavirus.adapter.CountryListAdapter;
import com.saltechdigital.coronavirus.models.ContaminatedCountry;
import com.saltechdigital.coronavirus.network.Tracker;
import com.saltechdigital.coronavirus.network.TrackerService;
import com.saltechdigital.coronavirus.utils.Final;
import com.saltechdigital.coronavirus.utils.ReadAndWrite;
import com.saltechdigital.coronavirus.views.detail.DetailContaminatedActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaysContaminesFragment extends Fragment {

    private Tracker tracker;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CountryListAdapter adapter;
    private ContaminatedCountry cctryy;

    private InterstitialAd interstitialAd;

    private String data;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        tracker = TrackerService.createService(Tracker.ENDPOINT, getContext());
        assert activity != null;
        recyclerView = activity.findViewById(R.id.country_list);
        swipeRefreshLayout = activity.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(this::refresh);
        refresh();

        interstitialAd = new InterstitialAd(requireActivity());
        interstitialAd.setAdUnitId("ca-app-pub-6002058442934755/6779659716");
        interstitialAd.loadAd(new AdRequest.Builder().build());

        MobileAds.initialize(requireActivity(), initializationStatus -> {
        });
    }

    private void showInterstitial(ContaminatedCountry country) {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
            interstitialAd.loadAd(new AdRequest.Builder().build());
        } else {
            Intent intent = new Intent(getContext(), DetailContaminatedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra(CountryListAdapter.EXTRA_CONTAMINED, country);
            startActivity(intent);
        }

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                interstitialAd.loadAd(new AdRequest.Builder().build());
                Intent intent = new Intent(getContext(), DetailContaminatedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra(CountryListAdapter.EXTRA_CONTAMINED, country);
                startActivity(intent);
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }
        });
    }

    //add the contaminated country into the list
    private void databind() {
        if (MainActivity.contaminatedCountries.size() != 0) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new CountryListAdapter(MainActivity.contaminatedCountries, getContext(), country -> {
                cctryy = country;
                showInterstitial(country);
            });
            recyclerView.setAdapter(adapter);
        }
    }

    private void bindSaveData(){
        try {
            String data = ReadAndWrite.readFromFile(requireContext(),Final.FILENAME);
            if (!data.equals("")){
                JSONObject jsonObject = new JSONObject(data);
                JSONArray jsonArray = jsonObject.getJSONArray("PaysData");
                ReadAndWrite.writeToFile(data,requireContext(),Final.FILENAME);
                List<ContaminatedCountry> countries = ContaminatedCountry.populate(jsonArray);
                MainActivity.jsonObject = jsonObject;
                MainActivity.contaminatedCountries = countries;
                databind();
            }
        } catch (JSONException | IOException ex) {
            ex.printStackTrace();
        }
    }

    //action made where we refresh the country recyclerview
    private void refresh() {
        swipeRefreshLayout.setRefreshing(true);
        Call<ResponseBody> call = tracker.downloadFileWithFixedUrl();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        data = Objects.requireNonNull(response.body()).string();
                        JSONObject jsonObject = new JSONObject(data);
                        JSONArray jsonArray = jsonObject.getJSONArray("PaysData");
                        ReadAndWrite.writeToFile(data,requireContext(),Final.FILENAME);
                        List<ContaminatedCountry> countries = ContaminatedCountry.populate(jsonArray);
                        if (countries.size() == 0){
                            bindSaveData();
                        }else {
                            MainActivity.jsonObject = jsonObject;
                            MainActivity.contaminatedCountries = countries;
                            databind();
                        }
                    } catch (JSONException | IOException e) {
                        bindSaveData();
                        e.printStackTrace();
                    }
                    //refresh the swipe refresh layout
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    bindSaveData();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                bindSaveData();
                Toast.makeText(getContext(), "UNE ERREUR", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.country_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        //search algorithm
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //la classe de l'adaptateur implémente l'interface filterable donc nous pouvons filtrer la liste suivant les caracteres saisis dans le champs de recherche par l'utilisateur
                adapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return true;
            }
        });
        //super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter) {
            if (adapter != null)
                //la classe country implement l'interface comparable
                //qui nous permet de trier la liste suivant le nombre d'infections dans le pays
                adapter.sort();
        }

        return super.onOptionsItemSelected(item);
    }
}

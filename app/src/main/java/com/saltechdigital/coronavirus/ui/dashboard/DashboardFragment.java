package com.saltechdigital.coronavirus.ui.dashboard;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.saltechdigital.coronavirus.MainActivity;
import com.saltechdigital.coronavirus.R;
import com.saltechdigital.coronavirus.adapter.CountryListAdapter;
import com.saltechdigital.coronavirus.models.ContaminatedCountry;
import com.saltechdigital.coronavirus.network.Tracker;
import com.saltechdigital.coronavirus.network.TrackerService;

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

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private Tracker tracker;
    private TrackerService trackerService;
    private Activity activity;
    private Context context;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CountryListAdapter adapter;

    private String data;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
        context = getContext();
        tracker = TrackerService.createService(Tracker.ENDPOINT, getContext());
        assert activity != null;
        recyclerView = activity.findViewById(R.id.country_list);
        swipeRefreshLayout = activity.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        refresh();
    }

    private void databind() {
        if (MainActivity.contaminatedCountries.size() != 0) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new CountryListAdapter(MainActivity.contaminatedCountries, getContext());
            recyclerView.setAdapter(adapter);
        }
    }

    private void refresh() {
        swipeRefreshLayout.setRefreshing(true);
        Call<ResponseBody> call = tracker.downloadFileWithFixedUrl();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        data = Objects.requireNonNull(response.body()).string();
                        Log.d("JEANPAUL", "firsst data: "+data);
                        JSONObject jsonObject = new JSONObject(data);
                        JSONArray jsonArray = jsonObject.getJSONArray("PaysData");
                        List<ContaminatedCountry> countries = new ContaminatedCountry().populate(jsonArray);
                        MainActivity.jsonObject = jsonObject;
                        MainActivity.contaminatedCountries = countries;
                        databind();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    Log.d("JEANPAUL", "error: " + response.errorBody());
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("JEANPAUL", "onFailure: " + t.getMessage(), t);
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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("JEANPAUL", "onQueryTextSubmit: ");
                adapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d("JEANPAUL", "onQueryTextChange: ");
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
        switch (id) {
            case R.id.action_filter:
                if (adapter != null)
                adapter.sort();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
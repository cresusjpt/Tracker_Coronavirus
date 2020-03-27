package com.saltechdigital.coronavirus.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.saltechdigital.coronavirus.DetailContaminatedActivity;
import com.saltechdigital.coronavirus.R;
import com.saltechdigital.coronavirus.models.ContaminatedCountry;

import java.text.MessageFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CountryListAdapter extends RecyclerView.Adapter<CountryListAdapter.ViewHolder> implements Filterable {

    private List<ContaminatedCountry> contaminatedCountries;
    private List<ContaminatedCountry> contaminatedCountriesFiltered;
    private Context context;
    public static final String EXTRA_CONTAMINED = "EXTRA_CONTAMINED";
    public static final String EXTRA_JSON_ARRAY = "jsonarray";

    public CountryListAdapter(List<ContaminatedCountry> contaminatedCountries, Context context) {
        this.contaminatedCountries = contaminatedCountries;
        this.contaminatedCountriesFiltered = contaminatedCountries;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_country_list, parent, false);
        return new ViewHolder(view);
    }

    private void setAnimation(View toAnimate) {
        Animation animation = AnimationUtils.loadAnimation(toAnimate.getContext(), android.R.anim.fade_in);
        animation.setDuration(1000);
        toAnimate.startAnimation(animation);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContaminatedCountry country = contaminatedCountriesFiltered.get(position);

        //set background click ressoure
        holder.display(country);
        setAnimation(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return contaminatedCountriesFiltered.size();
    }

    public void sort() {
        Collections.sort(contaminatedCountriesFiltered);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    contaminatedCountriesFiltered = contaminatedCountries;
                } else {
                    List<ContaminatedCountry> filteredList = new ArrayList<>();
                    for (ContaminatedCountry contamined : contaminatedCountries) {

                        charString = Normalizer.normalize(charString.trim(), Normalizer.Form.NFD);
                        charString = charString.replaceAll("[^\\p{ASCII}]", "");

                        String name = Normalizer.normalize(contamined.getName().trim(), Normalizer.Form.NFD);
                        name = name.replaceAll("[^\\p{ASCII}]", "");

                        if (name.toLowerCase().contains(charString.toLowerCase())) {
                            Log.d("JEANPAUL", "search: " + charString.toLowerCase() + " name : " + name);
                            //Toast.makeText(context, "Find", Toast.LENGTH_SHORT).show();
                            filteredList.add(contamined);
                        }
                    }
                    contaminatedCountriesFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = contaminatedCountriesFiltered;
                filterResults.count = contaminatedCountriesFiltered.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                Log.d("JEANPAUL", "publishResults: ");
                contaminatedCountriesFiltered = (List<ContaminatedCountry>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private View itemView;
        private TextView name;
        private TextView date;
        private TextView infection;

        private ContaminatedCountry current;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            name = itemView.findViewById(R.id.name);
            date = itemView.findViewById(R.id.date);
            infection = itemView.findViewById(R.id.infection);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailContaminatedActivity.class);
                intent.putExtra(CountryListAdapter.EXTRA_CONTAMINED, current);
                context.startActivity(intent);
            });
        }

        void display(ContaminatedCountry country) {
            current = country;
            String spl = country.getDate().split("T")[0];
            name.setText(country.getName());
            date.setText(spl);
            infection.setText(MessageFormat.format("{0}", country.getInfection()));
        }
    }
}

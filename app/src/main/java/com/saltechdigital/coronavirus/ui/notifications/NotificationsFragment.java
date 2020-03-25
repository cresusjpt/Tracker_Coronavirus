package com.saltechdigital.coronavirus.ui.notifications;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.saltechdigital.coronavirus.R;
import com.saltechdigital.coronavirus.ui.webview.WebViewActivity;

import static com.saltechdigital.coronavirus.ui.webview.WebViewActivity.LOAD_URL;

public class NotificationsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    private void bindPreferenceToSummary(Preference preference) {
        preference.setOnPreferenceChangeListener(this);

        if (preference instanceof ListPreference) {
            onPreferenceChange(preference,
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), getString(R.string.hopkins_link))
            );
        }

        if (preference instanceof SwitchPreference) {
            onPreferenceChange(
                    preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), false)
            );
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        bindPreferenceToSummary(preference);
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {

        if (preference.getKey().equals(getString(R.string.change_language)) ){
            Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
            startActivity(intent);
        }

        if (preference.getKey().equals(getString(R.string.about))){
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.about))
                    .setMessage(getString(R.string.about_message))
                    .setPositiveButton(R.string.ok,null)
                    .create();
            dialog.show();
        }

        if (preference.getKey().equals(getString(R.string.corona_virus_definition))){
            Intent intent = new Intent(getContext(), WebViewActivity.class);
            intent.putExtra(LOAD_URL, getString(R.string.coronavirus_definition));
            startActivity(intent);
        }

        if (preference.getKey().equals(getString(R.string.coronavirus_prevention))){
            Intent intent = new Intent(getContext(), WebViewActivity.class);
            intent.putExtra(LOAD_URL, getString(R.string.coronavirus_prevention));
            startActivity(intent);
        }

        if (preference.getKey().equals(getString(R.string.data_source_open))){
            Intent intent = new Intent(getContext(), WebViewActivity.class);
            intent.putExtra(LOAD_URL, getString(R.string.data_source_open));
            startActivity(intent);
        }

        if (preference.getKey().equals(getString(R.string.hopkins_link))){
            Intent intent = new Intent(getContext(), WebViewActivity.class);
            intent.putExtra(LOAD_URL, getString(R.string.hopkins_link));
            startActivity(intent);
        }

        return super.onPreferenceTreeClick(preference);
    }
}

package br.com.ia369.bichinhovirtual;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        EditTextPreference decayIntervalPreference = (EditTextPreference) findPreference("decay_interval");
        decayIntervalPreference.setSummary(decayIntervalPreference.getText());

        EditTextPreference decayFactorPreference = (EditTextPreference) findPreference("decay_factor");
        decayFactorPreference.setSummary(decayFactorPreference.getText());

        EditTextPreference dispositionTimeStartPreference = (EditTextPreference) findPreference("disposition_time_start");
        dispositionTimeStartPreference.setSummary(dispositionTimeStartPreference.getText());

        EditTextPreference dispositionTimeEndPreference = (EditTextPreference) findPreference("disposition_time_end");
        dispositionTimeEndPreference.setSummary(dispositionTimeEndPreference.getText());

        ListPreference weatherConditionPreference = (ListPreference) findPreference("weather_condition_prefs");
        weatherConditionPreference.setSummary(weatherConditionPreference.getEntry());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);

        if(preference instanceof EditTextPreference) {
            preference.setSummary(((EditTextPreference) preference).getText());
        }

        if(preference instanceof ListPreference) {
            preference.setSummary(((ListPreference) preference).getEntry());
        }
    }
}

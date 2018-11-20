package br.com.ia369.bichinhovirtual;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import java.lang.ref.WeakReference;

import br.com.ia369.bichinhovirtual.model.Creature;
import br.com.ia369.bichinhovirtual.room.EmotionRepository;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_screen, rootKey);

        ListPreference personalityTypePreference = (ListPreference) findPreference(getString(R.string.personality_type_pref));
        personalityTypePreference.setSummary(personalityTypePreference.getEntry());

        EditTextPreference decayIntervalPreference = (EditTextPreference) findPreference(getString(R.string.decay_interval_pref));
        decayIntervalPreference.setSummary(decayIntervalPreference.getText());

        EditTextPreference decayFactorPreference = (EditTextPreference) findPreference(getString(R.string.decay_factor_pref));
        decayFactorPreference.setSummary(decayFactorPreference.getText());

        EditTextPreference dispositionTimeStartPreference = (EditTextPreference) findPreference(getString(R.string.disposition_time_start_pref));
        dispositionTimeStartPreference.setSummary(dispositionTimeStartPreference.getText());

        EditTextPreference dispositionTimeEndPreference = (EditTextPreference) findPreference(getString(R.string.disposition_time_end_pref));
        dispositionTimeEndPreference.setSummary(dispositionTimeEndPreference.getText());

        ListPreference weatherConditionPreference = (ListPreference) findPreference(getString(R.string.weather_condition_pref));
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

            if(key.equals(getString(R.string.personality_type_pref))) {
                String newPersonalityString = ((ListPreference) preference).getValue();
                new SaveNewPersonalityAsyncTask(this).execute(newPersonalityString);
            }
        }
    }

    static class SaveNewPersonalityAsyncTask extends AsyncTask<String, Void, Void> {

        private WeakReference<SettingsFragment> instance;

        SaveNewPersonalityAsyncTask(SettingsFragment settingsFragment) {
            this.instance = new WeakReference<>(settingsFragment);
        }

        @Override
        protected Void doInBackground(String... params) {
            String newPersonalityString = params[0];
            SettingsFragment settingsFragment = instance.get();

            if(settingsFragment.getActivity() != null) {
                Application application = settingsFragment.getActivity().getApplication();
                EmotionRepository repository = new EmotionRepository(application);


                int newPersonality = Integer.valueOf(newPersonalityString);
                Creature creature = repository.getCreature();
                creature.setPersonality(newPersonality);
                repository.updateCreature(creature);
            }
            return null;
        }
    }
}

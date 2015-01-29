package com.dev.aproschenko.arduinocontroller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    SwitchPreference collectDevicesPreference;
    SwitchPreference showDatetimeLabelPreference;

    private MainApplication getApp() { return (MainApplication) getApplication(); }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings);
        addPreferencesFromResource(R.xml.preferences);

        collectDevicesPreference = (SwitchPreference)getPreferenceScreen().findPreference("collect_devices");
        showDatetimeLabelPreference = (SwitchPreference)getPreferenceScreen().findPreference("show_datetime_label");
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Setup the initial values
        collectDevicesPreference.setChecked(getApp().collectDevicesStat);
        showDatetimeLabelPreference.setChecked(getApp().showDateTimeLabels);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals("collect_devices"))
        {
            getApp().collectDevicesStat = collectDevicesPreference.isChecked();
        }
        if (key.equals("show_datetime_label"))
        {
            getApp().showDateTimeLabels = showDatetimeLabelPreference.isChecked();
        }

        getApp().saveSettings();
    }
}

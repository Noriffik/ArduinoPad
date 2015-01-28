package com.dev.aproschenko.arduinocontroller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    CheckBoxPreference collectDevicesPreference;

    private MainApplication getApp() { return (MainApplication) getApplication(); }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings);
        addPreferencesFromResource(R.xml.preferences);

        collectDevicesPreference = (CheckBoxPreference)getPreferenceScreen().findPreference("collect_devices");
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Setup the initial values
        collectDevicesPreference.setChecked(getApp().collectDevicesStat);

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

        getApp().saveSettings();
    }
}

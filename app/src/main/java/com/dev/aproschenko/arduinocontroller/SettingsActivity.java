package com.dev.aproschenko.arduinocontroller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.util.Log;

import com.dev.aproschenko.arduinocontroller.colorpicker.ColorPickerPreference;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final String TAG = "SettingsActivity";
    private static final boolean D = true;

    SwitchPreference collectDevicesPreference;
    SwitchPreference showDatetimeLabelPreference;
    ColorPickerPreference bondedDeviceBgPreference;
    ColorPickerPreference sentMessagePreference;
    ColorPickerPreference receivedMessagePreference;

    private MainApplication getApp() { return (MainApplication) getApplication(); }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings);
        addPreferencesFromResource(R.xml.preferences);

        if (D) Log.d(TAG, "+++ ON CREATE +++");

        collectDevicesPreference = (SwitchPreference)getPreferenceScreen().findPreference("collect_devices");
        showDatetimeLabelPreference = (SwitchPreference)getPreferenceScreen().findPreference("show_datetime_label");
        bondedDeviceBgPreference = (ColorPickerPreference)getPreferenceScreen().findPreference("bonded_device_color");
        sentMessagePreference = (ColorPickerPreference)getPreferenceScreen().findPreference("terminal_self_message");
        receivedMessagePreference = (ColorPickerPreference)getPreferenceScreen().findPreference("terminal_their_message");
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Setup the initial values
        collectDevicesPreference.setChecked(getApp().collectDevicesStat);
        showDatetimeLabelPreference.setChecked(getApp().showDateTimeLabels);

        bondedDeviceBgPreference.setColor(getApp().bondedBgColor);
        bondedDeviceBgPreference.setSummary(ColorPickerPreference.convertToARGB(getApp().bondedBgColor));

        sentMessagePreference.setColor(getApp().sentMessageColor);
        sentMessagePreference.setSummary(ColorPickerPreference.convertToARGB(getApp().sentMessageColor));

        receivedMessagePreference.setColor(getApp().receivedMessageColor);
        receivedMessagePreference.setSummary(ColorPickerPreference.convertToARGB(getApp().receivedMessageColor));

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
        if (D) Log.d(TAG, "onSharedPreferenceChanged " + key);

        if (key.equals("collect_devices"))
        {
            getApp().collectDevicesStat = collectDevicesPreference.isChecked();
        }
        if (key.equals("show_datetime_label"))
        {
            getApp().showDateTimeLabels = showDatetimeLabelPreference.isChecked();
        }
        if (key.equals("bonded_device_color"))
        {
            getApp().bondedBgColor = bondedDeviceBgPreference.getColor();
            bondedDeviceBgPreference.setSummary(ColorPickerPreference.convertToARGB(getApp().bondedBgColor));
        }
        if (key.equals("terminal_self_message"))
        {
            getApp().sentMessageColor = sentMessagePreference.getColor();
            sentMessagePreference.setSummary(ColorPickerPreference.convertToARGB(getApp().sentMessageColor));
        }
        if (key.equals("terminal_their_message"))
        {
            getApp().receivedMessageColor = receivedMessagePreference.getColor();
            receivedMessagePreference.setSummary(ColorPickerPreference.convertToARGB(getApp().receivedMessageColor));
        }

        getApp().saveSettings();
    }
}

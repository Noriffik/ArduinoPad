package com.dev.aproschenko.arduinocontroller;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;

public class MainApplication extends Application
{
    private static final String TAG = "MainApplication";
    private static final boolean D = true;

    private BluetoothAdapter btAdapter;
    private DeviceConnector connector;

    private SettingsData settings;

    public static final String PREFS_FOLDER_NAME = "com.dev.aproschenko.arduinocontroller";
    public static final String PREFS_KEY_COMMAND = "command";
    public static final String PREFS_KEY_SORTTYPE = "sorttype";
    public static final String PREFS_KEY_COLLECT_DEVICES = "collectdevices";
    public static final String PREFS_KEY_SHOW_NO_SERVICES = "noservices";
    public static final String PREFS_KEY_SHOW_AUDIOVIDEO = "audiovideo";
    public static final String PREFS_KEY_SHOW_COMPUTER = "computer";
    public static final String PREFS_KEY_SHOW_HEALTH = "health";
    public static final String PREFS_KEY_SHOW_MISC = "misc";
    public static final String PREFS_KEY_SHOW_IMAGING = "imaging";
    public static final String PREFS_KEY_SHOW_NETWORKING = "networking";
    public static final String PREFS_KEY_SHOW_PERIPHERAL = "peripheral";
    public static final String PREFS_KEY_SHOW_PHONE = "phone";
    public static final String PREFS_KEY_SHOW_TOY = "toy";
    public static final String PREFS_KEY_SHOW_WEARABLE = "wearable";
    public static final String PREFS_KEY_SHOW_UNCATEGORIZED = "uncategorized";
    public static final String PREFS_KEY_SHOW_DATETIME_LABELS = "showdatetime";

    public boolean showNoServicesDevices = true;
    public boolean showAudioVideo = true;
    public boolean showComputer = true;
    public boolean showHealth = true;
    public boolean showMisc = true;
    public boolean showImaging = true;
    public boolean showNetworking = true;
    public boolean showPeripheral = true;
    public boolean showPhone = true;
    public boolean showToy = true;
    public boolean showWearable = true;
    public boolean showUncategorized = true;

    public boolean collectDevicesStat = false;
    public boolean showDateTimeLabels = true;

    private ArrayList<String> buttonCommands = new ArrayList<>();

    public enum SortType
    {
        SORT_BY_NAME,
        SORT_BY_TYPE,
        SORT_BY_BONDED_STATE
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        if (D) Log.d(TAG, "onCreate");

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        loadSettings();
    }

    public BluetoothAdapter getAdapter() { return btAdapter; }
    public DeviceConnector getConnector() { return connector; }
    public SettingsData getSettings() { return settings; }
    public ArrayList<String> getButtonCommands() { return buttonCommands; }
    Set<BluetoothDevice> getBondedDevices() { return btAdapter.getBondedDevices(); }

    public void cancelDiscovery()
    {
        if (btAdapter != null && btAdapter.isDiscovering())
        {
            btAdapter.cancelDiscovery();
        }
    }

    public void createConnector(String address)
    {
        connector = new DeviceConnector(address);
    }

    public void deleteConnector()
    {
        connector = null;
    }

    public int getConnectorState()
    {
        int state = DeviceConnector.STATE_NONE;
        if (connector != null)
        {
            state = connector.getState();
        }
        return state;
    }

    public void disconnect()
    {
        if (connector != null)
        {
            connector.stop();
        }
    }

    public void addHandler(Handler handler)
    {
        if (connector != null)
        {
            connector.getHandlers().add(handler);
        }
    }

    public void removeHandler(Handler handler)
    {
        if (connector != null)
        {
            connector.getHandlers().remove(handler);
        }
    }

    public void createSettings()
    {
        settings = new SettingsData();
    }

    public void loadSettings()
    {
        createSettings();
        restoreSettings();

        if (collectDevicesStat)
            deserializeDevices();
    }

    private String getPrefsFileName(boolean createFolder)
    {
        String androidFolder = Environment.getExternalStorageDirectory().getPath() + "/Android";
        String dataFolder = androidFolder + "/data";
        String prefsFolder = dataFolder + "/" + PREFS_FOLDER_NAME;
        String fileName = prefsFolder + "/devices.txt";

        if (!createFolder)
            return fileName;

        boolean success;
        File f = new File(androidFolder);
        if (!f.exists() || !f.isDirectory())
        {
            success = f.mkdir();
        }
        else
            success = true;

        if (!success)
            return null;

        f = new File(dataFolder);
        if (success && (!f.exists() || !f.isDirectory()))
        {
            success = f.mkdir();
        }

        if (!success)
            return null;

        f = new File(prefsFolder);
        if (success && (!f.exists() || !f.isDirectory()))
        {
            success = f.mkdir();
        }

        if (!success)
            return null;

        return fileName;
    }

    private void restoreSettings()
    {
        SharedPreferences settings = getSharedPreferences(PREFS_FOLDER_NAME, 0);

        String defaultCmd = DeviceControlActivity.NOT_SET_TEXT;
        for (int i = 0; i < DeviceControlActivity.BTN_COUNT; i++)
        {
            String cmd = settings.getString(PREFS_KEY_COMMAND + i, "");
            if (cmd.isEmpty())
                cmd = defaultCmd;

            buttonCommands.add(cmd);
        }

        String sortValue = settings.getString(PREFS_KEY_SORTTYPE, "SORT_BY_NAME");

        switch (sortValue)
        {
            case "SORT_BY_TYPE":
                this.settings.setSortType(SortType.SORT_BY_TYPE);
                break;
            case "SORT_BY_BONDED_STATE":
                this.settings.setSortType(SortType.SORT_BY_BONDED_STATE);
                break;
            default:
                this.settings.setSortType(SortType.SORT_BY_NAME);
                break;
        }

        showNoServicesDevices = settings.getBoolean(PREFS_KEY_SHOW_NO_SERVICES, true);
        showAudioVideo = settings.getBoolean(PREFS_KEY_SHOW_AUDIOVIDEO, true);
        showComputer = settings.getBoolean(PREFS_KEY_SHOW_COMPUTER, true);
        showHealth = settings.getBoolean(PREFS_KEY_SHOW_HEALTH, true);
        showMisc = settings.getBoolean(PREFS_KEY_SHOW_MISC, true);
        showImaging = settings.getBoolean(PREFS_KEY_SHOW_IMAGING, true);
        showNetworking = settings.getBoolean(PREFS_KEY_SHOW_NETWORKING, true);
        showPeripheral = settings.getBoolean(PREFS_KEY_SHOW_PERIPHERAL, true);
        showPhone = settings.getBoolean(PREFS_KEY_SHOW_PHONE, true);
        showToy = settings.getBoolean(PREFS_KEY_SHOW_TOY, true);
        showWearable = settings.getBoolean(PREFS_KEY_SHOW_WEARABLE, true);
        showUncategorized = settings.getBoolean(PREFS_KEY_SHOW_UNCATEGORIZED, true);

        collectDevicesStat = settings.getBoolean(PREFS_KEY_COLLECT_DEVICES, false);
        showDateTimeLabels = settings.getBoolean(PREFS_KEY_SHOW_DATETIME_LABELS, true);
    }

    public DeviceData getDeviceDataByAddress(String address)
    {
        for (DeviceData data : settings.getDevices())
        {
            if (data.getAddress().equals(address))
                return data;
        }

        return null;
    }

    public void saveSettings()
    {
        saveSettingsInternal();
    }

    private void saveSettingsInternal()
    {
        SharedPreferences settings = getSharedPreferences(PREFS_FOLDER_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        String sortValue = "SORT_BY_NAME";
        switch (this.settings.getSortType())
        {
            case SORT_BY_TYPE:
                sortValue = "SORT_BY_TYPE";
                break;
            case SORT_BY_BONDED_STATE:
                sortValue = "SORT_BY_BONDED_STATE";
                break;
            default: //name
                break;
        }
        editor.putString(PREFS_KEY_SORTTYPE, sortValue);

        editor.putBoolean(PREFS_KEY_SHOW_NO_SERVICES, showNoServicesDevices);
        editor.putBoolean(PREFS_KEY_SHOW_AUDIOVIDEO, showAudioVideo);
        editor.putBoolean(PREFS_KEY_SHOW_COMPUTER, showComputer);
        editor.putBoolean(PREFS_KEY_SHOW_HEALTH, showHealth);
        editor.putBoolean(PREFS_KEY_SHOW_MISC, showMisc);
        editor.putBoolean(PREFS_KEY_SHOW_IMAGING, showImaging);
        editor.putBoolean(PREFS_KEY_SHOW_NETWORKING, showNetworking);
        editor.putBoolean(PREFS_KEY_SHOW_PERIPHERAL, showPeripheral);
        editor.putBoolean(PREFS_KEY_SHOW_PHONE, showPhone);
        editor.putBoolean(PREFS_KEY_SHOW_TOY, showToy);
        editor.putBoolean(PREFS_KEY_SHOW_WEARABLE, showWearable);
        editor.putBoolean(PREFS_KEY_SHOW_UNCATEGORIZED, showUncategorized);

        editor.putBoolean(PREFS_KEY_COLLECT_DEVICES, collectDevicesStat);
        editor.putBoolean(PREFS_KEY_SHOW_DATETIME_LABELS, showDateTimeLabels);

        editor.commit();
    }

    private void deserializeDevices()
    {
        String jsonData = "";
        String fileName = getPrefsFileName(false);

        try
        {
            File myFile = new File(fileName);
            if (!myFile.exists())
            {
                if (D) Log.e(TAG, "deserializeDevices(): file " + fileName + " not found.");
                return;
            }

            if (D)
                Log.d(TAG, "deserializeDevices(): try load from " + fileName);

            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow;
            String aBuffer = "";
            while ((aDataRow = myReader.readLine()) != null)
            {
                aBuffer += aDataRow + "\n";
            }
            jsonData = aBuffer;
            myReader.close();

            if (D)
                Log.d(TAG, "deserializeDevices(): loaded from " + fileName);
        }
        catch (Exception e)
        {
            if (D) Log.e(TAG, "deserializeDevices() failed", e);
        }

        String emptyName = getResources().getString(R.string.empty_device_name);
        SettingsData tmp = DeviceSerializer.deserialize(jsonData);

        SortType st = SortType.SORT_BY_NAME;
        if (settings != null)
        {
            st = settings.getSortType();
        }

        if (tmp != null)
        {
            settings = tmp;
            settings.setSortType(st);

            for (DeviceData item : settings.getDevices())
            {
                String deviceName = item.getName();
                if (deviceName == null || deviceName.isEmpty())
                    item.setName(emptyName);
            }
        }
        else
        {
            createSettings();
            settings.setSortType(st);
        }
    }

    public void serializeDevices()
    {
        serializeDevicesInternal();
    }

    private void serializeDevicesInternal()
    {
        String jsonData = DeviceSerializer.serialize(settings);
        String fileName = getPrefsFileName(true);

        if (fileName == null || fileName.isEmpty())
        {
            if (D)
                Log.d(TAG, "serializeDevices(): unable to prepare prefs folder.");
            return;
        }

        if (D)
            Log.d(TAG, "serializeDevices(): try save to " + fileName);

        try
        {
            File myFile = new File(fileName);

            FileWriter filewriter = new FileWriter(myFile);
            BufferedWriter out = new BufferedWriter(filewriter);

            out.write(jsonData);

            out.close();
        }
        catch (Exception e)
        {
            if (D) Log.e(TAG, "serializeDevices() failed", e);
        }
    }
}

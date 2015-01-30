package com.dev.aproschenko.arduinocontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Map;

public class DeviceInfoActivity extends Activity
{
    private static final String TAG = "TerminalActivity";
    private static final boolean D = true;

    private MainApplication getApp() { return (MainApplication) getApplication(); }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (D) Log.d(TAG, "+++ ON CREATE +++");

        Intent intent = getIntent();
        String connectedDeviceName = intent.getStringExtra(MainActivity.DEVICE_NAME);
        String connectedDeviceAddress = intent.getStringExtra(MainActivity.DEVICE_ADDRESS);

        DeviceData deviceData = getApp().getDeviceDataByAddress(connectedDeviceAddress);

        setContentView(R.layout.device_info);
        setTitle(String.format("%s - %s", getResources().getString(R.string.device_info), connectedDeviceName));

        final TextView infoView = (TextView) findViewById(R.id.deviceServicesInfo);
        infoView.setText(getDeviceServices(deviceData));
    }

    private String getDeviceServices(DeviceData itemData)
    {
        String text = "";

        Map<String, String> services = BluetoothUtils.getDeviceServicesMap(itemData.getUuids());
        for (Map.Entry<String, String> entry : services.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            text += String.format("%s - %s\r\n", key, value);
        }

        return text;
    }
}

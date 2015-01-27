package com.dev.aproschenko.arduinocontroller;

import android.app.Application;
import android.util.Log;

public class MainApplication extends Application
{
    private static final String TAG = "MainApplication";
    private static final boolean D = true;

    @Override
    public void onCreate()
    {
        super.onCreate();
        if (D) Log.d(TAG, "onCreate");
    }
}

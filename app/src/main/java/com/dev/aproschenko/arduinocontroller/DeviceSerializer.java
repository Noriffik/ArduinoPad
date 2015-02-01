package com.dev.aproschenko.arduinocontroller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DeviceSerializer
{
    public static String serialize(SettingsData settings)
    {
        Gson gson = new Gson();
        String s = gson.toJson(settings);
        return s;
    }

    public static SettingsData deserialize(String s)
    {
        Gson gson = new Gson();
        SettingsData settings = gson.fromJson(s, SettingsData.class);
        return settings;
    }

    public static String serializeMacs(ArrayList<MacData> macs)
    {
        Gson gson = new Gson();
        Type listOfMacObject = new TypeToken<List<MacData>>(){}.getType();
        String s = gson.toJson(macs, listOfMacObject);
        return s;
    }

    public static ArrayList<MacData> deserializeMacs(String s)
    {
        Gson gson = new Gson();
        Type listOfMacObject = new TypeToken<List<MacData>>(){}.getType();
        ArrayList<MacData> macs = gson.fromJson(s, listOfMacObject);
        return macs;
    }
}

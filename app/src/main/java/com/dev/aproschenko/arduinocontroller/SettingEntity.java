package com.dev.aproschenko.arduinocontroller;

import com.dev.aproschenko.arduinocontroller.MainActivity.SortType;

import java.util.ArrayList;

public class SettingEntity
{
    private ArrayList<DeviceData> devices = new ArrayList<>();
    private ArrayList<DeviceCustomName> customNames = new ArrayList<>();

    private MainActivity.SortType sortType = SortType.SORT_BY_NAME;

    public ArrayList<DeviceData> getDevices()
    {
        return devices;
    }

    public ArrayList<DeviceCustomName> getCustomNames()
    {
        return customNames;
    }

    public void setSortType(MainActivity.SortType value)
    {
        sortType = value;
    }

    public MainActivity.SortType getSortType()
    {
        return sortType;
    }
}

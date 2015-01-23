package com.dev.aproschenko.arduinocontroller;

public class DeviceCustomName
{
    private String name = "";
    private String address = "";

    public String getName()
    {
        return name;
    }

    public void setName(String deviceName)
    {
        name = deviceName;
    }

    public void setAddress(String deviceAddress)
    {
        address = deviceAddress;
    }

    public String getAddress()
    {
        return address;
    }
}

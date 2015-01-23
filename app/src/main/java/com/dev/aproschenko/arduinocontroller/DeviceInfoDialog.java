package com.dev.aproschenko.arduinocontroller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

public class DeviceInfoDialog extends DialogFragment
{
    public static DeviceInfoDialog newInstance(ArrayList<InfoData> services, String deviceName)
    {
        DeviceInfoDialog frag = new DeviceInfoDialog();

        Bundle args = new Bundle();
        args.putSerializable("services", services);
        args.putString("name", deviceName);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.device_info, null);

        final ListView infoView = (ListView)textEntryView.findViewById(R.id.devicesInfoView);

        ArrayList<InfoData> services = (ArrayList<InfoData>)getArguments().getSerializable("services");
        DeviceInfoRowAdapter adapter = new DeviceInfoRowAdapter(getActivity(), services);
        infoView.setAdapter(adapter);

        String deviceName = getArguments().getString("name");

        final Dialog dlg = new AlertDialog.Builder(getActivity())
                .setTitle(String.format("%s - %s", getResources().getString(R.string.device_info), deviceName))
                .setView(textEntryView)
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        dialog.dismiss();
                    }
                }).create();

        return dlg;
    }
}

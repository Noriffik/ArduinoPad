package com.dev.aproschenko.arduinocontroller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

public class DeviceRenameDialog extends DialogFragment
{
    public static DeviceRenameDialog newInstance(String address, String name)
    {
        DeviceRenameDialog frag = new DeviceRenameDialog();

        Bundle args = new Bundle();
        args.putString("address", address);
        args.putString("name", name);

        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.device_rename_dialog, null);

        final EditText editor = (EditText)textEntryView.findViewById(R.id.device_name_edit);

        final String address = getArguments().getString("address");
        final String name = getArguments().getString("name");

        editor.setText(name.trim());

        final Dialog dlg = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.device_name)
                .setView(textEntryView)
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        MainActivity activity = (MainActivity) getActivity();
                        String nameToSet = editor.getText().toString().trim();

                        if (!nameToSet.equals(""))
                        {
                            activity.renameDeviceCallback(address, nameToSet);
                        }

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).create();

        editor.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        return dlg;
    }
}

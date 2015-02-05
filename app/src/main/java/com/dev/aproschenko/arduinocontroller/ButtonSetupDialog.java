package com.dev.aproschenko.arduinocontroller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class ButtonSetupDialog extends DialogFragment
{
    public static int shapeIds[] = {
            R.drawable.shape_enabled,
            R.drawable.shape01, R.drawable.shape02, R.drawable.shape03, R.drawable.shape04,
            R.drawable.shape05, R.drawable.shape06, R.drawable.shape07, R.drawable.shape08,
            R.drawable.shape09, R.drawable.shape10, R.drawable.shape11, R.drawable.shape12,
            R.drawable.shape13, R.drawable.shape14, R.drawable.shape15, R.drawable.shape16,
            R.drawable.shape17, R.drawable.shape18, R.drawable.shape19, R.drawable.shape20,
            R.drawable.shape21, R.drawable.shape22, R.drawable.shape23, R.drawable.shape24
    };

    private static int buttonIds[] = {
            R.id.button0,
            R.id.button1, R.id.button2, R.id.button3, R.id.button4,
            R.id.button5, R.id.button6, R.id.button7, R.id.button8,
            R.id.button9, R.id.button10, R.id.button11, R.id.button12,
            R.id.button13, R.id.button14, R.id.button15, R.id.button16,
            R.id.button17, R.id.button18, R.id.button19, R.id.button20,
            R.id.button21, R.id.button22, R.id.button23, R.id.button24
    };

    private int selectedShapeIndex = 0;

    public static ButtonSetupDialog newInstance(int btnId, String text, boolean fromTerminal)
    {
        ButtonSetupDialog frag = new ButtonSetupDialog();

        Bundle args = new Bundle();
        args.putInt("id", btnId);
        args.putString("text", text);
        args.putBoolean("terminal", fromTerminal);

        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final int id = getArguments().getInt("id");
        final String text = getArguments().getString("text");
        final boolean fromTerminal = getArguments().getBoolean("terminal");

        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(fromTerminal ? R.layout.button_setup_dialog_terminal : R.layout.button_setup_dialog, null);

        final EditText editor = (EditText)textEntryView.findViewById(R.id.button_message_edit);

        if (text.trim().toLowerCase().equals(DeviceControlActivity.NOT_SET_TEXT.toLowerCase()))
            editor.setText("");
        else
            editor.setText(text.trim());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.button_message);
        builder.setView(textEntryView);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String textToSet = editor.getText().toString().trim();
                if (textToSet.equals(""))
                    textToSet = DeviceControlActivity.NOT_SET_TEXT;

                if (fromTerminal)
                {
                    TerminalActivity activity = (TerminalActivity) getActivity();
                    activity.updateButtonText(id, textToSet);
                }
                else
                {
                    DeviceControlActivity activity = (DeviceControlActivity) getActivity();
                    activity.updateButtonTextAndColor(id, textToSet, selectedShapeIndex);
                }

                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                dialog.dismiss();
            }
        });

        if (!fromTerminal)
        {
            for (int i = 0; i < buttonIds.length; i++)
            {
                Button btn = (Button) textEntryView.findViewById(buttonIds[i]);
                btn.setTag(i);
                btn.setOnClickListener(btnColorClick);
            }
        }

        final Dialog dlg = builder.create();

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

    private View.OnClickListener btnColorClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Button btn = (Button)v;
            selectedShapeIndex = (int)btn.getTag();
        }
    };
}

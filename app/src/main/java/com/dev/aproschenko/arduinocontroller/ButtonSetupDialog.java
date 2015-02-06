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
import android.widget.ImageButton;

import java.util.ArrayList;

public class ButtonSetupDialog extends DialogFragment
{
    public static final String PARAM_BUTTON_INDEX = "index";
    public static final String PARAM_BUTTON_FROM_TERMINAL = "terminal";

    public static int shapeIds[] = {
            R.drawable.shape_enabled, R.drawable.shape01, R.drawable.shape02, R.drawable.shape03,
            R.drawable.shape04, R.drawable.shape05, R.drawable.shape06, R.drawable.shape07,
            R.drawable.shape08, R.drawable.shape09, R.drawable.shape10, R.drawable.shape11,
            R.drawable.shape12, R.drawable.shape13, R.drawable.shape14, R.drawable.shape15,
            R.drawable.shape16, R.drawable.shape17, R.drawable.shape18, R.drawable.shape19,
            R.drawable.shape20, R.drawable.shape21, R.drawable.shape22, R.drawable.shape23
    };

    private static int colorButtonIds[] = {
            R.id.button_enabled, R.id.button1, R.id.button2, R.id.button3,
            R.id.button4, R.id.button5, R.id.button6, R.id.button7,
            R.id.button8, R.id.button9, R.id.button10, R.id.button11,
            R.id.button12, R.id.button13, R.id.button14, R.id.button15,
            R.id.button16, R.id.button17, R.id.button18, R.id.button19,
            R.id.button20, R.id.button21, R.id.button22, R.id.button23
    };

    private ArrayList<ImageButton> colorButtons = new ArrayList<>();

    private int selectedShapeIndex = 0;

    public static ButtonSetupDialog newInstance(int buttonIndex, boolean fromTerminal)
    {
        ButtonSetupDialog frag = new ButtonSetupDialog();

        Bundle args = new Bundle();
        args.putInt(PARAM_BUTTON_INDEX, buttonIndex);
        args.putBoolean(PARAM_BUTTON_FROM_TERMINAL, fromTerminal);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final int buttonIndex = getArguments().getInt(PARAM_BUTTON_INDEX);
        final boolean fromTerminal = getArguments().getBoolean(PARAM_BUTTON_FROM_TERMINAL);

        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(fromTerminal ? R.layout.button_setup_dialog_terminal : R.layout.button_setup_dialog, null);

        final EditText editor = (EditText)textEntryView.findViewById(R.id.button_message_edit);

        String command;
        if (fromTerminal)
        {
            TerminalActivity activity = (TerminalActivity) getActivity();
            command = activity.getApp().getTerminalCommands().get(buttonIndex);
        }
        else
        {
            DeviceControlActivity activity = (DeviceControlActivity) getActivity();
            command = activity.getApp().getButtonCommands().get(buttonIndex);
            int buttonShapeIndex = activity.getApp().getButtonShapes().get(buttonIndex);

            for (int i = 0; i < colorButtonIds.length; i++)
            {
                ImageButton btn = (ImageButton) textEntryView.findViewById(colorButtonIds[i]);
                btn.setTag(i);
                btn.setOnClickListener(btnColorClick);
                btn.setImageResource(i == buttonShapeIndex ? R.drawable.checkmark : 0);

                colorButtons.add(btn);
            }
        }

        if (command.trim().toLowerCase().equals(DeviceControlActivity.NOT_SET_TEXT.toLowerCase()))
            editor.setText("");
        else
            editor.setText(command.trim());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.button_message);
        builder.setView(textEntryView);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String commandToSet = editor.getText().toString().trim();
                if (commandToSet.equals(""))
                    commandToSet = DeviceControlActivity.NOT_SET_TEXT;

                if (fromTerminal)
                {
                    TerminalActivity activity = (TerminalActivity) getActivity();
                    activity.updateButtonCommand(buttonIndex, commandToSet);
                }
                else
                {
                    DeviceControlActivity activity = (DeviceControlActivity) getActivity();
                    activity.updateButtonCommandAndColor(buttonIndex, commandToSet, selectedShapeIndex);
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
            ImageButton btn = (ImageButton)v;
            selectedShapeIndex = (int)btn.getTag();
            setButtonSelected(selectedShapeIndex);
        }
    };

    private void setButtonSelected(int buttonIndex)
    {
        for (int i = 0; i < colorButtons.size(); i++)
        {
            ImageButton btn = colorButtons.get(i);
            btn.setImageResource(i == buttonIndex ? R.drawable.checkmark : 0);
        }
    }
}

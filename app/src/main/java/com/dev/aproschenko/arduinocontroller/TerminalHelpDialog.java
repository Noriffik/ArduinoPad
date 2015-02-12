package com.dev.aproschenko.arduinocontroller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class TerminalHelpDialog extends DialogFragment
{
    public static TerminalHelpDialog newInstance()
    {
        TerminalHelpDialog frag = new TerminalHelpDialog();

        Bundle args = new Bundle();
        frag.setArguments(args);

        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.terminal_help_layout, null);

        String atInfo = MacConverter.readEmbeddedTextFile(R.raw.atcommands, getActivity());
        final TextView helpView = (TextView) textEntryView.findViewById(R.id.helpView);
        helpView.setText(Html.fromHtml(atInfo), TextView.BufferType.SPANNABLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.menu_help);
        builder.setView(textEntryView);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}

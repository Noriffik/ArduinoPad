package com.dev.aproschenko.arduinocontroller;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TerminalActivity extends Activity
{
    private static final String TAG = "TerminalActivity";
    private static final boolean D = true;

    private Button buttonSend;
    private EditText commandBox;
    private TextView commandsView;

    private String commandsCache = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (D) Log.d(TAG, "+++ ON CREATE +++");

        setContentView(R.layout.terminal_layout);

        buttonSend = (Button)findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(buttonSendClick);

        commandBox = (EditText)findViewById(R.id.commandBox);
        commandsView = (TextView)findViewById(R.id.commandsView);

        DeviceConnector connector = DeviceControlActivity.getConnector();
        if (connector != null && connector.getState() == DeviceConnector.STATE_CONNECTED)
        {
            connector.setTerminalHandler(mHandler);
        }
    }

    private View.OnClickListener buttonSendClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            sendCommand();
        }
    };

    private void appendCommand(String command)
    {
        commandsCache += String.format("<font color='green'>ME&gt; </font>%s<br/>", command);
        commandsView.setText(Html.fromHtml(commandsCache), TextView.BufferType.SPANNABLE);
    }

    private void sendCommand()
    {
        DeviceConnector connector = DeviceControlActivity.getConnector();
        if (connector != null && connector.getState() == DeviceConnector.STATE_CONNECTED)
        {
            String command = commandBox.getText().toString().trim();
            connector.write(command);
            appendCommand(command);

            commandBox.setText("");
        }
    }

    private void receiveCommand(String command)
    {
        commandsCache += String.format("<font color='red'>THEY&gt; </font>%s<br/>", command);
        commandsView.setText(Html.fromHtml(commandsCache), TextView.BufferType.SPANNABLE);
    }

    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case DeviceControlActivity.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    receiveCommand(readMessage);
                    break;
            }
        }
    };}

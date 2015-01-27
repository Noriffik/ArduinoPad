package com.dev.aproschenko.arduinocontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TerminalActivity extends Activity
{
    private static final String TAG = "TerminalActivity";
    private static final boolean D = true;

    private String connectedDeviceName;

    private Button buttonSend;
    private EditText commandBox;
    private TextView commandsView;

    private String commandsCache = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (D) Log.d(TAG, "+++ ON CREATE +++");

        Intent intent = getIntent();
        connectedDeviceName = intent.getStringExtra(MainActivity.DEVICE_NAME);

        setContentView(R.layout.terminal_layout);
        setTitle(R.string.bluetooth_terminal);

        buttonSend = (Button)findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(buttonSendClick);

        commandBox = (EditText)findViewById(R.id.commandBox);
        commandsView = (TextView)findViewById(R.id.commandsView);

        commandsView.setMovementMethod(new ScrollingMovementMethod());
        commandsView.setTextIsSelectable(true);

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

    private void appendCommand(String command, int messageType)
    {
        String color = messageType == Messages.MESSAGE_READ ? "blue" : "red";
        String author = messageType == Messages.MESSAGE_READ ? connectedDeviceName : "ME";

        commandsCache = String.format("<font color='%s'>%s&gt; </font>%s<br/>", color, author, command) + commandsCache;

        commandsView.setText(Html.fromHtml(commandsCache), TextView.BufferType.SPANNABLE);
    }

    private void sendCommand()
    {
        DeviceConnector connector = DeviceControlActivity.getConnector();
        if (connector != null && connector.getState() == DeviceConnector.STATE_CONNECTED)
        {
            String command = commandBox.getText().toString().trim();
            connector.write(command);
            appendCommand(command, Messages.MESSAGE_WRITE);

            commandBox.setText("");
        }
    }

    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case Messages.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    appendCommand(readMessage, Messages.MESSAGE_READ);
                    break;
            }
        }
    };}

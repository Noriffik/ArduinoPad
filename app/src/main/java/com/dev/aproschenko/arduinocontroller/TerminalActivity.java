package com.dev.aproschenko.arduinocontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;

public class TerminalActivity extends Activity
{
    private static final String TAG = "TerminalActivity";
    private static final boolean D = true;

    private String connectedDeviceName;

    private Button buttonSend;
    private EditText commandBox;
    private TextView commandsView;

    private String commandsCache = "";

    private MainApplication getApp() { return (MainApplication) getApplication(); }

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

        getApp().addHandler(mHandler);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (D) Log.d(TAG, "--- ON DESTROY ---");
        getApp().removeHandler(mHandler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        if (D) Log.d(TAG, "onCreateOptionsMenu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_terminal, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_close:
                closeTerminal();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void closeTerminal()
    {
        finish();
    }

    private View.OnClickListener buttonSendClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            sendCommand();
        }
    };

    private String getFormattedDateTime()
    {
        Date date = new Date();
        return String.format("%s %s", DateFormat.getDateFormat(getApplicationContext()).format(date), DateFormat.format("H:mm:ss", date));
    }

    private void appendCommand(String command, int messageType)
    {
        String color = messageType == Messages.MESSAGE_READ ? "blue" : "red";
        String author = messageType == Messages.MESSAGE_READ ? connectedDeviceName : "ME";
        String date = getFormattedDateTime();

        commandsCache = String.format("%s <font color='%s'>%s&gt; </font>%s<br/>", date, color, author, command) + commandsCache;

        commandsView.setText(Html.fromHtml(commandsCache), TextView.BufferType.SPANNABLE);
    }

    private void sendCommand()
    {
        if (getApp().getConnectorState() == DeviceConnector.STATE_CONNECTED)
        {
            String command = commandBox.getText().toString().trim();
            if (!command.equals(""))
            {
                getApp().getConnector().write(command);
                appendCommand(command, Messages.MESSAGE_WRITE);
                commandBox.setText("");
            }
            else
            {
                if(commandBox.requestFocus())
                {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
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

                case Messages.MESSAGE_CONNECTION_LOST:
                    buttonSend.setEnabled(false);
                    commandBox.setEnabled(false);
                    appendCommand(String.format(getResources().getString(R.string.connection_was_lost), connectedDeviceName), Messages.MESSAGE_READ);
                    break;
            }
        }
    };}

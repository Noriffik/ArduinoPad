package com.dev.aproschenko.arduinocontroller;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TerminalActivity extends Activity
{
    private static final String TAG = "TerminalActivity";
    private static final boolean D = true;

    private Button buttonSend;
    private EditText commandBox;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (D) Log.d(TAG, "+++ ON CREATE +++");

        setContentView(R.layout.terminal_layout);

        buttonSend = (Button)findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(buttonSendClick);

        commandBox = (EditText)findViewById(R.id.commandBox);
    }

    private View.OnClickListener buttonSendClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            sendCommand();
        }
    };

    private void sendCommand()
    {
        DeviceConnector connector = DeviceControlActivity.getConnector();
        if (connector != null && connector.getState() == DeviceConnector.STATE_CONNECTED)
        {
            String command = commandBox.getText().toString().trim();
            connector.write(command);
        }
    }
}

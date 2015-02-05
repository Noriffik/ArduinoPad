package com.dev.aproschenko.arduinocontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class DeviceControlActivity extends Activity implements SensorEventListener
{
    private static final String TAG = "DeviceControlActivity";
    private static final boolean D = true;

    private boolean isSettingsMode = false;
    private String connectedDeviceName;
    private String connectedDeviceAddress;

    public static final String NOT_SET_TEXT = "-";

    private Button buttonOpenTerminal;
    private OrientationView forwardView;
    private OrientationView backwardView;
    private OrientationView leftView;
    private OrientationView rightView;

    private Integer buttonIds[] = {
            R.id.button1, R.id.button2, R.id.button3, R.id.button4,
            R.id.button5, R.id.button6, R.id.button7, R.id.button8,
            R.id.button9, R.id.button10, R.id.button11, R.id.button12,
            R.id.button13, R.id.button14, R.id.button15, R.id.button16
    };
    private ArrayList<Button> padButtons = new ArrayList<>();
    private ArrayList<Rect> padButtonsRect = new ArrayList<>();
    public static final int BTN_COUNT = 16;

    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;

    private char forwardPrefix = 'A';                               // A B C D E F
    private char backwardPrefix = (char)((int)forwardPrefix + 6);   // G H I J K L
    private char leftPrefix = (char)((int)backwardPrefix + 6);      // M N O P Q R
    private char rightPrefix = (char)((int)leftPrefix + 6);         // S T U V W X
    private char lastForwardCommand = forwardPrefix;
    private char lastLeftCommand = leftPrefix;

    final Context context = this;

    private MainApplication getApp() { return (MainApplication) getApplication(); }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (D) Log.d(TAG, "+++ ON CREATE +++");

        setContentView(R.layout.device_controller);

        Intent intent = getIntent();
        connectedDeviceName = intent.getStringExtra(MainActivity.DEVICE_NAME);
        connectedDeviceAddress = intent.getStringExtra(MainActivity.DEVICE_ADDRESS);

        String title = String.format(getResources().getString(R.string.is_not_connected), connectedDeviceName);
        setTitle(title);

        setupControls();
        setupSensors();
        enableControls();

        if (getApp().getConnector() != null)
        {
            if (D) Log.d(TAG, "+++ ON CREATE +++, connector state " + getApp().getConnector().getState());
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (D) Log.d(TAG, "++ ON START ++");

        if (!getApp().getAdapter().isEnabled())
        {
            if (D) Log.d(TAG, "++ ON START BT disabled ++");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, MainActivity.REQUEST_ENABLE_BT);
        }
        else // Otherwise, setup the chat session
        {
            if (D) Log.d(TAG, "++ ON START BT enabled ++");
            if (getApp().getConnector() == null)
            {
                if (D) Log.d(TAG, "++ ON START setupConnector() ++");
                setupConnector();
            }
            else
            {
                if (D) Log.d(TAG, "++ ON START ++, connector state " + getApp().getConnector().getState());
            }
        }
    }

    @Override
    public synchronized void onResume()
    {
        super.onResume();
        if (D) Log.d(TAG, "+ ON RESUME +");

        sensorManager.registerListener(this, rotationVectorSensor, 10000);

        if (getApp().getConnector() != null)
        {
            if (D) Log.d(TAG, "+ ON RESUME +, connector state " + getApp().getConnector().getState());
        }
    }

    private Rect getCoordinatesFromView(View v)
    {
        int t = v.getTop();
        int l = v.getLeft();
        int b = v.getBottom();
        int r = v.getRight();
        return new Rect(l, t, r, b);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        int marginSize = (int)getResources().getDimension(R.dimen.button_margin);
        int buttonHeight = padButtonsRect.get(1).height();
        int buttonWidth = padButtonsRect.get(1).width();

        forwardView.setMarginSize(marginSize);
        backwardView.setMarginSize(marginSize);
        leftView.setMarginSize(marginSize);
        rightView.setMarginSize(marginSize);

        Rect r2 = padButtonsRect.get(1);
        Rect r3 = padButtonsRect.get(2);
        Rect r6 = padButtonsRect.get(5);
        Rect r10 = padButtonsRect.get(9);
        Rect r14 = padButtonsRect.get(13);

        ViewGroup.LayoutParams params = forwardView.getLayoutParams();
        params.height = buttonHeight * 2 + marginSize * 2;
        params.width = r3.left - r2.right;
        forwardView.setLayoutParams(params);
        forwardView.setX(r2.right);
        forwardView.setY(marginSize);
        forwardView.setGravity(Gravity.RIGHT);
        forwardView.setOrientation(LinearLayout.VERTICAL);

        params = backwardView.getLayoutParams();
        params.height = buttonHeight * 2 + marginSize * 2;
        params.width = r3.left - r2.right;
        backwardView.setLayoutParams(params);
        backwardView.setX(r2.right);
        backwardView.setY(2 * r10.top + buttonHeight * 2 + marginSize * 3);
        backwardView.setGravity(Gravity.LEFT);
        backwardView.setOrientation(LinearLayout.VERTICAL);

        params = leftView.getLayoutParams();
        params.height = 2 * r10.top;
        params.width = buttonWidth * 2 + marginSize * 2;
        leftView.setLayoutParams(params);
        leftView.setX(marginSize);
        leftView.setY(buttonHeight * 2 + marginSize * 3);
        leftView.setGravity(Gravity.RIGHT);
        leftView.setOrientation(LinearLayout.HORIZONTAL);

        params = rightView.getLayoutParams();
        params.height = 2 * r10.top;
        params.width = buttonWidth * 2 + marginSize * 2;
        rightView.setLayoutParams(params);
        rightView.setX(buttonWidth * 2 + 2 * r10.top + marginSize * 3);
        rightView.setY(buttonHeight * 2 + marginSize * 3);
        rightView.setGravity(Gravity.LEFT);
        rightView.setOrientation(LinearLayout.HORIZONTAL);

        logDimensions(forwardView, "forward");
        logDimensions(backwardView, "back");
        logDimensions(rightView, "right");
        logDimensions(leftView, "left");
    }

    private void logDimensions(View view, String name)
    {
        if (D)
            Log.d(TAG, String.format("%s - x=%f y=%f w=%d h=%d", name, view.getX(), view.getY(), view.getWidth(), view.getHeight()));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (D) Log.d(TAG, "--- ON DESTROY ---");

        if (getApp().getConnector() != null)
        {
            if (D) Log.d(TAG, "--- ON DESTROY ---, connector state " + getApp().getConnector().getState());
            getApp().getConnector().stop();
            getApp().deleteConnector();
        }
    }

    @Override
    public synchronized void onPause()
    {
        super.onPause();
        if (D) Log.d(TAG, "- ON PAUSE -");

        sensorManager.unregisterListener(this);

        if (getApp().getConnector() != null)
        {
            if (D) Log.d(TAG, "- ON PAUSE -, connector state " + getApp().getConnector().getState());
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (D) Log.d(TAG, "-- ON STOP --");

        if (getApp().getConnector() != null)
        {
            if (D) Log.d(TAG, "-- ON STOP --, connector state " + getApp().getConnector().getState());
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (D) Log.d(TAG, "onActivityResult " + resultCode);

        if (requestCode == MainActivity.REQUEST_ENABLE_BT)
        {
            if (resultCode == RESULT_OK)
            {
                setupConnector();
            }
            else
            {
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setupConnector()
    {
        if (D) Log.d(TAG, "setupConnector");

        if (getApp().getConnector() != null)
        {
            if (D) Log.d(TAG, "setupConnector connector.stop(), state " + getApp().getConnector().getState());
            getApp().getConnector().stop();
            getApp().deleteConnector();
        }

        BluetoothDevice connectedDevice = getApp().getAdapter().getRemoteDevice(connectedDeviceAddress);

        getApp().createConnector(connectedDevice.getAddress());
        getApp().getConnector().getHandlers().add(mHandler);
        getApp().getConnector().connect();
    }

    private void setupControls()
    {
        for (int i = 0; i < buttonIds.length; i++)
        {
            int id = buttonIds[i];
            Button btn = (Button)findViewById(id);

            String cmd = getApp().getButtonCommands().get(i);
            btn.setText(cmd);

            int shape = getApp().getButtonShapes().get(i);
            btn.setBackgroundResource(ButtonSetupDialog.shapeIds[shape]);

            btn.setOnClickListener(btnControlClick);

            padButtons.add(btn);
        }

        checkButtonLabels();

        buttonOpenTerminal = (Button)findViewById(R.id.buttonTerminal);
        buttonOpenTerminal.setOnClickListener(buttonOpenTerminalClick);
        buttonOpenTerminal.setVisibility(View.INVISIBLE);

        forwardView = (OrientationView)findViewById(R.id.forwardView);
        backwardView = (OrientationView)findViewById(R.id.backwardView);
        leftView = (OrientationView)findViewById(R.id.leftView);
        rightView = (OrientationView)findViewById(R.id.rightView);

        for (final Button button : padButtons)
        {
            button.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            padButtonsRect.add(getCoordinatesFromView(button));
                            button.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
        }
    }

    private void setupSensors()
    {
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }

    private void checkButtonLabels()
    {
        for (Button btn : padButtons)
        {
            boolean enabled = !btn.getText().equals(NOT_SET_TEXT) || isSettingsMode;
            btn.setEnabled(enabled);
            btn.setBackgroundResource(enabled ? R.drawable.shape_enabled : R.drawable.shape_disabled);
        }
    }

    private void sendCommand(String command)
    {
        if (getApp().getConnector() != null && !isSettingsMode)
        {
            getApp().getConnector().write(command);
        }
    }

    private OnClickListener btnControlClick = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Button btn = (Button)v;
            if (isSettingsMode)
            {
                showButtonActionDialog(btn);
            }
            else
            {
                sendCommand(btn.getText().toString());
            }
        }
    };

    private void showButtonActionDialog(Button btn)
    {
        ButtonSetupDialog newFragment = ButtonSetupDialog.newInstance(btn.getId(), btn.getText().toString(), false);
        newFragment.show(getFragmentManager(), "ButtonSetupDialog");
    }

    private void saveSettingsHandler()
    {
        setSettingsMode(false);
        checkButtonLabels();

        for (int i = 0; i < padButtons.size(); i++)
        {
            Button btn = padButtons.get(i);
            String cmd = btn.getText().toString();

            getApp().getButtonCommands().set(i, cmd);
        }

        savePreferences();
    }

    private OnClickListener btnSaveSettingsClick = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            saveSettingsHandler();
        }
    };

    private OnClickListener buttonOpenTerminalClick = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            openTerminal();
        }
    };

    private void openTerminal()
    {
        Intent intent = new Intent(context, TerminalActivity.class);
        intent.putExtra(MainActivity.DEVICE_NAME, connectedDeviceName);
        startActivity(intent);
    }

    private void savePreferences()
    {
        SharedPreferences settings = getSharedPreferences(MainApplication.PREFS_FOLDER_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        for (int i = 0; i < padButtons.size(); i++)
        {
            String key = MainApplication.PREFS_KEY_COMMAND + i;
            String cmd = getApp().getButtonCommands().get(i);
            editor.putString(key, cmd);

            key = MainApplication.PREFS_KEY_SHAPE + i;
            int shape = getApp().getButtonShapes().get(i);
            editor.putInt(key, shape);

            if (D)
                Log.d(TAG, "save cmd key " + key + ":" + cmd + " shape:" + shape);
        }

        editor.apply();
    }

    @Override
    public void onBackPressed()
    {
        if (isSettingsMode)
        {
            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
            saveSettingsHandler();
            return;
        }

        finish();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        if (D) Log.d(TAG, "onPrepareOptionsMenu");

        int state = getApp().getConnectorState();

        menu.findItem(R.id.menu_settings).setEnabled(!isSettingsMode && (state == DeviceConnector.STATE_CONNECTED));
        menu.findItem(R.id.menu_connect).setEnabled(!isSettingsMode && (state == DeviceConnector.STATE_NONE));
        menu.findItem(R.id.menu_disconnect).setEnabled(!isSettingsMode && (state != DeviceConnector.STATE_NONE));
        menu.findItem(R.id.menu_open_terminal).setEnabled(!isSettingsMode && (state == DeviceConnector.STATE_CONNECTED));

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        if (D) Log.d(TAG, "onCreateOptionsMenu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_device, menu);

        return true;
    }

    private void setSettingsMode(boolean mode)
    {
        isSettingsMode = mode;
        buttonOpenTerminal.setVisibility(isSettingsMode ? View.INVISIBLE : (getApp().getConnectorState() == DeviceConnector.STATE_CONNECTED) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_settings:
                setSettingsMode(true);
                checkButtonLabels();
                return true;

            case R.id.menu_connect:
                setupConnector();
                return true;

            case R.id.menu_open_terminal:
                openTerminal();
                return true;

            case R.id.menu_disconnect:
                getApp().disconnect();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateButtonTextAndColor(int btnId, String text, int shapeIndex)
    {
        int i = 0;
        for (Button btn : padButtons)
        {
            if (btn.getId() == btnId)
            {
                getApp().getButtonShapes().set(i, shapeIndex);
                btn.setBackgroundResource(ButtonSetupDialog.shapeIds[shapeIndex]);
                btn.setText(text);
                return;
            }
            i++;
        }
    }

    private void enableControls()
    {
        boolean enable = getApp().getConnectorState() == DeviceConnector.STATE_CONNECTED;

        buttonOpenTerminal.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);

        for (Button btn : padButtons)
        {
            if (!enable)
            {
                btn.setBackgroundResource(R.drawable.shape_disabled);
                btn.setEnabled(false);
                continue;
            }
            boolean enabled = !btn.getText().equals(NOT_SET_TEXT) || isSettingsMode;
            btn.setEnabled(enabled);
            btn.setBackgroundResource(enabled ? R.drawable.shape_enabled : R.drawable.shape_disabled);
        }
    }

    private void appendIncomingMessage(String message)
    {
        if(D)
            Log.d(TAG, "Message received: " + message);
    }

    private void appendOutgoingMessage(String message)
    {
        if(D)
            Log.d(TAG, "Message sent: " + message);
    }

    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION)
        {
            int forward = (int)event.values[1];
            int left = (int)event.values[2];

            if (forward > OrientationView.MAX_VALUE)
                forward = OrientationView.MAX_VALUE;
            if (forward < -OrientationView.MAX_VALUE)
                forward = -OrientationView.MAX_VALUE;

            if (left > OrientationView.MAX_VALUE)
                left = OrientationView.MAX_VALUE;
            if (left < -OrientationView.MAX_VALUE)
                left = -OrientationView.MAX_VALUE;

            if (forward < 3 && forward > -3)
                forward = 0;
            if (left < 3 && left > -3)
                left = 0;

            char forwardCommand;
            if (forward > 0)
            {
                forwardCommand = (char)(forwardPrefix + forward / 3);
            }
            else
            {
                forwardCommand = (char)(backwardPrefix - forward / 3);
            }
            char leftCommand;
            if (left > 0)
            {
                leftCommand = (char)(leftPrefix + left / 3);
            }
            else
            {
                leftCommand = (char)(rightPrefix - left / 3);
            }

            if (lastForwardCommand != forwardCommand)
            {
                forwardView.setValue(forward);
                backwardView.setValue(forward);
                lastForwardCommand = forwardCommand;
                sendCommand(String.valueOf(lastForwardCommand));
            }
            if (lastLeftCommand != leftCommand)
            {
                leftView.setValue(left);
                rightView.setValue(left);
                lastLeftCommand = leftCommand;
                sendCommand(String.valueOf(lastLeftCommand));
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case Messages.MESSAGE_STATE_CHANGE:

                    if(D) Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    String messageText = "";

                    switch (msg.arg1)
                    {
                        case DeviceConnector.STATE_CONNECTED:
                            messageText = String.format(getResources().getString(R.string.connected_to), connectedDeviceName);
                            setTitle(messageText);
                            break;
                        case DeviceConnector.STATE_CONNECTING:
                            messageText = String.format(getResources().getString(R.string.connecting_to), connectedDeviceName);
                            setTitle(messageText);
                            break;
                        case DeviceConnector.STATE_NONE:
                            messageText = String.format(getResources().getString(R.string.is_not_connected), connectedDeviceName);
                            setTitle(messageText);
                            break;
                    }

                    enableControls();
                    invalidateOptionsMenu();
                    appendOutgoingMessage(messageText);
                    break;

                case Messages.MESSAGE_DEVICE_NAME:
                    Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.successfully_connected_to), connectedDeviceName), Toast.LENGTH_SHORT).show();
                    break;

                case Messages.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    appendOutgoingMessage(writeMessage);
                    break;

                case Messages.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    appendIncomingMessage(readMessage);
                    break;

                case Messages.MESSAGE_CONNECTION_FAILED:
                    Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.unable_connect_to), connectedDeviceName), Toast.LENGTH_SHORT).show();
                    break;

                case Messages.MESSAGE_CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.connection_was_lost), connectedDeviceName), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}

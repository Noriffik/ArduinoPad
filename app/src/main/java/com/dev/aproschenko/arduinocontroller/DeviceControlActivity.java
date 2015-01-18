package com.dev.aproschenko.arduinocontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class DeviceControlActivity extends Activity implements SensorEventListener
{
    private static final String TAG = "DeviceControlActivity";
    private static final boolean D = true;

    private boolean isSettingsMode = false;
    private String connectedDeviceName;
    private String connectedDeviceAddress;

    private DeviceConnector connector;
    private BluetoothAdapter btAdapter;

    // Message types sent from the DeviceConnector Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String TOAST = "toast";
    public static final String NOT_SET_TEXT = "-";

    private ArrayList<Button> padButtons = new ArrayList<Button>();
    private ArrayList<Integer> padButtonsIds = new ArrayList<Integer>();
    private ArrayList<Rect> padButtonsRect = new ArrayList<Rect>();

    private Button buttonSaveSettings;
    private OrientationView forwardView;
    private OrientationView backwardView;
    private OrientationView leftView;
    private OrientationView rightView;

    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;

    private char forwardPrefix = 'A';
    private char backwardPrefix = (char)((int)forwardPrefix + 6);
    private char leftPrefix = (char)((int)backwardPrefix + 6);
    private char rightPrefix = (char)((int)leftPrefix + 6);
    private char lastForwardCommand = forwardPrefix;
    private char lastLeftCommand = leftPrefix;

    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		setContentView(R.layout.device_controller);

		Intent intent = getIntent();
		connectedDeviceName = intent.getStringExtra(MainActivity.DEVICE_NAME);
		connectedDeviceAddress = intent.getStringExtra(MainActivity.DEVICE_ADDRESS);

		Integer ids[] = {
                R.id.button1, R.id.button2, R.id.button3, R.id.button4,
				R.id.button5, R.id.button6, R.id.button7, R.id.button8,
                R.id.button9, R.id.button10, R.id.button11, R.id.button12,
                R.id.button13, R.id.button14, R.id.button15, R.id.button16
        };

		padButtonsIds.addAll(Arrays.asList(ids));

		setTitle(connectedDeviceName + " not connected");

		btAdapter = BluetoothAdapter.getDefaultAdapter();

		setupControls();
        setupSensors();
		enableControls();

		if (connector != null)
        {
        	if (D) Log.d(TAG, "+++ ON CREATE +++, connector state " + connector.getState());
        }
	}

    @Override
    public void onStart()
    {
        super.onStart();
        if (D) Log.d(TAG, "++ ON START ++");

        if (!btAdapter.isEnabled())
        {
        	if (D) Log.d(TAG, "++ ON START BT disabled ++");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, MainActivity.REQUEST_ENABLE_BT);
        }
        else // Otherwise, setup the chat session
        {
            if (D) Log.d(TAG, "++ ON START BT enabled ++");
            if (connector == null)
            {
            	if (D) Log.d(TAG, "++ ON START setupConnector() ++");
            	setupConnector();
            }
            else
            {
            	if (D) Log.d(TAG, "++ ON START ++, connector state " + connector.getState());
            }
        }
    }

    @Override
    public synchronized void onResume()
    {
        super.onResume();
        if (D) Log.d(TAG, "+ ON RESUME +");

        sensorManager.registerListener(this, rotationVectorSensor, 10000);

        if (connector != null)
        {
        	if (D) Log.d(TAG, "+ ON RESUME +, connector state " + connector.getState());
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

        int buttonHeight = padButtonsRect.get(1).height();
        int buttonWidth = padButtonsRect.get(1).width();

        Rect r2 = padButtonsRect.get(1);
        Rect r3 = padButtonsRect.get(2);
        Rect r6 = padButtonsRect.get(5);
        Rect r10 = padButtonsRect.get(9);
        Rect r14 = padButtonsRect.get(13);

        ViewGroup.LayoutParams params = forwardView.getLayoutParams();
        params.height = buttonHeight * 2;
        params.width = r3.left - r2.right;
        forwardView.setLayoutParams(params);
        forwardView.setX(r2.right);
        forwardView.setGravity(Gravity.RIGHT);
        forwardView.setOrientation(LinearLayout.VERTICAL);

        params = backwardView.getLayoutParams();
        params.height = buttonHeight * 2;
        params.width = r3.left - r2.right;
        backwardView.setLayoutParams(params);
        backwardView.setX(r2.right);
        backwardView.setY(2 * r10.top + buttonHeight * 2);
        backwardView.setGravity(Gravity.LEFT);
        backwardView.setOrientation(LinearLayout.VERTICAL);

        params = leftView.getLayoutParams();
        params.height = 2 * r10.top;
        params.width = buttonWidth * 2;
        leftView.setLayoutParams(params);
        leftView.setY(buttonHeight * 2);
        leftView.setGravity(Gravity.RIGHT);
        leftView.setOrientation(LinearLayout.HORIZONTAL);

        params = rightView.getLayoutParams();
        params.height = 2 * r10.top;
        params.width = buttonWidth * 2;
        rightView.setLayoutParams(params);
        rightView.setX(buttonWidth * 2 + 2 * r10.top);
        rightView.setY(buttonHeight * 2);
        rightView.setGravity(Gravity.LEFT);
        rightView.setOrientation(LinearLayout.HORIZONTAL);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (D) Log.d(TAG, "--- ON DESTROY ---");

        if (connector != null)
        {
        	if (D) Log.d(TAG, "--- ON DESTROY ---, connector state " + connector.getState());
        	connector.stop();
        	connector = null;
        }
    }

    @Override
    public synchronized void onPause()
    {
        super.onPause();
        if (D) Log.d(TAG, "- ON PAUSE -");

        sensorManager.unregisterListener(this);

        if (connector != null)
        {
        	if (D) Log.d(TAG, "- ON PAUSE -, connector state " + connector.getState());
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (D) Log.d(TAG, "-- ON STOP --");

        if (connector != null)
        {
        	if (D) Log.d(TAG, "-- ON STOP --, connector state " + connector.getState());
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

		if (connector != null)
		{
			if (D) Log.d(TAG, "setupConnector connector.stop(), state " + connector.getState());
			connector.stop();
			connector = null;
		}

		BluetoothDevice connectedDevice = btAdapter.getRemoteDevice(connectedDeviceAddress);
		String emptyName = getResources().getString(R.string.empty_device_name);

		DeviceData data = new DeviceData(connectedDevice, emptyName);

		connector = new DeviceConnector(this, data, mHandler);
		connector.connect();
	}

    private void setupControls()
    {
        for (int id : padButtonsIds)
        {
            Button btn = (Button)findViewById(id);
            btn.setOnClickListener(btnControlClick);
            padButtons.add(btn);
        }

        for (int i = 0; i < 12; i++)
        {
            Button btn = padButtons.get(i);
            String cmd = MainActivity.buttonCommands.get(i);
            btn.setText(cmd);
        }

        checkButtonLabels();

        buttonSaveSettings = (Button)findViewById(R.id.buttonSaveSettings);

        buttonSaveSettings.setOnClickListener(btnSaveSettingsClick);
        buttonSaveSettings.setVisibility(View.INVISIBLE);

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
   			btn.setEnabled(!btn.getText().equals(NOT_SET_TEXT) || isSettingsMode);
    	}
    }

    private void sendCommand(String command)
    {
        if (connector != null)
        {
            connector.write(command);
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
    	ButtonSetupDialog newFragment = ButtonSetupDialog.newInstance(btn.getId(), btn.getText().toString());
        newFragment.show(getFragmentManager(), "ButtonSetupDialog");
    }

    private OnClickListener btnSaveSettingsClick = new OnClickListener()
    {
    	@Override
    	public void onClick(View v)
    	{
    		setSettingsMode(false);
    		checkButtonLabels();

    		for (int i = 0; i < 12; i++)
    		{
    			Button btn = padButtons.get(i);
    			String cmd = btn.getText().toString();

    			MainActivity.buttonCommands.set(i, cmd);
    		}

    		saveSettings();
    	}
    };

	private void saveSettings()
	{
		SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		for (int i = 0; i < 12; i++)
		{
			String cmd = MainActivity.buttonCommands.get(i);
			editor.putString(MainActivity.PREFS_KEY_COMMAND + i, cmd);
		}

		editor.commit();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		if (D) Log.d(TAG, "onPrepareOptionsMenu");

		int state = DeviceConnector.STATE_NONE;
		if (connector != null)
		{
			state = connector.getState();
		}

		menu.findItem(R.id.menu_settings).setEnabled(!isSettingsMode && (state == DeviceConnector.STATE_CONNECTED));
		menu.findItem(R.id.menu_connect).setEnabled(!isSettingsMode && (state == DeviceConnector.STATE_NONE));
		menu.findItem(R.id.menu_disconnect).setEnabled(!isSettingsMode && (state != DeviceConnector.STATE_NONE));

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
        buttonSaveSettings.setVisibility(isSettingsMode ? View.VISIBLE : View.INVISIBLE);
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

            case R.id.menu_disconnect:
        		if (connector != null)
        		{
        			connector.stop();
        		}
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateButtonText(int btnId, String text)
    {
    	for (Button btn : padButtons)
    	{
    		if (btn.getId() == btnId)
    		{
    			btn.setText(text);
    			return;
    		}
    	}
    }

	private void enableControls()
	{
		boolean enable = false;
		if (connector != null)
		{
			enable = connector.getState() == DeviceConnector.STATE_CONNECTED;
		}

    	for (Button btn : padButtons)
    	{
    		if (!enable)
    		{
    			btn.setEnabled(false);
    			continue;
    		}
   			btn.setEnabled(!btn.getText().equals(NOT_SET_TEXT) || isSettingsMode);
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
                if (D)
                    Log.d("commands to send", "f=" + forwardCommand);
            }
            if (lastLeftCommand != leftCommand)
            {
                leftView.setValue(left);
                rightView.setValue(left);
                lastLeftCommand = leftCommand;
                sendCommand(String.valueOf(lastLeftCommand));
                if (D)
                    Log.d("commands to send", "l=" + leftCommand);
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
	            case MESSAGE_STATE_CHANGE:

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

	            case MESSAGE_DEVICE_NAME:
	                Toast.makeText(getApplicationContext(), "Successfully connected to " + connectedDeviceName,
	                		Toast.LENGTH_SHORT).show();
	                break;

	            case MESSAGE_WRITE:
	                byte[] writeBuf = (byte[]) msg.obj;
	                // construct a string from the buffer
	                String writeMessage = new String(writeBuf);
	                appendOutgoingMessage(writeMessage);
	                break;

	            case MESSAGE_READ:
	                byte[] readBuf = (byte[]) msg.obj;
	                String readMessage = new String(readBuf, 0, msg.arg1);
	                appendIncomingMessage(readMessage);
	                break;

	            case MESSAGE_TOAST:
	                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
	                        Toast.LENGTH_SHORT).show();
	                break;
            }
        }
    };
}

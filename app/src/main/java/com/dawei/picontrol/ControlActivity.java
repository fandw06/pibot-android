package com.dawei.picontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dawei.picontrol.comm.BluetoothService;
import com.dawei.picontrol.comm.Constants;
import com.dawei.picontrol.fragment.ChassisControl;
import com.dawei.picontrol.fragment.DrawingArmControl;
import com.dawei.picontrol.fragment.MotionArmControl;
import com.dawei.picontrol.fragment.SeekbarArmControl;
import com.dawei.picontrol.module.Arm;
import com.dawei.picontrol.module.Chassis;

import java.lang.ref.WeakReference;
import java.util.Arrays;

public class ControlActivity extends FragmentActivity {

    private static final String TAG = "ControlActivity";
    private static final String PI_ADDRESS = "B8:27:EB:AC:39:04";
    private static final int REQUEST_ENABLE_BT = 3;

    // Bluetooth
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mControlService = null;

    // connect
    private Button bConnect;
    private TextView tStatus;

    // raw data transfer for debug
    private LinearLayout debugView;
    private Button bSend;
    private EditText tMessage;
    private TextView tReceived;

    // modules
    private Arm arm;
    private Chassis chassis;

    // fragments
    SeekbarArmControl seekbarArmControl;
    MotionArmControl motionArmControl;
    DrawingArmControl drawingArmControl;
    ChassisControl chassisControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        Log.d(TAG, "Completed content view set.");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else if (mControlService == null) {
            mControlService = new BluetoothService(this, mHandler);
        }
        initializeModules();
        initializeComponents();
        initializeFragments();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mControlService != null) {
            mControlService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mControlService != null) {
            if (mControlService.getState() == BluetoothService.STATE_NONE) {
                mControlService.start();
            }
        }
    }

    private void initializeComponents() {
        // connect
        bConnect = this.findViewById(R.id.btn_connect);
        bConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mControlService == null)
                    Toast.makeText(ControlActivity.this, "Cannot connect", Toast.LENGTH_SHORT).show();
                else if (mControlService.getState() == BluetoothService.STATE_NONE ||
                        mControlService.getState() == BluetoothService.STATE_LISTEN)
                    connectDevice(PI_ADDRESS);
                else
                    Toast.makeText(ControlActivity.this, "Already connected", Toast.LENGTH_SHORT).show();
            }
        });
        tStatus = this.findViewById(R.id.txt_status);

        // raw data transfer
        bSend = this.findViewById(R.id.btn_send);
        bSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = tMessage.getText().toString();
                sendMessage(message);
                tMessage.setText("");
            }
        });
        tMessage = this.findViewById(R.id.txt_message);
        tMessage.clearFocus();
        tReceived = this.findViewById(R.id.txt_receive);
        debugView = this.findViewById(R.id.debug_view);
        debugView.setVisibility(View.GONE);
    }

    private void initializeFragments() {
        seekbarArmControl = (SeekbarArmControl) getFragmentManager().findFragmentById(R.id.fragment_seekbar_control);
        seekbarArmControl.setArm(arm);
        motionArmControl = (MotionArmControl) getFragmentManager().findFragmentById(R.id.fragment_motion_control);
        SensorManager manager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if (manager != null)
            motionArmControl.setSensorManager(manager);
        else
            Log.d(TAG, "Cannot find sensor manager!");
        motionArmControl.setArm(arm);
        drawingArmControl = (DrawingArmControl) getFragmentManager().findFragmentById(R.id.fragment_drawing_control);
        drawingArmControl.setArm(arm);
        chassisControl = (ChassisControl) getFragmentManager().findFragmentById(R.id.fragment_chassis_control);
        chassisControl.setChassis(chassis);
    }

    private void initializeModules() {
        if (mControlService != null) {
            arm = new Arm(mControlService);
            chassis = new Chassis(mControlService);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check if already connected
        if (mControlService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, "Not connected!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.length() > 0) {
            byte[] data = message.getBytes();
            mControlService.write(data);
        }
    }

    private void setStatus(String s) {
        tStatus.setText(s);
    }

    private static class CustomHandler extends Handler {
        private final WeakReference<ControlActivity> mActivity;

        CustomHandler(ControlActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ControlActivity a = mActivity.get();
            if (a != null) {
                switch (msg.what) {
                    case Constants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothService.STATE_CONNECTED:
                                a.setStatus("Status: connected");
                                break;
                            case BluetoothService.STATE_CONNECTING:
                                a.setStatus("Status: connecting...");
                                break;
                            case BluetoothService.STATE_LISTEN:
                            case BluetoothService.STATE_NONE:
                                a.setStatus("Status: unconnected");
                                break;
                        }
                        break;
                    case Constants.MESSAGE_WRITE:
                        byte[] writeBuf = (byte[]) msg.obj;
                        // construct a string from the buffer
                        String writeMessage = new String(writeBuf);
                        break;
                    case Constants.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        a.tReceived.setText("Received: " + readMessage);
                        break;
                    case Constants.MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                        Toast.makeText(a, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final CustomHandler mHandler = new CustomHandler(this);

    private void connectDevice(String address) {
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        ParcelUuid supportedUuids[] = device.getUuids();
        System.out.println("Supported UUIDS: " + Arrays.toString(supportedUuids));
        Log.d(TAG, "Connecting to the device...");
        Log.d(TAG, "Name: " + device.getName());
        // Attempt to connect to the device
        mControlService.connect(device);
    }

    public Arm getArm() {
        return this.arm;
    }
}

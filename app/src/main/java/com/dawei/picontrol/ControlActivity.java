package com.dawei.picontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.dawei.picontrol.comm.BluetoothService;
import com.dawei.picontrol.comm.Constants;
import com.dawei.picontrol.module.Arm;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class ControlActivity extends AppCompatActivity
        implements SensorEventListener {

    private static final String TAG = "ControlActivity";
    private static final String PI_ADDRESS = "B8:27:EB:AC:39:04";
    private static final int REQUEST_ENABLE_BT = 3;

    // Bluetooth
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mControlService = null;

    // Motion sensor
    private Sensor mAccel;
    private SensorManager mSensorManager;
    private float lastAccel[];
    private static final float G = 9.80f;
    private static final float THR = 10.00f;

    // connect
    private Button bConnect;
    private TextView tStatus;
    // seekbar control
    private SeekBar sbBottom;
    private SeekBar sbRight;
    private SeekBar sbLeft;
    private Switch swClaw;
    // motion control
    private Switch swMotionControl;
    // drawing control
    private Switch swDrawingControl;
    private RadioGroup rgDrawing;
    private RadioButton rbRealtime;
    private RadioButton rbOnce;
    private DrawingView drawingView;
    private Button bPaint;
    private Button bUndo;
    private Button bRedo;
    private Button bClear;
    private Button bSave;
    private Button bFire;
    // raw data transfer
    private Button bSend;
    private EditText tMessage;
    private TextView tReceived;

    // modules
    private Arm arm;

    public boolean isDrawing;
    public boolean enabledDrawing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "on created.");
        setContentView(R.layout.activity_control);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }
        initializeComponents();

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "on started.");
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else if (mControlService == null) {
            mControlService = new BluetoothService(this, mHandler);
        }
        initializeMotionSensor();
        initializeModules();
        isDrawing = false;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "on destroy.");
        super.onDestroy();
        if (mControlService != null) {
            mControlService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "on resumed.");
        if (mControlService != null) {
            if (mControlService.getState() == BluetoothService.STATE_NONE) {
                mControlService.start();
            }
        }
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);
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
        // seekbar control
        sbBottom = this.findViewById(R.id.seek_bottom);
        sbBottom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mControlService != null
                        && mControlService.getState() == BluetoothService.STATE_CONNECTED)
                    arm.controlBottom(i * 1.80f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbRight = this.findViewById(R.id.seek_right);
        sbRight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mControlService != null
                        && mControlService.getState() == BluetoothService.STATE_CONNECTED)
                    arm.controlRight(i * 1.80f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbLeft = this.findViewById(R.id.seek_left);
        sbLeft.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mControlService != null
                        && mControlService.getState() == BluetoothService.STATE_CONNECTED)
                    arm.controlLeft(- 38 + i * 1.28f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        swClaw = this.findViewById(R.id.sw_claw);
        swClaw.setChecked(false);
        swClaw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mControlService != null
                        && mControlService.getState() == BluetoothService.STATE_CONNECTED) {
                    if (swClaw.isChecked())
                        arm.grab();
                    else
                        arm.loosen();
                }
            }
        });
        // motion control
        swMotionControl = this.findViewById(R.id.sw_motion);
        swMotionControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mControlService != null
                        && mControlService.getState() == BluetoothService.STATE_CONNECTED) {
                    if (swMotionControl.isChecked()) {
                        Log.d(TAG, "Motion control is turned on.");
                        swDrawingControl.setChecked(false);
                    }
                    else
                        Log.d(TAG, "Motion control is turned off.");
                }
            }
        });
        // drawing control
        swDrawingControl = this.findViewById(R.id.sw_drawing);
        swDrawingControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (swDrawingControl.isChecked()) {
                    Log.d(TAG, "Drawing control is turned on.");
                    swMotionControl.setChecked(false);
                    rbRealtime.setEnabled(false);
                    rbOnce.setEnabled(false);
                    enabledDrawing = true;
                    if (rbRealtime.isChecked()) {
                        bPaint.setEnabled(true);
                        bUndo.setEnabled(false);
                        bRedo.setEnabled(false);
                        bClear.setEnabled(true);
                        bSave.setEnabled(true);
                        bFire.setEnabled(false);
                        isDrawing = true;
                    } else {
                        bPaint.setEnabled(true);
                        bUndo.setEnabled(true);
                        bRedo.setEnabled(true);
                        bClear.setEnabled(true);
                        bSave.setEnabled(true);
                        bFire.setEnabled(true);
                        isDrawing = false;
                    }
                    drawingView.clear();
                    drawingView.setDefaultPaint();
                }
                else {
                    Log.d(TAG, "Drawing control is turned off.");
                    rbRealtime.setEnabled(true);
                    rbOnce.setEnabled(true);
                    bPaint.setEnabled(false);
                    bUndo.setEnabled(false);
                    bRedo.setEnabled(false);
                    bClear.setEnabled(false);
                    bSave.setEnabled(false);
                    bFire.setEnabled(false);
                    isDrawing = false;
                    enabledDrawing = false;
                    drawingView.clear();
                }
            }
        });
        rgDrawing = this.findViewById(R.id.rg_drawing);
        rbRealtime = this.findViewById(R.id.rb_realtime);
        rbRealtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        rbOnce = this.findViewById(R.id.rb_once);
        rbOnce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        drawingView = this.findViewById(R.id.drawing_pad);
        drawingView.setHostActivity(this);
        bPaint = this.findViewById(R.id.btn_paint);
        bPaint.setEnabled(false);
        bPaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        bUndo = this.findViewById(R.id.btn_undo);
        bUndo.setEnabled(false);
        bUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.undo();
            }
        });
        bRedo = this.findViewById(R.id.btn_redo);
        bRedo.setEnabled(false);
        bRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.redo();
            }
        });
        bClear = this.findViewById(R.id.btn_clear);
        bClear.setEnabled(false);
        bClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.clear();
            }
        });
        bSave = this.findViewById(R.id.btn_save);
        bSave.setEnabled(false);
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.saveImage();
            }
        });
        bFire = this.findViewById(R.id.btn_fire);
        bFire.setEnabled(false);
        bFire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bPaint.setEnabled(false);
                bUndo.setEnabled(false);
                bRedo.setEnabled(false);
                bClear.setEnabled(false);
                bSave.setEnabled(false);
                bFire.setEnabled(false);
                if (mControlService != null
                        && mControlService.getState() == BluetoothService.STATE_CONNECTED) {
                    Stack<DrawingView.Trail> trails = drawingView.getTrails();
                    int index = 0;

                    for (DrawingView.Trail t : trails) {
                        Path path = t.path;
                        PathMeasure pm = new PathMeasure(path, false);
                        float length = pm.getLength();
                        float distance = 0f;
                        float[] points = new float[2];
                        Log.d(TAG, "Trail " + (index++));

                        arm.reset();
                        try {
                            Thread.sleep(Arm.DRAW_INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        while (distance < length) {
                            // get point from the path
                            pm.getPosTan(distance, points, null);
                            Log.d(TAG, String.format("Measured points: (%f, %f)", transferX(points[0]), transferY(points[1])));
                            distance = distance + Arm.DRAW_SPEED;
                            arm.setPosition(transferX(points[0]), transferY(points[1]), Arm.DRAW_Z);
                            try {
                                Thread.sleep(Arm.DRAW_INTERVAL);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (distance < length) {
                            pm.getPosTan(length, points, null);
                            Log.d(TAG, String.format("Measured points: (%f, %f)", transferX(points[0]), transferY(points[1])));
                            arm.setPosition(transferX(points[0]), transferY(points[1]), Arm.DRAW_Z);
                        }
                    }
                }

                bPaint.setEnabled(true);
                bUndo.setEnabled(true);
                bRedo.setEnabled(true);
                bClear.setEnabled(true);
                bSave.setEnabled(true);
                bFire.setEnabled(true);
            }
        });

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
    }

    private void initializeMotionSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        lastAccel = new float[]{0, 0, G};
        for (Sensor s : deviceSensors)
            Log.d(TAG, "Sensor: " + s.getName());

        if (mAccel == null){
            // Use the accelerometer.
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
        }
        Log.d(TAG, "Accel name: " + mAccel.getName());
        Log.d(TAG, "Accel vendor: " + mAccel.getVendor());
        Log.d(TAG, "Accel range: " + mAccel.getMaximumRange());
        Log.d(TAG, "Accel min delay: " + mAccel.getMinDelay());
        Log.d(TAG, "Accel resolution: " + mAccel.getResolution());
        Log.d(TAG, "Accel power: " + mAccel.getPower());
    }

    private void initializeModules() {
        if (mControlService != null)
            arm = new Arm(mControlService);
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

    /**
     * Transfer image coordinates X to arm coordinates.
     * @param x Image coordinate x.
     * @return
     */
    public float transferX(float x) {
        return Arm.RADIUS - x * 2 * Arm.RADIUS / drawingView.getImgWidth();
    }

    /**
     * Transfer image coordinates X to arm coordinates.
     * @param y Image coordinate y.
     * @return
     */
    public float transferY(float y) {
        return y * 2 * Arm.RADIUS / drawingView.getImgWidth();
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus("Status: connected");
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus("Status: connecting...");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus("Status: unconnected");
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
                    tReceived.setText("Received: " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(ControlActivity.this, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (swMotionControl.isChecked()
                && mControlService != null
                && mControlService.getState() == BluetoothService.STATE_CONNECTED) {
            float val[] = sensorEvent.values;
            if (val[0] - lastAccel[0] < -THR) {
                Log.d(TAG, "Go right.");
                sbBottom.setProgress((int)Math.min(100, sbBottom.getProgress() - (val[0] - lastAccel[0])/2));
            } else if (val[0] - lastAccel[0] > THR) {
                Log.d(TAG, "Go left.");
                sbBottom.setProgress((int)Math.max(0, sbBottom.getProgress() - (val[0] - lastAccel[0])/2));
            }
            if (val[1] - lastAccel[1] > THR) {
                Log.d(TAG, "Go down.");
                sbLeft.setProgress((int)Math.min(100, sbLeft.getProgress() + (val[1] - lastAccel[1])/2));
            } else if (val[1] - lastAccel[1] < -THR) {
                Log.d(TAG, "Go up.");
                sbLeft.setProgress((int)Math.max(0, sbLeft.getProgress() + (val[1] - lastAccel[1])/2));
            }
            if (val[2] - lastAccel[2] > THR) {
                Log.d(TAG, "Go further.");
                sbRight.setProgress((int)Math.min(100, sbRight.getProgress() - (val[2] - lastAccel[2])/2));
            } else if (val[2] - lastAccel[2] < -THR) {
                Log.d(TAG, "Go close.");
                sbRight.setProgress((int)Math.max(0, sbRight.getProgress() - (val[2] - lastAccel[2])/2));
            }
            lastAccel = Arrays.copyOf(val, val.length);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public Arm getArm() {
        return this.arm;
    }
}

package com.dawei.picontrol.fragment;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.dawei.picontrol.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Dawei on 12/24/2017.
 */

public class MotionArmControl extends ArmControlFragment implements SensorEventListener {

    private static final String TAG = "MotionArmControl";
    // Motion sensor
    private Sensor mAccel;
    private SensorManager mSensorManager;
    private float lastAccel[];
    private static final float G = 9.80f;
    private static final float THR = 10.00f;
    private static final float SCALE = 1.0f;

    // motion control
    private Switch swMotionControl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View vMotionControl = inflater.inflate(R.layout.motion_control_fragment, container, false);
        initializeComponents(vMotionControl);
        return vMotionControl;
    }

    @Override
    public void onStart() {
        super.onStart();
        initializeMotionSensor();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "On resume");
        if (mSensorManager != null)
            mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initializeMotionSensor() {
        if (mSensorManager == null) {
            Log.d(TAG, "Cannot find sensor manager.");
            return;
        }
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (isEnabled()) {
            float val[] = sensorEvent.values;
            if (val[0] - lastAccel[0] < -THR) {
                Log.d(TAG, "Go right.");
                arm.moveBottom((-val[0] + lastAccel[0])*SCALE);
            } else if (val[0] - lastAccel[0] > THR) {
                Log.d(TAG, "Go left.");
                arm.moveBottom((-val[0] + lastAccel[0])*SCALE);
            }
            if (val[1] - lastAccel[1] > THR) {
                Log.d(TAG, "Go down.");
                arm.moveLeft((val[1] - lastAccel[1])*SCALE);
            } else if (val[1] - lastAccel[1] < -THR) {
                Log.d(TAG, "Go up.");
                arm.moveLeft((val[1] - lastAccel[1])*SCALE);
            }
            if (val[2] - lastAccel[2] > THR) {
                Log.d(TAG, "Go further.");
                arm.moveRight((-val[2] - lastAccel[2])*SCALE);
            } else if (val[2] - lastAccel[2] < -THR) {
                Log.d(TAG, "Go close.");
                arm.moveRight((-val[2] - lastAccel[2])*SCALE);
            }
            lastAccel = Arrays.copyOf(val, val.length);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void initializeComponents(View v) {
        swMotionControl = v.findViewById(R.id.sw_motion);
        swMotionControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (swMotionControl.isChecked())
                    enable();
                else
                    disable();
            }
        });
    }

    @Override
    public void enable() {
        super.enable();
    }

    @Override
    public void disable() {
        super.disable();
    }

    public void setSensorManager(SensorManager m) {
        Log.d(TAG, "Get sensor manager " + m.toString());
        this.mSensorManager = m;
    }
}

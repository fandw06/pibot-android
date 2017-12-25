package com.dawei.picontrol.module;

import android.util.Log;

import com.dawei.picontrol.comm.BluetoothService;
import com.dawei.picontrol.comm.Command;

import java.util.Arrays;



public class Arm {

    private static final String TAG = "Arm";
    // Arm move range
    public static final float RADIUS = 15.0f;
    // trail resolution on the phone
    public static final float DRAW_SPEED = 10f;
    public static final float DRAW_Z = 3f;
    public static final int DRAW_INTERVAL = 50;

    BluetoothService mControlService;

    public Arm(BluetoothService b) {
        this.mControlService = b;
    }

    public void reset() {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.ARM)
                .option(Command.Option.RESET)
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }

    public void setPosition(float x, float y, float z) {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.ARM)
                .option(Command.Option.SET_POSITION)
                .params(new float[]{x, y, z})
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }

    public void setAngle(float a1, float a2, float a3) {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.ARM)
                .option(Command.Option.SET_ANGLE)
                .params(new float[]{a1, a2, a3})
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }

    public void controlBottom(float a) {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.ARM)
                .option(Command.Option.BOTTOM)
                .params(new float[]{a})
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }

    public void controlRight(float a) {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.ARM)
                .option(Command.Option.RIGHT)
                .params(new float[]{a})
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }

    public void controlLeft(float a) {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.ARM)
                .option(Command.Option.LEFT)
                .params(new float[]{a})
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }

    public void grab() {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.ARM)
                .option(Command.Option.GRAB)
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }

    public void loosen() {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.ARM)
                .option(Command.Option.LOOSEN)
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }
}

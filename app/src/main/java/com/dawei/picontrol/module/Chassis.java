package com.dawei.picontrol.module;

import android.util.Log;

import com.dawei.picontrol.comm.BluetoothService;
import com.dawei.picontrol.comm.Command;

import java.util.Arrays;

/**
 * Created by Dawei on 12/27/2017.
 */

public class Chassis {
    private static final String TAG = "Chassis";

    /**
     * Actually the duty cycle of owm, in [0, 100].
     */
    private float speed;
    private BluetoothService mControlService;

    public Chassis(BluetoothService m) {
        this.mControlService = m;
    }

    public void stop() {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.CHASSIS)
                .option(Command.Option.STOP)
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }

    public void startForward() {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.CHASSIS)
                .option(Command.Option.START_F)
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }

    public void startBackward() {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.CHASSIS)
                .option(Command.Option.START_B)
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }

    public void speedUp() {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.CHASSIS)
                .option(Command.Option.SPEED_UP)
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }

    public void slowDown() {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.CHASSIS)
                .option(Command.Option.SLOW_DOWN)
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }

    public void turnLeft() {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.CHASSIS)
                .option(Command.Option.TURN_L)
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }

    public void turnRight() {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.CHASSIS)
                .option(Command.Option.TURN_R)
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }

    public void setSpeed(float s) {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.CHASSIS)
                .option(Command.Option.SET_SPEED)
                .params(new float[]{s})
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
    }
}

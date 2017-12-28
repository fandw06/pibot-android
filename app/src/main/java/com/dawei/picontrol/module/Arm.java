package com.dawei.picontrol.module;

import android.util.Log;

import com.dawei.picontrol.DrawingView;
import com.dawei.picontrol.comm.BluetoothService;
import com.dawei.picontrol.comm.Command;

import java.util.Arrays;

public class Arm {

    private static final String TAG = "Arm";

    public static final float LENGTH = 8.10f;
    // Arm move range
    public static final float RADIUS = 15.0f;
    // trail resolution on the phone
    public static final float DRAW_SPEED = 10f;
    public static final float DRAW_Z = 3f;
    public static final int DRAW_INTERVAL = 50;

    // position and angle coordinates.
    private Position position;
    private Angle angle;
    private boolean isTight;

    // if bluetooth service is connected.
    private boolean connected;

    private BluetoothService mControlService;

    public static final class Position {
        public float x;
        public float y;
        public float z;

        public Position(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static final class Angle {
        public float a1;
        public float a2;
        public float a3;

        public Angle(float a1, float a2, float a3) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
        }
    }

    public Arm(BluetoothService b) {
        this.mControlService = b;
        // reset position.
        reset();
        position = new Position(0, 0, 0);
        angle = new Angle(90, 90, 45);
        isTight = false;
        connected = false;
        updatePosition();
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
        updatePosition(x, y, z);
        updateAngle();
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
        updateAngle(a1, a2, a3);
        updatePosition();
    }

    public void setBottom(float a) {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.ARM)
                .option(Command.Option.BOTTOM)
                .params(new float[]{a})
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
        angle.a1 = a;
        updatePosition();
    }

    public void moveBottom(float da) {
        float a = angle.a1 + da;
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.ARM)
                .option(Command.Option.BOTTOM)
                .params(new float[]{a})
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
        angle.a1 = a;
        updatePosition();
    }

    public void setRight(float a) {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.ARM)
                .option(Command.Option.RIGHT)
                .params(new float[]{a})
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
        angle.a2 = a;
        updatePosition();
    }

    public void moveRight(float da) {
        float a = angle.a2 + da;
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.ARM)
                .option(Command.Option.RIGHT)
                .params(new float[]{a})
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
        angle.a2 = a;
        updatePosition();
    }

    public void setLeft(float a) {
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.ARM)
                .option(Command.Option.LEFT)
                .params(new float[]{a})
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
        angle.a3 = a;
        updatePosition();
    }

    public void moveLeft(float da) {
        float a = angle.a3 + da;
        byte[] command = Command.create()
                .type(Command.Type.CONTROL)
                .module(Command.Module.ARM)
                .option(Command.Option.LEFT)
                .params(new float[]{a})
                .build()
                .toBytes();
        Log.d(TAG, Arrays.toString(command));
        mControlService.write(command);
        angle.a3 = a;
        updatePosition();
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
        isTight = true;
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
        isTight = false;
    }

    /**
     * Transfer image coordinates X to arm coordinates.
     * @param x Image coordinate x.
     * @return x' Arm coordinate x'
     */
    public float transferX(float x) {
        return Arm.RADIUS - x * 2 * Arm.RADIUS / DrawingView.getImgWidth();
    }

    /**
     * Transfer image coordinates X to arm coordinates.
     * @param y Image coordinate y.
     * @return y' Arm coordinate y'
     */
    public float transferY(float y) {
        return y * 2 * Arm.RADIUS / DrawingView.getImgWidth();
    }

    public Position getPosition() {
        return this.position;
    }

    public Angle getAngle() {
        return this.angle;
    }

    /**
     * Update angle from position.
     */
    private void updateAngle() {
        float x = position.x;
        float y = position.y;
        float z = position.z;
        float a = (float)(Math.sqrt(x*x + y*y))/LENGTH;
        float b = z/LENGTH;

        angle.a1 = (float)Math.atan(y/x);
        angle.a2 = 2*(float)Math.atan(Math.sqrt(2*b - (-(a*a + b*b)*(a*a + b*b - 4)))/(a*a - 2*a + b*b));
        angle.a3 = -2*(float)Math.atan(Math.sqrt(2*b - (-(a*a + b*b)*(a*a + b*b - 4)))/(a*a + 2*a + b*b));
    }

    /**
     * Update position from angle.
     */
    private void updatePosition() {
        float a1 = angle.a1;
        float a2 = angle.a2;
        float a3 = angle.a3;
        position.x = (float)(LENGTH * (-Math.cos(a2) + Math.cos(a3))*Math.cos(a1));
        position.y = (float)(LENGTH * (-Math.cos(a2) + Math.cos(a3))*Math.sin(a1));
        position.z = (float)(LENGTH * (Math.sin(a2)-Math.sin(a3)));
    }

    private void updatePosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
    }

    private void updateAngle(float a1, float a2, float a3) {
        angle.a1 = a1;
        angle.a2 = a2;
        angle.a3 = a3;
    }

    public boolean isConnected() {
        connected = (mControlService.getState() == BluetoothService.STATE_CONNECTED);
        return connected;
    }
}

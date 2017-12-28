package com.dawei.picontrol.fragment;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.dawei.picontrol.DrawingView;
import com.dawei.picontrol.R;
import com.dawei.picontrol.module.Arm;

import java.util.Stack;


public class DrawingArmControl extends ArmControlFragment {

    private static final String TAG = "DrawingArmControl";

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

    public boolean isDrawing;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View vDrawingControl = inflater.inflate(R.layout.drawing_control_fragment, container, false);
        initializeComponents(vDrawingControl);
        return vDrawingControl;
    }

    @Override
    public void initializeComponents(View v) {
        // drawing control
        swDrawingControl = v.findViewById(R.id.sw_drawing);
        swDrawingControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (swDrawingControl.isChecked()) {
                    Log.d(TAG, "Drawing control is turned on.");
                    enable();
                }
                else {
                    Log.d(TAG, "Drawing control is turned off.");
                    disable();
                }
            }
        });
        rgDrawing = v.findViewById(R.id.rg_drawing);
        rbRealtime = v.findViewById(R.id.rb_realtime);
        rbRealtime.setChecked(true);
        rbRealtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        rbOnce = v.findViewById(R.id.rb_once);
        rbOnce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        drawingView = v.findViewById(R.id.drawing_pad);
        drawingView.setHost(this);
        bPaint = v.findViewById(R.id.btn_paint);
        bPaint.setEnabled(false);
        bPaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        bUndo = v.findViewById(R.id.btn_undo);
        bUndo.setEnabled(false);
        bUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.undo();
            }
        });
        bRedo = v.findViewById(R.id.btn_redo);
        bRedo.setEnabled(false);
        bRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.redo();
            }
        });
        bClear = v.findViewById(R.id.btn_clear);
        bClear.setEnabled(false);
        bClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.clear();
            }
        });
        bSave = v.findViewById(R.id.btn_save);
        bSave.setEnabled(false);
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.saveImage();
            }
        });
        bFire = v.findViewById(R.id.btn_fire);
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
                Stack<DrawingView.Trail> trails = drawingView.getTrails();
                int index = 0;

                for (DrawingView.Trail t : trails) {
                    Path path = t.path;
                    PathMeasure pm = new PathMeasure(path, false);
                    float length = pm.getLength();
                    float distance = 0f;
                    float[] points = new float[2];
                    Log.d(TAG, "Trail " + (index++));

                    // Lift the pen.
                    arm.setLeft(0);
                    try {
                        Thread.sleep(Arm.DRAW_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    while (distance < length) {
                        // get point from the path
                        pm.getPosTan(distance, points, null);
                        Log.d(TAG, String.format(
                                "Measured points: (%f, %f)", arm.transferX(points[0]), arm.transferY(points[1])));
                        distance = distance + Arm.DRAW_SPEED;
                        arm.setPosition(arm.transferX(points[0]), arm.transferY(points[1]), Arm.DRAW_Z);
                        try {
                            Thread.sleep(Arm.DRAW_INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (distance < length) {
                        pm.getPosTan(length, points, null);
                        Log.d(TAG, String.format(
                                "Measured points: (%f, %f)", arm.transferX(points[0]), arm.transferY(points[1])));
                        arm.setPosition(arm.transferX(points[0]), arm.transferY(points[1]), Arm.DRAW_Z);
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
    }

    @Override
    public void enable() {
        super.enable();
        swDrawingControl.setChecked(true);
        rbRealtime.setEnabled(false);
        rbOnce.setEnabled(false);
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

    @Override
    public void disable() {
        super.disable();
        swDrawingControl.setChecked(false);
        rbRealtime.setEnabled(true);
        rbOnce.setEnabled(true);
        bPaint.setEnabled(false);
        bUndo.setEnabled(false);
        bRedo.setEnabled(false);
        bClear.setEnabled(false);
        bSave.setEnabled(false);
        bFire.setEnabled(false);
        isDrawing = false;
        // Clear current drawing.
        drawingView.clear();
    }
}

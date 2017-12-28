package com.dawei.picontrol.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import com.dawei.picontrol.R;
import com.dawei.picontrol.module.Chassis;

/**
 * Created by Dawei on 12/27/2017.
 */

public class ChassisControl extends Fragment {

    private static final String TAG = "ChassisControl";
    private static final float MIN_DUTY = 45;

    private Button bUp;
    private Button bDown;
    private Button bLeft;
    private Button bRight;
    private Button bStop;
    private SeekBar sbSpeed;

    private Chassis chassis;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View vChassisControl = inflater.inflate(R.layout.chassis_control_fragment, container, false);
        initializeComponents(vChassisControl);
        return vChassisControl;
    }

    public void initializeComponents(View v) {
        sbSpeed = v.findViewById(R.id.seek_speed);
        sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float duty = MIN_DUTY + i / 100 * (100 - MIN_DUTY);
                chassis.setSpeed(duty);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bUp = v.findViewById(R.id.btn_up);
        bUp.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                chassis.stop();
                chassis.startForward();
            }
        });

        bDown = v.findViewById(R.id.btn_down);
        bDown.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                chassis.stop();
                chassis.startBackward();
            }
        });

        bLeft = v.findViewById(R.id.btn_left);
        bLeft.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                chassis.startForward();
                chassis.turnLeft();
            }
        });

        bRight = v.findViewById(R.id.btn_right);
        bRight.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                chassis.startForward();
                chassis.turnRight();
            }
        });

        bStop = v.findViewById(R.id.btn_stop);
        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chassis.stop();
            }
        });
    }

    public void setChassis(Chassis c) {
        this.chassis = c;
    }
}

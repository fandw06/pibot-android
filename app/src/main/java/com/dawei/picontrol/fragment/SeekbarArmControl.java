package com.dawei.picontrol.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;

import com.dawei.picontrol.R;


public class SeekbarArmControl extends ArmControlFragment {

    private static final String TAG = "SeekbarArmControl";

    private Switch swSeekbarControl;
    private SeekBar sbBottom;
    private SeekBar sbRight;
    private SeekBar sbLeft;
    private Switch swClaw;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View vSeekbarControl = inflater.inflate(R.layout.seekbar_control_fragment, container, false);
        initializeComponents(vSeekbarControl);
        return vSeekbarControl;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void initializeComponents(View v) {
        swSeekbarControl = v.findViewById(R.id.sw_seekbar);
        swSeekbarControl.setChecked(true);
        swSeekbarControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (swSeekbarControl.isChecked()) {
                    enable();
                }
                else {
                    disable();
                }
            }
        });
        sbBottom = v.findViewById(R.id.seek_bottom);
        sbRight = v.findViewById(R.id.seek_right);
        sbLeft = v.findViewById(R.id.seek_left);
        swClaw = v.findViewById(R.id.sw_claw);
        swClaw.setChecked(false);
        sbBottom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                arm.setBottom(i * 1.80f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbRight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                arm.setRight(i * 1.80f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbLeft.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                arm.setLeft(- 38 + i * 1.28f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        swClaw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (swClaw.isChecked())
                    arm.grab();
                else
                    arm.loosen();
            }
        });
    }

    @Override
    public void enable() {
        super.enable();
        sbBottom.setEnabled(true);
        sbRight.setEnabled(true);
        sbLeft.setEnabled(true);
    }

    @Override
    public void disable() {
        super.disable();
        swSeekbarControl.setChecked(false);
        sbBottom.setEnabled(false);
        sbRight.setEnabled(false);
        sbLeft.setEnabled(false);
    }
}

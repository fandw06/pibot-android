package com.dawei.picontrol.fragment;

import android.app.Fragment;
import android.view.View;

import com.dawei.picontrol.module.Arm;

public abstract class ArmControlFragment extends Fragment {

    /**
     * The arm module to control.
     */
    public Arm arm;

    /**
     * The control module status.
     */
    private boolean isEnabled;

    public void setArm(Arm a) {
        this.arm = a;
    }

    public abstract void initializeComponents(View v);

    /**
     * Enable this control module.
     */
    public void enable(){
        isEnabled = true;
    }

    /**
     * Disable this control module.
     */
    public void disable(){
        isEnabled = false;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}

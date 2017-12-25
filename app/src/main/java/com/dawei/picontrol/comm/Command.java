package com.dawei.picontrol.comm;

/**
 * Created by Dawei on 12/20/2017.
 */

public class Command {

    public enum Type {
        RAW(0x0),
        TEST(0x1),
        CONTROL(0x2);

        private final int val;
        Type(int v) {
            this.val = v;
        }
        public int getVal() {
            return this.val;
        }

    }

    public enum Module {
        ARM(0x10),
        ENGINE(0x11),
        ULTRASONIC(0x12),
        LED(0x1e),
        GPIO(0x20);

        private final int val;
        Module(int v) {
            this.val = v;
        }

        public int getVal() {
            return this.val;
        }
    }

    public enum Option {
        //arm options
        RESET(0x00),
        SET_POSITION(0x01),
        SET_ANGLE(0x02),
        TRAIL_POSITION(0x03),
        TRAIL_ANGLE(0x04),
        BOTTOM(0x05),
        RIGHT(0x06),
        LEFT(0x07),
        GRAB(0x0f),
        LOOSEN(0x10),

        //engine options
        STOP(0x00),
        START_F(0x01),
        START_B(0x02),
        SPEED_UP(0x03),
        SLOW_DOWN(0x04),
        TURN_L(0x05),
        TURN_R(0x06),
        GO_F(0x07),
        GO_B(0x08),

        //ultrasonic
        GET_DIST(0x00),

        // GPIO
        READ_PORT(0x00),
        WRITE_PORT(0x01);

        private final int val;
        Option(int v) {
            this.val = v;
        }
        public int getVal() {
            return this.val;
        }
    }

    private Type type;
    private Module module;
    private Option option;
    private float[] params;

    private Command(Type t, Module m, Option o, float[] p) {
        this.type = t;
        this.module = m;
        this.option = o;
        this.params = p;
    }

    public static Builder create() {
        return new Builder();
    }

    public byte[] toBytes() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%1x", type.getVal()));
        if (module != null)
            sb.append(String.format("%02x", module.getVal()));
        if (option != null)
            sb.append(String.format("%02x", option.getVal()));
        if (params != null) {
            sb.append("[");
            for (double d: params)
                sb.append(String.format("%.2f, ", d));
            sb.delete(sb.length() - 2, sb.length());
            sb.append("]");
        }
        return sb.toString().getBytes();
    }

    public static final class Builder {
        private Type type;
        private Module module;
        private Option option;
        private float[] params;

        public Builder() {}

        public Builder type(Type t) {
            this.type = t;
            return this;
        }

        public Builder module(Module m) {
            this.module = m;
            return this;
        }

        public Builder option(Option o) {
            this.option = o;
            return this;
        }

        public Builder params(float[] p) {
            this.params = p;
            return this;
        }

        public Command build() {
            return new Command(type, module, option, params);
        }
    }
}

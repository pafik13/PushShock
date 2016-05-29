package ru.lifeplus.pushshock;

import java.util.Date;

/**
 * Created by pavel on 19.05.2016.
 */
public final class ShockDeviceSettings {
    public byte dt;
    public byte ads;
    public byte nmpp;
    public byte np;
    public Date stamp;

    public ShockDeviceSettings(byte dt, byte ads, byte nmpp, byte np){
        this.dt = dt;
        this.ads= ads;
        this.nmpp = nmpp;
        this.np = np;
        this.stamp = new Date();
    }

    public ShockDeviceSettings(byte[] params){
        this.dt    = params[0];
        this.ads   = params[1];
        this.nmpp  = params[2];
        this.np    = params[3];
        this.stamp = new Date();
    }

    public byte[] toArray() {
        byte [] result = new byte[4];
        result[0] = dt;
        result[1] = ads;
        result[2] = nmpp;
        result[3] = np;

        return result;
    }
}

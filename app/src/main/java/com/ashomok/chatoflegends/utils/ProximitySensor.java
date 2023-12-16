package com.ashomok.chatoflegends.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by Devlomi on 04/10/2017.
 */

//this class will listen for proximity sensor state if it's near or far
//it will also turn the screen to black to prevent user input while it's near
public class ProximitySensor implements SensorEventListener {


    private final SensorManager mSensorManager;
    private final Delegate mDelegate;
    private final Sensor mSensor;
    private PowerManager.WakeLock mScreenLock;

    public ProximitySensor(Context context, Delegate delegate) {
        if (null == context || null == delegate)
            throw new IllegalArgumentException("You must pass a non-null context and delegate");


        Context appContext = context.getApplicationContext();
        mSensorManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
        mDelegate = delegate;

        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        //there is no proximity sensor in device
        if (null == mSensor) return;


        //request turn screen lock (turn to black)
        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);

        int screenLockValue;

        screenLockValue = PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK;

        mScreenLock = powerManager.newWakeLock(screenLockValue, getClass().getSimpleName());

    }

    //turn screen to black
    public void acquire() {
        if (null != mScreenLock && !mScreenLock.isHeld()) {
            mScreenLock.acquire();
        }
    }

    //revert screen to normal
    public void release() {
        if (null != mScreenLock && mScreenLock.isHeld())
            mScreenLock.release();
    }

    //the listener will not work unless this is called
    public void listenForSensor() {
        if (null != mSensorManager && null != mSensor) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stopListenForSensor() {
        if (null != mSensorManager && null != mSensor) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (Sensor.TYPE_PROXIMITY != event.sensor.getType()) return;

        try {
            //NEAR
            if (5.0f > event.values[0] && event.values[0] != mSensor.getMaximumRange()) {
                mDelegate.onProximitySensorNear();
            } else {
                //FAR
                mDelegate.onProximitySensorFar();
            }

        } catch (Exception exc) {
            Log.e(getClass().getSimpleName(), "onSensorChanged exception", exc);
        }


    }

    public interface Delegate {
        void onProximitySensorNear();

        void onProximitySensorFar();
    }

}

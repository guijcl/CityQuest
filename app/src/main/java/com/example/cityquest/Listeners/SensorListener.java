package com.example.cityquest.Listeners;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.widget.LinearLayout;

import com.example.cityquest.Fragments.MapFragment;
import com.example.cityquest.R;

public class SensorListener implements SensorEventListener {

    private final Context context;

    private final SensorManager sensorManager;
    private final Sensor sensorAccelerometer;

    private MapFragment map;

    private float mAcc;
    private float mAccCurrent;
    private float mAccLast;

    private boolean allowed;
    CountDownTimer timer;

    public void setMap(MapFragment map) { this.map = map; }

    public SensorListener(Context context) {
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mAcc = 0.00f;
        mAccCurrent = SensorManager.GRAVITY_EARTH;
        mAccLast = SensorManager.GRAVITY_EARTH;

        allowed = true;

        timer = new CountDownTimer(2000, 1000)  {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                allowed = true;
            }
        };
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            mAccLast = mAccCurrent;
            mAccCurrent = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = mAccCurrent - mAccLast;
            mAcc = mAcc * 0.9f + delta;
            if (mAcc > 40 && allowed) {
                map.checkLocation();
                allowed = false;
                timer.start();
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

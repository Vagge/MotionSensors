package com.example.motionsensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Gyroscope {


    public interface Listener {
        void onRotation (float rx, float ry, float rz, long timestamp);
    }

    private Listener listener;

    public void setListener (Listener l) {
        listener = l;
    }

    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;

    public Gyroscope (Context context){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorEventListener = new SensorEventListener(){

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (listener != null)
                    listener.onRotation(event.values[0], event.values[1], event.values[2], event.timestamp);

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    public void register () {
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
    }

    public void unregister () {
        sensorManager.unregisterListener(sensorEventListener);
    }

}

package com.example.motionsensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Accelerometer  {

    public interface Listener {
        void onTranslation (float tx, float ty, float tz, long timestamp);
    }

    private Listener listener;

    public void setListener (Listener l) {
        listener = l;
    }

    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;

    public Accelerometer (Context context){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorEventListener = new SensorEventListener(){

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (listener != null)
                    listener.onTranslation(event.values[0], event.values[1], event.values[2], event.timestamp);

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

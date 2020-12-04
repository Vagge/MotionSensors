package com.example.motionsensors;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Accelerometer accelerometer;
    private Gyroscope gyroscope;
    private TextView accDegree;
    private double oldV;
    private double CpitchOld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accelerometer = new Accelerometer(this);
        gyroscope = new Gyroscope(this);
        accDegree = findViewById(R.id.acc_degree);
        double gyroPitchOld = 0;
        double GyroRoll = 0;

        accelerometer.setListener(new Accelerometer.Listener() {
            @Override
            public void onTranslation(float tx, float ty, float tz) {
                double x = (double) tx;
                double y = (double) ty;
                double z = (double) tz;

                double newValue = Math.sqrt(Math.pow(x,2) * Math.pow(y,2));
                newValue = z/newValue;
                newValue = Math.atan(newValue);
                newValue = newValue * (180 / Math.PI);
                oldV = 0.1 * newValue + 0.9 * oldV;
            }
        });

        gyroscope.setListener(new Gyroscope.Listener() {
            @Override
            public void onRotation(float rx, float ry, float rz) {
                double alfa = 0.1;
                double pitch = oldV;
                double dT = 1/52;
                double Cpitch = alfa * (CpitchOld + dT*ry) + (1-alfa)*pitch;
                CpitchOld = Cpitch;
                accDegree.setText(String.valueOf(Cpitch));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        accelerometer.register();
        gyroscope.register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        accelerometer.unregister();
        gyroscope.unregister();
    }
}
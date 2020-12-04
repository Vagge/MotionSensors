package com.example.motionsensors;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Accelerometer accelerometer;
    private Gyroscope gyroscope;
    private TextView accDegree;
    private TextView gyroDegree;
    private Button toggelButton;
    private double oldV;
    private double CpitchOld;
    private boolean toggle;
    private double startTime;
    private ArrayList<Double> degrees;
    private ArrayList<Long> timestamps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accelerometer = new Accelerometer(this);
        gyroscope = new Gyroscope(this);
        accDegree = findViewById(R.id.acc_degree);
        gyroDegree = findViewById(R.id.gyro_degree);
        toggelButton = findViewById(R.id.button);
        toggle = true;
        degrees = new ArrayList<>();
        timestamps = new ArrayList<>();

        toggelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime = System.currentTimeMillis();
                if (toggle) {
                    toggle = false;
                    accelerometer.register();
                    gyroscope.register();
                    setAccelerometerListener();
                    setGyroscopeListener();
                } else {
                    unregister ();
                }
            }
        });
    }

    private void setAccelerometerListener() {
        accelerometer.setListener(new Accelerometer.Listener() {
            @Override
            public void onTranslation(float tx, float ty, float tz, long timestamp) {
                double x = (double) tx;
                double y = (double) ty;
                double z = (double) tz;

                double newValue = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
                newValue = z / newValue;
                newValue = Math.atan(newValue);
                newValue = newValue * (180 / Math.PI);
                oldV = 0.1 * newValue + 0.9 * oldV;
                accDegree.setText(String.valueOf(oldV));
            }
        });
    }

    private void setGyroscopeListener() {
        gyroscope.setListener(new Gyroscope.Listener() {
            @Override
            public void onRotation(float rx, float ry, float rz, long timestamp) {
                double alfa = 0.1;
                double pitch = oldV;
                double dT = 1 / 52;
                double Cpitch = alfa * (CpitchOld + dT * ry) + (1 - alfa) * pitch;
                CpitchOld = Cpitch;
                gyroDegree.setText(String.valueOf(Cpitch));
                degrees.add(Cpitch);
                timestamps.add(timestamp);

                if (System.currentTimeMillis() - startTime >= 10000) {
                    unregister ();
                }
            }
        });
    }

    private void unregister (){
        accelerometer.unregister();
        gyroscope.unregister();
        WriteToFile();
        degrees.clear();
        timestamps.clear();
        toggle = true;
    }

    public void WriteToFile() {
        // add-write text into file
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput("data.txt", this.MODE_PRIVATE));
            outputStreamWriter.write(degrees.toString());
            outputStreamWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput("timestamp.txt", this.MODE_PRIVATE));
            outputStreamWriter.write(timestamps.toString());
            outputStreamWriter.close();
            //display file saved message
            Toast.makeText(getBaseContext(), "File saved successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
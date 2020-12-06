package com.example.motionsensor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motionsensor.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
    private ArrayList<Double> degreesMethodOne;
    private ArrayList<Long> timestampsMethodOne;
    private ArrayList<Double> degreesMethodTwo;
    private ArrayList<Long> timestampsMethodTwo;

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
        degreesMethodOne = new ArrayList<>();
        timestampsMethodOne = new ArrayList<>();
        degreesMethodTwo = new ArrayList<>();
        timestampsMethodTwo = new ArrayList<>();

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

                double newValue = Math.sqrt(Math.pow(z, 2) + Math.pow(x, 2));
                if(newValue!=0)
                {
                    newValue = y / newValue;
                    newValue = Math.atan(newValue);
                    newValue = newValue * (180 / Math.PI);
                    oldV = 0.1 * newValue + 0.9 * oldV;
                    accDegree.setText(String.valueOf(oldV+90));
                    degreesMethodOne.add(oldV+90);
                    timestampsMethodOne.add(timestamp);
                }
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
                double Cpitch = alfa * (CpitchOld + dT * rx) + (1 - alfa) * pitch;
                CpitchOld = Cpitch;
                gyroDegree.setText(String.valueOf(Cpitch+90));
                degreesMethodTwo.add(Cpitch+90);
                timestampsMethodTwo.add(timestamp);

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
        degreesMethodOne.clear();
        degreesMethodTwo.clear();
        timestampsMethodOne.clear();
        timestampsMethodTwo.clear();
        toggle = true;
    }

    public void WriteToFile() {
        PrintWriter writer = null;
        try {
            OutputStream os = this.openFileOutput(
                    "data.txt", Context.MODE_PRIVATE);
            writer = new PrintWriter(os);
            for(int i = 0; i < timestampsMethodOne.size(); i++)
            {
                writer.println(timestampsMethodOne.get(i) + " " + degreesMethodOne.get(i));
            }

            OutputStream os2 = this.openFileOutput(
                    "data2.txt", Context.MODE_PRIVATE);
            for(int i = 0; i < timestampsMethodTwo.size(); i++)
            {
                writer.println(timestampsMethodTwo.get(i) + " " + degreesMethodTwo.get(i));
            }
            Toast.makeText(getBaseContext(), "File saved successfully!", Toast.LENGTH_SHORT).show();
        }
        catch(IOException ioe) {
            Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();
        }
        finally {
            if(writer != null) writer.close();
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

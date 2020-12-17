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
    private double newV;
    private double CpitchOld;
    private boolean toggle;
    private double startTime;
    private ArrayList<Double> degreesMethodOne;
    private ArrayList<Long> timestampsMethodOne;
    private ArrayList<Double> degreesMethodTwo;
    private ArrayList<Long> timestampsMethodTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accelerometer = new Accelerometer(this);
        gyroscope = new Gyroscope(this);
        accDegree = findViewById(R.id.acc_degree);
        gyroDegree = findViewById(R.id.gyro_degree);
        toggelButton = findViewById(R.id.button);
        toggle = false;
        degreesMethodOne = new ArrayList<>();
        timestampsMethodOne = new ArrayList<>();
        degreesMethodTwo = new ArrayList<>();
        timestampsMethodTwo = new ArrayList<>();
        setAccelerometerListener();
        setGyroscopeListener();
        toggelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!toggle) {
                    toggle = true;
                    startTime = System.currentTimeMillis();
                    toggelButton.setText(R.string.on);
                } else {
                    toggle = false;
                    toggelButton.setText(R.string.off);
                    WriteToFile();
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

                newV= Math.sqrt(Math.pow(y, 2) + Math.pow(x, 2));
                if(newV!=0)
                {
                    newV = z / newV;
                    newV = Math.atan(newV);
                    newV = newV* (180 / Math.PI);
                    oldV = 0.2 * newV + 0.8 * oldV;
                    accDegree.setText(String.valueOf(oldV));


                    if(toggle)
                    {
                        double time = System.currentTimeMillis() - startTime;
                        if (time <= 10000)
                        {
                            degreesMethodOne.add(oldV);
                            timestampsMethodOne.add(timestamp);
                        }
                        else
                        {
                            WriteToFile();
                        }
                    }
                }
            }
        });
    }

    private void setGyroscopeListener() {
        gyroscope.setListener(new Gyroscope.Listener() {
            @Override
            public void onRotation(float rx, float ry, float rz, long timestamp) {
                double alfa = 0.1;
                double dT = 1 / 52;
                double Cpitch = alfa * (CpitchOld + dT * rz) + (1 - alfa) * newV;
                CpitchOld = Cpitch;
                gyroDegree.setText(String.valueOf(Cpitch));
                if(toggle)
                {
                    double time = System.currentTimeMillis() - startTime;
                    if (time <= 10000)
                    {
                        degreesMethodTwo.add(Cpitch);
                        timestampsMethodTwo.add(timestamp);
                    }
                    else
                    {
                        toggle = false;
                        toggelButton.setText(R.string.off);
                        WriteToFile();
                    }
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
        PrintWriter writer2 = null;
        try {
            OutputStream os = this.openFileOutput(
                    "data.txt", Context.MODE_PRIVATE);
            writer = new PrintWriter(os);
            for(int i = 0; i < timestampsMethodOne.size(); i++)
            {
                writer.println(timestampsMethodOne.get(i));
            }
            for(int i = 0; i < degreesMethodOne.size(); i++)
            {
                writer.println(degreesMethodOne.get(i));
            }
            OutputStream os2 = this.openFileOutput(
                    "data2.txt", Context.MODE_PRIVATE);
            writer2 = new PrintWriter(os2);
            for(int i = 0; i < timestampsMethodTwo.size(); i++)
            {
                writer2.println(timestampsMethodTwo.get(i));
            }
            for(int i = 0; i < degreesMethodTwo.size(); i++)
            {
                writer2.println(degreesMethodTwo.get(i));
            }
            Toast.makeText(getBaseContext(), "File saved successfully!", Toast.LENGTH_SHORT).show();
        }
        catch(IOException ioe) {
            Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();
        }
        finally {
            if(writer != null) writer.close();
            if(writer2 != null) writer2.close();
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
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

package com.example.ketansharma.gps_accelerometer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import java.util.Locale;

/**
 * Created by ketan.sharma on 29/03/2016.
 * This actvity implements the SensorEventListener to return the accelerometer value
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private TextView acc;
    private LocationManager locationManager;
    private String bestProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        acc = (TextView) findViewById(R.id.accelerometer_value);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        bestProvider = locationManager.getBestProvider(criteria, true);

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView latitude_value = (TextView) findViewById(R.id.latitude_value);
                TextView longitude_value = (TextView) findViewById(R.id.longitude_value);
                double latitude = Double.parseDouble(latitude_value.getText().toString());
                double longitude = Double.parseDouble(longitude_value.getText().toString());

                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float[] gravity = new float[3];
        final float alpha = (float) 0.8;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        float x = event.values[0] - gravity[0];
        float y = event.values[1] - gravity[1];
        float z = event.values[2] - gravity[2];

        acc.setText("x="+x+" y="+y+" z="+z);
    }

    @Override
    protected void onResume() {
        if (locationManager != null) {
            if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
            && (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)) {
                Location location = locationManager.getLastKnownLocation(bestProvider);

                locationManager.requestLocationUpdates(bestProvider, 0, 0, locationListener);
            }
        }

        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                6000000);
    }

    @Override
    protected void onPause() {
        if (locationManager != null) {
            if (locationManager != null) {
                if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
                        && (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)) {
                    locationManager.removeUpdates(locationListener);
                }
            }
            super.onPause();
            sensorManager.unregisterListener(this);
        }
    }

    /** This listener returns GPS data, although the speed value is inaccurate
    when testing in a car.  Need to implement an algorithm to calculate the speed,
    based on locations and distances covered. */
    final LocationListener locationListener = new LocationListener() {
        // LocationListener Override Method
        @Override
        public void onLocationChanged(Location location) {
            TextView latitude_value = (TextView) findViewById(R.id.latitude_value);
            latitude_value.setText(String.valueOf(location.getLatitude()));

            TextView longitude_value = (TextView) findViewById(R.id.longitude_value);
            longitude_value.setText(String.valueOf(location.getLongitude()));

            TextView speed_value = (TextView) findViewById(R.id.speed_value);
            speed_value.setText(toMPH(location.getSpeed()));
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.v("Status", "AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.v("Status", "OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.v("Status", "TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    };

    String toMPH(float speed) {
        String speed_string = "";
        //to KPH
        speed *= 3.6;
        speed *= 0.621371;

        speed_string = String.valueOf(speed) + " mph";

        return speed_string;
    }
}
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

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private TextView acc;
    private LocationManager locationManager;
    private String bestProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        acc = (TextView) findViewById(R.id.acceleration_value);

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
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        double acceleration = Math.sqrt(accelationSquareRoot);
        acc.setText(String.valueOf(acceleration));
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

                locationManager.requestLocationUpdates(bestProvider, 0, 1, mLocationListener);
            }
        }

        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                60000);
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
                    locationManager.removeUpdates(mLocationListener);
                }
            }
            super.onPause();
            sensorManager.unregisterListener(this);
        }
    }

    final LocationListener mLocationListener = new LocationListener() {
        // LocationListener Override Method
        @Override
        public void onLocationChanged(Location location) {
            TextView latitude_value = (TextView) findViewById(R.id.latitude_value);
            latitude_value.setText(String.valueOf(location.getLatitude()));

            TextView longitude_value = (TextView) findViewById(R.id.longitude_value);
            longitude_value.setText(String.valueOf(location.getLongitude()));

            TextView speed_value = (TextView) findViewById(R.id.speed_value);
            speed_value.setText(String.valueOf(location.getSpeed()));
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
}
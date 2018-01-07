package com.example.adalynn.pushit2.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.adalynn.pushit2.R;
import com.example.adalynn.pushit2.activity.MainActivity;
import com.example.adalynn.pushit2.app.Config;

/**
 * Created by filipp on 6/16/2016.
 */
public class MyLocationService extends Service {

    private LocationListener listener;
    private LocationManager locationManager;
    private static final String TAG = MyLocationService.class.getSimpleName() + " PUSHIT : ";

// Reference Urls
//https://raw.githubusercontent.com/miskoajkula/GPS_service/master/app/src/main/java/testing/gps_service/MainActivity.java
//https://raw.githubusercontent.com/miskoajkula/GPS_service/master/app/src/main/java/testing/gps_service/GPS_Service.java
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {

        Log.e(TAG, "Service called successfully!");
        Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent(Config.LOCATION_UPDATE);
                Toast.makeText(MyLocationService.this, "Service Created" +location.getLongitude(), Toast.LENGTH_LONG).show();
                i.putExtra("coordinates", location.getLongitude() + " " + location.getLatitude());
                i.putExtra("longitude", location.getLongitude());
                i.putExtra("latitude", location.getLatitude());
                sendBroadcast(i);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                //Sending user to settings intent to enable the GPS
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "PERMISSION NOT GRANTED", Toast.LENGTH_LONG).show();
            return;
        } else {
            Toast.makeText(this, "PERMISSION GRANTED", Toast.LENGTH_LONG).show();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.LOCATION_TRACK_INTERVAL, Config.LOCATION_TRACK_MIN_DISTANCE, listener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }
}
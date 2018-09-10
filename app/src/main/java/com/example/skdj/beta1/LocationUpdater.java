package com.example.skdj.beta1;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class LocationUpdater extends Service {
    public static final int FIVE_SECOND = 5000;
    public static Boolean isRunning;
    public LocationManager mLocationManager;
    public LocationUpdaterListener mLocationListener;
    public Location previousBestLocation = null;
    Intent i;
    //Intent i2;
    // public static final String DIST_KEY="Distance";
    public static final String BD_KEY="Casting";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationUpdaterListener();
        super.onCreate();
        i=new Intent(BD_KEY);
        isRunning=true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        isRunning=true;
    }

   /* Handler mHandler = new Handler();
    Runnable mHandlerTask = new Runnable(){
        @Override
        public void run() {
            Log.d("LocationUpdater", "In run");
            if (isRunning==true) {
                isLocationEnabled();
                startListening();
            }
            mHandler.postDelayed(mHandlerTask, FIVE_SECOND);
        }
    };*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Location", "onStart");
       startListening();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopListening();
        //mHandler.removeCallbacks(mHandlerTask);
        super.onDestroy();
    }
    protected void isLocationEnabled(){
        String le = Context.LOCATION_SERVICE;
        mLocationManager = (LocationManager) getSystemService(le);
        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.d("LocationUpdater", "Network disabled");

        } else {
            Log.d("LocationUpdater", "Network enalbed");

        }
    }


    private void startListening() {

        Log.d("Location", "StartListerning");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if (mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                Log.d("LocationUpdater", "Netwrok provider enabled");
            }

            if (mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
            {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                Log.d("LocationUpdater", "GPS provider enabled");
            }
        }


        isRunning = true;
    }

    private void sendCoordinates() {

        sendBroadcast(i);
    }
    private  void stopListening(){
        Log.d("LocationUpdater", "stopListerning");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(mLocationListener);
        }
        isRunning = false;
    }

    public class LocationUpdaterListener extends AppCompatActivity implements LocationListener
    {



        @Override
        public void onLocationChanged(Location location)
        {
            previousBestLocation = location;
            i.putExtra("latitude", location.getLatitude());
            i.putExtra("longitude",location.getLongitude());
            sendCoordinates();
            Log.d("Location","got it");
        }

        @Override
        public void onProviderDisabled(String provider) {
            stopListening();
        }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    }
}

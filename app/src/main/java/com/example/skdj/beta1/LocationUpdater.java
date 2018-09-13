package com.example.skdj.beta1;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class LocationUpdater extends Service {
    public static final String MY_SERVICE = "com.example.skdj.beta1";
    public static final String ANDROID_CHANNEL_ID = "DRIVER_APP";
    public static final String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";
    public static final int FIVE_SECOND = 5000;
    public static Boolean isRunning;
    public LocationManager mLocationManager;
    public LocationUpdaterListener mLocationListener;
    public Location previousBestLocation = null;
    double strtLan;
    double strtLong;
    boolean isDestroyed=false;
    SharedPreferences prefs;
    NotificationCompat.Builder mBuilder;
    Intent i;
    boolean sendingStatus= false;
    boolean cancelStatus= false;
    int flag=0;
    String MY_PREFS_NAME="Start";
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


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Intent study", "in onStartCommand "+startId);
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        startListening();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("Intent study", "on destroy");
        stopListening();
        isDestroyed=true;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancelAll();
        super.onDestroy();
    }

    private void startListening() {

        showNotification("Connecting...");
        cancelStatus=true;
        Log.d("Intent study", "StartListerning");
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
    private void showNotification(String s)
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("FromNotification", 1);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        createNotificationChannel();
        mBuilder = new NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
                 .setSmallIcon(R.drawable.ic_launcher_foreground)
                 .setContentTitle("My notification")
                 .setContentText(s)
                  .setContentIntent(pendingIntent)
                 .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, mBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ANDROID_CHANNEL_ID, ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private  void stopListening(){
        Log.d("Intent study", "stopListerning");
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
            strtLan=location.getLatitude();
            strtLong=location.getLongitude();
            sendCoordinates();
            new SendLocation().execute();
            Log.d("Location","got it");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("Location", "onProviderDisabled");
        }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    }
    private class SendLocation extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try {
                flag=0;
                URL url = new URL(getResources().getString(R.string.gpsUpdate));// here is your URL path
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(8000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                String cc = prefs.getString("countryCode", null);
                String sc = prefs.getString("stateCode", null);
                String restoredText = cc+sc+prefs.getString("vehicleno", null); //concatatinating countrycode+statecode+vehicleno
                String restoredText11 = prefs.getString("phoneno1", null);

                String s="reg="+restoredText+"_"+restoredText11+"&lat="+strtLan+"&lon="+strtLong;
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(s);

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    flag=1;

                    InputStream is = null;
                    try {
                        is = conn.getInputStream();
                        int ch;
                        StringBuffer sb = new StringBuffer();
                        while ((ch = is.read()) != -1) {
                            sb.append((char) ch);
                        }
                        return sb.toString();
                    } catch (IOException e) {
                        throw e;
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }

                }
                else {
                    Log.d("False", ""+responseCode);
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                Log.d("Exception", e.getMessage());
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            if(isDestroyed==true)
                return;
            if(flag==1 )
            {   if(sendingStatus==false)
            {
                showNotification("Sending Location. Tap to cancel");
                sendingStatus=true;
            }
            }
            else
            {
                if(cancelStatus==false) {
                    showNotification("Connecting...   Tap to cancel");
                    cancelStatus=true;
                }
            }
        }
    }
}

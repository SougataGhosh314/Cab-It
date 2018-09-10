package com.example.skdj.beta1;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    public static final String fl = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String cl = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int per_req_code = 1234;
    private GoogleMap map;
    private boolean mLocationPermissionGranted;
    FragmentTransaction fragmentTransaction;
    MySupportMapFragment itsMySupportMapFragment;
    private static final float DfZoom = 15f;
    private FusedLocationProviderClient mFusedLocation;
    Window window;
    DrawerLayout drawer;
    Intent i;
    double strtLan;
    double strtLong;
    String MY_PREFS_NAME="Start";
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            strtLan=intent.getDoubleExtra("latitude", 0.0);
            strtLong=intent.getDoubleExtra("longitude", 0.0);
            new SendLocation().execute();
            Log.d("Location",strtLan+"  "+strtLong);
        }
    };
    int flag=0;
    String plateno;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //checking the internet connection

           internetStatus();
        //closed


        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        //To check if user is already register
        String restoredText = prefs.getString("name", null);
        if (restoredText == null) {
           flag=1;
            Intent i= new Intent(this, FormActivity.class);
             startActivity(i);
             return;

        }
        //user is register
        plateno = prefs.getString("detail", null);   //plateno.
        Log.d("hmm","not ok");
        window = getWindow();
        //To creating transparency in top status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0x00000000); // transparent
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            window.addFlags(flags);
        }


        setContentView(R.layout.activity_main);

        //service Intent initialization
        i=new Intent(this, LocationUpdater.class);



      /*  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        //for drawerlayout
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Log.d("serial", "4");

        registerReceiver(broadcastReceiver, new IntentFilter(LocationUpdater.BD_KEY));
        //To set status icon dark
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //For mapfragment replace

        itsMySupportMapFragment = new MySupportMapFragment();
        MySupportMapFragment.MapViewCreatedListener mapViewCreatedListener = new MySupportMapFragment.MapViewCreatedListener() {
            @Override
            public void onMapCreated() {
                PGMaps();
            }
        };
        Log.d("serial", "7");
        itsMySupportMapFragment.itsMapViewCreatedListener = mapViewCreatedListener;
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.map, itsMySupportMapFragment);
        Log.d("serial", "beforecommit");
        transaction.commit();
        Log.d("serial", "afterccommit");

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


        map = googleMap;
        startService(i);
        //For location permission to show location for first time and move camera
        getLocationPermission();


    }

    //Taking googlemap object ref
    public void PGMaps() {
        //get ready for to recieve map
        itsMySupportMapFragment.getMapAsync(this);
        // Do what you want...
    }

    public void getDeviceLocation() {

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            map.setMyLocationEnabled(true);
            Log.d("Mainactivity", "inside try");
            mFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location == null)
                        Log.d("Mainactivity", "Location not found");
                    else {
                        Log.d("Mainactivity", "Location fouund");
                        moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), DfZoom);
                    }
                }
            });
        }
        catch (Exception e){
            Log.d("Mainactivity", "Permisssion not granted");

        }
    }
   public void internetStatus()
   {
       if(CheckNetwork.isInternetAvailable(MainActivity.this)) //returns true if internet available
       {

           //do something. loadwebview.
           Toast.makeText(getApplicationContext(),"Internet Connection",Toast.LENGTH_LONG).show();

       }
       else
       {
           View parentlayout = findViewById(android.R.id.content);
           Snackbar.make(parentlayout, "No internet Connection", Snackbar.LENGTH_INDEFINITE)
                   .setAction("Try Again", new View.OnClickListener() {;
                       @Override
                       public void onClick(View view) {

                           Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                           startActivity(intent);

                       }
                   })
                   .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                   .show();
       }
   }
    private void getLocationPermission() {
        String[] permission = {fl, cl};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), fl) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), cl) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                getDeviceLocation();
            } else
                ActivityCompat.requestPermissions(this, permission, per_req_code);
        } else {
            ActivityCompat.requestPermissions(this, permission, per_req_code);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case per_req_code: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++)
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                }
                mLocationPermissionGranted = true;
                getDeviceLocation();
               // init();
            }

        }
    }

    public void moveCamera(LatLng latLng, float z)
    {
        Log.d("serial", "8");
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,z));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_first_layout) {
               Intent i= new Intent(this, Update.class);
               startActivity(i);
        } else if (id == R.id.nav_second_layout) {

        } else if (id == R.id.nav_third_layout) {

        }  else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
   /* private void loadFragment(Fragment fragment) {
        // create a FragmentManager
        // create a FragmentTransaction to begin the transaction and replace the Fragment
        fragmentTransaction=getSupportFragmentManager().beginTransaction();
        // replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        // save the changes
        fragmentTransaction.commit();
    }*/
   @Override
   public void onBackPressed() {
       drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
       if (drawer.isDrawerOpen(GravityCompat.START)) {
           drawer.closeDrawer(GravityCompat.START);
       } else {
              moveTaskToBack(true);
       }
   }
    @Override
    protected void onStop() {
        Toast.makeText(this, "On Stop", Toast.LENGTH_SHORT).show();
        super.onStop();
        if(flag==0) {
            unregisterReceiver(broadcastReceiver);
            stopService(i);
        }
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(this, "On Destroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    public void myPosition(View v){
        getDeviceLocation();
    }
    @Override
    public void onResume() {
        Toast.makeText(this, "On Resume", Toast.LENGTH_SHORT).show();
        super.onResume();
        if(flag==0) {
            startService(i);
            registerReceiver(broadcastReceiver, new IntentFilter(LocationUpdater.BD_KEY));
        }
    }
//Location send function
    private class SendLocation extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try {

                //internetStatus();
                URL url = new URL("http://192.168.43.29/cgi-bin/Pune/GPSUpdate/GPSUpdate.out"); // here is your URL path

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                String cc = prefs.getString("countryCode", null);
                String sc = prefs.getString("stateCode", null);
                String restoredText = cc+sc+prefs.getString("vehicleno", null);
                Log.d("status", restoredText);
                String restoredText11 = prefs.getString("phoneno1", null);

                //  Toast.makeText(getApplicationContext(),restoredText,Toast.LENGTH_LONG).show();

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
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            String restoredText = prefs.getString("detail", null);
            Toast.makeText(getApplicationContext(),restoredText,Toast.LENGTH_LONG).show();
            Log.d("gpsupdateresponse",result);
          Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

}

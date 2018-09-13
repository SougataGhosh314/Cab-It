package com.example.skdj.beta1;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
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
import android.os.Vibrator;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
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

import ng.max.slideview.SlideView;

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

        registerReceiver(broadcastReceiver, new IntentFilter(LocationUpdater.BD_KEY));


        setContentView(R.layout.activity_main);
        //To check if opened from notification
         if(getIntent().getIntExtra("FromNotification", 0)!=0)
         {
             Log.d("Location", "Intent");
             final View namebar = findViewById(R.id.slideView);
             final ViewGroup parent = (ViewGroup) namebar.getParent();
             if (parent != null) {
                 parent.removeView(namebar);
         }

             MaterialStyledDialog dialog = new MaterialStyledDialog.Builder(this)
                     .setTitle("Awesome!")
                     .setDescription("What can we improve? Your feedback is always welcome.")
                     .setPositiveText("Go Down")
                     .onPositive(new MaterialDialog.SingleButtonCallback(){
                         @Override
                         public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                         {
                             stopService(new Intent(MainActivity.this, LocationUpdater.class));
                             parent.addView(namebar);
                             SlideView slideView = (SlideView) findViewById(R.id.slideView);
                             slideView.setOnSlideCompleteListener(new SlideView.OnSlideCompleteListener() {
                                 @Override
                                 public void onSlideComplete(SlideView slideView) {
                                     View namebar = findViewById(R.id.slideView);
                                     ViewGroup parent = (ViewGroup) namebar.getParent();
                                     if (parent != null) {
                                         parent.removeView(namebar);
                                     }
                                     startService(new Intent(MainActivity.this, LocationUpdater.class));
                                 }
                             });
                         }
                     })
                     .setNegativeText("Cancel")
                     .build();

             dialog.show();

         }
         else
         {
             if(isMyServiceRunning(LocationUpdater.class)==true) {
                 View namebar = findViewById(R.id.slideView);
                 ViewGroup parent = (ViewGroup) namebar.getParent();
                 if (parent != null) {
                     parent.removeView(namebar);
                 }

             }
            else {
                 SlideView slideView = (SlideView) findViewById(R.id.slideView);
                 slideView.setOnSlideCompleteListener(new SlideView.OnSlideCompleteListener() {
                     @Override
                     public void onSlideComplete(SlideView slideView) {
                         View namebar = findViewById(R.id.slideView);
                         ViewGroup parent = (ViewGroup) namebar.getParent();
                         if (parent != null) {
                             parent.removeView(namebar);
                         }
                         startService(new Intent(MainActivity.this, LocationUpdater.class));
                     }
                 });
             }
         }
        //service Intent initialization
        i=new Intent(this, LocationUpdater.class);

        //for drawerlayout
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Log.d("serial", "4");

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
        //For location permission to show location for first time and move camera
        getLocationPermission();
        map.getUiSettings().setMyLocationButtonEnabled(false);


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
   @Override
   public void onBackPressed() {
       drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
       if (drawer.isDrawerOpen(GravityCompat.START)) {
           drawer.closeDrawer(GravityCompat.START);
       } else {
           unregisterReceiver(broadcastReceiver);
           moveTaskToBack(true);
       }
   }
    @Override
    protected void onStop() {
        Toast.makeText(this, "On Stop", Toast.LENGTH_SHORT).show();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(this, "On Destroy", Toast.LENGTH_SHORT).show();
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    public void myPosition(View v){
        getDeviceLocation();
    }
    @Override
    public void onResume() {
        registerReceiver(broadcastReceiver, new IntentFilter(LocationUpdater.BD_KEY));
        Toast.makeText(this, "On Resume", Toast.LENGTH_SHORT).show();
        super.onResume();
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}

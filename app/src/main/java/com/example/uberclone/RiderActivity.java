package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;
    Button CallUber;
    Handler handler = new Handler();
    Boolean request_active=false;
    Boolean driverActive = false;
    TextView update_dis;

    public void checkForUpdates(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.whereExists("driverusername");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null && objects.size()>0){
                    CallUber.setVisibility(View.INVISIBLE);
                    driverActive = true;
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("username",objects.get(0).getString("driverusername"));
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {
                            if(e==null && objects.size()>0){
                                final ParseGeoPoint driverLocation = objects.get(0).getParseGeoPoint("Location");
                                if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(RiderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    update_dis =(TextView) findViewById(R.id.textView3);
                                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    if(lastKnownLocation!=null){
                                        final ParseGeoPoint userlocation = new ParseGeoPoint(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
                                        Double Distanceinmiles = driverLocation.distanceInKilometersTo(userlocation);
                                        Double distanceInDp = (double) Math.round(Distanceinmiles * 10) / 10;
                                        if(distanceInDp <0.1){
                                            update_dis.setText("Your driver is here!");

                                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");
                                            query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                                            query.findInBackground(new FindCallback<ParseObject>() {
                                                @Override
                                                public void done(List<ParseObject> objects, ParseException e) {
                                                    if(e==null && objects.size()>0){
                                                        for(ParseObject object: objects){
                                                            object.deleteInBackground();
                                                        }
                                                    }
                                                }

                                            });
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    update_dis.setText("");
                                                    CallUber.setVisibility(View.VISIBLE);
                                                    CallUber.setText("CALL UBER");
                                                    request_active=false;
                                                    driverActive=false;
                                                }
                                            },5000);
                                        }
                                        else {
                                            update_dis.setText("Your driver is " + distanceInDp.toString() + " kms away");

                                                    RelativeLayout mapLayout = (RelativeLayout)findViewById(R.id.map_layout);
                                                    mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                                        @Override
                                                        public void onGlobalLayout() {
                                                            ArrayList<Marker> markers = new ArrayList<>();
                                                            LatLng driver = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());
                                                            LatLng user = new LatLng(userlocation.getLatitude(), userlocation.getLongitude());
                                                            markers.add(mMap.addMarker(new MarkerOptions().title("Your location").position(driver)));
                                                            markers.add(mMap.addMarker(new MarkerOptions().title(" Destination").position(user)));
                                                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                            for (Marker marker : markers) {
                                                                builder.include(marker.getPosition());
                                                            }
                                                            LatLngBounds bounds = builder.build();
                                                            int padding = 60; // offset from edges of the map in pixels
                                                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                                            mMap.animateCamera(cu);
                                                            mMap.setMaxZoomPreference(14);

                                                        }
                                                    });

                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                checkForUpdates();
                                                        }
                                                    },2000);
                                                }


                                        }

                                    }
                                }
                        }
                    });
                }
            }
        });
    }

    public void logout(View view){
        ParseUser.logOut();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }


    public void Calluber(View view) {
        if(request_active){
            ParseQuery query = new ParseQuery("Requests");
            query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List <ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0) {
                            for (ParseObject object:objects) {
                                    object.deleteInBackground();

                                            }
                                        }

                            }
                        }

            });
            request_active = false;
            CallUber.setText("CALL AN UBER");
            Log.i("Info", "Cancelled uber");
        }else{
        Log.i("Info", "Called an uber");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location lastknownlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastknownlocation != null) {
                ParseObject request = new ParseObject("Requests");
                request.put("username", ParseUser.getCurrentUser().getUsername());
                ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastknownlocation.getLatitude(), lastknownlocation.getLongitude());
                request.put("Location", parseGeoPoint);
                request.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(getApplicationContext(), "Your ride has been booked", Toast.LENGTH_LONG).show();
                            CallUber.setText("CANCEL UBER");
                            request_active=true;
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Sorry your location could not be traced", Toast.LENGTH_LONG).show();
            }
        }
        }
    }
    public void updateMap(Location location){
      if(driverActive==false) {
          LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
          mMap.clear();
          mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
          mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
      }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1)
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                updateMap(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            }}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        CallUber = (Button) findViewById(R.id.uberbutton) ;
        ParseQuery query = new ParseQuery("Requests");
        query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List objects, ParseException e) {
                if(e==null){
                    if(objects.size()>0){
                        request_active=true;
                        CallUber.setText("CANCEL UBER");
                        checkForUpdates();
                    }
                }
            }

            @Override
            public void done(Object o, Throwable throwable) {

            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateMap(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if(Build.VERSION.SDK_INT<23){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

        }else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }

            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastknownlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastknownlocation!=null){
                    updateMap(lastknownlocation);
                }
            }
        }


    }
}

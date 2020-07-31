package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestsActivity extends AppCompatActivity {

    ListView requestListView;
    ArrayAdapter arrayAdapter;
    ArrayList<String> requestList ;
    ArrayList<Double> requestLatitude = new ArrayList<Double>();
    ArrayList<Double> requestLongitude= new ArrayList<Double>();
    ArrayList<String> usernames= new ArrayList<String>();
    LocationManager locationManager;
    LocationListener locationListener;



    public void updateListView(Location location){
        if(location!=null){

            final ParseGeoPoint Driverlocation = new ParseGeoPoint(location.getLatitude(),location.getLongitude());
            ParseQuery parseQuery = ParseQuery.getQuery("Requests");
            parseQuery.whereNear("Location",Driverlocation);
            parseQuery.whereDoesNotExist("driverusername");
            parseQuery.setLimit(10);
            parseQuery.findInBackground(new FindCallback <ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null){
                        requestList.clear();
                        requestLongitude.clear();
                        requestLatitude.clear();

                        if(objects.size()>0){
                            for(ParseObject object:objects){
                                ParseGeoPoint riderlocation = object.getParseGeoPoint("Location");
                                if(riderlocation!=null) {
                                    Double Distanceinmiles = Driverlocation.distanceInKilometersTo(riderlocation);
                                    Double distanceInDp = (double) Math.round(Distanceinmiles * 10) / 10;
                                    requestList.add(distanceInDp.toString() + " Kms");
                                    requestLatitude.add(riderlocation.getLatitude());
                                    requestLongitude.add(riderlocation.getLongitude());
                                    usernames.add(object.getString("username"));
                                }
                            }
                        }
                        else{
                            requestList.add("No active requests nearby");
                        }

                        arrayAdapter.notifyDataSetChanged();
                    }

                }


            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1)
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    updateListView(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                }}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests_activiy);
        setTitle("Nearby Requests");
        requestListView = (ListView) findViewById(R.id.listview1);
        requestList = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,requestList);
        requestList.clear();
        requestListView.setAdapter(arrayAdapter);
        requestList.add("Getting nearby requests..");
        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(Build.VERSION.SDK_INT<23 || ContextCompat.checkSelfPermission(ViewRequestsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastknownlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(requestLatitude.size()>position && requestLongitude.size()>position && usernames.size()>0){
                    Intent intent = new Intent(getApplicationContext(),DriverLocationActivity.class);
                    intent.putExtra("request latitude",requestLatitude.get(position));
                    intent.putExtra("request longitude",requestLongitude.get(position));
                    intent.putExtra("Driver latitude",lastknownlocation.getLatitude());
                    intent.putExtra("Driver longitude",lastknownlocation.getLongitude());
                    intent.putExtra("username", usernames.get(position));
                    startActivity(intent);
                }
            }
        });
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateListView(location);
                ParseUser.getCurrentUser().put("Location", new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();
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
                    updateListView(lastknownlocation);
                }
            }
        }

    }
}

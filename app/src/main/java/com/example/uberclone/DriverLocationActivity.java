package com.example.uberclone;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriverLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Intent intent;

    public void accept_request(View view){
        ParseQuery<ParseObject> query = new ParseQuery("Requests");
        query.whereEqualTo("username", intent.getStringExtra("username"));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    if(objects.size()>0){
                        for(ParseObject object:objects){
                            object.put("driverusername", ParseUser.getCurrentUser().getUsername());
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        String uri = String.format(Locale.ENGLISH,
                                                "http://maps.google.com/maps?saddr=%f,%f(%s)&daddr=%f,%f (%s)",
                                                intent.getDoubleExtra("Driver latitude", 0), intent.getDoubleExtra("Driver longitude", 0), "Your location", intent.getDoubleExtra("request latitude", 0), intent.getDoubleExtra("request longitude", 0), "Destination");
                                        Intent mapintent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                        mapintent.setPackage("com.google.android.apps.maps");
                                        startActivity(mapintent);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_location);

        intent = getIntent();

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
        RelativeLayout mapLayout = (RelativeLayout)findViewById(R.id.map_layout);
        mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ArrayList<Marker> markers =new ArrayList<>();
                LatLng driverlocation = new LatLng(intent.getDoubleExtra("Driver latitude",0),intent.getDoubleExtra("Driver longitude",0));
                LatLng userlocation = new LatLng(intent.getDoubleExtra("request latitude",0),intent.getDoubleExtra("request longitude",0));
                markers.add( mMap.addMarker(new MarkerOptions().title("Your location").position(driverlocation)));
                markers.add(mMap.addMarker(new MarkerOptions().title(" Destination").position(userlocation)));
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




    }
}

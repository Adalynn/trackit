package com.example.adalynn.pushit2.activity;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.adalynn.pushit2.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class AniMapView extends AppCompatActivity implements OnMapReadyCallback {

    //private GoogleMap mMap;
    public double latitude;
    public double longitute;
    public String contact_number;
    public static final String EXTRA_MESSAGE_MAP_VIEW = "Message Set in contactLsist activivty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ani_map_view);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        latitude = Double.parseDouble(extras.getString("latitude"));
        longitute = Double.parseDouble(extras.getString("longitute"));
        contact_number = extras.getString("contact_number");
        Toast.makeText(getApplicationContext(), " Latitude  : " + latitude + " Longitude : " + longitute, Toast.LENGTH_LONG).show();
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
        //mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        LatLng sydney = new LatLng(latitude, longitute);
        googleMap.addMarker(new MarkerOptions().position(sydney).title(contact_number));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        googleMap.setMaxZoomPreference(6);
    }
}

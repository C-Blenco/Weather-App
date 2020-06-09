package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class SplashActivity extends AppCompatActivity {
    private LatLng latLong;
    private String locName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Button getLocationButton = findViewById(R.id.getLocationButton);

        Places.initialize(getApplicationContext(), getApplicationContext().getString(R.string.MAPS_API_KEY));


        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG));

        // Only display cities in results
        autocompleteFragment.setTypeFilter(TypeFilter.CITIES);

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.d("Splash", "Name: " + place.getName());
                Log.d("Splash", "LatLong: " + place.getLatLng());
                latLong = place.getLatLng();
                locName = place.getName();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.d("Splash", "An error occurred: " + status);
            }
        });

        getLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (latLong != null) {
                    Intent intent = new Intent();
                    intent.putExtra("LATITUDE", latLong.latitude);
                    intent.putExtra("LONGITUDE", latLong.longitude);
                    intent.putExtra("NAME", locName);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else {
                    Toast.makeText(SplashActivity.this, "Please select a location first", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

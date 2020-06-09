package com.example.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    // Create response listener to pass to RequestAPI
    private Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject response) {
            // Toast for test purposes
            // Toast.makeText(MainActivity.this, "Response " + response.toString(), Toast.LENGTH_LONG).show();
            Log.d("MainActivity ", "onResponse");
            updateLocation(response);
        }
    };

    // Create error listener to pass to RequestAPI
    private Response.ErrorListener errorListener = new Response.ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(MainActivity.this, "Error requesting weather data", Toast.LENGTH_LONG).show();
            Log.d("MainActivity ", "onErrorResponse");
        }
    };

    Location current_location;
    String locName;
    ArrayList<Location> savedLoc = new ArrayList<Location>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestAPI.getInstance(this);

        Gson gson = new Gson();
        Type savedLocType = new TypeToken<ArrayList<Location>>(){}.getType();

        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        String prefs = sharedPreferences.getString("locations", "");
        savedLoc = gson.fromJson(prefs, savedLocType);

        startSplash();

        ImageView favButton = findViewById(R.id.favouriteButton);
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFavourite();
            }
        });
    }

    public void startSplash() {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivityForResult(intent, 1);
    }


    private void updateLocation(JSONObject jsonObject) {
        // TODO: if the Location object already created for "name", update instead of new Location object
        current_location = new Location(jsonObject, locName);

        Log.d("MainActivity", "updateLocation");
        TextView temp = findViewById(R.id.tempText);
        TextView loc = findViewById(R.id.locText);
        TextView desc = findViewById(R.id.descText);

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        temp.setText(current_location.getTemp() + "\u00B0");
        loc.setText(current_location.getName());
        desc.setText(current_location.getDescription());
    }

    private void createRequest(double latitude, double longitude) {
        RequestAPI.getInstance().requestJSON(latitude, longitude, responseListener, errorListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Double latitude = data.getDoubleExtra("LATITUDE", 0);
            Double longitude = data.getDoubleExtra("LONGITUDE", 0);
            locName = data.getStringExtra("NAME");

            Log.d("MainActivity", latitude + " " + longitude);

            createRequest(latitude, longitude);
        }
    }

    private void addFavourite() {
        if (savedLoc.contains(current_location)) {
            Log.d("MainActivity", "Location already saved");
        }
        else {
            savedLoc.add(current_location);
        }
    }
    // TODO: Create update fields function to update visual display from current_location Location object
}

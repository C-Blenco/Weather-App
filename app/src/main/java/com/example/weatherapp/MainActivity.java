package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity {

    // Create response listener to pass to RequestAPI
    private Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject response) {
            // Toast for test purposes
            Toast.makeText(MainActivity.this, "Response " + response.toString(), Toast.LENGTH_LONG).show();
            Log.d("MainActivity ", "onResponse");
            updateLocation(response);
        }
    };

    // Create error listener to pass to RequestAPI
    private Response.ErrorListener errorListener = new Response.ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(MainActivity.this, "Response " + error.toString(), Toast.LENGTH_LONG).show();
            Log.d("MainActivity ", "onErrorResponse");
        }
    };

    Location current_location;
    String locName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startSplash();
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
        RequestAPI.requestJSON(MainActivity.this, latitude, longitude, responseListener, errorListener);
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

    // TODO: Create update fields function to update visual display from current_location Location object
}

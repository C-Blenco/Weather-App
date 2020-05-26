package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Button for testing
        Button button = findViewById(R.id.button);

        // On click, request dummy weather data
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestAPI.requestJSON(MainActivity.this, -37.8259, 145.0972, responseListener, errorListener);
            }
        });

        startSplash();
    }

    public void startSplash() {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
    }


    private void updateLocation(JSONObject jsonObject) {
        // TODO: if the Location object already created for "name", update instead of new Location object
        current_location = new Location(jsonObject);

        Log.d("MainActivity", "updateLocation");
        TextView text = findViewById(R.id.textView);

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        text.setText(current_location.getTemp() + format1.format(current_location.getDatetimeAsof()));
    }

    // TODO: Create update fields function to update visual display from current_location Location object
}

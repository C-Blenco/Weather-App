package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    // Create response listener to pass to RequestAPI
    private Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject response) {
            Toast.makeText(MainActivity.this, "Response " + response.toString(), Toast.LENGTH_LONG).show();
            Log.d("MainActivity ", "onResponse");
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
                RequestAPI.requestJSON(MainActivity.this, 37.8259, 145.0972, responseListener, errorListener);
            }
        });



    }
}

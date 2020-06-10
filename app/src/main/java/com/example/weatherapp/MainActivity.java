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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

// SimpleLoc is used to store as an array of favourited locations. It is used over Location
// as gson is unable to initialise a Location object from a json string
class SimpleLoc {
    public double latitude, longitude;
    public String name;

    public SimpleLoc(double latitude, double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public boolean equals(SimpleLoc loc) {
        return (loc.name == this.name);
    }
}

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs = null;
    private SharedPreferences.Editor prefEditor;

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

    Location currentLocation;
    SimpleLoc currentSimpleLoc;
    String locName;
    ArrayList<SimpleLoc> savedLoc = new ArrayList<SimpleLoc>();
    boolean favourited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestAPI.getInstance(this);

        Gson gson = new Gson();
        Type savedLocType = new TypeToken<ArrayList<SimpleLoc>>(){}.getType();

        prefs = this.getPreferences(Context.MODE_PRIVATE);
        prefEditor = prefs.edit();

        String locArrayString = prefs.getString("locations", "");

        if (locArrayString != "") {
            Log.d("MainActivity", locArrayString);
            savedLoc = gson.fromJson(locArrayString, savedLocType);
        }

        if (savedLoc.isEmpty()) {
            startSplash();
        }
        else {
            SimpleLoc loc = savedLoc.get(0);
            currentSimpleLoc = loc;
            createRequest(loc.latitude, loc.longitude);
            locName = loc.name;
            favourited = true;
        }

        final ImageView favButton = findViewById(R.id.favouriteButton);
        if (favourited) {
            favButton.setImageResource(R.drawable.star_solid);
        }

        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favourited) {
                    removeFavourite();
                    favButton.setImageResource(R.drawable.star_empty);
                    favourited = false;
                }
                else {
                    addFavourite();
                    favButton.setImageResource(R.drawable.star_solid);
                    favourited = true;
                }
            }
        });
    }

    private void populateRecycler() {
        // Get recycler and set layoutmanager
        RecyclerView dailyRecylcer = findViewById(R.id.dailyRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        dailyRecylcer.setLayoutManager(layoutManager);

        // Adapter
        DailyAdapter dailyAdapter = new DailyAdapter(currentLocation.getDailyTemp());
        dailyRecylcer.setAdapter(dailyAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(dailyRecylcer.getContext(),
                layoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        dailyRecylcer.addItemDecoration(dividerItemDecoration);
    }

    public void startSplash() {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivityForResult(intent, 1);
    }


    private void updateLocation(JSONObject jsonObject) {
        // TODO: if the Location object already created for "name", update instead of new Location object
        currentLocation = new Location(jsonObject, locName);

        Log.d("MainActivity", "updateLocation");
        TextView temp = findViewById(R.id.tempText);
        TextView loc = findViewById(R.id.locText);
        TextView desc = findViewById(R.id.descText);

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        temp.setText(currentLocation.getTemp() + "\u00B0");
        loc.setText(currentLocation.getName());
        desc.setText(currentLocation.getDescription());

        populateRecycler();
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
        SimpleLoc loc = new SimpleLoc(currentLocation.getLatitude(), currentLocation.getLongitude(), currentLocation.getName());

        for (int i = 0; i < savedLoc.size(); i++) {
            if (loc.equals(savedLoc.get(i))) {
                Log.d("MainActivity", "Location already saved");
                return;
            }
        }
        Log.d("MainActivity", "Add favourite " + loc.name);
        savedLoc.add(loc);
        currentSimpleLoc = loc;
        Gson gson = new Gson();
        String savedLocJson = gson.toJson(savedLoc);
        prefEditor.putString("locations", savedLocJson);
        prefEditor.commit();
    }

    private void removeFavourite() {
        Log.d("MainActivity", "Remove favourite " + currentSimpleLoc.name);
        savedLoc.remove(currentSimpleLoc);
        Gson gson = new Gson();
        String savedLocJson = gson.toJson(savedLoc);
        prefEditor.putString("locations", savedLocJson);
        prefEditor.commit();
    }
    // TODO: Create update fields function to update visual display from currentLocation Location object
}

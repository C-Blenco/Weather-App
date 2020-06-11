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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Response.Listener<JSONObject> newResponseListener = new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject response) {
            // Toast for test purposes
//            Toast.makeText(MainActivity.this, "Response " + response.toString(), Toast.LENGTH_LONG).show();
            Log.d("MainActivity ", "onResponse (new)");
            createLocation(response);
        }
    };
    private Response.Listener<JSONObject> updateResponseListener = new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject response) {
            Log.d("MainActivity ", "onResponse (update)");
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
    String locName;
    Map<String, List<Double>> favourites = new HashMap<String, List<Double>>();
    RecyclerView dailyRecylcer;
    LinearLayoutManager layoutManager;
    ImageView favButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestAPI.getInstance(this);
        favButton = findViewById(R.id.favouriteButton);
        Places.initialize(getApplicationContext(), getApplicationContext().getString(R.string.MAPS_API_KEY));

        // Inititalise recycler
        dailyRecylcer = findViewById(R.id.dailyRecycler);
        layoutManager = new LinearLayoutManager(this);
        dailyRecylcer.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(dailyRecylcer.getContext(),
                layoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        dailyRecylcer.addItemDecoration(dividerItemDecoration);

        // Initialise gson
        Gson gson = new Gson();
        Type favMapType = new TypeToken<HashMap<String, List<Double>>>(){}.getType();

        // Get preferences and initialise editor
        prefs = this.getPreferences(Context.MODE_PRIVATE);
        prefEditor = prefs.edit();
        String favMapString = prefs.getString("favourites", "");

        // If preferences exist, get the saved locations
        if (favMapString != "") {
            Log.d("MainActivity", favMapString);
            favourites = gson.fromJson(favMapString, favMapType);
        }

        // If the saved locations is empty (none are saved), start the splash
        if (favourites.isEmpty()) {
            startSplash();
        }
        // Otherwise, choose the first favourited location and create request for it
        else {
            Map.Entry<String, List<Double>> entry = favourites.entrySet().iterator().next();
            switchLocation(entry.getValue().get(0), entry.getValue().get(1), entry.getKey());
        }

        // Initialise favourited button and set onclick listener to remove/add favourite
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavourite();
            }
        });

        // Initialise refresh button and set onclick listener to refresh current location
        final ImageView refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRequest();
            }
        });

        // Initialise search button and set onclick listener to start search activity
        final ImageView searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch();
            }
        });
    }

    public void startSplash() {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivityForResult(intent, 1);
    }

    public void startSearch() {
        List<Place.Field> fields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);

        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, 2);
    }

    private void createLocation(JSONObject jsonObject) {
        Location location = new Location(locName);
        currentLocation = location;
        updateLocation(jsonObject);
    }

    public void updateLocation(JSONObject jsonObject) {
        currentLocation.updateData(jsonObject);

        Log.d("MainActivity", "updateLocation");
        TextView temp = findViewById(R.id.tempText);
        TextView loc = findViewById(R.id.locText);
        TextView desc = findViewById(R.id.descText);
        TextView refresh = findViewById(R.id.refreshText);

        temp.setText(currentLocation.getTemp() + "\u00B0");
        loc.setText(currentLocation.getName());
        desc.setText(currentLocation.getDescription());

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy 'at' h:mm a");
        String time = format.format(currentLocation.getDatetimeRequested());
        refresh.setText("Last refresh:\n" + time);

        populateRecycler();
    }

    private void populateRecycler() {
        // Adapter
        DailyAdapter dailyAdapter = new DailyAdapter(currentLocation.getDailyTemp());
        dailyRecylcer.setAdapter(dailyAdapter);
    }

    private void createRequest() {
        RequestAPI.getInstance().requestJSON(currentLocation.getLatitude(), currentLocation.getLongitude(), updateResponseListener, errorListener);
    }

    private void createRequest(double latitude, double longitude) {
        RequestAPI.getInstance().requestJSON(latitude, longitude, newResponseListener, errorListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Double latitude = data.getDoubleExtra("LATITUDE", 0);
            Double longitude = data.getDoubleExtra("LONGITUDE", 0);
            String name = data.getStringExtra("NAME");

            Log.d("MainActivity", latitude + " " + longitude);

            switchLocation(latitude, longitude, name);
        }
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                String name = place.getName();
                LatLng coords = place.getLatLng();

                switchLocation(coords.latitude, coords.longitude, name);
            }
        }
    }

    private void toggleFavourite() {
        if (favourites.containsKey(currentLocation.getName())) {
            favourites.remove(currentLocation.getName());
            favButton.setImageResource(R.drawable.star_empty);
        }
        else {
            List<Double> coords = Arrays.asList(currentLocation.getLatitude(), currentLocation.getLongitude());
            favourites.put(currentLocation.getName(), coords);
            favButton.setImageResource(R.drawable.star_solid);
        }
        Gson gson = new Gson();
        String favouriteGson = gson.toJson(favourites);
        prefEditor.putString("favourites", favouriteGson);
        prefEditor.commit();
    }

    private void removeFavourite() {
        favourites.remove(currentLocation.getName());
        Gson gson = new Gson();
        String favouriteGson = gson.toJson(favourites);
        prefEditor.putString("locations", favouriteGson);
        prefEditor.commit();
    }

    private void switchLocation(Double latitude, Double longitude, String name) {
        if (favourites.containsKey(name)) {
            favButton.setImageResource(R.drawable.star_solid);
            createRequest(latitude, longitude);
            locName = name;
        }
        else {
            favButton.setImageResource(R.drawable.star_empty);
            createRequest(latitude, longitude);
            locName = name;
        }
    }
}

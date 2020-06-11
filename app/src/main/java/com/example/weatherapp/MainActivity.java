package com.example.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs = null;
    private SharedPreferences.Editor prefEditor;

    // Create response listener to pass to RequestAPI
    private Response.Listener<JSONObject> newResponseListener = new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject response) {
            // Toast for test purposes
            // Toast.makeText(MainActivity.this, "Response " + response.toString(), Toast.LENGTH_LONG).show();
            Log.d("MainActivity ", "onResponse (new)");
            createLocation(response);
        }
    };
    // Response listener for updating the current location (refresh)
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

    private Location currentLocation;
    private String locName;
    private Map<String, List<Double>> favourites = new LinkedHashMap<String, List<Double>>();
    private RecyclerView dailyRecylcer;
    private LinearLayoutManager layoutManager;
    private ImageView favButton;
    private Spinner locSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inititalise various components
        RequestAPI.getInstance(this);
        favButton = findViewById(R.id.favouriteButton);
        locSpinner = findViewById(R.id.locSpinner);
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
        Type favMapType = new TypeToken<LinkedHashMap<String, List<Double>>>(){}.getType();

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
            setSpinner();
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

        // Initialise the listener to detect when a new location is selected from spinner (favourites)
        locSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("MainActivity", "Spinner onItemSelected");
                String selectedLoc = parent.getItemAtPosition(position).toString();
                // If the location we select is a favourite, update location
                // this is done as spinners natively can activate from an update of the adapter,
                // therefore we do this to prevent querying the API multiple times.
                if (favourites.containsKey(selectedLoc)) {
                    List<Double> coords = favourites.get(selectedLoc);
                    switchLocation(coords.get(0), coords.get(1), selectedLoc);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("MainActivity", "Spinner onNothingSelected");
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
        Log.d("MainActivity", "updateLocation");
        currentLocation.updateData(jsonObject);

        TextView temp = findViewById(R.id.tempText);
        TextView desc = findViewById(R.id.descText);
        TextView refresh = findViewById(R.id.refreshText);

        // Set the temperature and description text
        temp.setText(currentLocation.getTemp() + "\u00B0");
        desc.setText(currentLocation.getDescription());

        // Format and set the date of last refresh/update
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy 'at' h:mm a");
        String time = format.format(currentLocation.getDatetimeRequested());
        refresh.setText("Last refresh:\n" + time);

        // Populate the daily weather recycler
        populateRecycler();
    }

    private void populateRecycler() {
        // Adapter
        DailyAdapter dailyAdapter = new DailyAdapter(currentLocation.getDailyTemp());
        dailyRecylcer.setAdapter(dailyAdapter);
    }

    // createRequest for update
    private void createRequest() {
        if (currentLocation != null) {
            RequestAPI.getInstance().requestJSON(currentLocation.getLatitude(), currentLocation.getLongitude(), updateResponseListener, errorListener);
        }
    }

    // createRequest for new location
    private void createRequest(double latitude, double longitude) {
        RequestAPI.getInstance().requestJSON(latitude, longitude, newResponseListener, errorListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        // SplashActivity result
        if (requestCode == 1) {
            Double latitude = data.getDoubleExtra("LATITUDE", 0);
            Double longitude = data.getDoubleExtra("LONGITUDE", 0);
            String name = data.getStringExtra("NAME");

            Log.d("MainActivity", latitude + " " + longitude);

            switchLocation(latitude, longitude, name);
        }
        // Search activity result
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
        // If current location is a favourite, remove it and toggle the star image
        if (favourites.containsKey(currentLocation.getName())) {
            favourites.remove(currentLocation.getName());
            favButton.setImageResource(R.drawable.star_empty);
        }
        // Else if current location is not a favourite, add it and toggle the star image
        else {
            List<Double> coords = Arrays.asList(currentLocation.getLatitude(), currentLocation.getLongitude());
            favourites.put(currentLocation.getName(), coords);
            favButton.setImageResource(R.drawable.star_solid);
        }
        // Save favourites using gson
        Gson gson = new Gson();
        String favouriteGson = gson.toJson(favourites);
        prefEditor.putString("favourites", favouriteGson);
        prefEditor.commit();
    }

    // Used to switch locations when a new one is selected or searched for
    private void switchLocation(Double latitude, Double longitude, String name) {
        // If the location is a favourite, ensure the image resource changes
        if (favourites.containsKey(name)) {
            Log.d("MainActivity", "switchLocation (fav)");
            favButton.setImageResource(R.drawable.star_solid);
        }
        else {
            Log.d("MainActivity", "switchLocation");
            favButton.setImageResource(R.drawable.star_empty);
        }
        createRequest(latitude, longitude);
        locName = name;
        setSpinner();
    }

    // boolean value for the first spinner call to prevent creating new adapter each time
    boolean setSpinnerCall = true;
    // stores the names of the favourite locations
    ArrayAdapter<String> spinnerArrayAdapter;
    private void setSpinner() {
        // Convert favourite keys to array for spinner adapter
        Set<String> keys = favourites.keySet();
        List<String> locList = new ArrayList<>(Arrays.asList(keys.toArray(new String[keys.size()])));
        // if location isn't a favourite, add to the front of the array and set spinner location
        // to it to display the name
        if (!favourites.containsKey(locName)) {
            locList.add(0, locName);
            locSpinner.setSelection(0);
        }
        // otherwise set it to the location of the favourited location
        else {
            locSpinner.setSelection(locList.indexOf(locName));
        }

        // if first spinnner call
        if (setSpinnerCall) {
            spinnerArrayAdapter = new ArrayAdapter<String>
                    (this, R.layout.spinner_layout,
                            locList);
            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
            locSpinner.setAdapter(spinnerArrayAdapter);
            setSpinnerCall = false;
        }
        // otherwise, update adapter
        else {
            spinnerArrayAdapter.clear();
            spinnerArrayAdapter.addAll(locList);
            spinnerArrayAdapter.notifyDataSetChanged();
        }
    }
}

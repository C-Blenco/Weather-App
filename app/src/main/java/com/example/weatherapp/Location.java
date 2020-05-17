package com.example.weatherapp;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Location {
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTemp() {
        // TODO: Round up
        return Double.toString(temp);
    }

    public String getDescription() {
        return description;
    }

    public int getDatetimeAsof() {
        return datetime_asof;
    }

    public int getDatetimeRequested() {
        return datetime_requested;
    }

    private String name; // will have to be set from initial geocoding
    private double latitude;
    private double longitude;
    private double temp;
    private String description; // Weather description (e.g. cloudy)
    private int datetime_asof; // The datetime returned by the API
    private int datetime_requested; // datetime of last request/refresh

    public Location(JSONObject jsonObject) {
        try {
            processJSON(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void processJSON(JSONObject jsonObject) throws JSONException {
        // Get longitude and latitude
        latitude = jsonObject.getDouble("lat");
        longitude = jsonObject.getDouble("lon");

        // Get current data
        JSONObject current = jsonObject.getJSONObject("current");
        temp = current.getDouble("temp");
        // description is stored in a JSONObject inside an array
        description = current.getJSONArray("weather").optJSONObject(0).getString("main");
        datetime_asof = current.getInt("dt");

        // TODO: Set datetime requested
    }

    // TODO:
    //  - Create updateData function to update from JSONObject
}

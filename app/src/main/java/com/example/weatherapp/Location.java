package com.example.weatherapp;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Location {
    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTemp() {
        // TODO: Round up
        return String.valueOf((int) temp);
    }

    public String getDescription() {
        return description;
    }

    public Date getDatetimeAsof() {
        return datetime_asof;
    }

    public Date getDatetimeRequested() {
        return datetime_requested;
    }

    private String name;
    private double latitude;
    private double longitude;
    private double temp;
    private String description; // Weather description (e.g. cloudy)
    private Date datetime_asof; // The datetime returned by the API
    private Date datetime_requested; // datetime of last request/refresh

    public Location(JSONObject jsonObject, String locName) {
        name = locName;
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
        datetime_asof = new Date(current.getLong("dt"));

        // TODO: Set datetime requested
    }

    // TODO:
    //  - Create updateData function to update from JSONObject
}

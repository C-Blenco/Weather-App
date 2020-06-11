package com.example.weatherapp;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

class Daily {
    public Date day;
    public double max;
    public double min;
    public String description;

    public String getMin() {
        return String.valueOf((int) min);
    }
    public String getMax() {
        return String.valueOf((int) max);
    }
}

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

    public Date getDatetimeRequested() {
        return datetime_requested;
    }

    public Daily[] getDailyTemp() {
        return daily_temp;
    }

    private String name;
    private double latitude;
    private double longitude;
    private double temp;
    private String description; // Weather description (e.g. cloudy)
    private Date datetime_requested; // datetime of last request/refresh
    private Daily[] daily_temp = new Daily[8];

    public Location(String locName) {
        name = locName;
    }

    private void processJSON(JSONObject jsonObject) throws JSONException {
        // Get longitude and latitude
        latitude = jsonObject.getDouble("lat");
        longitude = jsonObject.getDouble("lon");

        // Get current data
        JSONObject current = jsonObject.getJSONObject("current");
        temp = current.getDouble("temp");
        // description is stored in a JSONObject inside an array
        description = current.getJSONArray("weather").getJSONObject(0).getString("main");
        datetime_requested = new Date();

        // Get daily data
        JSONArray daily = jsonObject.getJSONArray("daily");
        for (int i = 0; i < daily.length(); i++) {
            Daily new_day = new Daily();
            JSONObject day = daily.getJSONObject(i);
            JSONObject temp = day.getJSONObject("temp");

            new_day.description = day.getJSONArray("weather").getJSONObject(0).getString("icon");
            new_day.day = new Date(day.getLong("dt") * 1000);
            new_day.max = temp.getDouble("max");
            new_day.min = temp.getDouble("min");
            daily_temp[i] = new_day;
        }
    }

    public void updateData(JSONObject jsonObject) {
        try {
            processJSON(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

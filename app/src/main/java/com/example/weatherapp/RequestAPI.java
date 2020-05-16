package com.example.weatherapp;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class RequestAPI {
    public static void requestJSON(Context context, double latitude, double longitude,
                                   Response.Listener<JSONObject> responseListener,
                                   Response.ErrorListener errorListener) {
        // Formattable url to imput lat, long and API key
        String url = "https://api.openweathermap.org/data/2.5/onecall?lat=%f&lon=%f&exclude=hourly,minutely&units=metric&appid=%s";
        // Format string with passed in lat, long and API key
        url = String.format(url, latitude, longitude, context.getString(R.string.OPENWEATHER_API_KEY));
        Log.d("RequestAPI ", url);

        // Create a new Volley request queue
        RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());

        // Create JsonObjectRequest with url and listeners
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, responseListener, errorListener);

        // Add request to queue
        queue.add(jsonObjectRequest);
        Log.d("RequestAPI ", "requestJSON");
    }
}

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
    private static RequestAPI instance = null;
    public RequestQueue queue;
    private Context appContext;

    private RequestAPI(Context context)
    {
        // Create a new Volley request queue
         queue = Volley.newRequestQueue(context.getApplicationContext());
         appContext = context;
    }

    public static RequestAPI getInstance(Context context) {
        if (instance == null)
            instance = new RequestAPI(context);
        return instance;
    }

    public static RequestAPI getInstance() {
        if (instance == null) {
            Log.d("RequestAPI", "Instance not initialised");
        }
        return instance;
    }

    public void requestJSON(double latitude, double longitude,
                                   Response.Listener<JSONObject> responseListener,
                                   Response.ErrorListener errorListener) {
        // Formattable url to imput lat, long and API key
        String url = "https://api.openweathermap.org/data/2.5/onecall?lat=%f&lon=%f&exclude=hourly,minutely&units=metric&appid=%s";
        // Format string with passed in lat, long and API key
        url = String.format(url, latitude, longitude, appContext.getString(R.string.OPENWEATHER_API_KEY));
        Log.d("RequestAPI ", url);

        // Create JsonObjectRequest with url and listeners
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, responseListener, errorListener);

        // Add request to queue
        queue.add(jsonObjectRequest);
        Log.d("RequestAPI ", "requestJSON");
    }
}

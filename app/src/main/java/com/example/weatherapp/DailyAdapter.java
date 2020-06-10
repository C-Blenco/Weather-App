package com.example.weatherapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.DailyViewHolder> {
    Daily[] dailyData;

    public static class DailyViewHolder extends RecyclerView.ViewHolder {
        public TextView date, temp;
        public ImageView desc;

        // The view holder allows us to get the resource IDs of the text views
        public DailyViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.dateText);
            temp = itemView.findViewById(R.id.tempText);
            desc = itemView.findViewById(R.id.descImage);
        }

    }

    // Constructor for the adapter, we pass in the news array created in main to
    // set to the daily temp array
    public DailyAdapter(Daily[] dailyData) {
        this.dailyData = dailyData;
    }

    @NonNull
    @Override
    // Creates the view holder with the daily_layout.xml
    public DailyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view holder from the daily_layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_layout, parent, false);
        DailyViewHolder dailyHolder = new DailyViewHolder(view);

        // Return the new view holder.
        return dailyHolder;
    }

    // Suffixes for the date string
    static String[] suffixes =
            { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                    "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",
                    "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th", "th", "st" };

    // Map the icon "description" from request to drawable icon
    static Map<String, Integer> icons = new HashMap<String, Integer>();
    static {
        icons.put("01d", R.drawable.icon_01d);
        icons.put("02d", R.drawable.icon_02d);
        icons.put("03d", R.drawable.icon_03d);
        icons.put("04d", R.drawable.icon_04d);
        icons.put("09d", R.drawable.icon_09d);
        icons.put("10d", R.drawable.icon_10d);
        icons.put("11d", R.drawable.icon_11d);
        icons.put("13d", R.drawable.icon_13d);
        icons.put("50d", R.drawable.icon_50d);
    }
    @Override
    public void onBindViewHolder(@NonNull DailyViewHolder holder, int position) {
        // Get the item from the array corresponding to the position of the RecyclerView
        // we are creating
        Daily active = dailyData[position];

        Log.d("DailyAdapter/onBindViewHolder", active.day.toString());

        // Format strings
        Calendar c = Calendar.getInstance();
        c.setTime(active.day);
//        int dayNum = c.get(Calendar.DAY_OF_MONTH);
//        String day = dayNum + suffixes[dayNum];
        String day = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());

        String tempFormat = active.getMin() + "\u00B0/" + active.getMax() + "\u00B0";

        // Set the text/imageViews to display the image/temperature from the array item
        holder.date.setText(day);
        holder.temp.setText(tempFormat);
        holder.desc.setImageResource(icons.get(active.description));
    }

    @Override
    public int getItemCount() {
        // The number of items in the recycler view will be equal to the size of the array
        return dailyData.length;
    }

}


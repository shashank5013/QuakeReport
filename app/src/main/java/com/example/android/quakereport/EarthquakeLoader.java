package com.example.android.quakereport;


import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.util.List;

public class EarthquakeLoader extends AsyncTaskLoader<List<Earthquake>>{

    //URL to fetch data
    String mURL;

    /**
     *
     * @param context of the activity it is called in
     * @param URL of the website to fetch data from
     */
    EarthquakeLoader(Context context,String URL){
        super(context);
        mURL=URL;
    }


    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     *
     * @return the list of earthquakes fetched from database
     */
    @Nullable
    @Override
    public List<Earthquake> loadInBackground() {

        List<Earthquake> earthquakes=QueryUtils.extractEarthquakes(mURL);
        return earthquakes;
    }
}

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class EarthquakeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Earthquake>> {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();

    private static final String USGS_URL="https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&latitude=28.6139&longitude=77.2090&maxradiuskm=1500";

    private static final int LOADER_ID=1;

    private EarthquakeAdapter mEarthquakeAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        //checking if connected or not
        ConnectivityManager cm =
                (ConnectivityManager)EarthquakeActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        mEarthquakeAdapter=new EarthquakeAdapter(EarthquakeActivity.this,new ArrayList<Earthquake>());


        ListView listView=(ListView)findViewById(R.id.list);
        listView.setAdapter(mEarthquakeAdapter);

        TextView textView=(TextView)findViewById(R.id.empty_view);
        listView.setEmptyView(textView);

        if(isConnected){

            //Initiating Loaders
            getSupportLoaderManager().initLoader(LOADER_ID,null, this);

        }
        else {

            ProgressBar progressBar=(ProgressBar)findViewById(R.id.loading_spinner);
            progressBar.setVisibility(View.GONE);

            textView.setText("No Internet");
        }


        //Refreshes the data when swiped down
        final SwipeRefreshLayout swipeRefreshLayout=findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mEarthquakeAdapter.clear();
                ProgressBar progressBar=(ProgressBar)findViewById(R.id.loading_spinner);
                progressBar.setVisibility(View.VISIBLE);

                //checking if connected or not
                ConnectivityManager cm =
                        (ConnectivityManager)EarthquakeActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();

                if(isConnected){
                    TextView textView=findViewById(R.id.empty_view);
                    textView.setText("");
                    getSupportLoaderManager().restartLoader(LOADER_ID,null,EarthquakeActivity.this);
                }
                else{
                    progressBar.setVisibility(View.GONE);
                    TextView textView=findViewById(R.id.empty_view);
                    textView.setText("No Internet");
                }
                swipeRefreshLayout.setRefreshing(false);

            }
        });
        swipeRefreshLayout.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(@NonNull SwipeRefreshLayout parent, @Nullable View child) {
                ListView recyclerView=(ListView)findViewById(R.id.list);
                if (recyclerView != null) {
                    return recyclerView.canScrollVertically(-1);
                }
                return false;
            }
        });


        /**
         * Sets OnClickListener on each list item which opens URL for each earthquake
         * in browser
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Earthquake earthquake=(Earthquake)parent.getItemAtPosition(position);
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                int colorInt = Color.parseColor("#0D47A1"); //red
                builder.setToolbarColor(colorInt);
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(EarthquakeActivity.this, Uri.parse(earthquake.getUrl()));
            }
        });


}

    //Loader Callback methods
    @NonNull
    @Override
    public Loader<List<Earthquake>> onCreateLoader(int id, @Nullable Bundle args) {
        return new EarthquakeLoader(this,USGS_URL);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Earthquake>> loader, List<Earthquake> data) {
        ProgressBar progressBar=(ProgressBar)findViewById(R.id.loading_spinner);
        progressBar.setVisibility(View.GONE);

        mEarthquakeAdapter.clear();
        mEarthquakeAdapter.addAll(data);
        TextView textView =(TextView)findViewById(R.id.empty_view);
        textView.setText("No Earthquakes found");

    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Earthquake>> loader) {

         mEarthquakeAdapter.clear();
    }
}


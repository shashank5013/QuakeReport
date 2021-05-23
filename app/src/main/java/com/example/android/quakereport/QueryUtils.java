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

import android.renderscript.ScriptGroup;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtils {

     private  static  final String LOG_TAG=QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Return a list of {@link Earthquake} objects that has been built up from
     * parsing a JSON response.
     */
    public static List<Earthquake> extractEarthquakes(String requestUrl) {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }



        URL url=createURL(requestUrl);
        List<Earthquake> earthquakes =new ArrayList<Earthquake>();
        String jsonResponse="";
        try {
            jsonResponse=makeHTTPrequest(url);
            earthquakes=ExtractJson(jsonResponse);
        } catch (IOException e) {
            Log.e(LOG_TAG,"IO Exception at extractEarthquakes\n",e);
        }
        return earthquakes;

    }

    private static URL createURL(String stringurl){
        URL url=null;
        try {
            url=new URL(stringurl);
        } catch (MalformedURLException e) {
           Log.e(LOG_TAG,"Problem with creating URL\n",e);
        }
        return url;
    }

    private static String makeHTTPrequest(URL url) throws IOException{
       if(url==null){
           return "";
       }

        String jsonResponse="";
        HttpURLConnection httpURLConnection=null;
        InputStream inputStream=null;
        try {
            httpURLConnection=(HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.connect();
            int responseCode=httpURLConnection.getResponseCode();
            if(responseCode==200){
                inputStream=httpURLConnection.getInputStream();
                jsonResponse=readFromInputStream(inputStream);
            }
            else{
                Log.e(LOG_TAG,"Response Code : " + responseCode +"\n");
            }

        } catch (IOException e) {
            Log.e(LOG_TAG,"IO Exception at makeHTTPRequest\n",e);
        }
        finally {
            if(httpURLConnection!=null){
                httpURLConnection.disconnect();
            }

            if(inputStream!=null){
                inputStream.close();
            }
        }
        return jsonResponse;

    }

    private static String readFromInputStream(InputStream inputStream) throws  IOException{

        StringBuilder jsonResponse=new StringBuilder();

        if(inputStream!=null){

            InputStreamReader inputStreamReader=new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
            String line=bufferedReader.readLine();
            while(line!=null){
                jsonResponse.append(line);
                line=bufferedReader.readLine();
            }

        }
        return jsonResponse.toString();
    }

    private  static List<Earthquake> ExtractJson(String jsonResponse){
        List<Earthquake> earthquakes=new ArrayList<>();

        if(jsonResponse.isEmpty()){
            return earthquakes;
        }
        try {
            JSONObject data=new JSONObject(jsonResponse);

            JSONObject metaData=data.getJSONObject("metadata");
            int no_of_earthquakes=metaData.getInt("count");

            JSONArray features=data.getJSONArray("features");
            for(int i=0;i<no_of_earthquakes;i++){

                JSONObject curreq=features.getJSONObject(i);
                JSONObject properties=curreq.getJSONObject("properties");

                Earthquake temp=new Earthquake(properties.getDouble("mag"),properties.getString("place"),properties.getLong("time"),properties.getString("url"));
                earthquakes.add(temp);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG,"JSON Exception\n",e);
        }
        return earthquakes;

    }

}

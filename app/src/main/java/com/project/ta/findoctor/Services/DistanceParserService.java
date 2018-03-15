package com.project.ta.findoctor.Services;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.Utils.MethodLib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DistanceParserService extends AsyncTask<Void, Void, String> {
    private String SERVICE_URL = "";
    public String title = "Loading";
    private String message = "Please wait...";
    public Context activity;
    private ProgressDialog progress;
    private AsyncResponse delegate = null;
    String distance;
    public DistanceParserService(Context activity,String latLong1,String latLong2, AsyncResponse delegate)
    {
        this.SERVICE_URL="https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins="+latLong1+"&destinations="+latLong2+"&key=AIzaSyBrS7KBLv9tMXGshUz7HSp9ytFs96H3ofw";
        this.activity=activity;
        this.delegate = delegate;
        progress = new ProgressDialog(activity);
        progress.setTitle(title);
        progress.setMessage(message);
        progress.setCancelable(false);
        if(!this.progress.isShowing())
        {
            this.progress.show();
        }
    }
    // Invoked by execute() method of this object
    @Override
    protected String doInBackground(Void... args) {
        HttpURLConnection conn = null;
        final StringBuilder json = new StringBuilder();
        try {
            URL url = new URL(SERVICE_URL);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                json.append(buff, 0, read);
            }
        } catch (IOException e) {
            Log.e("DISTANCE PARSER SERVICE", "Error connecting to service", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return json.toString();
    }

    @Override
    protected void onPostExecute(String json) {
        try {
            JSONObject result = new JSONObject(json);
            JSONArray rowsArray = result.getJSONArray("rows");
            JSONArray elements = rowsArray.getJSONObject(0).getJSONArray("elements");
            distance = elements.getJSONObject(0).getJSONObject("distance").getString("text");
            delegate.processFinish(distance);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("DISTANCE PARSER SERVICE","DistanceParserService responseServer from "+this.SERVICE_URL+" : "+distance);
        this.progress.dismiss();
    }
}


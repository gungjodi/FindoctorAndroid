package com.project.ta.findoctor.Services;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.project.ta.findoctor.Interfaces.AsyncResponse;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BackgroundWebService extends AsyncTask<Void, Void, String> {
    private String LOG_TAG = "Background WebService";
    private String IPADDR = "http://findoctorapp.com";
    private String SERVICE_URL = "";
    private AsyncResponse delegate = null;

    public BackgroundWebService(String url, AsyncResponse delegate)
    {
        this.SERVICE_URL=IPADDR+url;
        this.delegate = delegate;
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
            Log.e(LOG_TAG, "Error connecting to service", e);
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
            delegate.processFinish(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG,"BackgroundWebService responseServer from "+this.SERVICE_URL+" : "+json);
    }
}


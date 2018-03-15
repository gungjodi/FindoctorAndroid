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

public class WebService extends AsyncTask<Void, Void, String> {
    private String LOG_TAG = "WebService";
    private String IPADDR = "http://findoctorapp.com";
    private String SERVICE_URL = "";
    public String title = "Loading";
    private String message = "Please wait...";
    public Context activity;
    private ProgressDialog progress;
    private AsyncResponse delegate = null;

    public WebService(Context activity,String url, String title, String message, AsyncResponse delegate)
    {
        this.SERVICE_URL=IPADDR+url;
        this.activity=activity;
        this.delegate = delegate;
        this.title=title;
        this.message=message;
        progress = new ProgressDialog(activity);
        progress.setTitle(title);
        progress.setMessage(message);
        progress.setCancelable(false);
        if(!this.progress.isShowing())
        {
            this.progress.show();
        }
    }

    public WebService(Context activity,String url,AsyncResponse delegate)
    {
        this.SERVICE_URL=IPADDR+url;
        this.activity=activity;
        this.delegate = delegate;
        progress = new ProgressDialog(activity);
        progress.setTitle(this.title);
        progress.setMessage(this.message);
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
        Log.d(LOG_TAG,"WebService responseServer from "+this.SERVICE_URL+" : "+json);
        this.progress.dismiss();
    }
}


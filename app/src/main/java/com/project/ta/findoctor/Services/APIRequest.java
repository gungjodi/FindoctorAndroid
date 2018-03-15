package com.project.ta.findoctor.Services;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.project.ta.findoctor.Interfaces.AsyncResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class APIRequest extends AsyncTask<Void, Void, String> {
    private String IPADDR = "http://findoctorapp.com";
    private String SERVICE_URL = "";
    public String title = "Loading";
    private String message = "Please wait...";
    public Context activity;
    private ProgressDialog progress;
    private AsyncResponse delegate = null;
    private JSONObject requestBody;
    private String method;

    public APIRequest(Context activity, String url, JSONObject requestBody,String method, AsyncResponse delegate)
    {
        this.SERVICE_URL=IPADDR+url;
        this.delegate = delegate;
        this.activity = activity;
        this.requestBody =requestBody;
        this.method = method;
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
    protected String doInBackground(Void... args){
        String responseString = null;
        HttpURLConnection con = null;
        try {
            URL url = new URL(SERVICE_URL);
            con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod(this.method);
            con.setRequestProperty("Content-Type", "application/json");
            con.connect();

            OutputStream os = con.getOutputStream();
            os.write(this.requestBody.toString().getBytes("UTF-8"));
            os.close();

            // Read the response into a string
            InputStream is = con.getInputStream();
            responseString = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
            is.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String json) {
        try {
            delegate.processFinish(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("WebServiceAPI",SERVICE_URL +" : "+this.requestBody.toString());
        Log.d("WebServiceAPI","APIRequest responseServer : "+json);
        this.progress.dismiss();
    }
}


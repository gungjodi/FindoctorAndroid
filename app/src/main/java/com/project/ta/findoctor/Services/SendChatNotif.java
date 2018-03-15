package com.project.ta.findoctor.Services;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.project.ta.findoctor.Interfaces.AsyncResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class SendChatNotif extends AsyncTask<Void, Void, String> {
    private AsyncResponse delegate = null;
    private String to;
    private JSONObject body;

    public SendChatNotif(JSONObject body, String to, AsyncResponse delegate)
    {
        this.delegate = delegate;
        this.body =body;
        this.to = to;
    }
    // Invoked by execute() method of this object
    @Override
    protected String doInBackground(Void... args){
        String responseString = null;
        HttpURLConnection con = null;
        try {
            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            // HTTP request header
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "key=AIzaSyBxXIG-5iCxoJ-Z0wVibfOhWX816dWhyrU");
            con.setRequestMethod("POST");
            con.connect();

            JSONObject data = new JSONObject();
            data.put("to",to);
            data.put("data", body);

            OutputStream os = con.getOutputStream();
            os.write(data.toString().getBytes("UTF-8"));
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
        Log.d("SendChatNotif","SendChatNotif responseServer : "+json);
    }
}


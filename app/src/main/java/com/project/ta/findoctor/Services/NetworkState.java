package com.project.ta.findoctor.Services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by GungJodi on 6/20/2017.
 */

public class NetworkState {
    private final String event;

    public NetworkState(String message) {
        this.event = message;
    }

    public String getMessage() {
        return event;
    }
}
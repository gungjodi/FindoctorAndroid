package com.project.ta.findoctor.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by GungJodi on 6/20/2017.
 */

public class NetworkReceiver extends ConnectivityManager.NetworkCallback {

    private final NetworkRequest networkRequest;

    public NetworkReceiver() {
        networkRequest = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
    }

    public void enable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(networkRequest , this);
    }

    @Override
    public void onAvailable(Network network) {
        EventBus.getDefault().post(new NetworkState("onAvailable"));
    }

    @Override
    public void onLosing(Network network, int maxMsToLive)
    {
        EventBus.getDefault().post(new NetworkState("onLosing"));
    }
    @Override
    public void onLost(Network network)
    {
        EventBus.getDefault().post(new NetworkState("onLost"));
    }

    @Override
    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties)
    {
        EventBus.getDefault().post(new NetworkState("onLinkPropertiesChanged"));
    }

    @Override
    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities)
    {
        EventBus.getDefault().post(new NetworkState("onCapabilitiesChanged"));
    }
}
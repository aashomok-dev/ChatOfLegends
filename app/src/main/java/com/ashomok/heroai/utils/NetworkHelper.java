package com.ashomok.heroai.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Devlomi on 20/03/2018.
 */

//this class will get the current network state if it's wifi , data ,roaming, or not connected
public enum NetworkHelper {
    ;


    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    //is connected to internet regardless wifi or data
    public static boolean isConnected(Context context) {
        NetworkInfo info = NetworkHelper.getNetworkInfo(context.getApplicationContext());
        return (null != info && info.isConnected());
    }
}

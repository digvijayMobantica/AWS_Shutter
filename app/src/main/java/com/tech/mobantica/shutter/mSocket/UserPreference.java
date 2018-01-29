package com.tech.mobantica.shutter.mSocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

public class UserPreference {
    private static final String HOST_SOCKET_TIMEOUT = "hostTimeout";


    public static int getHostSocketTimeout(@NonNull Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(preferences.getString(HOST_SOCKET_TIMEOUT, "150"));
    }
}

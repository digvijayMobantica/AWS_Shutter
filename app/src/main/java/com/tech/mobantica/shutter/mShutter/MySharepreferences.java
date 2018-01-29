package com.tech.mobantica.shutter.mShutter;

import android.content.Context;
import android.content.SharedPreferences;

import static com.tech.mobantica.shutter.mShutter.UtilVariable.EndPoint;
import static com.tech.mobantica.shutter.mShutter.UtilVariable.PoolId;
import static com.tech.mobantica.shutter.mShutter.UtilVariable.ShutterPosition;
import static com.tech.mobantica.shutter.mShutter.UtilVariable.ThingName;

/**
 * Created by Comp2 on 1/5/2018.
 */

public class MySharepreferences {

    private Context ctx;
    private String prefName = "AppPrefs";
    private String upTime = "UpTime";
    private String downTime = "DownTime";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public MySharepreferences(Context context) {
        this.ctx = context;
        if (preferences == null) {
            preferences = ctx.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        }
        if (editor == null) {
            editor = preferences.edit();
        }
    }

    public void setUpDownTime(long up, long down) {
        editor.clear();
        editor.putLong(upTime, up);
        editor.putLong(downTime, down);
        editor.commit();
    }

    public long getUpTime() {
        return preferences.getLong(upTime, 8000);
    }

    public long getDownTime() {
        return preferences.getLong(downTime, 10000);
    }

    //Setting Device Value**********************************************************************************************

    public void setDeviceManageValue(String endPoint, String poolId, String thingName) {
        editor.clear();
        editor.putString(EndPoint, endPoint);
        editor.putString(PoolId, poolId);
        editor.putString(ThingName, thingName);
        editor.commit();
    }

    public String getEndPoint() {
        return preferences.getString(EndPoint, "azur83gz1t6vj.iot.us-east-1.amazonaws.com");  //azur83gz1t6vj.iot.us-east-1.amazonaws.com
    }

    public String getPoolId() {
        return preferences.getString(PoolId, "us-east-1:e9d8b374-4a40-48d2-b32d-cf8f52c87fbb");  // us-east-1:e9d8b374-4a40-48d2-b32d-cf8f52c87fbb
    }

    public String getThingName() {
        return preferences.getString(ThingName, "palmat2"); //
    }

    //shutter position**********************************************************************************************

    public void setShutterCurrentPosition(long position) {
        editor.clear();
        editor.putLong(ShutterPosition, position);
        editor.commit();
    }

    public String getShutterPosition() {
        return preferences.getString(ShutterPosition, "");
    }

}


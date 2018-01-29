package com.tech.mobantica.shutter.mSocket;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Comp2 on 1/18/2018.
 */

public class LogPreferences {

    private Context ctx;
    private String prefName = "AppPrefs";
    private String LOG = "LOG";
    private String TIME = "TIME";


    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public LogPreferences(Context context)
    {
        this.ctx = context;
        if(preferences == null){
            preferences = ctx.getSharedPreferences(prefName,Context.MODE_PRIVATE);
        }
        if(editor == null){
            editor = preferences.edit();
        }
    }

    public void setLog(String log,String time){
        editor.putString(LOG,log);
        editor.putString(TIME,time);
        editor.commit();
    }

    public String getLOG(){
        return preferences.getString(LOG,"");
    }

    public String getTIME(){
        return preferences.getString(TIME,"");
    }


    //**********************************************************************************************


    public String getErrorString(){
        return preferences.getString("Error","");
    }

    public void putErrorString(String string){
         editor.putString("Error",string);
         editor.commit();
    }

    public void clearPreferences(){
        editor.clear();
        editor.commit();
    }

}

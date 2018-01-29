package com.tech.mobantica.shutter.mSocket;

import android.content.Context;
import android.widget.Toast;

public class Errors {

    public static void showError(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void saveErrorLogs(Context context,String logs){
        LogPreferences ll=new LogPreferences(context);
        String m=ll.getErrorString()+"\n"+logs;
        ll.putErrorString(m);

    }

}

package com.tech.mobantica.shutter.mSocket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tech.mobantica.shutter.MainActivity;
import com.tech.mobantica.shutter.R;

public class LogActivity extends AppCompatActivity {

    private LogPreferences logPreferences;
    private TextView mTvLog;
    private Button mClear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        mTvLog = findViewById(R.id.tvLog);
        mClear = findViewById(R.id.btnClear);
        getPreferencesVal();
    }

    private void getPreferencesVal() {
        logPreferences = new LogPreferences(getApplicationContext());
        
        if(logPreferences.getErrorString() != ""){
            mTvLog.setText(logPreferences.getErrorString());
        }
        else {
            mClear.setText("Go Back");
        }
    }

    public void onClick(View view) {
        logPreferences = new LogPreferences(getApplicationContext());
        logPreferences.clearPreferences();
       startActivity(new Intent(LogActivity.this,PortActivity.class));
    }
}

package com.tech.mobantica.shutter.mShutter;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tech.mobantica.shutter.R;

public class ManageDeviceActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText metEndPoint, metPoolId, metThingName;
    private Button btnSubmit;
    private ImageButton mClearEndPoint, mClearPoolId, mClearThingName;
    private ImageButton mBtnBack;
    private MySharepreferences mySharepreferences;
    private String mEndPoint, mPoolId, mThingName;
    private ImageView txtBack;
    private TextView txtTitle;
    private ImageView imgSettings;
    private ImageView imgManageDevice;
    private ImageView imgEdit;
    private RelativeLayout parentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_device);

        initToolbar();
        initWidget();
        setListeners();
        getSharedPreferenceValue();
        checkPreferencesIsEmpty();
        setText();
        // setEnableStatus(false, false, false, View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);

    }

    private void checkPreferencesIsEmpty() {

        if (mEndPoint.equals("")) {

            txtTitle.setText("Device Manager");
            txtBack.setVisibility(View.INVISIBLE);
            setEnableStatus(false, false, true, View.VISIBLE, View.GONE, View.VISIBLE, View.VISIBLE, View.VISIBLE);
            } else {

            setEnableStatus(false, false, false, View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
        }
    }

    private void setEnableStatus(boolean endPoint, boolean poolId, boolean thingName, int save, int edit, int ClearEndPoint, int ClearPoolId, int ClearThingName) {

        metEndPoint.setEnabled(endPoint);
        metPoolId.setEnabled(poolId);
        metThingName.setEnabled(thingName);
        btnSubmit.setVisibility(save);
        imgEdit.setVisibility(edit);
        mClearEndPoint.setVisibility(ClearEndPoint);
        mClearPoolId.setVisibility(ClearPoolId);
        mClearThingName.setVisibility(ClearThingName);
    }

    private void initToolbar() {
        txtTitle = findViewById(R.id.txtTitle);
        imgSettings =  findViewById(R.id.txtSettings);
        imgManageDevice = (ImageView) findViewById(R.id.txtManageThings);
        imgEdit = (ImageView) findViewById(R.id.txtEdit);
        imgEdit.setOnClickListener(this);
        imgEdit.setVisibility(View.VISIBLE);
        imgSettings.setVisibility(View.INVISIBLE);
        imgManageDevice.setVisibility(View.INVISIBLE);
        txtBack = (ImageView) findViewById(R.id.txtBack);
        txtTitle.setText("Manage Device");
        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void getSharedPreferenceValue() {
        mEndPoint = mySharepreferences.getEndPoint();
        mPoolId = mySharepreferences.getPoolId();
        mThingName = mySharepreferences.getThingName();
    }

    private void setText() {
        metEndPoint.setText(mEndPoint);
        metPoolId.setText(mPoolId);
        metThingName.setText(mThingName);
    }

    private void setListeners() {

        btnSubmit.setOnClickListener(this);
        mClearEndPoint.setOnClickListener(this);
        mClearEndPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                metEndPoint.setText("");
            }
        });
        mClearPoolId.setOnClickListener(this);
        mClearPoolId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                metPoolId.setText("");
            }
        });
        mClearThingName.setOnClickListener(this);
        mClearThingName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                metThingName.setText("");
            }
        });

    }

    private void initWidget() {
        metEndPoint = (EditText) findViewById(R.id.etEndPoint);
        metPoolId = (EditText) findViewById(R.id.etPoolId);
        metThingName = (EditText) findViewById(R.id.etThingName);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        mClearEndPoint = (ImageButton) findViewById(R.id.clear_end_point);
        mClearPoolId = (ImageButton) findViewById(R.id.clear_pool_id);
        mClearThingName = (ImageButton) findViewById(R.id.clear_thing_name);
        parentLayout = (RelativeLayout) findViewById(R.id.parentLayoutd);

        mySharepreferences = new MySharepreferences(getApplicationContext());


    }

    private void setPrefrences(String message) {
        mySharepreferences = new MySharepreferences(getApplicationContext());
        mySharepreferences.setDeviceManageValue(metEndPoint.getText().toString(), metPoolId.getText().toString(), metThingName.getText().toString());
        setEnableStatus(false, false, false, View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
        showSnackbar(message);

    }

    private void showSnackbar(String message) {
        mySharepreferences = new MySharepreferences(getApplicationContext());
        mySharepreferences.setDeviceManageValue(metEndPoint.getText().toString(), metPoolId.getText().toString(), metThingName.getText().toString());

        Snackbar snackbar = Snackbar
                .make(parentLayout, message, Snackbar.LENGTH_LONG)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

        snackbar.show();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnSubmit:
                if (metEndPoint.getText().toString().length() == 0) {
                    metEndPoint.setError("Enter End Point");
                } else if (metPoolId.getText().toString().length() == 0) {
                    metPoolId.setError("Enter Pool Id");
                } else if (metThingName.getText().toString().length() == 0) {
                    metThingName.setError("Enter Thing Name");
                } else {
                    if (!mEndPoint.equals("")) {
                        setPrefrences("Device details saved successfully");
                        Intent intent = new Intent(ManageDeviceActivity.this, ShutterActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {
                        setPrefrences("Issue to save device details");
                    }
                }
                break;

            case R.id.txtEdit:
                setEnableStatus(true, true, true, View.VISIBLE, View.INVISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE);
                break;

//            case R.id.clear_end_point:
//
//                break;
//
//            case R.id.clear_pool_id:
//
//                break;
//
//            case R.id.clear_thing_name:
//
//                break;
        }
    }

}

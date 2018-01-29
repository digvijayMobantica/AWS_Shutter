package com.tech.mobantica.shutter.mShutter;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttMessageDeliveryCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Regions;
import com.tech.mobantica.shutter.MainActivity;
import com.tech.mobantica.shutter.R;
import com.tech.mobantica.shutter.mSocket.Errors;
import com.tech.mobantica.shutter.mSocket.PortActivity;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import static com.tech.mobantica.shutter.mShutter.UtilMethods.isNetworkAvailable;
import static com.tech.mobantica.shutter.mShutter.UtilMethods.myToast;
public class ShutterActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {

    static final String LOG_TAG = MainActivity.class.getCanonicalName();
    private String mEndPoint = "", mPoolId = "", mThingName = "";
    private ImageButton up, down, stop, button;
    private String BASE_TOPIC = "";

    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.US_EAST_1;
    private String downtopic;
    private String topicCurrentStatus;
    private String uptopic;
    long lastDown;
    long lastDuration;

    // Button btnConnect;
    AWSIotMqttManager mqttManager;
    private String clientId;
    private String uiStatus;
    private String stopmessage;
    public boolean status = true;
    private ImageView animated_text;
    private ProgressBar progressBar;
    AWSCredentials awsCredentials;

    CognitoCachingCredentialsProvider credentialsProvider;

    private List<Animator> mAnimatorList = new ArrayList<>();
    private String upmessage = "";
    private String downmessage = "";

    // private long upTime = 8000,downTime = 10000;
    private long upTime, downTime;
    private TextView txtTitle;

    private RelativeLayout parentLayout;

    private SharedPreferences preferences;

    private MySharepreferences mySharepreferences;
    private ImageView txtBack;
    private ImageView txtSettings;
    private ImageView txtManageDevice;
    private TextView txtStatus;

    //End point - azur83gz1t6vj.iot.us-east-1.amazonaws.com
    //Pool ID - us-east-1:e9d8b374-4a40-48d2-b32d-cf8f52c87fbb
    //Policy name - palmatAPP_policy
    // thing name - HomAuto_NVir

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shutter);

        upmessage = "{\"state\":{\"desired\":{\"D8\":false},\"reported\":{\"D6\":true,\"D7\":false}}}";
        downmessage = "{\"state\":{\"desired\":{\"D8\":false},\"reported\":{\"D6\":false,\"D7\":true}}}";

        stopmessage = "{\"state\":{\"desired\":{\"D8\":false},\"reported\":{\"D6\":false,\"D7\":false}}}";


        animated_text = (ImageView) findViewById(R.id.shuttergate);
//
        getDeviceValueFromSharedPreferences();
        initToolbar();
        initWidget();
        initListeners();
//
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    @Override
    protected void onResume() {
        setTopics();
        initCognitoCachingCredentialsProvider();
        clientId = UUID.randomUUID().toString();
        connectToMqtt();
        setUpDownTime();
        super.onResume();
    }

    private void setTopics() {
        BASE_TOPIC = "$aws/things/" + mThingName + "/shadow/";
        downtopic = BASE_TOPIC + "update";
        topicCurrentStatus = BASE_TOPIC + "get";
        uptopic = BASE_TOPIC + "update";
    }


    private void initCognitoCachingCredentialsProvider() {
        // Initialize the AWS Cognito credentials provider

        if (!mPoolId.equals("")) {
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(), // context
                    mPoolId, // Identity Pool ID
                    MY_REGION // Region
            );
        }

    }

    private void showSnackbar() {
        Snackbar snackbar = Snackbar
                .make(parentLayout, "Please check your internet connection and retry", Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        connectToMqtt();
                    }
                });

        snackbar.show();


        String currentDate = DateFormat.getDateTimeInstance().format(new Date());
        String isEnable = "Please check your internet connection and retry" +" : "+currentDate;
        Errors.saveErrorLogs(this,isEnable);

    }

    private void connectToMqtt() {
        if (isNetworkAvailable(ShutterActivity.this)) {

            if (mEndPoint.equals("")) {
                startActivity(new Intent(ShutterActivity.this, ManageDeviceActivity.class));
            }
            else
            {
                try {

                    mqttManager = new AWSIotMqttManager(clientId, mEndPoint);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            awsCredentials = credentialsProvider.getCredentials();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    connect();
                                }
                            });
                        }
                    }).start();
                } catch (Exception e) {

                    myToast(ShutterActivity.this, e.getMessage().toString());

                    String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    String isEnable = e.getMessage() +" : "+currentDate;
                    Errors.saveErrorLogs(this,isEnable);

                }
            }

        } else {
            showSnackbar();
        }

    }

    private void initListeners() {

        button.setOnClickListener(btnL);
        down.setOnTouchListener(this);
        up.setOnTouchListener(this);
        stop.setOnTouchListener(this);

    }

    private void initWidget() {
        up = (ImageButton) findViewById(R.id.up);
        down = (ImageButton) findViewById(R.id.down);
        stop = (ImageButton) findViewById(R.id.stop);

        txtStatus = (TextView) findViewById(R.id.txtStatus);
        button = (ImageButton) findViewById(R.id.btnl);
        parentLayout = (RelativeLayout) findViewById(R.id.parentLayoutd);

    }

    private void initToolbar() {
        txtBack = (ImageView) findViewById(R.id.txtBack);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtSettings = (ImageView) findViewById(R.id.txtSettings);
        txtManageDevice = (ImageView) findViewById(R.id.txtManageThings);
        txtManageDevice.setOnClickListener(this);
        txtSettings.setOnClickListener(this);
        txtTitle.setText("PALMAT");
        txtBack.setVisibility(View.INVISIBLE);
    }

    private void setUpDownTime() {
        upTime = mySharepreferences.getUpTime();
        downTime = mySharepreferences.getDownTime();
    }

    private void getDeviceValueFromSharedPreferences() {
        mySharepreferences = new MySharepreferences(getApplicationContext());
        mEndPoint = mySharepreferences.getEndPoint();
        mThingName = mySharepreferences.getThingName();
        mPoolId = mySharepreferences.getPoolId();

    }

    void updateUiScreenFirstTime() {

        if (uiStatus != "") {
            if (uiStatus.equals("true")) {
                progressBar.setVisibility(View.GONE);
                animated_text.setVisibility(View.VISIBLE);
                Animator animator = AnimatorInflater.loadAnimator(ShutterActivity.this, R.animator.object_animator_up);
                animator.setTarget(animated_text);
                long duration = 0;
                animator.setDuration(duration);
                animator.start();
                mAnimatorList.add(animator);
            }
            updateUiScreen();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            txtStatus.setText("Connecting...");
        }

    }

    void updateUiScreen() {

        if (uiStatus != "") {
            if (uiStatus.equals("false")) {

                progressBar.setVisibility(View.GONE);
                animated_text.setVisibility(View.VISIBLE);
                Animator animator = AnimatorInflater.loadAnimator(ShutterActivity.this, R.animator.object_animator_down);
                animator.setTarget(animated_text);
                //long duration = 10000;
                animator.setDuration(downTime);
                animator.start();
                mAnimatorList.add(animator);


            } else {
                progressBar.setVisibility(View.GONE);
                animated_text.setVisibility(View.VISIBLE);
                Animator animator = AnimatorInflater.loadAnimator(ShutterActivity.this, R.animator.object_animator_up);
                animator.setTarget(animated_text);
                //long duration = 8000;
                animator.setDuration(upTime);
                animator.start();
                mAnimatorList.add(animator);

            }
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    void stopUiScreen() {

        for (Animator animator : mAnimatorList) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                animator.pause();
            }
        }

    }

    void connect() {
        mqttManager.connect(credentialsProvider, new AWSIotMqttClientStatusCallback() {
            @Override
            public void onStatusChanged(final AWSIotMqttClientStatus status,
                                        final Throwable throwable) {
                Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (status == AWSIotMqttClientStatus.Connecting) {
                            // tvStatus.setText("Connecting...");
                            //Toast.makeText(ActivityScanWifi.this, "Connecting...", Toast.LENGTH_SHORT).show();
                            txtStatus.setText("Connecting...");

                        } else if (status == AWSIotMqttClientStatus.Connected) {
                            // tvStatus.setText("Connected");
                            // Toast.makeText(ActivityScanWifi.this, "Connected", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            subscribeTopics();
                            getCurrentStatus();
                            txtStatus.setText("Connected & Listening...");


                        } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                            if (throwable != null) {
                                Log.e(LOG_TAG, "Connection error.", throwable);
                            }
                            // tvStatus.setText("Reconnecting");
                            // Toast.makeText(ActivityScanWifi.this, "Reconnecting", Toast.LENGTH_SHORT).show();

                        } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                            if (throwable != null) {
                                Log.e(LOG_TAG, "Connection error.", throwable);
                                throwable.printStackTrace();
                            }
                            //tvStatus.setText("Disconnected");
                            // Toast.makeText(ActivityScanWifi.this, "Disconnected", Toast.LENGTH_SHORT).show();

                        } else {
                            //tvStatus.setText("Disconnected");
                            // Toast.makeText(ActivityScanWifi.this, "Disconnected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (view.getId()) {
            case R.id.up:

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {



//                    lastDown = System.currentTimeMillis();
//                    lastDuration = System.currentTimeMillis() - lastDown;
//                    long sec = lastDuration / 1000;
//                    Toast.makeText(this, String.valueOf(sec), Toast.LENGTH_SHORT).show();


                    mqttManager.publishString(upmessage, uptopic, AWSIotMqttQos.QOS0, new AWSIotMqttMessageDeliveryCallback() {
                        @Override
                        public void statusChanged(MessageDeliveryStatus status, Object userData) {
                            Log.e(LOG_TAG, "Press Event Up");
                            //Toast.makeText(ActivityScanWifi.this, "Status Up: "+status, Toast.LENGTH_SHORT).show();
                        }
                    }, status);

                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

//                    mqttManager.publishString(stopmessage, downtopic, AWSIotMqttQos.QOS0, new AWSIotMqttMessageDeliveryCallback() {
//                        @Override
//                        public void statusChanged(MessageDeliveryStatus status, Object userData) {
//                            Log.e(LOG_TAG, "Release Event Stop");
//                            //Toast.makeText(ActivityScanWifi.this, "Status Down: "+status, Toast.LENGTH_SHORT).show();
//                            //Toast.makeText(ActivityScanWifi.this, "Success", Toast.LENGTH_SHORT).show();
//                        }
//                    }, status);

                }

                break;

            case R.id.down:

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    lastDown = System.currentTimeMillis();

                    mqttManager.publishString(downmessage, downtopic, AWSIotMqttQos.QOS0, new AWSIotMqttMessageDeliveryCallback() {
                        @Override
                        public void statusChanged(MessageDeliveryStatus status, Object userData) {
                            Log.e(LOG_TAG, "Press Event Down");
                            //Toast.makeText(ActivityScanWifi.this, "Status Down: "+status, Toast.LENGTH_SHORT).show();
                            //Toast.makeText(ActivityScanWifi.this, "Success", Toast.LENGTH_SHORT).show();
                        }
                    }, status);


                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                    mqttManager.publishString(stopmessage, downtopic, AWSIotMqttQos.QOS0, new AWSIotMqttMessageDeliveryCallback() {
//                        @Override
//                        public void statusChanged(MessageDeliveryStatus status, Object userData) {
//                            Log.e(LOG_TAG, "Release Event Stop");
//                            //Toast.makeText(ActivityScanWifi.this, "Status Down: "+status, Toast.LENGTH_SHORT).show();
//                            //Toast.makeText(ActivityScanWifi.this, "Success", Toast.LENGTH_SHORT).show();
//                        }
//                    }, status);

                }
                break;

            case R.id.stop:

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {

                    for (Animator animator : mAnimatorList) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            animator.pause();
                        }
                    }

                    //publish**************************************************************************************************

                    mqttManager.publishString(stopmessage, downtopic, AWSIotMqttQos.QOS0, new AWSIotMqttMessageDeliveryCallback() {
                        @Override
                        public void statusChanged(MessageDeliveryStatus status, Object userData) {
                            Log.e(LOG_TAG, "Button Stop");
                            //Toast.makeText(ActivityScanWifi.this, "Status Down: "+status, Toast.LENGTH_SHORT).show();
                            //Toast.makeText(ActivityScanWifi.this, "Success", Toast.LENGTH_SHORT).show();
                        }
                    }, status);
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {

                }

                break;
        }

        return false;
    }

//    View.OnClickListener btnUp = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            up.setEnabled(false);
//            down.setEnabled(true);
//            stop.setEnabled(true);
//
//            //publish**************************************************************************************************
//
//            mqttManager.publishString(upmessage, uptopic, AWSIotMqttQos.QOS0, new AWSIotMqttMessageDeliveryCallback() {
//                @Override
//                public void statusChanged(MessageDeliveryStatus status, Object userData) {
//                    Log.e(LOG_TAG, "Up Status Changed");
//                    //Toast.makeText(ActivityScanWifi.this, "Status Up: "+status, Toast.LENGTH_SHORT).show();
//                    //Toast.makeText(ActivityScanWifi.this, "Success", Toast.LENGTH_SHORT).show();
//                }
//            }, status);
//        }
//    };

    View.OnClickListener btnDown = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            down.setEnabled(false);
            up.setEnabled(true);
            stop.setEnabled(true);

            //publish**************************************************************************************************

            mqttManager.publishString(downmessage, downtopic, AWSIotMqttQos.QOS0, new AWSIotMqttMessageDeliveryCallback() {
                @Override
                public void statusChanged(MessageDeliveryStatus status, Object userData) {
                    Log.e(LOG_TAG, "Down Status Changed");
                    //Toast.makeText(ActivityScanWifi.this, "Status Down: "+status, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(ActivityScanWifi.this, "Success", Toast.LENGTH_SHORT).show();
                }
            }, status);
        }
    };

    View.OnClickListener btnStop = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

//            stop.setEnabled(false);
//            up.setEnabled(true);
//            down.setEnabled(true);

            for (Animator animator : mAnimatorList) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    animator.pause();
                }
            }

            //publish**************************************************************************************************

            mqttManager.publishString(stopmessage, downtopic, AWSIotMqttQos.QOS0, new AWSIotMqttMessageDeliveryCallback() {
                @Override
                public void statusChanged(MessageDeliveryStatus status, Object userData) {
                    Log.e(LOG_TAG, "Button Stop");
                    //Toast.makeText(ActivityScanWifi.this, "Status Down: "+status, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(ActivityScanWifi.this, "Success", Toast.LENGTH_SHORT).show();
                }
            }, status);

        }
    };

    void getCurrentStatus() {

        mqttManager.publishString("", topicCurrentStatus, AWSIotMqttQos.QOS0, new AWSIotMqttMessageDeliveryCallback() {
            @Override
            public void statusChanged(MessageDeliveryStatus status, Object userData) {
                Log.e(LOG_TAG, "Current Status...");
                //Toast.makeText(ActivityScanWifi.this, "Status Up: "+status, Toast.LENGTH_SHORT).show();
                //Toast.makeText(ActivityScanWifi.this, "Success", Toast.LENGTH_SHORT).show();
            }
        }, status);

    }

    View.OnClickListener btnL = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(ShutterActivity.this, "None", Toast.LENGTH_SHORT).show();
        }
    };

    void subscribeTopics() {
        try {
            mqttManager.subscribeToTopic(BASE_TOPIC + "get/accepted", AWSIotMqttQos.QOS0, new AWSIotMqttNewMessageCallback() {
                @Override
                public void onMessageArrived(final String topic, final byte[] data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String message = new String(data, "UTF-8");
                                Log.d(LOG_TAG, "Message arrived:");
                                Log.d(LOG_TAG, "   Topic: " + topic);
                                Log.d(LOG_TAG, " Message get accepted: " + message);

                                //tvLastMessage.setText(message);

                                JSONObject jsonObject = new JSONObject(message);
                                JSONObject jsonState = jsonObject.getJSONObject("state");
                                JSONObject jsonDesired = jsonState.getJSONObject("reported");
                                uiStatus = jsonDesired.getString("D6");

                                Log.d(LOG_TAG, "JsonResponse: " + String.valueOf(uiStatus));

                                updateUiScreenFirstTime();
                               // updateUiScreen();

                                /*if(jsonDesired.getString("D6").equals("false") && jsonDesired.getString("D7").equals("false")){
                                    updateUiScreenFirstTime();
                                }*/

                            } catch (UnsupportedEncodingException e) {
                                Log.e(LOG_TAG, "Message encoding error.", e);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
        }

        try {
            mqttManager.subscribeToTopic(BASE_TOPIC + "get/rejected", AWSIotMqttQos.QOS0, new AWSIotMqttNewMessageCallback() {
                @Override
                public void onMessageArrived(final String topic, final byte[] data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String message = new String(data, "UTF-8");
                                Log.d(LOG_TAG, "Message arrived:");
                                Log.d(LOG_TAG, "   Topic: " + topic);
                                Log.d(LOG_TAG, " Message get rejected: " + message);

                                //tvLastMessage.setText(message);

                            } catch (UnsupportedEncodingException e) {
                                Log.e(LOG_TAG, "Message encoding error.", e);
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
        }

        try {
            mqttManager.subscribeToTopic(BASE_TOPIC + "update/accepted", AWSIotMqttQos.QOS0, new AWSIotMqttNewMessageCallback() {
                @Override
                public void onMessageArrived(final String topic, final byte[] data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String message = new String(data, "UTF-8");
                                Log.d(LOG_TAG, "Message arrived:");
                                Log.d(LOG_TAG, "   Topic: " + topic);
                                Log.d(LOG_TAG, " Message update accepted: " + message);

                                //tvLastMessage.setText(message);

                                JSONObject jsonObject = new JSONObject(message);
                                JSONObject jsonState = jsonObject.getJSONObject("state");
                                JSONObject jsonDesired = jsonState.getJSONObject("reported");
                                uiStatus = jsonDesired.getString("D6");

                                if (jsonDesired.getString("D6").equals("true") && jsonDesired.getString("D7").equals("false")) {
                                    updateUiScreen();
                                }

                                if (jsonDesired.getString("D6").equals("false") && jsonDesired.getString("D7").equals("true")) {
                                    updateUiScreen();
                                }

                                if (jsonDesired.getString("D6").equals("false") && jsonDesired.getString("D7").equals("false")) {
                                    //stopUiScreen();
                                    return;
                                }

                                Log.d(LOG_TAG, "JsonResponse: " + String.valueOf(uiStatus));

                            } catch (UnsupportedEncodingException e) {
                                Log.e(LOG_TAG, "Message encoding error.", e);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
        }

        try {
            mqttManager.subscribeToTopic(BASE_TOPIC + "update/rejected", AWSIotMqttQos.QOS0, new AWSIotMqttNewMessageCallback() {
                @Override
                public void onMessageArrived(final String topic, final byte[] data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String message = new String(data, "UTF-8");
                                Log.d(LOG_TAG, "Message arrived:");
                                Log.d(LOG_TAG, "   Topic: " + topic);
                                Log.d(LOG_TAG, " Message update rejected: " + message);

                                //tvLastMessage.setText(message);


                            } catch (UnsupportedEncodingException e) {
                                Log.e(LOG_TAG, "Message encoding error.", e);
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.txtSettings:
                startActivity(new Intent(ShutterActivity.this, ActivitySettingNew.class));

                break;

            case R.id.txtManageThings:
                startActivity(new Intent(ShutterActivity.this, ManageDeviceActivity.class));
                break;
        }

    }

}

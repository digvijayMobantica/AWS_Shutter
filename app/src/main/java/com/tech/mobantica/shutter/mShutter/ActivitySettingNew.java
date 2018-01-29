package com.tech.mobantica.shutter.mShutter;

import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tech.mobantica.shutter.R;

import java.text.DecimalFormat;

public class ActivitySettingNew extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {

    private boolean isPaused = false;
    private boolean isCanceled = false;
    private long timeRemaining = 0;

    private ImageView imgArrowUp, imgArrowDown;
    private ImageButton imgUp, imgDown;
    private TextView tvup_Time, tvdown_Time;
    private long upTime;
    private long downTime;
    private double savedUpTime;
    private double savedDownTime;
    private static DecimalFormat decimalFormat = new DecimalFormat(".#");
    private MySharepreferences mySharepreferences;
    private TextView txtTitle;
    private ImageView txtBack;
    private ImageView txtSettings;
    private ImageView txtManageDevice;
    private Animation ArrowDownAnimation, ArrowUpAnimation;
    private RelativeLayout parentLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_new);

        initToolbar();
        initWidget();
        setListeners();
        getValuesFromSharedPreference();
        setSharedPreferenceValues();
    }

    private void setSharedPreferenceValues() {

        double upT;
        double downT;
        if (savedUpTime < 0.1) {
            upT = 0.0;
            tvup_Time.setText("" + upT);

        } else {
            upT = savedUpTime / 1000;
            if (upT < 1.0) {
                tvup_Time.setText("0" + decimalFormat.format(upT));

            } else {

                tvup_Time.setText("" + decimalFormat.format(upT));
            }
        }

        if (savedDownTime < 0.1) {
            downT = 0.0;
            tvdown_Time.setText("" + downT);

        } else {

            downT = savedDownTime / 1000;
            if (downT < 1.0) {
                tvdown_Time.setText("0" + decimalFormat.format(downT));

            } else {

                tvdown_Time.setText("" + decimalFormat.format(downT));
            }

        }
    }

    private void setListeners() {
        imgUp.setOnTouchListener(this);
        imgDown.setOnTouchListener(this);

        ArrowDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!isPaused) {
                    imgArrowDown.startAnimation(ArrowDownAnimation);
                } else {
                    ArrowDownAnimation.cancel();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        ArrowUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!isPaused) {
                    imgArrowUp.startAnimation(ArrowUpAnimation);
                } else {
                    ArrowUpAnimation.cancel();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackbar("Successfully saved up and down time.");
            }
        });

        findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimerText();
                upTime = 0;
                downTime = 0;
                showSnackbar("Successfully reset timer");
            }
        });
    }

    private void initWidget() {
        tvup_Time = (TextView) findViewById(R.id.time_Sec_Up);
        tvdown_Time = (TextView) findViewById(R.id.time_Sec_Down);

        imgUp = (ImageButton) findViewById(R.id.imgUp);
        imgDown = (ImageButton) findViewById(R.id.imgDown);

        imgArrowUp = (ImageView) findViewById(R.id.imgArrowUp);
        imgArrowDown = (ImageView) findViewById(R.id.imgArrowDown);

        ArrowUpAnimation = AnimationUtils.loadAnimation(this, R.anim.arrow_up);
        ArrowDownAnimation = AnimationUtils.loadAnimation(this, R.anim.arrow_down);
        parentLayout = (RelativeLayout) findViewById(R.id.parentLayoutd);

    }

    private void initToolbar() {
        txtBack = (ImageView) findViewById(R.id.txtBack);

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtSettings = (ImageView) findViewById(R.id.txtSettings);
        txtManageDevice = (ImageView) findViewById(R.id.txtManageThings);

        txtBack.setOnClickListener(this);
        txtTitle.setText("Settings");
        txtSettings.setVisibility(View.INVISIBLE);
        txtManageDevice.setVisibility(View.INVISIBLE);
    }

    private void getValuesFromSharedPreference() {
        mySharepreferences = new MySharepreferences(getApplicationContext());
        upTime = mySharepreferences.getUpTime();
        downTime = mySharepreferences.getDownTime();
        savedUpTime = mySharepreferences.getUpTime();
        savedDownTime = mySharepreferences.getDownTime();
    }

    private void resetTimerText() {
        tvup_Time.setText("" + 0.0);
        tvdown_Time.setText("" + 0.0);
    }

    private void showSnackbar(String message) {

        mySharepreferences = new MySharepreferences(getApplicationContext());
        mySharepreferences.setUpDownTime(upTime, downTime);
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
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (view.getId()) {
            case R.id.imgUp:
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // lastUp = System.currentTimeMillis();
                    imgArrowUp.setVisibility(View.VISIBLE);
                    isPaused = false;
                    imgArrowUp.startAnimation(ArrowUpAnimation);
                    CountDownTimer timer;
                    long millisInFuture = 10000000; //30 seconds
                    long countDownInterval = 10; //1 second

                    timer = new CountDownTimer(millisInFuture, countDownInterval) {
                        public void onTick(long millisUntilFinished) {
                            //do something in every tick
                            if (isPaused) {
                                cancel();

                            } else {

                                double time = 10000000 - millisUntilFinished;
                                double dTime = time / 1000;
                                if (dTime < 1.0) {
                                    tvup_Time.setText("0" + decimalFormat.format(dTime));

                                } else {

                                    tvup_Time.setText("" + decimalFormat.format(dTime));
                                }

                                upTime = 10000000 - millisUntilFinished;
                                timeRemaining = millisUntilFinished;
                            }
                        }

                        public void onFinish() {


                        }
                    }.start();

                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    isPaused = true;
                    imgArrowUp.setVisibility(View.INVISIBLE);
                }

                break;
            case R.id.imgDown:
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    imgArrowDown.setVisibility(View.VISIBLE);

                    isPaused = false;
                    imgArrowDown.startAnimation(ArrowDownAnimation);
                    CountDownTimer timer;
                    final long millisInFuture = 10000000; //30 seconds
                    long countDownInterval = 10; //1 second


                    timer = new CountDownTimer(millisInFuture, countDownInterval) {
                        public void onTick(long millisUntilFinished) {
                            //do something in every tick
                            if (isPaused) {
                                cancel();

                            } else {


                                double time = 10000000 - millisUntilFinished;
                                double dTime = time / 1000;
                                if (dTime < 1.0) {
                                    tvdown_Time.setText("0" + decimalFormat.format(dTime));

                                } else {

                                    tvdown_Time.setText("" + decimalFormat.format(dTime));
                                }
                                downTime = 10000000 - millisUntilFinished;
                                timeRemaining = millisUntilFinished;
                            }
                        }

                        public void onFinish() {

                        }
                    }.start();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    isPaused = true;
                    imgArrowDown.setVisibility(View.INVISIBLE);
                }
                break;
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        finish();
    }

}

package com.tech.mobantica.shutter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tech.mobantica.shutter.mSocket.ActivityScanWifi;
import com.tech.mobantica.shutter.mSocket.LogActivity;
import com.tech.mobantica.shutter.mSocket.PortActivity;
import com.tech.mobantica.shutter.mSocket.RecyclerItemListener;
import com.tech.mobantica.shutter.mSocket.WifiAdapter;
import com.tech.mobantica.shutter.mSocket.WifiPojo;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity implements View.OnClickListener {

    WifiManager wifiManager;
    WifiScanReceiver wifiReciever;
    ListView listView;
    String wifis[];
    EditText etPassword;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int MY_PERMISSIONS_STORAGE_LOCATION = 98;

    private Button button;
    private List<WifiPojo> wifiList = new ArrayList<>();
    private RecyclerView recyclerView;
    private WifiAdapter mAdapter;

    private AlertDialog.Builder builder;
    private String SSID;

    private ImageView mArrowBack;
    private TextView mTitle;
    private ImageView mManageThings;
    private ImageView mSettings;

    IntentFilter filter = new IntentFilter();
    private ProgressDialog progressDialog;
    private boolean isDismissProgressDialog = false;

    private String strWifiConnectionMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        builder = new AlertDialog.Builder(MainActivity.this);

        initToolBar();
        initWidget();
        initWifiAdapter();
        initListeners();
        checkRuntimePermissions();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new WifiAdapter(this, wifiList);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

    }

    private void checkRuntimePermissions() {

        if (wifiManager.isWifiEnabled()) {
            int currentVersion = Build.VERSION.SDK_INT;
            if (currentVersion >= Build.VERSION_CODES.M) {
                checkLocationPermission();
            } else {
                loadAdapter();
            }
        } else {
            //set try again text
            // call checkRuntimePermissions onClcik listener
            Toast.makeText(MainActivity.this, "Try again", Toast.LENGTH_SHORT).show();
        }

    }

    private void initListeners() {
        mArrowBack.setVisibility(View.INVISIBLE);
        mTitle.setText("Wi-Fi");
        mManageThings.setVisibility(View.INVISIBLE);
        mSettings.setVisibility(View.INVISIBLE);
    }

    private void initWidget() {
        listView = getListView();
        button = findViewById(R.id.log);
        button.setOnClickListener(this);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        addActionToBroadcast();

        initWifiReceiver();
    }

    private void initWifiReceiver() {
        wifiReciever = new WifiScanReceiver();
        if (wifiReciever != null) {
            registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
    }

    private void addActionToBroadcast() {
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
//        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
    }

    private void initWifiAdapter() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(), recyclerView,
                new RecyclerItemListener.RecyclerTouchListener() {
                    public void onClickItem(View v, int position) {

                        WifiPojo wifiPojo = wifiList.get(position);
                        SSID = wifiPojo.ssidName;
                        if (wifiPojo.flagIsconnected.equals("1")) {
                            startActivity(new Intent(MainActivity.this, PortActivity.class));
                        } else {
                            connectToWifi(position, wifiPojo.ssidName);
                            getWifiList();
                        }
                    }

                    public void onLongClickItem(View v, int position) {

                        WifiPojo wifiPojo = wifiList.get(position);
                        if (wifiPojo.flagIsconnected.equals("1")) {
                            removeSelectedWifiNetwork(position);
                        } else {
                            connectToWifi(position, wifiPojo.ssidName);
                        }
                    }
                }));

        mAdapter = new WifiAdapter(this, wifiList);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    private void initToolBar() {
        mArrowBack = findViewById(R.id.txtBack);
        mTitle = findViewById(R.id.txtTitle);
        mManageThings = findViewById(R.id.txtManageThings);
        mSettings = findViewById(R.id.txtSettings);
    }

    private void loadAdapter() {
        wifiManager.startScan();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(MainActivity.this, LogActivity.class));
    }

    @Override
    protected void onDestroy() {
        if (wifiReciever != null) {
            unregisterReceiver(wifiReciever);
        }

        try {

            if (receiver != null) {
                unregisterReceiver(receiver);
                receiver = null;
            }
        } catch (Exception e) {
            e.getMessage();
        }
        super.onDestroy();
    }

    public class WifiScanReceiver extends BroadcastReceiver {
        @SuppressLint("UseValueOf")
        public void onReceive(Context c, Intent intent) {
            getWifiList();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void getWifiList() {

        WifiManager wifiManager1 = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> list = wifiManager1.getScanResults();

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        String info = mWifi.getExtraInfo();

        wifis = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            wifis[i] = ((list.get(i)).toString());
        }

        if (wifiList.size() > 0) {
            wifiList.clear();
        }

        for (String eachWifi : wifis) {
            WifiPojo wifiPojo = null;
            String[] temp = eachWifi.split(",");
            String ssid = temp[0].substring(5).trim();

            if (info != null && info.equals("\"" + ssid + "\"")) {
                wifiPojo = new WifiPojo(ssid, "1");
            } else {
                wifiPojo = new WifiPojo(ssid, "0");
            }

            wifiList.add(wifiPojo);

        }

        mAdapter.notifyDataSetChanged();

        if (isDismissProgressDialog) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, strWifiConnectionMessage, Toast.LENGTH_SHORT).show();
                isDismissProgressDialog = false;
            }
        }

    }

    private void finallyConnect(final int position, String password, final String networkSSID) {

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);//  BSSID
        wifiConfig.preSharedKey = String.format("\"%s\"", password);

        // remember id
        WifiManager wifiManager1 = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int netId = wifiManager1.addNetwork(wifiConfig);
        wifiManager1.disconnect();
        wifiManager1.enableNetwork(netId, true);
        wifiManager1.reconnect();

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"\"" + networkSSID + "\"\"";
        conf.preSharedKey = "\"" + password + "\"";
        wifiManager1.addNetwork(conf);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Connecting...");
        progressDialog.show();
        progressDialog.setCancelable(true);

        registerReceiver(receiver, filter);

    }

    private void removeAllWifiNetworks() {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            //int networkId = wifiManager.getConnectionInfo().getNetworkId();
            wifiManager.removeNetwork(i.networkId);
            wifiManager.saveConfiguration();
        }

    }

    private void connectToWifi(final int position, final String wifiSSID) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.connect);
        dialog.setTitle("Connect to Network");
        TextView textSSID = (TextView) dialog.findViewById(R.id.textSSID1);

        Button dialogButton = (Button) dialog.findViewById(R.id.okButton);
        etPassword = (EditText) dialog.findViewById(R.id.textPassword);
        textSSID.setText(wifiSSID);

        // if button is clicked, connect to the network;
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pass = etPassword.getText().toString().trim();
                if (pass.isEmpty() || pass.length() == 0 || pass.equals("") || pass == null) {
                    Toast.makeText(MainActivity.this, "Password is empty", Toast.LENGTH_SHORT).show();
                } else if (pass.length() < 8) {
                    Toast.makeText(MainActivity.this, "Password minimum 8 character", Toast.LENGTH_SHORT).show();
                } else {
                    String checkPassword = etPassword.getText().toString();
                    finallyConnect(position, checkPassword, wifiSSID);
                    dialog.dismiss();

                }
            }
        });
        dialog.show();
    }

    private void removeSelectedWifiNetwork(final int position) {

        builder.setMessage(SSID)
                .setCancelable(true)
                .setPositiveButton("Forget", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        int networkId = wifiManager.getConnectionInfo().getNetworkId();
                        wifiManager.removeNetwork(networkId);
                        wifiManager.saveConfiguration();
                        getWifiList();
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setTitle("Forget Password");
        dialog.show();
    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }

        } else {
            loadAdapter();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                checkStoragePermission();
                break;

            case MY_PERMISSIONS_STORAGE_LOCATION:
                loadAdapter();
                break;
        }
    }

    public void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_storage_permission)
                        .setMessage(R.string.text_storage_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_STORAGE_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_STORAGE_LOCATION);
            }

        } else {
            loadAdapter();
        }
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                Log.d("WifiReceiver", ">>>>SUPPLICANT_STATE_CHANGED_ACTION<<<<<<");
                SupplicantState supl_state = ((SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE));
                switch (supl_state) {
                    case ASSOCIATED:
                        Log.i("SupplicantState", "ASSOCIATED");
                        break;
                    case ASSOCIATING:
                        Log.i("SupplicantState", "ASSOCIATING");
                        break;
                    case AUTHENTICATING:
                        Log.i("SupplicantState", "Authenticating...");
                        break;
                    case COMPLETED:
                        Log.i("SupplicantState", "Connected");
                        strWifiConnectionMessage = "Wi-fi connected successfully";
                        isDismissProgressDialog = true;
                        mAdapter.notifyDataSetChanged();
                        setHandlerForWifiList();
                        break;
                    case DISCONNECTED:
                        Log.i("SupplicantState", "Disconnected");
                        removeAllWifiNetworks();
                        strWifiConnectionMessage = "Network connection issue";
                        isDismissProgressDialog = true;
                        getWifiList();
                        break;
                    case DORMANT:
                        Log.i("SupplicantState", "DORMANT");
                        break;
                    case FOUR_WAY_HANDSHAKE:
                        Log.i("SupplicantState", "FOUR_WAY_HANDSHAKE");
                        break;
                    case GROUP_HANDSHAKE:
                        Log.i("SupplicantState", "GROUP_HANDSHAKE");
                        break;
                    case INACTIVE:
                        Log.i("SupplicantState", "INACTIVE");
                        break;
                    case INTERFACE_DISABLED:
                        Log.i("SupplicantState", "INTERFACE_DISABLED");
                        break;
                    case INVALID:
                        Log.i("SupplicantState", "INVALID");
                        break;
                    case SCANNING:
                        Log.i("SupplicantState", "SCANNING");
                        break;
                    case UNINITIALIZED:
                        Log.i("SupplicantState", "UNINITIALIZED");
                        break;
                    default:
                        Log.i("SupplicantState", "Unknown");
                        break;

                }
                int supl_error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                if (supl_error == WifiManager.ERROR_AUTHENTICATING) {
                    Log.i("ERROR_AUTHENTICATING", "ERROR_AUTHENTICATING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                    if (receiver != null)
                    {
                        unregisterReceiver(receiver);
                        strWifiConnectionMessage = "Network connection issue";
                        isDismissProgressDialog = true;
                        getWifiList();

                    }
                }
            }
        }
    };

    private void setHandlerForWifiList() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getWifiList();
            }
        }, 5000);

    }

    private void checkForAuthentication(Intent intent) {

        int supl_error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
        if (supl_error == WifiManager.ERROR_AUTHENTICATING) {
            Log.i("ERROR_AUTHENTICATING", "ERROR_AUTHENTICATING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }
}
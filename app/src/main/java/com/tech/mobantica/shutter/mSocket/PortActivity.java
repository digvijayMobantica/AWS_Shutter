package com.tech.mobantica.shutter.mSocket;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tech.mobantica.shutter.MainActivity;
import com.tech.mobantica.shutter.R;
import com.tech.mobantica.shutter.mShutter.ManageDeviceActivity;
import com.tech.mobantica.shutter.mShutter.MySharepreferences;
import com.tech.mobantica.shutter.mShutter.ShutterActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PortActivity extends AppCompatActivity implements MainAsyncResponse, View.OnClickListener {

    private Wireless wifi;
    private ListView hostList;
    private Button discoverHostsBtn;
    private String discoverHostsStr;
    private Handler signalHandler = new Handler();
    private ProgressDialog scanProgressDialog;
    private Handler scanHandler;
    private IntentFilter intentFilter = new IntentFilter();
    private HostAdapter hostAdapter;
    private List<Host> hosts = Collections.synchronizedList(new ArrayList<Host>());
    private RelativeLayout parentLayout;
    private Database db;
    private Button button;

    private ImageView mArrowBack;
    private TextView mTitle;
    private ImageView mManageThings;
    private ImageView mSettings;

    private MySharepreferences mySharepreferences;
    private String mEndPoint = "", mPoolId = "", mThingName = "";

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info == null) {
                return;
            }
            getNetworkInfo(info);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_port);

        getPreferences();
        initToolBar();
        initWidget();
        initListeners();
        setupHostsAdapter();

    }

    private void getPreferences() {
        mySharepreferences = new MySharepreferences(getApplicationContext());
        mEndPoint = mySharepreferences.getEndPoint();
        mThingName = mySharepreferences.getThingName();
        mPoolId = mySharepreferences.getPoolId();
    }

    private void initListeners() {
        mArrowBack.setVisibility(View.INVISIBLE);
        mTitle.setText("Discover Host");
        mManageThings.setVisibility(View.INVISIBLE);
        mSettings.setVisibility(View.INVISIBLE);

        button.setOnClickListener(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PortActivity.this,LogActivity.class));
                finish();
            }
        });

    }

    private void initWidget() {

        parentLayout = findViewById(R.id.relativelayout);
        hostList = findViewById(R.id.hostList);
        discoverHostsBtn = findViewById(R.id.discoverHosts);
        button = findViewById(R.id.log);
        discoverHostsStr = getResources().getString(R.string.hostDiscovery);
        discoverHostsBtn.setOnClickListener(this);
        wifi = new Wireless(getApplicationContext());
        scanHandler = new Handler(Looper.getMainLooper());

        db = Database.getInstance(getApplicationContext());
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    private void initToolBar() {
        mArrowBack = findViewById(R.id.txtBack);
        mTitle = findViewById(R.id.txtTitle);
        mManageThings = findViewById(R.id.txtManageThings);
        mSettings = findViewById(R.id.txtSettings);
    }

    private void setupHostsAdapter() {
        hostAdapter = new HostAdapter(this, hosts);
        hostList.setAdapter(hostAdapter);

        if (!hosts.isEmpty()) {
            discoverHostsBtn.setText(discoverHostsStr + " (" + hosts.size() + ")");
        }
    }


    private void getNetworkInfo(NetworkInfo info) {

        final Resources resources = getResources();
        final Context context = getApplicationContext();
        try {
            boolean enabled = wifi.isEnabled();
            if (!info.isConnected() || !enabled) {
                signalHandler.removeCallbacksAndMessages(null);

            }

        } catch (Wireless.NoWifiManagerException e) {
            Errors.showError(context, resources.getString(R.string.failedWifiManager));

            String failedwifiManager = DateFormat.getDateTimeInstance().format(new Date());
            String isEnable = getResources().getString(R.string.failedWifiManager)+" : "+failedwifiManager;
            Errors.saveErrorLogs(this,isEnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(receiver);
        signalHandler.removeCallbacksAndMessages(null);

        if (scanProgressDialog != null) {
            scanProgressDialog.dismiss();
        }
        scanProgressDialog = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(receiver, intentFilter);


    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);

        ListAdapter adapter = hostList.getAdapter();
        if (adapter != null) {
            ArrayList<Host> adapterData = new ArrayList<>();
            for (int i = 0; i < adapter.getCount(); i++) {
                Host item = (Host) adapter.getItem(i);
                adapterData.add(item);
            }
            savedState.putSerializable("hosts", adapterData);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);

        hosts = (ArrayList<Host>) savedState.getSerializable("hosts");
        if (hosts != null) {
            setupHostsAdapter();
        }
    }


    @Override
    public void processFinish(final Host h, final AtomicInteger i) {
        scanHandler.post(new Runnable() {

            @Override
            public void run() {
                hosts.add(h);
                hostAdapter.sort(new Comparator<Host>() {

                    @Override
                    public int compare(Host lhs, Host rhs) {
                        try {
                            int leftIp = ByteBuffer.wrap(InetAddress.getByName(lhs.getIp()).getAddress()).getInt();
                            int rightIp = ByteBuffer.wrap(InetAddress.getByName(rhs.getIp()).getAddress()).getInt();

                            return leftIp - rightIp;
                        } catch (UnknownHostException ignored) {
                            return 0;
                        }
                    }
                });

                discoverHostsBtn.setText(discoverHostsStr + " (" + hosts.size() + ")");
                if (i.decrementAndGet() == 0) {
                    discoverHostsBtn.setAlpha(1);
                    discoverHostsBtn.setEnabled(true);
                }
            }
        });
    }


    @Override
    public void processFinish(int output) {
        if (scanProgressDialog != null && scanProgressDialog.isShowing()) {
            scanProgressDialog.incrementProgressBy(output);
        }
    }


    @Override
    public void processFinish(String output) {

    }

    @Override
    public void processFinish(final boolean output) {
        scanHandler.post(new Runnable() {

            @Override
            public void run() {
                if (output && scanProgressDialog != null && scanProgressDialog.isShowing()) {
                    scanProgressDialog.dismiss();
                }
            }
        });
    }

    @Override
    public <T extends Throwable> void processFinish(final T output) {
        scanHandler.post(new Runnable() {

            @Override
            public void run() {
                Errors.showError(getApplicationContext(), output.getLocalizedMessage());
            }
        });
    }


    @Override
    public void onClick(View view) {
        getHostList();
    }

    private void getHostList() {
        Resources resources = getResources();
        final Context context = getApplicationContext();
        try {
            if (!wifi.isEnabled()) {
                Errors.showError(context, resources.getString(R.string.wifiDisabled));

                String dateTimeEnabled = DateFormat.getDateTimeInstance().format(new Date());
                String isEnable = getResources().getString(R.string.wifiDisabled)+" : "+dateTimeEnabled;
                Errors.saveErrorLogs(this,isEnable);

                return;
            }

            if (!wifi.isConnectedWifi()) {
                Errors.showError(context, resources.getString(R.string.notConnectedWifi));

                String dateTimeNotConnected = DateFormat.getDateTimeInstance().format(new Date());
                String isEnable = getResources().getString(R.string.notConnectedWifi)+" : "+dateTimeNotConnected;
                Errors.saveErrorLogs(this,isEnable);

                return;
            }
        } catch (Wireless.NoWifiManagerException | Wireless.NoConnectivityManagerException e) {
            Errors.showError(context, resources.getString(R.string.failedWifiManager));

            String dateTimeManager = DateFormat.getDateTimeInstance().format(new Date());
            String isEnable = getResources().getString(R.string.failedWifiManager)+" : "+dateTimeManager;
            Errors.saveErrorLogs(this,isEnable);

            return;
        }

        int numSubnetHosts;
        try {
            numSubnetHosts = wifi.getNumberOfHostsInWifiSubnet();
        } catch (Wireless.NoWifiManagerException e) {
            Errors.showError(context, resources.getString(R.string.failedSubnetHosts));

            String dateTimeSubnet = DateFormat.getDateTimeInstance().format(new Date());
            String isEnable = getResources().getString(R.string.failedSubnetHosts)+" : "+dateTimeSubnet;
            Errors.saveErrorLogs(this,isEnable);

            return;
        }

        hosts.clear();
        discoverHostsBtn.setText(discoverHostsStr);
        hostAdapter.notifyDataSetChanged();

        scanProgressDialog = new ProgressDialog(PortActivity.this);
        scanProgressDialog.setCancelable(true);
        scanProgressDialog.setTitle(resources.getString(R.string.hostScan));
        scanProgressDialog.setMessage(String.format(resources.getString(R.string.subnetHosts), numSubnetHosts));
        scanProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        scanProgressDialog.setProgress(0);
        scanProgressDialog.setMax(numSubnetHosts);
        scanProgressDialog.show();

        try {
            Integer ip = wifi.getInternalWifiIpAddress(Integer.class);
            new ScanHostsAsyncTask(PortActivity.this, db).execute(ip, wifi.getInternalWifiSubnet(), UserPreference.getHostSocketTimeout(context));
            discoverHostsBtn.setAlpha(.3f);
            discoverHostsBtn.setEnabled(false);
        } catch (UnknownHostException | Wireless.NoWifiManagerException e) {
            Errors.showError(context, resources.getString(R.string.notConnectedWifi));

            String currentDateTimeStringEnable = DateFormat.getDateTimeInstance().format(new Date());
            String isEnable = getResources().getString(R.string.notConnectedWifi)+" : "+currentDateTimeStringEnable;
            Errors.saveErrorLogs(this,isEnable);

        }

        hostList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


//                String ipValue = String.valueOf(hostList.getItemAtPosition(i));
                String ipValue = hosts.get(i).getIp().toString();
//                getPopup(ipValue);

                socketConnection(ipValue);
            }
        });

    }

    private void socketConnection(final String ipValue) {
        final Dialog dialog = new Dialog(PortActivity.this);
        dialog.setContentView(R.layout.socket);
        dialog.setTitle("Network Information");

        final EditText etSSID = dialog.findViewById(R.id.ssid);
        final EditText etPASSWORD = dialog.findViewById(R.id.password);
        Button btnCancel = dialog.findViewById(R.id.btnNigative);
        Button btnConnect = dialog.findViewById(R.id.btnPositive);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ssid = etSSID.getText().toString();
                String password = etPASSWORD.getText().toString();

                if(ssid.isEmpty() || ssid.length() == 0 || ssid.equals("") || ssid == null) {
                    Toast.makeText(PortActivity.this, "Enter SSID", Toast.LENGTH_SHORT).show();

                } else if(password.isEmpty() || password.length() == 0 || password.equals("") || password == null){
                    Toast.makeText(PortActivity.this, "Password is empty", Toast.LENGTH_SHORT).show();

                } else if(password.length() < 8) {
                    Toast.makeText(PortActivity.this, "Password minimum 8 character", Toast.LENGTH_SHORT).show();
                } else {

//                String connectionString = "SSID:" + ssid + "PSWD:" + password + "THNM:" + "palmat2";
                    String connectionString = "\\cfg\\SSID:\"" + ssid + "\"\\PSWD:\"" + password + "\"\\THNM:\"palmat2\"";
                    new connectSocketTask().execute(connectionString, ipValue);
                    dialog.dismiss();

                    if((mEndPoint.equals("") && mPoolId.equals("")) || mThingName.equals("")) {
                        startActivity(new Intent(PortActivity.this, ManageDeviceActivity.class));
                    }
                    else {
                        startActivity(new Intent(PortActivity.this, ShutterActivity.class));
                    }
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               //startActivity(new Intent(PortActivity.this, ManageDeviceActivity.class));
               dialog.dismiss();
            }
        });
        dialog.show();
    }

    public class connectSocketTask extends AsyncTask<String, Void, String> {

        private String mString="";

        @Override
        protected String doInBackground(String... connectionString) {

            Socket pingSocket = null;
            PrintWriter out = null;
            try {
                pingSocket = new Socket(connectionString[1], 5045); // ip value
                out = new PrintWriter(pingSocket.getOutputStream(), true);
                mString = connectionString[0];   // ip connection string
                out.println(mString);    // writing string on ip

                final Handler handler = new Handler();
                final Socket finalPingSocket = pingSocket;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 8000ms
                        try {

                            byte[] buffer = new byte[2048];
                            int bytes;
                            InputStream ing = finalPingSocket.getInputStream();
                            bytes = ing.read(buffer);
                            String readMessage = new String(buffer, 0, bytes);
                            Log.d("MainActivity->", "Message :: "+readMessage);

                            String currentDateTimeStringEnable = DateFormat.getDateTimeInstance().format(new Date());
                            String response = readMessage +" : "+ currentDateTimeStringEnable;
                            Errors.saveErrorLogs(PortActivity.this,response);

                            if(!readMessage.isEmpty()){
                                startActivity(new Intent(PortActivity.this, ManageDeviceActivity.class));
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }, 8000);

            } catch (IOException e) {
                mString = e.getMessage();

            } finally {
                if (null != out) {
                    out.close();
                }
                try {
                    if (null != pingSocket) {
                        pingSocket.close();
                    }
                } catch (Exception e) {

                    return e.getMessage();
                }
            }
            return mString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Snackbar snackbar = Snackbar
                    .make(parentLayout, result, Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // connectToMqtt();
                        }
                    });

            snackbar.show();

            // Toast.makeText(PortActivity.this, result, Toast.LENGTH_SHORT).show();
            String currentDateTimeStringEnable = DateFormat.getDateTimeInstance().format(new Date());
            String response = result +" : "+ currentDateTimeStringEnable;
            Errors.saveErrorLogs(PortActivity.this,response);

        }
    }
}



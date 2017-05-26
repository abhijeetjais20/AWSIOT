package com.tyco.drypipemonitoring.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.tyco.drypipemonitoring.R;
import com.tyco.drypipemonitoring.datamodel.DataModel;
import com.tyco.drypipemonitoring.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import com.tyco.drypipemonitoring.activity.AWSIOtConnectionService.*;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by abhijitk on 9/17/2016.
 */
public class DryPipeMonitor extends AppCompatActivity {

    private static final String TAG = "DryPipeMonitor";
    public static AWSIOtConnectionService mService;
    public static boolean mAWSBound;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    Toolbar toolbar;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;

    //Receiver
    mHomeReceiver receiver;
    TextView aTV, cTV, iTV, tTV, wTV;
    TextView aTime, cTime, iTime, wTime, tTime;
    Button aGraph, cGraph, iGraph, tGraph, wGraph;

    private static String[] system = {"D_a6-c9", "D_af-42", "D_a6-c8", "D_a1:f5"};
    private TextView statusTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
//        startAWSCommunication();
        receiver = new mHomeReceiver();
        IntentFilter home = new IntentFilter("updateUI");
        registerReceiver(receiver, home);

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        setupToolbar();

        statusTV = (TextView) findViewById(R.id.status);
        aTV = (TextView) findViewById(R.id.valueA);
        cTV = (TextView) findViewById(R.id.valueC);
        iTV = (TextView) findViewById(R.id.valueI);
        tTV = (TextView) findViewById(R.id.valueT);
        wTV = (TextView) findViewById(R.id.valueW);

        aTime = (TextView) findViewById(R.id.valueAtime);
        cTime = (TextView) findViewById(R.id.valueCtime);
        iTime = (TextView) findViewById(R.id.valueItime);
        tTime = (TextView) findViewById(R.id.valueTtime);
        wTime = (TextView) findViewById(R.id.valueWtime);

        aGraph = (Button) findViewById(R.id.aGraph);
        cGraph = (Button) findViewById(R.id.cGraph);
        iGraph = (Button) findViewById(R.id.iGraph);
        tGraph = (Button) findViewById(R.id.tGraph);
        wGraph = (Button) findViewById(R.id.wGraph);

        aGraph.setOnClickListener(onAClick);
        cGraph.setOnClickListener(onCClick);
        iGraph.setOnClickListener(onIClick);
        tGraph.setOnClickListener(onTClick);
        wGraph.setOnClickListener(onWClick);

        DataModel[] drawerItem = new DataModel[system.length];
        for (int i = 0; i < system.length; i++) {
            drawerItem[i] = new DataModel(R.drawable.nav_image, system[i]);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.list_view_item_row, drawerItem);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        setupDrawerToggle();
    }

    View.OnClickListener onAClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onAClick");
        }
    };

    View.OnClickListener onCClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onCClick");
        }
    };

    View.OnClickListener onIClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onIClick");
        }
    };

    View.OnClickListener onTClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onTClick");
        }
    };

    View.OnClickListener onWClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onWClick");
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        try {
            Intent intent = new Intent(this, AWSIOtConnectionService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

//    private void startAWSCommunication() {
//        try {
//            Intent intent = new Intent(this, AWSIOtConnectionService.class);
//            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }


    public static ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            try {
                AWSBinder binder = (AWSBinder) service;
                mService = binder.getService();
                mService.connect(system[0]);
                mAWSBound = true;
                Log.d(TAG, "AWS initialized");
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mAWSBound = false;
            Log.d(TAG, "AWS Disconnected");
        }
    };

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

    }

    private void selectItem(int position) {
        Log.i(TAG, "System: " + system[position]);
        mService.Disconnect();
        mService.connect(system[position]);

        if (position >= 0) {
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(system[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }

        aTV.setText("0" + " \u2109");
        cTV.setText("");
        iTV.setText("");
        tTV.setText("");
        wTV.setText("");
        aTime.setText("");
        cTime.setText("");
        iTime.setText("");
        tTime.setText("");
        wTime.setText("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    void setupDrawerToggle() {
        mDrawerToggle = new android.support.v7.app.ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        //This is necessary to change the icon of the Drawer Toggle upon state change.
        mDrawerToggle.syncState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unbindService(mConnection);
    }

    private String timestamp;
    private int value;
    private String key;

    public class mHomeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String params;
            String status;
            if (extras != null) {
                if (extras.containsKey("Params")) {
                    params = extras.getString("Params");
                    Log.v(TAG, "params: " + params);
                    try {
                        JSONObject jObj = new JSONObject(params);
                        timestamp = jObj.get("timestamp").toString().trim();

                        String number = jObj.get("value").toString().trim();
                        Double obj = new Double(number);
                        value = obj.intValue();

                        key = jObj.get("tag").toString().trim();
                        updateFeatured(key, value, timestamp);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.v(TAG, "timestamp: " + timestamp + ", value: " + value + ", key: " + key);
                } else if (extras.containsKey("Status")) {
                    status = extras.getString("Status");
                    Log.v(TAG, "status: " + status);
                    statusTV.setText(status);
                }

            }
        }
    }

    private void updateFeatured(final String key, final int value, final String timestamp) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (Constant.DEBUG) Log.d(TAG, "Home screen Update");
                    if (key != null && key.equalsIgnoreCase("A")) {
                        aTV.setText(value + " \u2109");
                        aTime.setText(getDateTime(timestamp));
                    } else if (key != null && key.equalsIgnoreCase("C")) {
                        cTV.setText(value + " PSI");
                        cTime.setText(getDateTime(timestamp));
                    } else if (key != null && key.equalsIgnoreCase("I")) {
                        iTV.setText(value + " PSI");
                        iTime.setText(getDateTime(timestamp));
                    } else if (key != null && key.equalsIgnoreCase("T")) {
                        tTV.setText(value + " PSI");
                        tTime.setText(getDateTime(timestamp));
                    } else if (key != null && key.equalsIgnoreCase("W")) {
                        wTV.setText(value + " PSI");
                        wTime.setText(getDateTime(timestamp));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getDateTime(String tickTime) {
        tickTime = tickTime + "000";
        String diff = "0 minutes ago";
        String minutes = "";
        String hours = "";
        String days = "";
        try {
            if (tickTime != null) {
                Date currDate = getTime();
                long time = Long.parseLong(tickTime);
                SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm a");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(time);
                Log.d(TAG, "system date : " + formatter.format(calendar.getTime()));
                Date accutuleDate = calendar.getTime();

                try {
                    long mills = currDate.getTime() - accutuleDate.getTime();
                    int hrs = (int) (mills / (1000 * 60 * 60));
                    int mins = (int) (mills / (1000 * 60)) % 60;

                    if (hrs > 0) {
                        hours = hrs + " hours ";
                    } else if (mins > 0) {
                        minutes = mins + " minutes ";
                    }
                    diff = hours + " " + minutes + "ago";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return diff;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Date getTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm a");
        Date d = c.getTime();
        String currdate = dateFormat.format(c.getTime());
        Log.d(TAG, "current date : " + currdate);
        return d;
    }

}

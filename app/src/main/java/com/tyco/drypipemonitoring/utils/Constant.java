package com.tyco.drypipemonitoring.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.amazonaws.regions.Regions;

/**
 * Created by abhijitk on 9/17/2016.
 */
public class Constant {

    private static final String TAG = "";
    public static boolean DEBUG = true;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_ERROR = 6;
    public static final int MESSAGE_LOST = 7;

    public static final String SYSTEM_NAME = "device_name";

    public static final String CUSTOMER_SPECIFIC_ENDPOINT = "a21b4zymvte6s7.iot.us-east-1.amazonaws.com";
    public static final String COGNITO_POOL_ID = "us-east-1:9f10639b-a424-44f8-b460-f34ee26ce1a8";
    public static final Regions MY_REGION = Regions.US_EAST_1;

    public static boolean isNetworkAvailable(Context c) {
        try{
            if(Constant.DEBUG)  Log.d(TAG ,"isNetworkAvailable(): ");
            ConnectivityManager connectivityManager= (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if(Constant.DEBUG)  Log.d(TAG,"isConnected: "+isConnected);
            return isConnected;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
}

package com.tyco.drypipemonitoring.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tyco.drypipemonitoring.datamodel.AWSDataModel;
import com.tyco.drypipemonitoring.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by abhijitk on 9/17/2016.
 */
public class AWSListener extends Handler {

    private static String TAG = "AWSListener";
    public static String readMessage = "";
    private static Context context;

    public AWSListener(Context applicationContext) {
        this.context = applicationContext;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.MESSAGE_STATE_CHANGE:
                if (Constant.DEBUG) Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                    case AWSIOtConnectionService.STATE_CONNECTED:
                        if (Constant.DEBUG) Log.d(TAG, "MESSAGE_STATE_CHANGE: STATE_CONNECTED");
                        sendSystemStatus("System Connected");
                        break;
                    case AWSIOtConnectionService.STATE_CONNECTING:
                        if (Constant.DEBUG) Log.d(TAG, "MESSAGE_STATE_CHANGE: STATE_CONNECTING");
                        sendSystemStatus("System Connecting...");
                        break;
                    case AWSIOtConnectionService.STATE_NONE:
                        if (Constant.DEBUG) Log.d(TAG, "MESSAGE_STATE_CHANGE: STATE_NONE");
                        sendSystemStatus("System Disconnected");
                        break;
                }
                break;

            case Constant.MESSAGE_READ:
                String readMessage = (String) msg.obj;
                Log.i(TAG, "Message: " + readMessage);
                sendMessage(readMessage);
                break;
            case Constant.MESSAGE_DEVICE_NAME:
                // save the connected system's name
                String mConnectedSystemName = msg.getData().getString(Constant.SYSTEM_NAME);
                Log.i(TAG, "System Name: " + mConnectedSystemName);
                break;
            case Constant.MESSAGE_TOAST:
                readMessage = "";
                break;
            case Constant.MESSAGE_ERROR:
                if (Constant.DEBUG) Log.d(TAG, " Reinitializing readMessage ");
                readMessage = "";
                break;
        }
    }

    private void sendMessage(String readMessage) {
        try {
            if (readMessage != null && !readMessage.equalsIgnoreCase("")) {
                Log.v("writeMessage", "Valid Receiving Message:  " + readMessage);
                try {
                    sendMessagetoQueue(readMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                readMessage = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void sendMessagetoQueue(String jsonString) throws JSONException {
        String timestamp = "";
        String value = "";
        String tag = "";
        Gson gson = new Gson();
        gson.toJson(jsonString);
        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject) jsonParser.parse(jsonString);
        JsonObject stateObj = jo.get("state").getAsJsonObject();
        JsonObject obj = stateObj.get("reported").getAsJsonObject();
        timestamp = obj.get("timestamp").getAsString();
        Log.d(TAG, "TimeStamp: " + timestamp);

        JSONObject objData = new JSONObject();

        try {

            AWSDataModel awsObject = gson.fromJson( jsonString, AWSDataModel.class);
            String A = awsObject.getState().getReported().getA();
            String C = awsObject.getState().getReported().getC();
            String I = awsObject.getState().getReported().getI();
            String T = awsObject.getState().getReported().getT();
            String W = awsObject.getState().getReported().getW();
//            Log.d(TAG, "Value A: " + A + ",Value I: " + I + ",Value W: " + W );
//            Log.d(TAG, "Value C: " + C + ",Value T: " + T);

            if (A != null) {
                objData.put("timestamp", timestamp);
                objData.put("value", A);
                objData.put("tag", "A");
            }else if (C  != null) {
                objData.put("timestamp", timestamp);
                objData.put("value", C);
                objData.put("tag", "C");
            }else if (I  != null) {
                objData.put("timestamp", timestamp);
                objData.put("value", I);
                objData.put("tag", "I");
            }else if (T  != null) {
                objData.put("timestamp", timestamp);
                objData.put("value", T);
                objData.put("tag", "T");
            }else if (W != null) {
                objData.put("timestamp", timestamp);
                objData.put("value", W);
                objData.put("tag", "W");
            }
            Log.d(TAG, "JSONObject : " + objData.toString());
            Intent intent = new Intent();
            intent.putExtra("Params", objData.toString());
            intent.setAction("updateUI");
            context.sendBroadcast(intent);
        } catch (Exception d) {
            d.printStackTrace();
        }
    }

    private void sendSystemStatus(String state){
        Intent intent = new Intent();
        intent.putExtra("Status", state);
        intent.setAction("updateUI");
        context.sendBroadcast(intent);
    }
}

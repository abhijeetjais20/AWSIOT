package com.tyco.drypipemonitoring.activity;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.tyco.drypipemonitoring.utils.Constant;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by abhijitk on 9/17/2016.
 */
public class AWSIOtConnectionService extends Service {
    // Debugging
    private static final String TAG = "AWSService";
    private static final boolean D = true;

    private Handler mHandler;
    private int mState;
    private static String system;

    private AWSIotMqttManager mqttManager;
    private String clientId;
    private AWSCredentials awsCredentials;
    private CognitoCachingCredentialsProvider credentialsProvider;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 2;  // now connected to a remote system


    public final IBinder mBinder = new AWSBinder();

    public class AWSBinder extends Binder {
        public AWSIOtConnectionService getService() {
            try {
                mHandler = new AWSListener(getApplicationContext());
                return AWSIOtConnectionService.this;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constant.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    public synchronized void connect(String system) {
        if (D) Log.d(TAG, "connect to: " + system);
        try {
            this.system = system;
            AWSServerConnection(system);
            setState(STATE_CONNECTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void connected(String system) {
        if (D) Log.d(TAG, "connected");
        try {
            // Send the name of the connected system back to the UI Activity
            Message msg = mHandler.obtainMessage(Constant.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(Constant.SYSTEM_NAME, system);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            AWSServerMessage(system);
            setState(STATE_CONNECTED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        setState(STATE_NONE);
        // MQTT Client
        if (Constant.isNetworkAvailable(getApplicationContext())) {
            mqttManager = new AWSIotMqttManager(clientId, Constant.CUSTOMER_SPECIFIC_ENDPOINT);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    awsCredentials = credentialsProvider.getCredentials();
                }
            }).start();
        }
    }

    public synchronized void Disconnect() {
        try {
            mqttManager.disconnect();
            mqttManager = null;
            stop();
        } catch (Exception e) {
            Log.e(TAG, "Disconnect error.", e);
        }

    }

    private void connectionFailed() {
        try {
            // Send a failure message back to the Activity
            Message msg = mHandler.obtainMessage(Constant.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString("toast", "Unable to connect system");
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            //start again
            AWSIOtConnectionService.this.connect(system);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void AWSServerConnection(final String system) {
        try {

            if (mqttManager != null) {
                mqttManager.connect(credentialsProvider, new AWSIotMqttClientStatusCallback() {
                    @Override
                    public void onStatusChanged(final AWSIotMqttClientStatus status, final Throwable throwable) {
                        Log.d(TAG, "Status = " + String.valueOf(status));

                        if (status == AWSIotMqttClientStatus.Connecting) {
//                        tvStatus.setText("Connecting...");
                            mHandler.obtainMessage(Constant.MESSAGE_STATE_CHANGE, STATE_CONNECTING, -1).sendToTarget();
                        } else if (status == AWSIotMqttClientStatus.Connected) {
//                        tvStatus.setText("Connected");
                            connected(system);
                        } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                            if (throwable != null) {
                                Log.e(TAG, "Connection error.", throwable);
                                throwable.printStackTrace();
                            }
                            mHandler.obtainMessage(Constant.MESSAGE_STATE_CHANGE, STATE_CONNECTING, -1).sendToTarget();
                        } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                            if (throwable != null) {
                                Log.e(TAG, "Connection error.", throwable);
                                throwable.printStackTrace();
                            }
                            mHandler.obtainMessage(Constant.MESSAGE_STATE_CHANGE, STATE_NONE, -1).sendToTarget();
                        } else {
                            mHandler.obtainMessage(Constant.MESSAGE_STATE_CHANGE, STATE_NONE, -1).sendToTarget();
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Connection error.", e);
            e.printStackTrace();
        }
    }

    private void AWSServerMessage(String system) {
        try {
            mqttManager.subscribeToTopic("mDot/" + system + "/update", AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            try {
                                String message = new String(data, "UTF-8");
                                Log.d(TAG, "Message arrived:");
                                Log.d(TAG, "   Topic: " + topic);
//                                Log.d(TAG, " Message: " + message);
                                Message msg = Message.obtain(); // Creates an new Message instance
                                msg.obj = message; // Put the string into Message, into "obj" field.
                                mHandler.obtainMessage(Constant.MESSAGE_READ, -1, -1, msg.obj).sendToTarget();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        try {
            clientId = UUID.randomUUID().toString();
            // Initialize the AWS Cognito credentials provider
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(), // context
                    Constant.COGNITO_POOL_ID, // Identity Pool ID
                    Constant.MY_REGION // Region
            );

            if (Constant.isNetworkAvailable(getApplicationContext())) {
                // MQTT Client
                mqttManager = new AWSIotMqttManager(clientId, Constant.CUSTOMER_SPECIFIC_ENDPOINT);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        awsCredentials = credentialsProvider.getCredentials();
                    }
                }).start();
            }
            mState = STATE_NONE;

            Log.i(TAG, "onBind");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mBinder;
    }

}

package com.example.wataru.helloandroidwear;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;

public class MainActivity extends Activity implements SensorEventListener {
    private final String TAG = MainActivity.class.getName();
    private TextView mTextView;
    private GoogleApiClient mGoogleApiClient;
    private SensorManager mSensorManager;
    private String mNode;
    private final float GAIN = 0.9f;
    private final String acce = "/accelerate";
//    private final String acceX = "/accelerate/x";
//    private final String acceY = "/accelerate/y";
//    private final String acceZ = "/accelerate/z";
    private float x, y, z;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "onConnected");
                        //GoogleApiClient.ConnectionCallback内でNodeIDを取得する
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                                //Nodeは一個に限定
                                if (nodes.getNodes().size() > 0) {
                                    mNode = nodes.getNodes().get(0).getId();
                                    Log.d(TAG, mNode);
                                }
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {

                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed : " + connectionResult.toString());
                    }
                })
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        mGoogleApiClient.connect();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = (x * GAIN + event.values[0] * (1 - GAIN));
            y = (y * GAIN + event.values[1] * (1 - GAIN));
            z = (z * GAIN + event.values[2] * (1 - GAIN));

            if (mTextView != null)
                mTextView.setText(String.format("X : %f\nY : %f\nZ : %f\n", x, y, z));
        }
        final String message = "Hello world";
        final DataMap dataMap = new DataMap();
        dataMap.putFloat("X", x);
        dataMap.putFloat("Y", y);
        dataMap.putFloat("Z", z);


        if(mNode!=null) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, acce, dataMap.toByteArray()).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    if (!sendMessageResult.getStatus().isSuccess()) {
                        Log.d(TAG, "ERROR : failed to send Message" + sendMessageResult.getStatus().getStatusCode());
                    }
                }
            });

//            Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, acceX, fromFloat(x)).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
//                @Override
//                public void onResult(MessageApi.SendMessageResult result) {
//                    if (!result.getStatus().isSuccess()) {
//                        Log.d(TAG, "ERROR : failed to send Message" + result.getStatus().getStatusCode());
//                    }
//                }
//            });
//            Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, acceY, fromFloat(y)).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
//                @Override
//                public void onResult(MessageApi.SendMessageResult result) {
//                    if (!result.getStatus().isSuccess()) {
//                        Log.d(TAG, "ERROR : failed to send Message" + result.getStatus().getStatusCode());
//                    }
//                }
//            });
//            Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, acceZ, fromFloat(z)).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
//                @Override
//                public void onResult(MessageApi.SendMessageResult result) {
//                    if (!result.getStatus().isSuccess()) {
//                        Log.d(TAG, "ERROR : failed to send Message" + result.getStatus().getStatusCode());
//                    }
//                }
//            });
        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public static byte[] fromFloat(float value) {
        int arraySize = Float.SIZE / Byte.SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(arraySize);
        return buffer.putFloat(value).array();
    }
}


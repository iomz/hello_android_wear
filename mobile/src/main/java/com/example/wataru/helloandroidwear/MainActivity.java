package com.example.wataru.helloandroidwear;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;


public class MainActivity extends ActionBarActivity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = MainActivity.class.getName();
    private final String acce = "/accelerate";
//    private final String acceX = "/accelerate/x";
//    private final String acceY = "/accelerate/y";
//    private final String acceZ = "/accelerate/z";
    private GoogleApiClient mGoogleApiClient;
    private TextView mtextview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mtextview = (TextView) findViewById(R.id.textView);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed:" + connectionResult.toString());
                    }
                })
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
//        Log.d(TAG, "onMessageReceived : " + messageEvent.getPath());
//        DataInputStream x,y,z  = new DataInputStream(new ByteArrayInputStream(messageEvent.getData()));

        if (messageEvent.getPath().equals(acce)){
            DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());
            Log.d(TAG, "x = " + dataMap.getFloat("X"));
            Log.d(TAG, "y = " + dataMap.getFloat("Y"));
            Log.d(TAG, "z = " + dataMap.getFloat("Z"));
            //毎回おくると処理が追いつかない？
//            mtextview.setText(String.format("X : %f\nY : %f\nZ : %f\n", dataMap.getFloat("X"), dataMap.getFloat("Y"), dataMap.getFloat("Z")));
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");

    }
}
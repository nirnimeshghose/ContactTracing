package com.example.contacttracing;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.net.wifi.WifiManager.*;

public class MainActivity extends AppCompatActivity {

    Button mButton;

    private FileOutputStream outputStream = null;
    private Handler mHandler = new Handler();

    private WifiManager wifiManager;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;

    String display;
    String FILE_NAME_WIFI = "contactTrace";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }


    }

    public void startTrace(View v){
        FILE_NAME_WIFI = FILE_NAME_WIFI + Double.toString(Math.random()) + ".txt";
        try {
            outputStream = openFileOutput(FILE_NAME_WIFI, MODE_APPEND);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Permission is not granted
            // Should we show an explanation?
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},101);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else
            {

            }
            return;
        }

        mToastRunnableWifi.run();
    }

    public void stopTrace(View v){
        mHandler.removeCallbacks(mToastRunnableWifi);
        unregisterReceiver(wifiScanReceiver);
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            boolean success = intent.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                Log.e("WiFI","In Success/Reg");
            }
        }
    };

    private Runnable mToastRunnableWifi = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void run() {

            Log.e("WiFI","In Run");

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            registerReceiver(wifiScanReceiver, intentFilter);

            boolean success = wifiManager.startScan();
            if(success)
            {
                results = wifiManager.getScanResults();
                Log.e("WiFI","In Success");
                for (ScanResult scanResult : results) {
                    long actualTimestamp = System.currentTimeMillis() - SystemClock.elapsedRealtime() + (scanResult.timestamp / 1000);
                    display = scanResult.BSSID +" " + Long.toString(actualTimestamp) + "\n";

                    try {
                        outputStream.write(display.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                Log.e("WiFI","In Fail");
            }
            mHandler.postDelayed(mToastRunnableWifi, 1000);
        }
    };



}
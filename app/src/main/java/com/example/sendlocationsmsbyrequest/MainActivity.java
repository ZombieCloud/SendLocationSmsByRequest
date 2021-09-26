package com.example.sendlocationsmsbyrequest;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btnStartStop;
    private static final String TAG = MainActivity.class.getSimpleName();
    private SmsReceiver smsReceiver = new SmsReceiver();
    private Boolean isServiceRunning;
    private int MY_KEY_FOR_RETURNED_VALUE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartStop = (Button) findViewById(R.id.btnStartStop);
        btnStartStop.setText("Start");
        isServiceRunning = false;
    }

    public void btnStartStop_OnClick(View v) throws InterruptedException{

        // проверить, включен ли gps
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return;
        }

        // проверить, включена ли отсылка sms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                // permission is not granted, ask for permission:
                requestPermissions(new String[] { Manifest.permission.SEND_SMS}, MY_KEY_FOR_RETURNED_VALUE);
                return;
            }
        }

        // проверить, включен ли прием sms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                // permission is not granted, ask for permission:
                requestPermissions(new String[] { Manifest.permission.RECEIVE_SMS}, MY_KEY_FOR_RETURNED_VALUE);
                return;
            }
        }

        // проверить, включено ли определение местоположения
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // permission is not granted, ask for permission:
                requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION}, MY_KEY_FOR_RETURNED_VALUE);
                return;
            }
        }



        if (isServiceRunning) {
            unregisterBroadcastReceiver();
            btnStartStop.setText("Start");
        } else {
            registerBroadcastReceiver();
            btnStartStop.setText("Stop");
        }
    }



    public void registerBroadcastReceiver() {
        this.registerReceiver(smsReceiver, new IntentFilter(
                "android.provider.Telephony.SMS_RECEIVED"));
        Toast.makeText(getApplicationContext(), "Started",
                Toast.LENGTH_SHORT).show();
        isServiceRunning = true;
    }



    public void unregisterBroadcastReceiver() {
        this.unregisterReceiver(smsReceiver);
        Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
        isServiceRunning = false;
    }



    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

}
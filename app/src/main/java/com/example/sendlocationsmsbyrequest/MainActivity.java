package com.example.sendlocationsmsbyrequest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btnStartStop;
    private static final String TAG = MainActivity.class.getSimpleName();
    private SmsReceiver smsReceiver = new SmsReceiver();
    private Boolean isServiceRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartStop = (Button) findViewById(R.id.btnStartStop);
        btnStartStop.setText("Start");
        isServiceRunning = false;
    }

    public void btnStartStop_OnClick(View v) throws InterruptedException{
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

}
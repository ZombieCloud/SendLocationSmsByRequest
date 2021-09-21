package com.example.sendlocationsmsbyrequest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Context;

public class MainActivity extends AppCompatActivity {

    Button btnStartStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartStop = (Button) findViewById(R.id.btnStartStop);

        if (isServiceRunning(SmsReceiver.class)) {
            btnStartStop.setText("Stop");
        } else {
            btnStartStop.setText("Start");
        }
//        finish();
    }

    public void btnStartStop_OnClick(View v) throws InterruptedException{
        if (isServiceRunning(SmsReceiver.class)) {
            stopService(new Intent(this, SmsReceiver.class));
            btnStartStop.setText("Start");
        } else {
            startService(new Intent(this, SmsReceiver.class));
            btnStartStop.setText("Stop");
        }
    }


    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
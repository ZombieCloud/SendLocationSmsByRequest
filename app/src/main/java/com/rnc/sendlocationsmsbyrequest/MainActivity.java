package com.rnc.sendlocationsmsbyrequest;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btnStartStop;
    TextView tvSecretWord;
    CheckBox checkBox_WakeUp;
    CheckBox checkBox_ScreenOn;
    CheckBox checkBox_LaunchMaps;

    private static final String TAG = MainActivity.class.getSimpleName();
    private final SmsReceiver smsReceiver = new SmsReceiver();
    private Boolean isServiceRunning;
    private final int MY_KEY_FOR_RETURNED_VALUE = 0;
    private SharedPreferences settings;             // хранилище переменных
    private SharedPreferences.Editor editor;

    private String secretWord;
    private String sendResponse;

    private String cbWakeUp;
    private String cbScreenOn;
    private String cbLaunchMaps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartStop = findViewById(R.id.btnStartStop);
        tvSecretWord = findViewById(R.id.tvSecretWord);
        checkBox_WakeUp = findViewById(R.id.checkBox_WakeUp);
        checkBox_ScreenOn = findViewById(R.id.checkBox_ScreenOn);
        checkBox_LaunchMaps = findViewById(R.id.checkBox_LaunchMaps);
//        btnStartStop.setText("Start");
//        isServiceRunning = false;

        settings = this.getSharedPreferences("SLSbR_STORAGE", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);  // инициируем хранилище переменных   MODE_MULTI_PROCESS - чтоб было доступно из broadcast receiver
        editor = settings.edit();


        //  Достаем checkBox_WakeUp из хранилища
        cbWakeUp = settings.getString( "state_WakeUp", null);    // достаем переменную из хранилища
        if (cbWakeUp == null) {
            cbWakeUp = "0";
            editor.putString( "state_WakeUp", cbWakeUp);   // положить переменную в хранилище
            editor.commit();
        }
        if ((cbWakeUp.equals("1"))) {
            checkBox_WakeUp.setChecked(true);
        } else {
            checkBox_WakeUp.setChecked(false);
        }



        //  Достаем checkBox_ScreenOn из хранилища
        cbScreenOn = settings.getString( "state_ScreenOn", null);    // достаем переменную из хранилища
        if (cbScreenOn == null) {
            cbScreenOn = "0";
            editor.putString( "state_ScreenOn", cbScreenOn);   // положить переменную в хранилище
            editor.commit();
        }
        if ((cbScreenOn.equals("1"))) {
            checkBox_ScreenOn.setChecked(true);
        } else {
            checkBox_ScreenOn.setChecked(false);
        }



        //  Достаем checkBox_LaunchMaps из хранилища
        cbLaunchMaps = settings.getString( "state_LaunchMaps", null);    // достаем переменную из хранилища
        if (cbLaunchMaps == null) {
            cbLaunchMaps = "0";
            editor.putString( "state_LaunchMaps", cbLaunchMaps);   // положить переменную в хранилище
            editor.commit();
        }
        if ((cbLaunchMaps.equals("1"))) {
            checkBox_LaunchMaps.setChecked(true);
        } else {
            checkBox_LaunchMaps.setChecked(false);
        }



        //  Достаем secretWord из хранилища
        secretWord = settings.getString( "secretWord", null);    // достаем переменную из хранилища
        if (secretWord == null) {
            editor.putString( "secretWord", "put secret word here");   // положить переменную в хранилище
            editor.commit();
            secretWord = "put secret word here";
        }
        tvSecretWord.setText(secretWord);



        //  Достаем слатьОтвет из хранилища
        sendResponse = settings.getString("sendResponse", null);
        if (sendResponse == null) {
            editor.putString("sendResponse", "0");// положить переменную в хранилище
            editor.commit();
            sendResponse = settings.getString("sendResponse", null);
        }

        if (sendResponse.equals("1")) {
            isServiceRunning = true;
            btnStartStop.setText("Stop");
        } else {
            isServiceRunning = false;
            btnStartStop.setText("Start");
        }
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
            if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                // permission is not granted, ask for permission:
                requestPermissions(new String[] { Manifest.permission.SEND_SMS}, MY_KEY_FOR_RETURNED_VALUE);
                return;
            }
        }

        // проверить, включен ли прием sms
/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                // permission is not granted, ask for permission:
                requestPermissions(new String[] { Manifest.permission.RECEIVE_SMS}, MY_KEY_FOR_RETURNED_VALUE);
                return;
            }
        }*/

        // проверить, включено ли определение местоположения
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // permission is not granted, ask for permission:
                requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION}, MY_KEY_FOR_RETURNED_VALUE);
                return;
            }
        }

        // проверить, включено ли определение местоположение  в фоновом режиме
/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // permission is not granted, ask for permission:
                requestPermissions(new String[] { Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_KEY_FOR_RETURNED_VALUE);
                return;
            }
        }*/


        if (isServiceRunning) {
//            unregisterBroadcastReceiver();       // так можно остановить broadcast receiver, запущенный из activity
            editor.putString( "sendResponse", "0");
            editor.commit();
            btnStartStop.setText("Start");
            Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
            isServiceRunning = false;
        } else {
//            registerBroadcastReceiver();     // так можно запустить broadcast receiver из activity. Жить будет пока жива activity
            editor.putString( "sendResponse", "1");
            editor.commit();
            btnStartStop.setText("Stop");
            Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_SHORT).show();
            isServiceRunning = true;
        }
    }



    public void btnSecretWord_OnClick(View v) throws InterruptedException {
        editor.putString( "secretWord", tvSecretWord.getText().toString().trim());
        editor.commit();
        Toast.makeText(getApplicationContext(), "Secret word saved", Toast.LENGTH_SHORT).show();
    }


    // Сохранить состояние чек бокса   checkBox_WakeUp
    public void checkBox_WakeUp_OnClick(View v) throws InterruptedException {
        editor.putString( "state_WakeUp", (this.checkBox_WakeUp.isChecked()) ? "1" : "0" );
        editor.commit();
    }


    // Сохранить состояние чек бокса   checkBox_ScreenOn
    public void checkBox_ScreenOn_OnClick(View v) throws InterruptedException {
        editor.putString( "state_ScreenOn", (this.checkBox_ScreenOn.isChecked()) ? "1" : "0" );
        editor.commit();
    }


    // Сохранить состояние чек бокса   checkBox_LaunchMaps
    public void checkBox_LaunchMaps_OnClick(View v) throws InterruptedException {
        editor.putString( "state_LaunchMaps", (this.checkBox_LaunchMaps.isChecked()) ? "1" : "0" );
        editor.commit();
    }



    //  Не используется. Для ручного старта broadcast receiver
    public void registerBroadcastReceiver() {
        this.registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        isServiceRunning = true;
    }


    //  Не используется. Для ручного стопа broadcast receiver
    public void unregisterBroadcastReceiver() {
        this.unregisterReceiver(smsReceiver);
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
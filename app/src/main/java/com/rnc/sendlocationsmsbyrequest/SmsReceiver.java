package com.rnc.sendlocationsmsbyrequest;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
//import android.content.SharedPreferences;


public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = SmsReceiver.class.getSimpleName();
    public static final String pdu_type = "pdus";

    private SharedPreferences settings;             // хранилище переменных

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {

        // Get the SMS message.
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        String strMessage = "";
        String format = bundle.getString("format");

        // Retrieve the SMS message received.
        try {
            Object[] pdus = (Object[]) bundle.get(pdu_type);

            if (pdus != null) {
                // Check the Android version.
                boolean isVersionM = (Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.M);

                // Fill the msgs array.
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    // Check Android version and use appropriate createFromPdu.
                    if (isVersionM) {
                        // If Android version M or newer:
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                    } else {
                        // If Android version L or older:
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }

                    // Build the message to show.
                    strMessage += "SMS from " + msgs[i].getOriginatingAddress();
                    strMessage += " :" + msgs[i].getMessageBody() + "\n";

                    // display the SMS message.
//                    Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();

                    // определяем координаты и ответная смс
                    try {
                        settings = context.getSharedPreferences("SLSbR_STORAGE", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);  // инициируем хранилище переменных      MODE_MULTI_PROCESS - чтоб было доступно из broadcast receiver
                        String keyString = settings.getString( "secretWord", null).trim().toLowerCase();    // достаем переменную из хранилища
//                        Toast.makeText(context, keyString, Toast.LENGTH_LONG).show();
                        String sendResponse = settings.getString("sendResponse", null);
//                        Toast.makeText(context, sendResponse, Toast.LENGTH_LONG).show();
                        if (msgs[i].getMessageBody().toLowerCase().contains(keyString)) {
                            if (sendResponse.trim().equals("1")) {
                                String stLaunchMaps = settings.getString("state_LaunchMaps", null);
                                Toast.makeText(context, "go location", Toast.LENGTH_LONG).show();
                                GetCurrentLocation gcl = new GetCurrentLocation(msgs[i].getOriginatingAddress(), context, keyString, stLaunchMaps);
                                gcl = null;
                            }
                        }
                    } catch (Exception e) {
                        //  хранилища или переменных может и не быть. Оно все должно создаться из activity
                    }

                }
        }

        } catch(Exception e) {
                            Log.i("Exception caught",e.getMessage());
        }

    }
}
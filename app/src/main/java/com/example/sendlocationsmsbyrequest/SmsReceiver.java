package com.example.sendlocationsmsbyrequest;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = SmsReceiver.class.getSimpleName();
    public static final String pdu_type = "pdus";

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

                    // Log and display the SMS message.
//                    Log.d(TAG, "PRIVETTT_8: " + strMessage);
//                    Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();

                    // Стартуем MainActivity, при старте определяем координаты и ответная смс
                    String keyString = "WhereAreYouuu";
                    if (msgs[i].getMessageBody().toLowerCase().contains(keyString.toLowerCase())) {
                        GetLocation gl = new GetLocation(msgs[i].getOriginatingAddress(), context);


//                        Intent mIntent = new Intent(context, MainActivity.class);
//                        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TASK);   // FLAG_ACTIVITY_CLEAR_TASK - для того, чтоб MainActivity рестартовалась при каждой смс
//                        mIntent.putExtra("STR_TEL_NUMBER", msgs[i].getOriginatingAddress());
//                        mIntent.putExtra("STR_MESSAGE", msgs[i].getMessageBody());
//                        context.startActivity(mIntent);
                    }
                }
        }

        } catch(Exception e) {
                            Log.d("Exception caught",e.getMessage());
        }


//        throw new UnsupportedOperationException("Not yet implemented");   // privet
    }
}
package com.rnc.sendlocationsmsbyrequest;


import static android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP;
import static android.os.PowerManager.FULL_WAKE_LOCK;
import static android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;
import android.Manifest;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.os.PowerManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

//import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
//import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

//import java.text.DateFormat;
//import java.util.Date;

public class GetCurrentLocation {

    private static final String TAG = MainActivity.class.getSimpleName();

    //Constant used in the location settings dialog.
    private static final int REQUEST_CHECK_SETTINGS = 0x1;


    private static final String SMS_SENT_ACTION = "SMS_SENT_ACTION";
    private static final String SMS_DELIVERED_ACTION = "SMS_DELIVERED_ACTION";


    //Provides access to the Location Settings API.
    private SettingsClient mSettingsClient;

    //Stores the types of location services the client is interested in using. Used for checking
    //settings to determine if the device has optimal location settings.
    private LocationSettingsRequest mLocationSettingsRequest;

    //Provides access to the Fused Location Provider API.
    private FusedLocationProviderClient mFusedLocationClient;

    //Stores parameters for requests to the FusedLocationProviderApi.
    private LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */

    public CancellationToken stopGettingLocation;

    public String latitude = "";
    public String longitude = "";
    public Integer attemptionCount;
    public Integer attemptionCountLeftForStartingMaps;


    public String telNumber;
    public Context context;
    public String keyString;
    public String stLaunchMaps;
    public String stWakeUp;

    //  ??????????????????????
    public GetCurrentLocation(String telNumber1, Context context1, String keyString1, String stLaunchMaps1, String stWakeUp1) {

        telNumber = telNumber1;
        context = context1;
        keyString = keyString1;
        stLaunchMaps = stLaunchMaps1;
        stWakeUp = stWakeUp1;
        attemptionCount = 20;
        attemptionCountLeftForStartingMaps = 15;
        mCurrentLocation = null;


        // START
        if (stWakeUp.equals("1")) {
            wakeUp();
        }

        if (stLaunchMaps.equals("1")) {
            Toast.makeText(context, "start maps", Toast.LENGTH_LONG).show();
            startGoogleMaps();
        }

        startLocationUpdates();
    }


//  ?????????? ???????????????? ?????? ?????????? ???????????????????? ??  mSettingsClient,  buildLocationSettingsRequest
    private void startLocationUpdates() {

//        Toast.makeText(context, attemptionCount.toString(), Toast.LENGTH_LONG).show();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mSettingsClient = LocationServices.getSettingsClient(context);
        buildLocationSettingsRequest();   //  ?????????? ???????????????? ?????? ?????????? ???????????????????? ??  mSettingsClient,  buildLocationSettingsRequest

        // Begin by checking if the device has the necessary location settings.
        mSettingsClient = LocationServices.getSettingsClient(context);

        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {      //    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                            Toast.makeText(context, "Bad permissions", Toast.LENGTH_LONG).show();
                            SmsManager sms = SmsManager.getDefault();
                            sms.sendTextMessage(telNumber, null, "Bad permissions", PendingIntent.getBroadcast(
                                    context, 0, new Intent(SMS_SENT_ACTION), 0), PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED_ACTION), 0));

                            return;
                        }

                        mFusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, stopGettingLocation)
                                .addOnSuccessListener(new OnSuccessListener<Location> () {
                                    @Override
                                    public void onSuccess(Location location) {
                                        mCurrentLocation = location;
                                        updateLocationUI();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        attemptionCount--;
                                        if (attemptionCount >= 0) {

                                            //   start google map
                                            if ((attemptionCount == attemptionCountLeftForStartingMaps) && (!stLaunchMaps.equals("1"))) {
                                                startGoogleMaps();
                                            }

                                            startLocationUpdates();
                                        } else {
                                            SmsManager sms = SmsManager.getDefault();
                                            sms.sendTextMessage(telNumber, null, "Couldn't get location. Try later", PendingIntent.getBroadcast(
                                                    context, 0, new Intent(SMS_SENT_ACTION), 0), PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED_ACTION), 0));
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage(telNumber, null, "Check app permissions, or Turn on location, or No location sources are available", PendingIntent.getBroadcast(
                                        context, 0, new Intent(SMS_SENT_ACTION), 0), PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED_ACTION), 0));
//                        }
                    }
                }
                );
    }



    private void updateLocationUI() {

        if (mCurrentLocation != null) {

            latitude = String.format("%f", mCurrentLocation.getLatitude());
            longitude = String.format("%f", mCurrentLocation.getLongitude());
            latitude = latitude.replace(",",".");
            longitude = longitude.replace(",",".");
//            Toast.makeText(context, "Location ok " + NumberOfRequests.toString(), Toast.LENGTH_LONG).show();

            //  ???????????????? ??????
            if (!keyString.equals("zzzzzzzzzz")) {
                String msgLocation = "http://maps.google.com/?q=" + latitude + "," + longitude;
                if (telNumber != null) {
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(telNumber, null, msgLocation, PendingIntent.getBroadcast(
                            context, 0, new Intent(SMS_SENT_ACTION), 0), PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED_ACTION), 0));

                    Toast.makeText(context, "Location sent", Toast.LENGTH_LONG).show();

                    mCurrentLocation = null;
                }
            } else {
                Toast.makeText(context, "Latitude = " + latitude + "     " + "Longitude = " + longitude + "    " + "Tel = " + telNumber, Toast.LENGTH_LONG).show();
            }


        } else {
//            Log.i(TAG, "No location");   // ???????? ?????????????? ????????????????, ?? ?????????? ????????????????????
            attemptionCount--;
            if (attemptionCount >= 0) {

                //   start google map
                if ((attemptionCount == attemptionCountLeftForStartingMaps) && (!stLaunchMaps.equals("1"))) {
                    startGoogleMaps();
                }

                startLocationUpdates();
            } else {
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(telNumber, null, "Device did not get location. Try later", PendingIntent.getBroadcast(
                        context, 0, new Intent(SMS_SENT_ACTION), 0), PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED_ACTION), 0));

                Toast.makeText(context, "Device did not get location", Toast.LENGTH_LONG).show();
            }
        }
    }




    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }



    private void startGoogleMaps() {
        try {
            String uri = "https://www.google.com/maps/@?api=1&map_action=map";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Couldn't start google maps", Toast.LENGTH_LONG).show();
//            Log.i("Exception caught",e.getMessage());
        }

    }


    //to wake the screen
    protected void wakeUp() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock((SCREEN_BRIGHT_WAKE_LOCK | FULL_WAKE_LOCK | ACQUIRE_CAUSES_WAKEUP), "GetCurrentLocation:TAG");
        wakeLock.acquire();
        //to release the screen lock
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
        //add permission in the manifest file for the disablekeyguard
        keyguardLock.disableKeyguard();
    }

}

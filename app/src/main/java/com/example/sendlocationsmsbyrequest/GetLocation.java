package com.example.sendlocationsmsbyrequest;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.util.Date;

public class GetLocation{

    private static final String TAG = MainActivity.class.getSimpleName();

    //Constant used in the location settings dialog.
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


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


    //Callback for Location events.
    private LocationCallback mLocationCallback;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;


    public String latitude = "";
    public String longitude = "";
    public String mLastUpdateTime = "";


    public String telNumber;
    public Context context;
    public GetLocation(String telNumber1, Context context1) {

        telNumber = telNumber1;
        context = context1;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mSettingsClient = LocationServices.getSettingsClient(context);

//        mLastUpdateTime = "";
        mRequestingLocationUpdates = false;

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        // START
        startUpdatesButtonHandler();
    }




    public void startUpdatesButtonHandler() {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        }
    }


    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient = LocationServices.getSettingsClient(context);

        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateUI();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        SmsManager sms = SmsManager.getDefault();

                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                                sms.sendTextMessage(telNumber, null, "Check app permissions, or Turn on location, or No location sources are available", PendingIntent.getBroadcast(
                                        context, 0, new Intent(SMS_SENT_ACTION), 0), PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED_ACTION), 0));

                                mRequestingLocationUpdates = false;
                                break;
                        }
                    }
                }
                );
    }


    private void updateUI() {
        updateLocationUI();
    }


    private void updateLocationUI() {

        if (mCurrentLocation != null) {

            latitude = String.format("%f", mCurrentLocation.getLatitude());
            longitude = String.format("%f", mCurrentLocation.getLongitude());
            latitude = latitude.replace(",",".");
            longitude = longitude.replace(",",".");

            // Только один раз получаем данные. Без этого - постоянное обновление
            stopLocationUpdates();

            Toast.makeText(context, "Latitude = " + latitude + "     " + "Longitude = " + longitude + "    " + "Tel = " + telNumber, Toast.LENGTH_LONG).show();

            //  Отсылаем смс
            String msgLocation = "http://maps.google.com/?q=" + latitude + "," + longitude;
            if (telNumber != null) {
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(telNumber, null, msgLocation, PendingIntent.getBroadcast(
                        context, 0, new Intent(SMS_SENT_ACTION), 0), PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED_ACTION), 0));
            }

        } else {
//            Log.i(TAG, "No location");   // Сюда сначала попадает, а потом определяет
//            Toast.makeText(mainActivity, "No location", Toast.LENGTH_LONG).show();
        }
    }




    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocationUI();
            }
        };
    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }



    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }



    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            return;
        }
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mRequestingLocationUpdates = false;
    }


}

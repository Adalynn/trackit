package com.example.adalynn.pushit2.util;

/**
 * Created by adalynn on 16/11/17.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.adalynn.pushit2.activity.SignupActivity;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

/**
 * BroadcastReceiver to wait for SMS messages. This can be registered either
 * in the AndroidManifest or at runtime.  Should filter Intents on
 * SmsRetriever.SMS_RETRIEVED_ACTION.
 */
public class MySMSBroadcastReceiver extends BroadcastReceiver {

    private String TAG = MySMSBroadcastReceiver.class.getSimpleName();
    //Log.e(TAG, "BroadcastReceiver Class callled.");
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "BroadcastReceiver onReceive callled.");
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            //9810393766
            Log.e(TAG, "BroadcastReceiver received message.");
            Log.e(TAG, "BroadcastReceiver received message code" + status.getStatusCode());
            Log.e(TAG, "BroadcastReceiver received message code" + CommonStatusCodes.SUCCESS + "#" + CommonStatusCodes.TIMEOUT);

            switch(status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    // Get SMS message contents
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    // Extract one-time code from the message and complete verification
                    // by sending the code back to your server.
                    Log.e(TAG, "message received BroadcastReceiver in " + message);
                    break;
                case CommonStatusCodes.TIMEOUT:
                    // Waiting for SMS timed out (5 minutes)
                    // Handle the error ...
                    Log.e(TAG, "message time out occours");
                    break;
            }
        }
    }
}

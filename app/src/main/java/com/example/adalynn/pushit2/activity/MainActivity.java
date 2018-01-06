package com.example.adalynn.pushit2.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adalynn.pushit2.app.CommonUtil;
import com.example.adalynn.pushit2.util.AppSignatureHelper;
import com.example.adalynn.pushit2.util.HttpHandler;
import com.example.adalynn.pushit2.util.User;
import com.google.firebase.messaging.FirebaseMessaging;

import com.example.adalynn.pushit2.R;
import com.example.adalynn.pushit2.app.Config;
import com.example.adalynn.pushit2.util.NotificationUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;



public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {


    private static final String TAG = MainActivity.class.getSimpleName() + " PUSHIT : ";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
//    private TextView txtRegId, txtMessage;

    boolean isFirstTime = true;
    public String httpAction= null;
    public String fbID = null;
    public int dbID;
    public String ScreenUserData = null;
    String is_verified = null;
    CommonUtil common_util;
    ProgressDialog dialog;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG, "Programme Started from MainActivity");

//        appSignatures gives a 11 digit code which is required to be generated to send sms
//        AppSignatureHelper signatureHelper = new AppSignatureHelper(MainActivity.this);
//        ArrayList<String> appSignatures = signatureHelper.getAppSignatures();
//        if (!appSignatures.isEmpty()) {
//            Log.e(TAG, appSignatures.get(0));
//        }

        common_util = new CommonUtil();
        dbID = common_util.getDbIdInPref(getApplicationContext());

        if (dbID == 0) {
            Log.e(TAG, "dbID is 0 going to signin new user");
            /*
            * User is not registered yet going to save the user.
            * First check if app has the permission to read the phone state or not
            * if not request for the permission here.
            * */
            Log.e(TAG, "Build.VERSION.SDK_INT is "+Build.VERSION.SDK_INT);
            if (Build.VERSION.SDK_INT >= 23) {
                Log.e(TAG, "Checking for runtime permissions for the Build.VERSION.SDK_INT "+Build.VERSION.SDK_INT);
                if ( checkForPermissions( new String[]{android.Manifest.permission.READ_PHONE_STATE,
                                android.Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.WRITE_CONTACTS},
                        Config.ASK_MULTIPLE_PERMISSION_REQUEST_CODE, R.id.main_layout) ) {
                    // DO YOUR STUFF
                    Log.e(TAG, "All permissions granted");
                    goToSignupActivity();
                } else {
                    Log.e(TAG, "Waiting for permissions granting");
                    Toast.makeText(
                            getApplicationContext(), "All required permissions not granted.",
                            Toast.LENGTH_LONG
                    ).show();
                }

            } else {
                Log.e(TAG, "Not Checking for runtime permissions as the Build.VERSION.SDK_INT "+Build.VERSION.SDK_INT + " is older");
                goToSignupActivity();
            }
        } else {
            Log.e(TAG, "dbID is " + dbID + " user is already registered going to user home screen");
            /*
            * Get db id verification status if verified than go to home screen else
            * */
            showLoading(Config.WAIT_STR_MSG);

            /*
            * From here get the user data from db now using dbid
            * */
            getUserDataByDbId();
        }

/*

        fbID = getFbIdInPref();

        if (fbID == null) {

            showLoading("Waiting for FBID");

            mRegistrationBroadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

                    // checking for type intent filter
                    if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                        // gcm successfully registered
                        // now subscribe to `global` topic to receive app wide notifications
                        FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                        Log.e(TAG, "New user registered at firebase");
                        //displayFirebaseRegId("Firebase displayFirebaseRegId called from mRegistrationBroadcastReceiver");

                        showLoading("Setting up new user");
                        fbID = getFbIdInPref();
                        saveUserDataByFbId();
                    } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                        // new push notification is received

                        String message = intent.getStringExtra("message");
                        //Toast.makeText(getApplicationContext(), "PUSH_NOTIFICATION", Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();
                        //txtMessage.setText(message);
                    }
                }
            };
        } else {
            showLoading("Waiting for FBID");
            if (!TextUtils.isEmpty(fbID)) {
                isFirstTime = false;
                Log.e(TAG, "User is coming nth time not a new user fbid already set" + "SDK_INT " + Build.VERSION.SDK_INT);

                String dbid = getDbIdInPref();
                Log.e(TAG, "dbID " + dbid);
                if(Build.VERSION.SDK_INT >= 23) {
                    CommonUtil common_util = new CommonUtil();
                    if(!common_util.hasPermission(this, Config.READ_PHONE_STATE_PERMISSION)){
                        hideLoading();
                        common_util.requestPermission(MainActivity.this, Config.READ_PHONE_STATE_PERMISSION);
                    } else {

                        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
                        //tm.isD
                        try {
                            String line1 = tm.getLine1Number();
                        } catch (Exception e) {
                            Log.e(TAG, "CAME IN EXCEPTION tm " + e.getMessage());
                        }
                    }
                }
*/

//                String number = tm.getLine1Number();
//                Log.e(TAG, "MOBILE NUMBER " + number);

                /* get dbid from shared prefrences and then request the data from php
                if shared prefrences dbid is null then it means this fbid is not saved in db yet
                so save this fbid ib db and then confirm that sahred prefrences contains the dbid now
                */
//                String dbid = getDbIdInPref();

//                if (dbid !=null) {
//                    // get data from db now using dbid
//                    Log.e(TAG, "Got db id " + dbid + " in sf");
//                    dbID = dbid;
//                    /*
//                    * From here get the user data from db now using dbid
//                    * */
//                    getUserDataByDbId();
//                } else {
//                    // save data in db and dbid in shared prefrences
//                    Log.e(TAG, "Got db id null in sf");
//                    /*
//                        Check if data exists in db or not using fbid if so then getdata using fbid
//                        also store the dbid in prefrences
//                    */
//                    storeDbIdInPrefByFbId();
//                }
        /*
            } else {
                Log.e(TAG, "As per understading i should not come here");
            }
//            saveUserDataByFbId();
        }
        */

//        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//
//                // checking for type intent filter
//                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
//                    // gcm successfully registered
//                    // now subscribe to `global` topic to receive app wide notifications
//                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
//                    //Toast.makeText(getApplicationContext(), "REGISTRATION_COMPLETE", Toast.LENGTH_LONG).show();
//                    displayFirebaseRegId("Firebase displayFirebaseRegId called from mRegistrationBroadcastReceiver");
//                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
//                    // new push notification is received
//
//                    String message = intent.getStringExtra("message");
//                    //Toast.makeText(getApplicationContext(), "PUSH_NOTIFICATION", Toast.LENGTH_LONG).show();
//                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();
//                    //txtMessage.setText(message);
//                }
//            }
//        };
//
//        displayFirebaseRegId("Firebase displayFirebaseRegId called from outside");
//
//
//        if (!isFirstTime) {
//
//            Log.e(TAG, "Firebase reg fbid already set!");
//            /* get dbid from shared prefrences and then request the data from php
//            if shared prefrences dbid is null then it means this fbid is not saved in db yet
//            so save this fbid ib db and then confirm that sahred prefrences contains the dbid now
//            */
//            String dbid = getDbIdInPref();
//
//            if (dbid !=null) {
//                // get data from db now using dbid
//                Log.e(TAG, "Firebase reg id: Got db id " + dbid + " in sf");
//                dbID = dbid;
//                /*
//                * From here get the user data from db now using dbid
//                * */
//                getUserDataByDbId();
//            } else {
//                // save data in db and dbid in shared prefrences
//                Log.e(TAG, "Firebase reg id: Got db id null in sf");
//                /*
//                    Check if data exists in db or not using fbid if so then getdata using fbid
//                    also store the dbid in prefrences
//                */
//                storeDbIdInPrefByFbId();
//            }
//
//        } else {
//            /* Save fbid in db and its db id in shared prefrences */
//            Log.e(TAG, "Firebase reg id: This is first time" + fbID);
//
//            /*
//            * If FbId is null might be it is registering at firbase end so please wait here till the fbId is
//            * null once the fbId is set then save it in db
//            * */
//
//
//            if(fbID == null) {
//                Log.e(TAG, "Waiting for FbID to get set" + fbID);
//            }
//
//            Log.e(TAG, "fbID get set in shared prefrences now fbid : " + fbID);
//            saveUserDataByFbId();
//        }

    }


    public boolean checkForPermissions(final String[] permissions, final int permRequestCode, int msgResourceId) {
        final List<String> permissionsNeeded = new ArrayList<>();

        final View view = (View) ((View) MainActivity.this.findViewById(R.id.main_layout));
        for (int i = 0; i < permissions.length; i++) {
            final String perm = permissions[i];
            Log.e(TAG, "Required Permissions : " + perm);
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                //Log.e(TAG, "Requesting Permissions : " + perm);
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissions[i])) {
                    Log.e(TAG, "Requesting Permissions in shouldShowRequestPermissionRationale : " + perm);

                    Snackbar.make(view, "We will use your phone number for signup", Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.e(TAG, "Snack bar ok button clicked!");
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    permissions,
                                    Config.ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
                        }
                    }).show();
                    //final AlertDialog dialog = AlertDialog.newInstance( getResources().getString(R.string.permission_title), getResources().getString(msgResourceId) );

                    /*final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.show();
                    // Set up the buttons
                    builder.setPositiveButton("Verify Contact", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing here because we override this button later to change the close behaviour.
                            //However, we still need this because on older versions of Android unless we
                            //pass a handler the button doesn't get instantiated
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });*/

                } else {
                    Log.e(TAG, "Requesting Permissions : " + perm);
                    // add the request.
                    permissionsNeeded.add(perm);
                }
            }
        }

        if (permissionsNeeded.size() > 0) {
            // go ahead and request permissions
            ActivityCompat.requestPermissions(MainActivity.this, permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                    Config.ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
            //ActivityCompat.requestPermissions(permissionsNeeded.toArray(new String[permissionsNeeded.size()]), permRequestCode);
            return false;
        } else {
            // no permission need to be asked so all good...we have them all.
            return true;
        }
    }

    public void getUserDataByDbId() {
        Log.e(TAG, "Getting user data for dbid : " + dbID);

        try {
            showLoading("getuserdatabydbid");
            new HttpAsyncTask().execute(Config.HTTP_API_URL, "getuserdatabydbid").get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    public void showLoading(String msg) {
        Log.e(TAG, "showLoading Called with msg : " + msg);
        dialog = new ProgressDialog(MainActivity.this);
        //dialog.setMessage("Please wait" + msg);
        dialog.setMessage(msg);
        dialog.show();

    }

    public void hideLoading() {
        if (dialog.isShowing()) {
            Log.e(TAG, "hideLoading Called");
            dialog.dismiss();
        }
    }

    public void goToSignupActivity() {
        Log.e(TAG, "goToSignupActivity Called");
        Intent intent = new Intent(this, SignupActivity.class);
        intent.putExtra("action","sign_up_new_user");
        startActivity(intent);
    }
    
    public void saveUserDataByFbId() {
        try {
            Log.e(TAG, "saveUserDataByFbId Called");
            new HttpAsyncTask().execute(Config.HTTP_API_URL, "saveuserdatabyfbid").get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void storeDbIdInPrefByFbId() {

//        https://github.com/Adalynn/ecomm.git
//        http://localhost/ecomm/landit/landit.php?action=getuserdbid&fbid=test
//        http://localhost/ecomm/landit/landit.php?action=getuserdatabyfbid&fbid=test

        try {
            showLoading("getuserdatabyfbid");
            new HttpAsyncTask().execute(Config.HTTP_API_URL, "getuserdatabyfbid").get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    /*
    * This function will store th db id returned in stored prefrences
    * */
    public void ReturnThreadResult(String result)
    {
        if(httpAction == "getuserdatabydbid") {
            try {
                JSONObject jsonObj = new JSONObject(result);
                ScreenUserData = result;
                Log.e(TAG, "getuserdatabydbid Firebase data by dbid: " + result);
//                String dbId = jsonObj.getJSONObject("data").getString("id");
                hideLoading();
                /*
                    At this place we will surely have the dbid so get the user data by dbid
                    if mobile number is empty in result show the box to add the number and emailid
                    mobile number is a compulsory field
                    @TODO : Goto next activity with data in string format
                */

                is_verified = jsonObj.getJSONObject("data").getString("is_verified");
                if(is_verified.equals("0")) {
                    goToSignupActivity();
                } else {
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.putExtra("ScreenUserData",ScreenUserData);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if(httpAction == "getuserdatabyfbid") {
            try {
                JSONObject jsonObj = new JSONObject(result);
                ScreenUserData = result;
                String dbId = jsonObj.getJSONObject("data").getString("id");
                common_util.setDbIdInPref(getApplicationContext(), dbId);
                //setDbIdInPref(dbId);
                Log.e(TAG, "Firebase reg dbid now set in sf: " + dbId);
                hideLoading();
                /*
                    @TODO : Goto next activity with data in string format
                */
                Log.e(TAG, "ScreenUserData " + httpAction + " # " + ScreenUserData);
                Intent intent = new Intent(this, HomeActivity.class);
                //String message = "Sending some data to map class from contact class";
                intent.putExtra("ScreenUserData",ScreenUserData);
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if(httpAction == "saveuserdatabyfbid") {
            try {
                JSONObject jsonObj = new JSONObject(result);
                ScreenUserData = result;
                boolean userInserted = jsonObj.getBoolean("user_inserted");
                hideLoading();
                if(userInserted) {
                    /*
                        @TODO : Goto next activity with data in string format
                    */
//                    String dbId = jsonObj.getJSONObject("data").getString("id");
//                    setDbIdInPref(dbId);
                    //setDbIdInPref
                    Log.e(TAG, "ScreenUserData " + httpAction + " # " + ScreenUserData);
                    Intent intent = new Intent(this, HomeActivity.class);
                    //String message = "Sending some data to map class from contact class";
                    intent.putExtra("ScreenUserData",ScreenUserData);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



    }

//    public void setDbIdInPref(String dbId) {
//        SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences(Config.DB_SHARED_PREF, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedpreferences.edit();
//        editor.putString("dbid", dbId);
//        editor.commit();
//    }

    public String getDbIdInPrefOld() {

        SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences(Config.DB_SHARED_PREF, 0);
        String dbid = sharedpreferences.getString("dbid", null);
        if (!TextUtils.isEmpty(dbid)) {
            return dbid;
        } else {
            return null;
        }
    }

    public String getFbIdInPref() {

        SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String fbid = sharedpreferences.getString("regId", null);
        if (!TextUtils.isEmpty(fbid)) {
            return fbid;
        } else {
            return null;
        }
    }

    // Fetches reg id from shared preferences and displays on the screen
    private void displayFirebaseRegId(String called_from) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);
        fbID = regId;
        Log.e(TAG, called_from);
        Log.e(TAG, "Firebase reg id displayFirebaseRegId : " + regId);
        if (!TextUtils.isEmpty(regId)) {
            isFirstTime = false;
            //txtRegId.setText("Firebase Reg Id: " + regId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e(TAG, "onRequestPermissionsResult Called." + requestCode);
        if (requestCode == Config.MY_PERMISSIONS_REQUEST_SEND_SMS) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for Send Sms.
            Log.e(TAG, "Received response for Send Sms permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Send Sms permission has been granted, sms can be send now
                Log.e(TAG, "Send Sms permission has now been granted.");
                //Snackbar.make(mLayout, R.string.permision_send_sms_granted,Snackbar.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Send Sms permission was NOT granted.");
                //Snackbar.make(mLayout, R.string.permissions_not_granted,Snackbar.LENGTH_SHORT).show();
            }
            // END_INCLUDE(permission_result)

        }

        if (requestCode == Config.READ_PHONE_STATE_PERMISSION) {

            Log.e(TAG, "Read Phone State Permission response " + grantResults[0]);

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Send Sms permission has been granted, sms can be send now
                Log.e(TAG, "READ_PHONE_STATE_PERMISSION granter Going to register user!");
                Intent intent = new Intent(this, SignupActivity.class);
                intent.putExtra("action","sign_up_new_user");
                startActivity(intent);
                //Snackbar.make(mLayout, R.string.permision_send_sms_granted,Snackbar.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "READ_PHONE_STATE_PERMISSION was NOT granted.");
                Toast.makeText(
                        getApplicationContext(), "Unable to read mobile number",
                        Toast.LENGTH_LONG
                ).show();

                //Snackbar.make(mLayout, R.string.permissions_not_granted,Snackbar.LENGTH_SHORT).show();

            }
            // END_INCLUDE(permission_result)
        }

        if (requestCode == Config.ASK_MULTIPLE_PERMISSION_REQUEST_CODE) {
            /*
            [
                android.permission.READ_PHONE_STATE,
                android.permission.READ_SMS,
                android.permission.SEND_SMS,
                android.permission.READ_CONTACTS,
                android.permission.WRITE_CONTACTS
            ]
            */

            Log.e(TAG, "Array of permissions requested : " + Arrays.toString(permissions));
            Log.e(TAG, "ASK_MULTIPLE_PERMISSION_REQUEST_CODE " + grantResults.length + "#" + grantResults[0] +"#"+ grantResults[1]);

            boolean all_permissions_granted = true;
            if(permissions.length == grantResults.length) {
                Log.e(TAG, "permissions count match permissions.length#grantResults.length " + permissions.length +"#"+ grantResults.length);
                for (int i = 0; i < grantResults.length; i++) {
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED ) {
                        Log.e(TAG, "permission not granted for " + i +"#"+ + grantResults[i] + "#" + permissions[i]);
                        all_permissions_granted = false;
                    } else {
                        Log.e(TAG, "permission granted for " + i +"#"+ + grantResults[i] + "#" + permissions[i]);
                    }
                }
            } else {
                Log.e(TAG, "permissions count mismatch permissions.length#grantResults.length " + permissions.length +"#"+ grantResults.length);
            }

            if(all_permissions_granted) {
                Log.e(TAG, "Starting SignupActivity");
                goToSignupActivity();
            } else {
                Log.e(TAG, "Not starting SignupActivity due to permissions issue");
            }


        }
    }

    class HttpAsyncTask extends AsyncTask<String, String, String> {

//        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);
//

        /** application context. */
        @Override
        protected void onPreExecute() {
//            this.dialog.setMessage("Please wait");
//            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d("AniTry", "doInBackground Called!");
            //https://www.tutorialspoint.com/android/android_json_parser.htm
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String url = params[0];

            httpAction = params[1];

            if(params[1] == "getuserdatabyfbid") {
                url += "?action="+params[1]+"&fbid="+fbID;
            }

            if(params[1] == "getuserdatabydbid") {
                url += "?action="+params[1]+"&dbid="+dbID;
            }

            if(params[1] == "saveuserdatabyfbid") {
                url += "?action="+params[1]+"&fbid="+fbID;
            }

            Log.e(TAG, "Firebase req url: " + url);

            String jsonStr = sh.makeServiceCall(url);
            if (jsonStr != null) {
                Log.e(TAG, "Firebase Response from url for action " + params[1] + ": " + jsonStr);
                return jsonStr;
            }
            // Log.e(TAG, "Response from url: " + jsonStr);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Toast.makeText(getBaseContext(), "Data  sent!", Toast.LENGTH_LONG).show();
            ReturnThreadResult(result);
        }
    }


}

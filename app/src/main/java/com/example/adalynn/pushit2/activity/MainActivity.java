package com.example.adalynn.pushit2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adalynn.pushit2.util.HttpHandler;
import com.example.adalynn.pushit2.util.User;
import com.google.firebase.messaging.FirebaseMessaging;

import com.example.adalynn.pushit2.R;
import com.example.adalynn.pushit2.app.Config;
import com.example.adalynn.pushit2.util.NotificationUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;



public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private TextView txtRegId, txtMessage;

    boolean isFirstTime = true;
    public String httpAction= null;
    public String fbID = null;
    public String dbID = null;

    ProgressDialog dialog;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG, "Firebase reg id: Programme Started");
        txtRegId = (TextView) findViewById(R.id.txt_reg_id);
        txtMessage = (TextView) findViewById(R.id.txt_push_message);


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    //Toast.makeText(getApplicationContext(), "REGISTRATION_COMPLETE", Toast.LENGTH_LONG).show();
                    displayFirebaseRegId("Firebase displayFirebaseRegId called from mRegistrationBroadcastReceiver");
                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");
                    //Toast.makeText(getApplicationContext(), "PUSH_NOTIFICATION", Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                    txtMessage.setText(message);
                }
            }
        };

        displayFirebaseRegId("Firebase displayFirebaseRegId called from outside");


        if (!isFirstTime) {
            Log.e(TAG, "Firebase reg fbid already set!");
            /* get dbid from shared prefrences and then request the data from php
            if shared prefrences dbid is null then it means this fbid is not saved in db yet
            so save this fbid ib db and then confirm that sahred prefrences contains the dbid now
            */
            String dbid = getDbIdInPref();

            if (dbid !=null) {
                // get data from db now using dbid
                Log.e(TAG, "Firebase reg id: Got db id " + dbid + " in sf");
                dbID = dbid;
                /*
                * From here get the user data from db now using dbid
                * */
                getUserDataByDbId();
            } else {
                // save data in db and dbid in shared prefrences
                Log.e(TAG, "Firebase reg id: Got db id null in sf");
                /*
                    Check if data exists in db or not using fbid if so then getdata using fbid
                    also store the dbid in prefrences
                */
                storeDbIdInPrefByFbId();
                //getUserDataByDbId();
            }

        } else {
            /* Save fbid in db and its db id in shared prefrences */
            Log.e(TAG, "Firebase reg id: This is first time");
            saveUserDataByFbId();
        }
    }

    public void getUserDataByDbId() {
        String dbid = getDbIdInPref();
        dbID = dbid;
        Log.e(TAG, "Firebase getting user data for dbid : " + dbID);

        try {
            showLoading("getuserdatabydbid");
            new HttpAsyncTask().execute("http://10.0.2.2/ecomm/landit/landit.php","getuserdatabydbid").get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    public void showLoading(String msg) {
        Log.e(TAG, "Firebase show loading " + msg);
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Please wait" + msg);
        dialog.show();

    }

    public void hideLoading() {
        if (dialog.isShowing()) {
            Log.e(TAG, "Firebase hiding loading image");
            dialog.dismiss();
        }
    }

    public void saveUserDataByFbId() {
        try {
            showLoading("saveuserdatabyfbid");
            new HttpAsyncTask().execute("http://10.0.2.2/ecomm/landit/landit.php","saveuserdatabyfbid").get(1000, TimeUnit.MILLISECONDS);
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
            new HttpAsyncTask().execute("http://10.0.2.2/ecomm/landit/landit.php","getuserdatabyfbid").get(1000, TimeUnit.MILLISECONDS);
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
                Log.e(TAG, "Firebase data by dbid: " + result);
//                String dbId = jsonObj.getJSONObject("data").getString("id");
                hideLoading();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if(httpAction == "getuserdatabyfbid") {
            try {
                JSONObject jsonObj = new JSONObject(result);
                String dbId = jsonObj.getJSONObject("data").getString("id");
                setDbIdInPref(dbId);
                Log.e(TAG, "Firebase reg dbid now set in sf: " + dbId);
                hideLoading();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if(httpAction == "saveuserdatabyfbid") {
            try {
                JSONObject jsonObj = new JSONObject(result);
//                String dbId = jsonObj.getJSONObject("data").getString("id");
//                setDbIdInPref(dbId);
//                Log.e(TAG, "Firebase reg dbid now set in sf after saving in db: " + dbId);
                Log.e(TAG, "Firebase reg dbid now set in sf after saving in db: ");
                hideLoading();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



    }

    public void setDbIdInPref(String dbId) {
        SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences(Config.DB_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("dbid", dbId);
        editor.commit();
    }

    public String getDbIdInPref() {

        //SharedPreferences sharedpreferences = getSharedPreferences(Config.SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences(Config.DB_SHARED_PREF, 0);
        String dbid = sharedpreferences.getString("dbid", null);
        if (!TextUtils.isEmpty(dbid)) {
            return dbid;
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
        Log.e(TAG, "Firebase reg id displayFirebaseRegId: " + regId);
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

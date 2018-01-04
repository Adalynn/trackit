package com.example.adalynn.pushit2.app;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.adalynn.pushit2.R;
import com.example.adalynn.pushit2.activity.MainActivity;

public class CommonUtil extends AppCompatActivity {

    private static final String TAG = CommonUtil.class.getSimpleName() + " PUSHIT : ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_util);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });


    }

    public int getDbIdInPref(Context c) {
        Log.e(TAG, "getDbIdInPref Called");
        try{
            SharedPreferences sharedpreferences = c.getSharedPreferences(Config.DB_SHARED_PREF, 0);
            int dbID = sharedpreferences.getInt("dbid", MODE_PRIVATE);
            Log.e(TAG, "Found dbID " + dbID);
            return dbID;
        } catch (Exception e){
            Log.e(TAG, "getDbIdInPref Exception " + e.getMessage());
            return 0;
        }
    }

    public void setDbIdInPref(Context c, String dbId) {
        Log.e(TAG, "setDbIdInPref Called ");
        try{
            SharedPreferences sharedpreferences = c.getSharedPreferences(Config.DB_SHARED_PREF, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            //editor.putString("dbid", dbId);
            editor.putInt("dbid", Integer.parseInt(dbId));
            editor.commit();
        } catch (Exception e){
            Log.e(TAG, "getDbIdInPref Exception " + e.getMessage());
        }
    }

    public boolean hasPermission(Context context, int permissionType) {

        boolean retrun_var = true;
        if(permissionType == Config.READ_PHONE_STATE_PERMISSION) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // READ_PHONE_STATE has not been granted.
                Log.e(TAG, "hasPermission Called NOT GRANTED " + permissionType);
                retrun_var = false;

            }
        }
        return retrun_var;
    }

    /**
     * Requests the Permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    public void requestPermission(Activity activity, int permissionType) {
        Log.e(TAG, "requestPermission Called " + permissionType);
        /*
        * Declared it as final to get make it available to inner class also
        * */
        final Activity this_activity = activity;
        final View view = (View) ((View) activity.findViewById(R.id.main_layout));

        if(permissionType == Config.READ_PHONE_STATE_PERMISSION) {
            Log.e(TAG, "requestPermission Requested  " + permissionType);
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)) {
                // Provide an additional rationale to the user if the permission was not granted
                // and the user would benefit from additional context for the use of the permission.
                // For example if the user has previously denied the permission.
                Log.e(TAG, "Came to create the snack bar!");
                Snackbar.make(view, "We will use your phone number for signup", Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.e(TAG, "Snack bar ok button clicked!");
                                ActivityCompat.requestPermissions(this_activity,
                                        new String[]{android.Manifest.permission.READ_PHONE_STATE},
                                        Config.READ_PHONE_STATE_PERMISSION);
                            }
                        }).show();
            } else {
                // permission has not been granted yet. Request it directly.
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_PHONE_STATE},
                        Config.READ_PHONE_STATE_PERMISSION);
            }
        }

    }
}

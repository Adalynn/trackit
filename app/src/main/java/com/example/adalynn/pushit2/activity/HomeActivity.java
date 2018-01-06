package com.example.adalynn.pushit2.activity;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adalynn.pushit2.Manifest;
import com.example.adalynn.pushit2.R;
import com.example.adalynn.pushit2.app.CommonUtil;
import com.example.adalynn.pushit2.app.Config;
import com.example.adalynn.pushit2.util.HttpHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by adalynn on 28/10/17.
 */

public class HomeActivity extends AppCompatActivity {

    ProgressDialog dialog;
    public Button saveUserDetails;
    String ScreenUserData = null;
    public String httpAction= null;
    String dbId = null;
    String user_mobile = null;
    private String TAG = HomeActivity.class.getSimpleName();
    public Button showContact;
    public Button addContact;
    public String head_text;
    public String sub_text;
    public int total_contacts = 0;
    private String contact_mobile_number = "";
    private String contact_name = "";
    private boolean request_processed = false;
    CommonUtil common_util;
    AlertDialog c_dialog;

    /**
     * Id to identify a contacts permission request.
     */
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 12345;
    /**
     * Root of the layout of this Activity.
     */
    private View mLayout;
    private RelativeLayout home_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        common_util = new CommonUtil();

        home_layout = (RelativeLayout) findViewById(R.id.home_layout);


        Bundle extras = getIntent().getExtras();
        ScreenUserData = extras.getString("ScreenUserData");

        Log.e(TAG, "ScreenUserData Received User Data: " + ScreenUserData);
        try {
            JSONObject jsonObj = new JSONObject(ScreenUserData);
            dbId = jsonObj.getJSONObject("data").getString("id");
            Log.e(TAG, "ScreenUserData Received User Data Id: " + dbId);
            user_mobile = jsonObj.getJSONObject("data").getString("mobile");
            Log.e(TAG, "ScreenUserData Received User Data Mobile: " + user_mobile);

            if(user_mobile.isEmpty()) {
                Intent intent = new Intent(this, SignupActivity.class);
                intent.putExtra("ScreenUserData",ScreenUserData);
                startActivity(intent);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        head_text = "Dude Finder";
        TextView txtView1 = (TextView)findViewById(R.id.textView1);
        txtView1.setText(head_text);

        sub_text = "Add your dude in the contact list and start tracking them at the same moment, this is going to be an amazing start!";
        TextView txtView2= (TextView)findViewById(R.id.textView2);
        txtView2.setText(sub_text);


        /* Show Contact Button */
        this.showContact = (Button)this.findViewById(R.id.show_contacts_btn);
        this.showContact.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                viewContact(v);
            }
        });

        this.addContact = (Button)this.findViewById(R.id.add_contacts_button);
        this.addContact.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                getUserContactsCount();
                while(!request_processed){
                }
                Log.e(TAG, "Total contacts after while loop : " + total_contacts);
                if(total_contacts >= Config.MAX_CONTACTS_LIMIT) {
                    hideLoading();
                    common_util.addMaxContactAddedAlert(v, HomeActivity.this);
                } else {
                    addContact(v);
                }
                //addContact(v);
            }
        });


    }

    public void showLoading(String msg) {
        Log.e(TAG, "Firebase show loading " + msg);
        dialog = new ProgressDialog(HomeActivity.this);
        dialog.setMessage(msg);
        dialog.show();

    }

    public void hideLoading() {
        if (dialog.isShowing()) {
            Log.e(TAG, "Firebase hiding loading image");
            dialog.dismiss();
        }
    }

    public void getUserContactsCount() {
        try {
            showLoading(Config.WAIT_STR_MSG);
            new HomeHttpAsyncTask().execute(Config.HTTP_API_URL,"getcontactscount").get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
    /** Called to add the contact */
    public void addContact(View view) {

        Log.e(TAG, "addContact Callaed");

        final EditText mobile_number = new EditText(this);
        mobile_number.setHint(R.string.contact_mobilenumber);
        mobile_number.setInputType(InputType.TYPE_CLASS_PHONE);

        final EditText name = new EditText(this);
        name.setHint(R.string.contact_name);
        name.setInputType(InputType.TYPE_CLASS_TEXT);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Contact");

        builder.setMessage("You can track user after code verification!");

        LinearLayout ll=new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(mobile_number);
        ll.addView(name);
        builder.setView(ll);

        // Set up the buttons
        builder.setPositiveButton("Add Contact", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //builder.show();

        c_dialog = builder.create();
        c_dialog.show();
        c_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Boolean wantToCloseDialog = false;
                contact_mobile_number = mobile_number.getText().toString();
                contact_name = name.getText().toString();
                if(contact_mobile_number.length() == 10) {
                    wantToCloseDialog = true;
                    Log.e(TAG, "Posted details " + contact_mobile_number + "#" + contact_name + "#" + dbId);
                    insert();
                } else {
                    Log.e(TAG, "mobile_number " + mobile_number + contact_mobile_number);
                    Animation shake = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.shake);
                    mobile_number.startAnimation(shake);
                    Toast.makeText(
                            getApplicationContext(), "Please enter 10 digit mobile number",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }
                //c_dialog.dismiss();
                if(wantToCloseDialog) {
                    c_dialog.dismiss();
                }
            }
        });
    }

    public void insert()
    {
        try {
            showLoading("Please wait while we save the contact.");
            new HomeHttpAsyncTask().execute(Config.HTTP_API_URL,"addcontacts").get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /** Called to view the contact */
    public void viewContact(View view) {
        Intent intent = new Intent(this, ContactListActivity.class);
        String message = "Sending some data";
        intent.putExtra("data", message);
        intent.putExtra("dbId", dbId);
        Log.e(TAG, "Starting ContactListActiviy with db id : " + dbId);
        startActivity(intent);
    }

    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {
        Log.e(TAG, "sendSMS Called to send  : " + message + " on mobile number " + phoneNumber);
        try {

            PendingIntent sentPI;
            String SENT = "SMS_SENT";
            sentPI = PendingIntent.getBroadcast(this, 0,new Intent(SENT), 0);

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
            //sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
        } catch (Exception e){

            Log.e(TAG, "sendSMS Called Exception : " + e.getMessage());
            //e.getMessage() response => uid 10078 does not have android.permission.SEND_SMS
            Toast.makeText(getApplicationContext(), "Could Not Send OTP. Try On Another Device", Toast.LENGTH_LONG).show();

            return;
        }



        Log.e(TAG, "sendSMS Called to send came till end");
    }


    /**
     * Requests the Sms permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestSmsPermission() {

        Log.e(TAG, "Sms permission has NOT been granted. Requesting permission.");
        // BEGIN_INCLUDE(send_sms_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.SEND_SMS)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.e(TAG, "Displaying Sms permission rationale to provide additional context.");

            Snackbar.make(home_layout, R.string.permission_send_sms,Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(HomeActivity.this,
                                    new String[]{android.Manifest.permission.SEND_SMS},
                                    MY_PERMISSIONS_REQUEST_SEND_SMS);
                        }
                    }).show();
        } else {
            // Sms permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        }
        // END_INCLUDE(send_sms_permission_request)
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
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
    }


    /*
    * This function will add the user contacts in db
    * */
    public void ReturnThreadResult(String result)
    {
        if(httpAction == "addcontacts") {

            Log.e(TAG, "Add Contact Response  : " + result);

            try {
                JSONObject jsonObj = new JSONObject(result);
                Log.e(TAG, "Add Contact Response contact_added : " + jsonObj.getString("contact_added"));
                if(jsonObj.getString("contact_added") == "true"){
                    Toast.makeText(getApplicationContext(), "Contact Added Successfully", Toast.LENGTH_SHORT).show();

                    // Send verification code to contact using sms
                    //String send_sms_on = jsonObj.getString("contact_number");
                    String send_sms_on = "5556";
                    String verification_code = jsonObj.getString("verification_code");
                    String app_name = Config.APP_NAME;
                    String app_url = Config.APP_URL;
                    String text_message = user_mobile + " want to connect with you on " + app_name;
                    text_message += " send the verification code ";
                    text_message += verification_code;
                    text_message += " to the user for more info on " + app_name + " visit " + app_url;
                    Log.e(TAG, "Message send to the user : " + text_message);
                    //sendSMS(send_sms_on, text_message);

                    /**
                        Check for sms sending permission if granted then send sms else request for permission
                    */
                    if(Build.VERSION.SDK_INT >= 23) {
                        Log.e(TAG, "Checkingfor the permissions  : "+ Build.VERSION.SDK_INT);
                        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                            // Semd SMS permission has not been granted.
                            Log.e(TAG, "Checkingfor the permissions not granted: "+ Build.VERSION.SDK_INT);
                            requestSmsPermission();

                        }
//                        else {
//                            // Send Sms permissions is already available, send sms.
//                            Log.i(TAG,"CAMERA permission has already been granted. Displaying camera preview.");
//                            sendSMS(send_sms_on, text_message);
//                        }
                    }
//                    else {
//                        // Send Sms Directly
//                        sendSMS(send_sms_on, text_message);
//                    }
                    sendSMS(send_sms_on, text_message);

                } else {
                    Toast.makeText(getApplicationContext(), "Contact Already Added", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            hideLoading();
        } else if(httpAction == "getcontactscount") {
            hideLoading();
        }
    }




    class HomeHttpAsyncTask extends AsyncTask<String, String, String> {

        /** application context. */
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String url = params[0];

            httpAction = params[1];

            if(params[1] == "addcontacts") {
                url += "?action="+params[1]+"&dbid="+dbId+"&contact_number="+contact_mobile_number+"&contact_name="+contact_name;
            }

            if(params[1] == "getcontactscount") {
                url += "?action="+params[1]+"&dbid="+dbId;
            }

            //Log.e(TAG, "Posted details " + contact_mobile_number + "#" + contact_name + "#" + dbId);
            Log.e(TAG, "Firebase req url: " + url);

            String jsonStr = sh.makeServiceCall(url);
            if (jsonStr != null) {
                Log.e(TAG, "Firebase Response from url for action " + params[1] + ": " + jsonStr);

                if(params[1] == "getcontactscount") {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        total_contacts = jsonObj.getInt("users_contact_count");
                        request_processed =true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

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

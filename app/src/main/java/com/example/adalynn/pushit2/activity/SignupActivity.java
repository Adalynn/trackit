package com.example.adalynn.pushit2.activity;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.adalynn.pushit2.app.CommonUtil;
import com.example.adalynn.pushit2.util.HttpHandler;

import com.example.adalynn.pushit2.R;
import com.example.adalynn.pushit2.app.Config;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SignupActivity extends AppCompatActivity {

    private String TAG = SignupActivity.class.getSimpleName() + " PUSHIT : ";

    public Button saveUserDetails;
    String ScreenUserData = null;
    public String action= null;
    public String httpAction= null;
    public String fbID = null;
    public String dbID = null;
    ProgressDialog dialog;
    AlertDialog c_dialog;
    CommonUtil common_util;
    public String entered_verification_code;
    public String contact_verification_code;


    //EditText user_email;
    EditText user_mobile;

    String userMobile;
    String userEmail;

//
//
//    private SubscriptionManager mSubscriptionManager;
//    public static boolean isMultiSimEnabled = false;
//    public static String defaultSimName;
//    public static List<SubscriptionInfo> subInfoList;
//    public static ArrayList<String> Numbers;
//
//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Log.e(TAG, "SignupActivity called:");

        showLoading("Please wait.");
        common_util = new CommonUtil();

        Bundle extras = getIntent().getExtras();
        action= extras.getString("action");

        Log.e(TAG, "SignupActivity called with action " + action);
        Log.e(TAG, "Build Version Found : " + Build.VERSION.SDK_INT);

        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        try {
            String number = tm.getLine1Number();
            //user_email = (EditText)findViewById(R.id.EditTextEmail);
            //user_email.setText(number);

            user_mobile = (EditText)findViewById(R.id.EditTextMobile);
            user_mobile.setText(number);
            Log.e(TAG, "MOBILE NUMBER " + number);
            hideLoading();

            this.saveUserDetails = (Button)this.findViewById(R.id.ButtonSaveUserDetails);
            this.saveUserDetails.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                Log.e(TAG, "ScreenUserData Save User Details Btn Clicked");
                userMobile = user_mobile.getText().toString();
                if(!userMobile.isEmpty()) {
                    // Save user data here
                    Log.e(TAG, "New user data to be saved " + userMobile);
                    addNewUser();
                    //saveUserDataByDbId();
                } else {
                    Animation shake = AnimationUtils.loadAnimation(SignupActivity.this, R.anim.shake);
                    user_mobile.startAnimation(shake);
                    Log.e(TAG, "Mobile number is empty");
                }
                }

            });
        } catch (Exception e) {
            Log.e(TAG, "Unable to get the mobile number " + e.getMessage());
        }



        /*
        try {
            JSONObject jsonObj = new JSONObject(ScreenUserData);
            dbID = jsonObj.getJSONObject("data").getString("id");
            fbID = jsonObj.getJSONObject("data").getString("fbid");

            this.saveUserDetails = (Button)this.findViewById(R.id.ButtonSaveUserDetails);
            this.saveUserDetails.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    Log.e(TAG, "ScreenUserData Save User Details Btn Clicked");
                    user_email   = (EditText)findViewById(R.id.EditTextEmail);
                    user_mobile   = (EditText)findViewById(R.id.EditTextMobile);

                    userMobile = user_mobile.getText().toString();
                    userEmail = user_email.getText().toString();

                    if(!userEmail.isEmpty() && !userMobile.isEmpty()) {
                        // Save user data here
                        Log.e(TAG, "ScreenUserData Data to be save " + dbID + "#" + userMobile + "#" + userEmail);
                        saveUserDataByDbId();

                    } else {
                        Log.e(TAG, "ScreenUserData Email or mobile is empty!");
                    }
                }

            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
        */
    }


//    private void GetCarriorsInformation() {
//        subInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
//        if (subInfoList.size() > 1) {
//            isMultiSimEnabled = true;
//        }
//        for (SubscriptionInfo subscriptionInfo : subInfoList) {
//            Numbers.add(subscriptionInfo.getNumber());
//        }
//    }

    public void updateUserVerification(){
        Log.e(TAG, "Updating isVerified for the user");
        try {
            showLoading("Please wait while we verifiy the code.");
            new HttpAsyncTask().execute(Config.HTTP_API_URL,"update_user_isverified").get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void addNewUser() {
        try {
            showLoading("Please wait while we add you.");
            new HttpAsyncTask().execute(Config.HTTP_API_URL,"addnewuser").get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void saveUserDataByDbId() {
        try {
            showLoading("saveuserdatabydbid");
            new HttpAsyncTask().execute(Config.HTTP_API_URL,"saveuserdatabydbid").get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void showLoading(String msg) {
        Log.e(TAG, "showLoading called with msg " + msg);
        dialog = new ProgressDialog(SignupActivity.this);
        dialog.setMessage(msg);
        dialog.show();

    }

    public void hideLoading() {
        if (dialog.isShowing()) {
            Log.e(TAG, "hideLoading called");
            dialog.dismiss();
        }
    }

    /*
    *
    *
    * */
    public void ReturnThreadResult(String result)
    {
        if(httpAction == "saveuserdatabydbid") {
            try {
                JSONObject jsonObj = new JSONObject(result);
                ScreenUserData = result;
                Log.e(TAG, "AA User data by dbid: " + result);
                hideLoading();
                /*
                    @TODO : Goto next activity with data in string format
                */

                Log.e(TAG, "ScreenUserData : " + httpAction + " # " + ScreenUserData);
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("ScreenUserData",ScreenUserData);
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(httpAction == "addnewuser") {
            try {
                JSONObject jsonObj = new JSONObject(result);
                //ScreenUserData = result;
                //hideLoading();
                /*
                    @TODO : Goto next activity with data in string format
                */
                String id = jsonObj.getJSONObject("data").getString("id");
                String verification_code = jsonObj.getJSONObject("data").getString("verification_code");
                String mobile = jsonObj.getJSONObject("data").getString("mobile");

                String user_exists = jsonObj.getString("user_exists");

                Log.e(TAG, "id : " + id + " # verification_code " + verification_code);
                if(user_exists.equals("true")) {
                    Log.e(TAG, "User already exists setting user now");
                }
                common_util.setDbIdInPref(getApplicationContext(), id);
                //Log.e(TAG, "ScreenUserData : " + httpAction + " # " + ScreenUserData);
//                Intent intent = new Intent(this, HomeActivity.class);
//                intent.putExtra("ScreenUserData",ScreenUserData);
//                startActivity(intent);
                /*
                * Send verification_code using sms
                * */
                sendSMS(mobile, verification_code);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(httpAction == "update_user_isverified") {

            try{
                JSONObject jsonObj = new JSONObject(result);
                ScreenUserData = result;
                if(jsonObj.getString("user_updated")== "true"){
                    Toast.makeText(
                            getApplicationContext(), "Verfication Code Update",
                            Toast.LENGTH_LONG
                    ).show();
                    // Show the home screen
                    hideLoading();
                    goToHomeActivity();
                } else if(jsonObj.getString("user_updated") == "false") {
                    Toast.makeText(
                            getApplicationContext(), "Invalid Verfication Code",
                            Toast.LENGTH_LONG
                    ).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



    }

    public void goToHomeActivity() {
        Log.e(TAG, "goToHomeActivity Called" + ScreenUserData);
        Intent intent = new Intent(this, HomeActivity.class);
        //intent.putExtra("action","sign_up_new_user");
        intent.putExtra("ScreenUserData",ScreenUserData);
        startActivity(intent);
    }

    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String verification_code)
    {
        String hash = "H3zX2wIWspU";
        String message = "<#> Use code to verify: " + verification_code + " " + hash;
        //H3zX2wIWspU
        Log.e(TAG, "sendSMS Called to send  : " + message + " on mobile number " + phoneNumber);
        try {

            PendingIntent sentPI;
            String SENT = "SMS_SENT";
            sentPI = PendingIntent.getBroadcast(this, 0,new Intent(SENT), 0);

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
            //sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
            //Log.e(TAG, "Go to new activity");
            requestForVerifyUserCode(phoneNumber, verification_code);
        } catch (Exception e){

            Log.e(TAG, "sendSMS Called Exception : " + e.getMessage());
            //e.getMessage() response => uid 10078 does not have android.permission.SEND_SMS
            Toast.makeText(getApplicationContext(), "Could Not Send OTP. Try On Another Device", Toast.LENGTH_LONG).show();

            return;
        }

        Log.e(TAG, "sendSMS Called to send came till end");
    }


    /**
     * Function used to request for the contact verification
     */
    public void requestForVerifyUserCode(String mobile, String verifiy_code) {

        final EditText enter_verification_code = new EditText(this);
        enter_verification_code.setHint(R.string.contact_verification_code);
        enter_verification_code.setInputType(InputType.TYPE_CLASS_PHONE);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verifiy Contact");
        builder.setMessage("Please enter the received verification code from the contact");
        //builder.setCancelable(false);

        LinearLayout ll=new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(enter_verification_code);
        builder.setView(ll);


//        pid = parent_id;
//        cid = contact_id;

        contact_verification_code = verifiy_code;

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
        });


        c_dialog = builder.create();
        c_dialog.show();




        if (Build.VERSION.SDK_INT >= 23) {

            SmsRetrieverClient client = SmsRetriever.getClient(this);
            // Starts SmsRetriever, which waits for ONE matching SMS message until timeout
            // (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
            // action SmsRetriever#SMS_RETRIEVED_ACTION.
            Task<Void> task = client.startSmsRetriever();

            // Listen for success/failure of the start Task. If in a background thread, this
            // can be made blocking using Tasks.await(task, [timeout]);
            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Successfully started retriever, expect broadcast intent
                    // ...
                    Log.e(TAG, "Task OnSuccessListener Called");
                    //aVoid.get
                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Failed to start retriever, inspect Exception for more details
                    // ...
                    Log.e(TAG, "Task OnFailureListener Called");
                }
            });
        } else {
            Log.e(TAG, "User have to maually enter the verification code as the build ver is older " + Build.VERSION.SDK_INT);
        }




        //Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
        c_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Boolean wantToCloseDialog = false;
                entered_verification_code = enter_verification_code.getText().toString();
                //Do stuff, possibly set wantToCloseDialog to true then...
                Log.e(TAG, "Verifiy Code Btn Clicked " + entered_verification_code + "#" + contact_verification_code);
                if(entered_verification_code.equals(contact_verification_code)) {
                    /*
                    * User entered verification code is what share to it's contact so we can update the isverfified
                    * in db for the contact
                    * */
                    wantToCloseDialog = true;
                    updateUserVerification();
                } else {
                    //Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
                    Animation shake = AnimationUtils.loadAnimation(SignupActivity.this, R.anim.shake);
                    enter_verification_code.startAnimation(shake);
                    Toast.makeText(
                            getApplicationContext(), "Invalid verfication code",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }


                if(wantToCloseDialog) {
                    c_dialog.dismiss();
                }

                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
            }
        });
    }




    class HttpAsyncTask extends AsyncTask<String, String, String> {

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

            if(params[1] == "saveuserdatabydbid") {
                url += "?action="+params[1]+"&dbid="+dbID+"&fbid="+fbID+"&email="+userEmail+"&mobile="+userMobile;
            }

            if(params[1] == "addnewuser") {
                url += "?action="+params[1]+"&mobile="+userMobile;
            }

            if(params[1] == "update_user_isverified") {
                url += "?action="+params[1]+"&mobile="+userMobile+"&dbid="+common_util.getDbIdInPref(getApplicationContext());
            }

            Log.e(TAG, "Req url: " + url);

            String jsonStr = sh.makeServiceCall(url);
            if (jsonStr != null) {
                Log.e(TAG, "Response from url for action " + params[1] + ": " + jsonStr);
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

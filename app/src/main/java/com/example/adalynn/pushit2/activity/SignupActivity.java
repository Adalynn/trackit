package com.example.adalynn.pushit2.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.adalynn.pushit2.util.HttpHandler;

import com.example.adalynn.pushit2.R;
import com.example.adalynn.pushit2.app.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SignupActivity extends AppCompatActivity {

    public Button saveUserDetails;
    String ScreenUserData = null;
    public String httpAction= null;
    public String fbID = null;
    public String dbID = null;
    ProgressDialog dialog;

    private String TAG = HomeActivity.class.getSimpleName();

    EditText user_email;
    EditText user_mobile;

    String userMobile;
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Bundle extras = getIntent().getExtras();
        ScreenUserData = extras.getString("ScreenUserData");

        Log.e(TAG, "ScreenUserData Received User Data: " + ScreenUserData);
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
        Log.e(TAG, "Firebase show loading " + msg);
        dialog = new ProgressDialog(SignupActivity.this);
        dialog.setMessage("Please wait" + msg);
        dialog.show();

    }

    public void hideLoading() {
        if (dialog.isShowing()) {
            Log.e(TAG, "Firebase hiding loading image");
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

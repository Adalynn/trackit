package com.example.adalynn.pushit2.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adalynn.pushit2.R;
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
    private String TAG = HomeActivity.class.getSimpleName();
    public Button showContact;
    public Button addContact;
    public String head_text;
    public String sub_text;

    private String contact_mobile_number = "";
    private String contact_name = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Bundle extras = getIntent().getExtras();
        ScreenUserData = extras.getString("ScreenUserData");

        Log.e(TAG, "ScreenUserData Received User Data: " + ScreenUserData);
        try {
            JSONObject jsonObj = new JSONObject(ScreenUserData);
            dbId = jsonObj.getJSONObject("data").getString("id");
            Log.e(TAG, "ScreenUserData Received User Data Id: " + dbId);
            String mobile = jsonObj.getJSONObject("data").getString("mobile");
            Log.e(TAG, "ScreenUserData Received User Data Mobile: " + mobile);

            if(mobile.isEmpty()) {
                Log.e(TAG, "ScreenUserData emtpty mobile Received User Data Mobile: " + mobile);
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
                addContact(v);
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

    /** Called to add the contact */
    public void addContact(View view) {

        Log.e(TAG, "addContact Called");

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
                contact_mobile_number = mobile_number.getText().toString();
                contact_name = name.getText().toString();
                Log.e(TAG, "Posted details " + contact_mobile_number + "#" + contact_name + "#" + dbId);
                insert();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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
                } else {
                    Toast.makeText(getApplicationContext(), "Contact Already Added", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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


            //Log.e(TAG, "Posted details " + contact_mobile_number + "#" + contact_name + "#" + dbId);
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

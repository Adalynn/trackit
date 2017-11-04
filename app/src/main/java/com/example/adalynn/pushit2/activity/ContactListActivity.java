package com.example.adalynn.pushit2.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.adalynn.pushit2.R;
import com.example.adalynn.pushit2.app.Config;
import com.example.adalynn.pushit2.util.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ContactListActivity extends AppCompatActivity {

    ProgressDialog dialog;
    String dbId = null;
    public String httpAction= null;
    public String ScreenUserData = null;
    public String UserContactsListData = null;
    ArrayList<HashMap<String, String>> contactList;
    private ListView lv;
    AlertDialog c_dialog;

    public String pid,cid,contact_verification_code;

    private static final String TAG = ContactListActivity.class.getSimpleName();
    public static final String EXTRA_MESSAGE_FROM_MAP_VIEW = "Message Set in contactLsist activivty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        Bundle extras = getIntent().getExtras();
        dbId = extras.getString("dbId");
        getUserContactsByDbId();
        contactList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.contact_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(contactList.get(position).get("is_verified") == "1") {
                    showMap(view, contactList.get(position).get("latitude"), contactList.get(position).get("longitude"));
                } else {
                    requestForVerify(
                            contactList.get(position).get("contact_id"),
                            contactList.get(position).get("parent_id"),
                            contactList.get(position).get("verification_code")
                    );
                }


            }
        });
        Log.e(TAG, "LIST VIEW " + UserContactsListData);
    }

    /**
     * Function used to request for the contact verification
     */
    public void requestForVerify(String contact_id, String parent_id, String verifiy_code) {

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


        pid = parent_id;
        cid = contact_id;
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
        //Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
        c_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Boolean wantToCloseDialog = false;
                //Do stuff, possibly set wantToCloseDialog to true then...


                if(enter_verification_code.getText().toString() == contact_verification_code) {
                    //updateIsVerified();
                } else {
                    enter_verification_code.getText().toString();
                    //Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
                    Animation shake = AnimationUtils.loadAnimation(ContactListActivity.this, R.anim.shake);
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

    /** Called when the user taps the Send button */
    public void showMap(View view, String latitude, String longitude) {
        Intent intent = new Intent(this, AniMapView.class);
        String message = "Sending some data to map class from contact class";
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitute",longitude);
        intent.putExtra(EXTRA_MESSAGE_FROM_MAP_VIEW, message);
        startActivity(intent);
    }

    public void showLoading(String msg) {
        Log.e(TAG, "Firebase show loading " + msg);
        dialog = new ProgressDialog(ContactListActivity.this);
        dialog.setMessage("Please wait " + msg);
        dialog.show();

    }

    public void hideLoading() {
        if (dialog.isShowing()) {
            Log.e(TAG, "Firebase hiding loading image");
            dialog.dismiss();
        }
    }

    public void getUserContactsByDbId() {

        Log.e(TAG, "Firebase getting user contact list by dbid : " + dbId);
        try {
            showLoading("Waiting for the contact list to load " + dbId);
            new HttpAsyncTask().execute(Config.HTTP_API_URL,"getusercontactsbydbid").get(1000, TimeUnit.MILLISECONDS);
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
        if(httpAction == "getusercontactsbydbid") {
            UserContactsListData = result;

            Log.e(TAG, "User Contact List  : " + UserContactsListData);
            try {
                JSONObject jsonObj = new JSONObject(UserContactsListData);
                JSONArray contacts = jsonObj.getJSONArray("data");
                for (int i = 0; i < contacts.length(); i++) {

                    JSONObject c = contacts.getJSONObject(i);
                    String contact_id = c.getString("contact_id");
                    String parent_id = c.getString("parent_id");
                    String contact_name = c.getString("contact_name");
                    String contact_number = c.getString("contact_number");
                    String is_verified = c.getString("is_verified");
                    String latitude = c.getString("latitude");
                    String longitude = c.getString("longitude");
                    String verification_code = c.getString("verification_code");

                    HashMap<String, String> contact = new HashMap<>();
                    // adding each child node to HashMap key => value
                    contact.put("contact_id", contact_id);
                    contact.put("parent_id", parent_id);
                    contact.put("contact_name", contact_name);
                    contact.put("contact_number", contact_number);
                    contact.put("is_verified", is_verified);
                    contact.put("latitude", latitude);
                    contact.put("longitude", longitude);
                    contact.put("textstring", contact_id+"#"+contact_name);
                    contact.put("verification_code", verification_code);


                    // adding contact to contact list
                    contactList.add(contact);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Getting JSON Array node
            //JSONArray contacts = jsonObj.getJSONArray("users");

            ListAdapter adapter = new SimpleAdapter(ContactListActivity.this, contactList,
                    R.layout.list_item, new String[]{"textstring", "contact_number"},
                    new int[]{R.id.textstring, R.id.mobile_number});
            lv.setAdapter(adapter);
            hideLoading();
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

            String url = params[0];
            httpAction = params[1];

            if(params[1] == "getusercontactsbydbid") {
                url += "?action="+params[1]+"&dbid="+dbId;
            }

            Log.e(TAG, "Firebase req url: " + url);

            String jsonStr = sh.makeServiceCall(url);
            if (jsonStr != null) {
                Log.e(TAG, "Firebase Response from url for action " + params[1] + ": " + jsonStr);
                return jsonStr;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ReturnThreadResult(result);
        }
    }

}

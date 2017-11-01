package com.example.adalynn.pushit2.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.adalynn.pushit2.R;
import com.example.adalynn.pushit2.app.Config;
import com.example.adalynn.pushit2.util.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
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
                //requestForVerify();
                showMap(view, contactList.get(position).get("latitude"), contactList.get(position).get("longitude"));

            }
        });
        Log.e(TAG, "LIST VIEW " + UserContactsListData);
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

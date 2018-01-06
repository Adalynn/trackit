package com.example.adalynn.pushit2.activity;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.adalynn.pushit2.R;
import com.example.adalynn.pushit2.app.CommonUtil;
import com.example.adalynn.pushit2.app.Config;
import com.example.adalynn.pushit2.util.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    SimpleAdapter adapter;
    private ListView lv;
    AlertDialog c_dialog;

    public String pid;
    public String cid;
    public String contact_verification_code;
    public String entered_verification_code;

    public String requested_user_latitude;
    public String requested_user_longitude;
    public String requested_contact_number;

    public Button addContact;
    public int total_contacts = 0;
    private static final String TAG = ContactListActivity.class.getSimpleName();
    public static final String EXTRA_MESSAGE_FROM_MAP_VIEW = "Message Set in contactLsist activivty";
    CommonUtil common_util;
    /**
     * Id to identify a contacts permission request.
     */
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 12345;
    private String contact_mobile_number = "";
    private String contact_name = "";
    String user_mobile = null;
    private LinearLayout contact_list_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        common_util = new CommonUtil();

        contact_list_layout = (LinearLayout) findViewById(R.id.contact_list_layout);

        Bundle extras = getIntent().getExtras();
        dbId = extras.getString("dbId");
        getUserContactsByDbId();
        lv = (ListView) findViewById(R.id.contact_list);
        contactList = new ArrayList<>();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                requested_user_latitude = contactList.get(position).get("latitude");
                requested_user_longitude = contactList.get(position).get("longitude");
                requested_contact_number = contactList.get(position).get("contact_number");
                if(contactList.get(position).get("is_verified").equals("1")) {
                    showMap(requested_user_latitude, requested_user_longitude, requested_contact_number);
                } else {
                    requestForVerify(
                            contactList.get(position).get("contact_id"),
                            contactList.get(position).get("parent_id"),
                            contactList.get(position).get("verification_code")
                    );
                }


            }
        });


        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            // setting onItemLongClickListener and passing the position to the function
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                removeItemFromList(position);
                return true;
            }
        });

        Log.e(TAG, "LIST VIEW " + UserContactsListData);
        lv.setEmptyView(findViewById(R.id.empty_list));



        this.addContact = (Button)this.findViewById(R.id.add_contacts_button);
        this.addContact.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //Log.e(TAG, "ADD CONTACTS BUTTON CLICKED!");

                if(total_contacts >= Config.MAX_CONTACTS_LIMIT) {
                    Log.e(TAG, "MAX CONTACT LIMIT REACHED!");
                    common_util.addMaxContactAddedAlert(v, ContactListActivity.this);
                    //addMaxContactAddedAlert(v);
                } else {
                    addContact(v);
                }
            }
        });

    }

    public void removeContactFromUserList(String parent_id, String contact_id) {
        try {
            showLoading(Config.WAIT_STR_MSG);
            new ContactListActivity.HttpAsyncTask().execute(Config.HTTP_API_URL,"removecontact", parent_id, contact_id).get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    // method to remove list item
    protected void removeItemFromList(int position) {
        final int deletePosition = position;

        AlertDialog.Builder alert = new AlertDialog.Builder(
                ContactListActivity.this);

        alert.setTitle("Delete");
        alert.setMessage("Do you really want to delete this contact? You will not be able to track this contact once deleted.");

        // Set up the buttons
        alert.setPositiveButton("Ok Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //contact_mobile_number = mobile_number.getText().toString();
                //contact_name = name.getText().toString();
                //Log.e(TAG, "Posted details " + contact_mobile_number + "#" + contact_name + "#" + dbId);
                //insert();
                //lv.remove(deletePosition);
                //arr.remove(deletePosition);
                contactList.remove(deletePosition);
                total_contacts = total_contacts-1;
                adapter.notifyDataSetChanged();
                adapter.notifyDataSetInvalidated();
                Map<String, Object> map = (Map<String, Object>)lv.getItemAtPosition(deletePosition);
                removeContactFromUserList(map.get("parent_id").toString(), map.get("contact_id").toString());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
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
                //contact_mobile_number = mobile_number.getText().toString();
                //contact_name = name.getText().toString();
                //Log.e(TAG, "Posted details " + contact_mobile_number + "#" + contact_name + "#" + dbId);
                //insert();
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
                    Animation shake = AnimationUtils.loadAnimation(ContactListActivity.this, R.anim.shake);
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
            new ContactListActivity.HttpAsyncTask().execute(Config.HTTP_API_URL,"addcontacts").get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function used to request for the contact verification
     */
    public void requestForVerify(String contact_id, final String parent_id, String verifiy_code) {

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
                entered_verification_code = enter_verification_code.getText().toString();
                //Do stuff, possibly set wantToCloseDialog to true then...
                if(entered_verification_code.equals(contact_verification_code)) {
                    /*
                    * User entered verification code is what share to it's contact so we can update the isverfified
                    * in db for the contact
                    * */
                    wantToCloseDialog = true;
                    updateIsVerified();
                } else {
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
    public void showMap(String latitude, String longitude, String contact_number) {

        Log.e(TAG, "showMap Called " + latitude +"#"+ longitude);

        Intent intent = new Intent(this, AniMapView.class);
        String message = "Sending some data to map class from contact class";
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitute",longitude);
        intent.putExtra("contact_number",contact_number);
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

    public void updateIsVerified(){
        Log.e(TAG, "Updating isVerified for the conatct id : " + cid + " of parent id " + pid);
        try {
            showLoading("while we verifiy the code");
            new HttpAsyncTask().execute(Config.HTTP_API_URL,"updateisverified").get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
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

            Log.e(TAG, "User Contact List1  : " + UserContactsListData);
            try {
                JSONObject jsonObj = new JSONObject(UserContactsListData);
                Log.e(TAG, "User Contact Lists 2  : " + UserContactsListData);
                //String id = jsonObj.getJSONObject("data").getString("id");
                total_contacts = jsonObj.getInt("total_contacts");
                Log.e(TAG, "Count of users contact list  : " + total_contacts);
                if(total_contacts == 0) {
                } else {
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
                        contact.put("textstring", contact_id+"#"+contact_name+"#"+is_verified);
                        contact.put("verification_code", verification_code);

                        // adding contact to contact list
                        contactList.add(contact);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Getting JSON Array node
            //JSONArray contacts = jsonObj.getJSONArray("users");

            adapter = new SimpleAdapter(ContactListActivity.this, contactList,
                    R.layout.list_item, new String[]{"textstring", "contact_number"},
                    new int[]{R.id.textstring, R.id.mobile_number});

            lv.setAdapter(adapter);
            hideLoading();
        } else if (httpAction == "updateisverified") {
            hideLoading();
            try{
                JSONObject jsonObj = new JSONObject(result);
                if(jsonObj.getString("user_updated")== "true"){
                    Toast.makeText(
                            getApplicationContext(), "Verfication Code Update",
                            Toast.LENGTH_LONG
                    ).show();
                    showMap(requested_user_latitude, requested_user_longitude, requested_contact_number);

                } else if(jsonObj.getString("user_updated") == "false") {
                    Toast.makeText(
                            getApplicationContext(), "Invalid Verfication Code",
                            Toast.LENGTH_LONG
                    ).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if(httpAction == "addcontacts") {

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

                    }
                    sendSMS(send_sms_on, text_message);

                    total_contacts = total_contacts+1;

                    Intent refresh = new Intent(this, ContactListActivity.class);
                    String message = "Sending some data";
                    refresh.putExtra("data", message);
                    refresh.putExtra("dbId", dbId);
                    startActivity(refresh);
                    finish(); //finish Activity.


                } else {
                    Toast.makeText(getApplicationContext(), "Contact Already Added", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            hideLoading();
        } else if(httpAction == "removecontact") {
            Log.e(TAG, "Remove Contact " + result);
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

            if(params[1] == "updateisverified") {
                url += "?action="+params[1]+"&cid="+cid+"&pid="+pid;
            }

            if(params[1] == "addcontacts") {
                url += "?action="+params[1]+"&dbid="+dbId+"&contact_number="+contact_mobile_number+"&contact_name="+contact_name;
            }

            if(params[1] == "removecontact") {
                url += "?action="+params[1]+"&parent_id="+params[2]+"&contact_id="+params[3];
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


    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {
        Log.e(TAG, "send SMS Called to send  : " + message + " on mobile number " + phoneNumber);
        try {

            PendingIntent sentPI;
            String SENT = "SMS_SENT";
            sentPI = PendingIntent.getBroadcast(this, 0,new Intent(SENT), 0);

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
            //sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
        } catch (Exception e){

            Log.e(TAG, "send SMS Called Exception : " + e.getMessage());
            //e.getMessage() response => uid 10078 does not have android.permission.SEND_SMS
            Toast.makeText(getApplicationContext(), "Could Not Send OTP. Try On Another Device", Toast.LENGTH_LONG).show();

            return;
        }
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

            Snackbar.make(contact_list_layout, R.string.permission_send_sms,Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(ContactListActivity.this,
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

}

package com.example.adalynn.pushit2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.adalynn.pushit2.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adalynn on 28/10/17.
 */

public class HomeActivity extends AppCompatActivity {

    public Button saveUserDetails;
    String ScreenUserData = null;
    private String TAG = HomeActivity.class.getSimpleName();

    public String head_text;
    public String sub_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Bundle extras = getIntent().getExtras();
        ScreenUserData = extras.getString("ScreenUserData");

        Log.e(TAG, "ScreenUserData Received User Data: " + ScreenUserData);
        try {
            JSONObject jsonObj = new JSONObject(ScreenUserData);
            String dbId = jsonObj.getJSONObject("data").getString("id");
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
    }
}

package com.example.uberclone;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.ParseAnalytics;
import com.parse.SaveCallback;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    public void redirectActivity(){
        if(ParseUser.getCurrentUser().get("RiderorDriver").equals("rider")){
            Intent intent = new Intent(getApplicationContext(),RiderActivity.class);
            startActivity(intent);
        }
        else
        {

            Intent intent = new Intent(getApplicationContext(),ViewRequestsActivity.class);
            startActivity(intent);
        }
    }

    public void getstarted(View view){
        Switch userType_switch = (Switch) findViewById(R.id.switch1);
        Log.i("Switch value",String.valueOf(userType_switch.isChecked()));
        String usertype= "rider";
        if(userType_switch.isChecked()){
            usertype = "driver";
        }

        ParseUser.getCurrentUser().put("RiderorDriver", usertype);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                redirectActivity();
            }
        });
        Log.i("Info","Redirecting as "+usertype);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        if(ParseUser.getCurrentUser()==null){
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e==null){
                        Log.i("Info","Anonymous login successful");
                    }else{
                        Log.i("Info","Anonymous login failed");
                    }
                }
            });
        }else{
            if(ParseUser.getCurrentUser().get("RiderorDriver")!=null){
                Log.i("Info","Redirecting as "+ ParseUser.getCurrentUser().get("Rider or Driver"));
                redirectActivity();
            }
        }
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

    }
}

package com.example.uberclone;
import android.app.Application;
import android.app.DownloadManager;
import android.util.Log;
import com.parse.*;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class StarterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        //Parse.initialize(getApplicationContext(),"myappID","Kk9xMfRY9dAe");
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("myappID")
                .clientKey("Kk9xMfRY9dAe")
                .server("http://18.220.182.168/parse")
                .build());



        //ParseUser.enableAutomaticUser();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

    }
}


package com.example.artmates;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("pXspa8c60CY4MMUw50r8irhybrOsa221TOtg6bHR")
                .clientKey("wmFDkWXrWH2PdzzTZdRHURlqdvbhmXuopIBfLgGS")
                .server("https://parseapi.back4app.com")
                .build()
        );
        ParseObject.registerSubclass(Post.class);

    }
}

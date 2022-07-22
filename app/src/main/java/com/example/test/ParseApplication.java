package com.example.test;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("pXspa8c60CY4MMUw50r8irhybrOsa221TOtg6bHR")
                .clientKey("wmFDkWXrWH2PdzzTZdRHURlqdvbhmXuopIBfLgGS")
                .server("https://parseapi.back4app.com")
                .build()
        );
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Message.class);
        ParseObject.registerSubclass(Comment.class);


    }
}



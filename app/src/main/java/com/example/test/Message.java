package com.example.test;


import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Message")
public class Message extends ParseObject {
    public static final String USER_ID_KEY = "userId";
    public static final String USER_ID_KEY2 = "userId2";
    public static final String BODY_KEY = "body";

    public String getUserId() {
        return getString(USER_ID_KEY);
    }
    public ParseUser getUser() {

        ParseUser user = getParseUser(USER_ID_KEY2);
        if (user != null){
            try {
                assert user != null;
                user.fetchIfNeeded();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return user;
    }

    public void setUser(ParseUser user) {
        put(USER_ID_KEY2, user);
    }

    public String getBody() {
        return getString(BODY_KEY);
    }

    public void setUserId(String userId) {
        put(USER_ID_KEY, userId);
    }

    public void setBody(String body) {
        put(BODY_KEY, body);
    }
}

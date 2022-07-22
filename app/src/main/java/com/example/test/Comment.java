package com.example.test;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Comment")
public class Comment extends ParseObject {
    public static final String KEY_TEXT = "Text";
    private static final String USER_ID_KEY = "userId";

    public void setText(String text){put(KEY_TEXT, text);}

    public String getText(){return getString(KEY_TEXT);};

    public ParseUser getUser() {

        ParseUser user = getParseUser(USER_ID_KEY);
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
        put(USER_ID_KEY, user);
    }

}

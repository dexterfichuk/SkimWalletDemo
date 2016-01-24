package com.simplytapp.demo;

import java.text.SimpleDateFormat;
import java.util.Locale;

public interface Constants {

    String KEY_ACTIVE_CARD = "com.simplytapp.demo.ACTIVE_CARD";
    String KEY_ACTIVE_CARD_JSON = "com.simplytapp.demo.ACTIVE_CARD_JSON";
    String KEY_CARD_ID = "com.simplytapp.demo.CARD_ID";
    String KEY_CONSUMER_KEY = "com.simplytapp.demo.CONSUMER_KEY";
    String KEY_CONSUMER_SECRET = "com.simplytapp.demo.CONSUMER_SECRET";
    String KEY_ACCESS_TOKEN = "com.simplytapp.demo.ACCESS_TOKEN";
    String KEY_TOKEN_SECRET = "com.simplytapp.demo.TOKEN_SECRET";

    String KEY_CREDENTIALS = "CREDENTIALS";

    int REQUEST_CODE_GET_CREDENTIALS = 9999;

    SimpleDateFormat DATE_FORMAT_MM_YY = new SimpleDateFormat("MM/yy", Locale.US);
    SimpleDateFormat DATE_FORMAT_UTC = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.US);
}
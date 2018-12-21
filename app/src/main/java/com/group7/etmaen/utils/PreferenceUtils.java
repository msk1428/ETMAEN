package com.group7.etmaen.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by delaroy on 9/13/18.
 */

public class PreferenceUtils {
    public PreferenceUtils(){

    }

    public static boolean savePhoneNumber(String phonenumber, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(Constants.PHONE_NUMBER, phonenumber);
        prefsEditor.apply();
        return true;
    }

    public static String getPhoneNumber(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(Constants.PHONE_NUMBER, "");
    }

}

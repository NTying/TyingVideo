package com.tying.videoinfo.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class DataStorageUtils {

    private static Context mContext;
    private static String fileName;

    public DataStorageUtils(Context mContext, String fileName) {
        this.mContext = mContext;
        this.fileName = fileName;
    }

    public void insertVal(String key, String value) {

        SharedPreferences sp = mContext.getSharedPreferences(fileName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String findByKey(String key) {
        SharedPreferences sp = mContext.getSharedPreferences(fileName, MODE_PRIVATE);
        return sp.getString(key, "");
    }

    public void removeByKey(String key) {
        SharedPreferences sp = mContext.getSharedPreferences(fileName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.commit();
    }
}

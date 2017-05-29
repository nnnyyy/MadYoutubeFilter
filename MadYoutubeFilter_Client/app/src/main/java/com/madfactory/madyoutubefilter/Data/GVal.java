package com.madfactory.madyoutubefilter.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by nnnyy on 2017-05-20.
 */

public class GVal {
    public static final String URL_Search = "http://4seasonpension.com:4000/v/";
    public static final String URL_Description = "http://4seasonpension.com:4000/videosinfo/";
    public static final String ANDROID_KEY = "AIzaSyAgOtMxWNk2NmaCsiBynf8O7kBty9SXPrk";
    public static final String KEY_FAVORATE = "ReadedArticles";

    public static HashSet<Integer> liFavorate = new HashSet<>();
    static public List<MCategory> liCategories = new ArrayList<>();
    static public boolean LoadCategory(String sResponse){
        liCategories.clear();
        try {
            JSONArray jsonArr = new JSONArray(sResponse);
            int len = jsonArr.length();
            for( int i = 0 ; i < len ; ++i ) {
                MCategory newCategory = new MCategory();
                JSONObject category = jsonArr.getJSONObject(i);
                newCategory.sName = category.getString("name");
                newCategory.sKey = category.getString("key");
                newCategory.sType = category.getString("type");
                if(!category.isNull("subCategory")) {
                    JSONArray arrSub = category.getJSONArray("subCategory");
                    for( int j = 0 ; j < arrSub.length() ; ++j ) {
                        JSONObject subCategory = arrSub.getJSONObject(j);
                        String sSubName = subCategory.getString("name");
                        String sSubKey = subCategory.getString("key");
                        String sSubType = subCategory.getString("type");
                        newCategory.AddSub(sSubName, sSubKey, sSubType);
                    }
                }
                liCategories.add(newCategory);
            }
            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    static public List<MCategory> GetCategories() {
        return liCategories;
    }

    public static class MCategory {
        public String sName;
        public String sKey;
        public String sType;
        public List<SubCategory> liSubCategories = new ArrayList<>();

        public void AddSub(String _sName, String _sKey, String _sType) {
            SubCategory newSub = new SubCategory();
            newSub.sKey = _sKey;
            newSub.sName = _sName;
            newSub.sType = _sType;
            liSubCategories.add(newSub);
        }

        public String getType(int subIdx) {
            if(liSubCategories.size() != 0 ) {
                return liSubCategories.get(subIdx).sType;
            }

            return sType;
        }
    }

    public static class SubCategory {
        public String sName;
        public String sKey;
        public String sType;
    }

    public static void setStringArrayPref(Context context , String sKey, ArrayList<String> values){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for(int i =  0 ; i < values.size() ; ++i) {
            a.put(values.get(i));
        }

        if(!values.isEmpty()) {
            editor.putString(sKey, a.toString());
        }
        else {
            editor.putString(sKey, null);
        }

        editor.apply();
    }

    public static ArrayList<String> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> a = new ArrayList<>();
        if( json != null) {
            try {
                JSONArray ja = new JSONArray(json);
                for(int i = 0 ; i < ja.length() ; ++i) {
                    String sData = ja.optString(i);
                    a.add(sData);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return a;
    }

    public static ArrayList<Integer> getIntArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<Integer> a = new ArrayList<>();
        if( json != null) {
            try {
                JSONArray ja = new JSONArray(json);
                for(int i = 0 ; i < ja.length() ; ++i) {
                    Integer sData = ja.optInt(i);
                    a.add(sData);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return a;
    }

    public static void setFavorate(Context context, int hash) {

        if(liFavorate.size() >= 200) {
            liFavorate.remove(liFavorate.size()-1);
        }
        liFavorate.add(hash);
        ArrayList li = new ArrayList(liFavorate);
        setStringArrayPref(context, KEY_FAVORATE, li);
    }

    public static void loadFavorate(Context context) {
        liFavorate = new HashSet<>(getIntArrayPref(context, KEY_FAVORATE));
    }

    public static void removeFavorate(Context context, int hash) {
        if(liFavorate.contains(hash)) {
            liFavorate.remove(hash);
        }
        ArrayList li = new ArrayList(liFavorate);
        setStringArrayPref(context, KEY_FAVORATE, li);
    }

    public static boolean isFavorated(int hash) {
        return liFavorate.contains(hash);
    }
}



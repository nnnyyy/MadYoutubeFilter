package com.madfactory.madyoutubefilter.Data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nnnyy on 2017-05-20.
 */

public class GVal {
    public static final String URL_Search = "http://4seasonpension.com:4000/v/";
    public static final String URL_Description = "http://4seasonpension.com:4000/videosinfo/";

    public static final String ANDROID_KEY = "AIzaSyAgOtMxWNk2NmaCsiBynf8O7kBty9SXPrk";

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
                        newCategory.AddSub(sSubName, sSubKey);
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

        public void AddSub(String _sName, String _sKey) {
            SubCategory newSub = new SubCategory();
            newSub.sKey = _sKey;
            newSub.sName = _sName;
            liSubCategories.add(newSub);
        }
    }

    public static class SubCategory {
        public String sName;
        public String sKey;
    }
}



package com.madfactory.madyoutubefilter.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nnnyy on 2017-05-20.
 */

public class GVal {
    static public List<MCategory> liCategories = new ArrayList<>();
    static public void LoadCategory(){
        MCategory newCategory = new MCategory();
        newCategory.sName = "인기";
        newCategory.sKey = "popul";
        newCategory.AddSub("비제이", "bj");
        newCategory.AddSub("가요", "kpop");
        liCategories.add(newCategory);
        newCategory = new MCategory();
        newCategory.sName = "라이브";
        newCategory.sKey = "live";
        newCategory.AddSub("비제이", "bj");
        newCategory.AddSub("음악방송", "music");
        liCategories.add(newCategory);
    }

    static public List<MCategory> GetCategories() {
        return liCategories;
    }

    public static class MCategory {
        public String sName;
        public String sKey;
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



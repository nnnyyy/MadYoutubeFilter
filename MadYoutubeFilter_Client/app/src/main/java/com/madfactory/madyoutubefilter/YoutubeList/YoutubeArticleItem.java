package com.madfactory.madyoutubefilter.YoutubeList;

import android.graphics.drawable.Drawable;

import com.madfactory.madyoutubefilter.CategoryFragment;

/**
 * Created by nnnyy on 2017-05-21.
 */

public class YoutubeArticleItem {
    private CategoryFragment.VideoInfo info;

    public void setInfo(CategoryFragment.VideoInfo _vi) {
        info = _vi ;
    }

    public String getThumbnailURL() {
        return info.thumbnailUrl ;
    }
    public String getTitle() { return info.title; }
    public String getDuration() {
        return info.duration;
    }
    public String getID() { return info.id; }
}

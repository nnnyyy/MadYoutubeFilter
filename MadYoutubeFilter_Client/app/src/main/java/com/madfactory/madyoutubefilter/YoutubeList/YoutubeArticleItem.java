package com.madfactory.madyoutubefilter.YoutubeList;

import android.graphics.drawable.Drawable;

/**
 * Created by nnnyy on 2017-05-21.
 */

public class YoutubeArticleItem {
    private String thumbnail;
    private String titleStr ;
    private String descStr ;

    public void setThumbnail(String sThumbnail) {
        thumbnail = sThumbnail ;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setDesc(String desc) {
        descStr = desc ;
    }

    public String getThumbnailURL() {
        return this.thumbnail ;
    }
    public String getTitle() {
        return this.titleStr ;
    }
    public String getDesc() {
        return this.descStr ;
    }
}

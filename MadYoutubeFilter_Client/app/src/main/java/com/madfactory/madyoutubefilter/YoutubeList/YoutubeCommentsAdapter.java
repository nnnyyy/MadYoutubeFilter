package com.madfactory.madyoutubefilter.YoutubeList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import java.text.NumberFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.madfactory.madyoutubefilter.CategoryFragment;
import com.madfactory.madyoutubefilter.Data.GVal;
import com.madfactory.madyoutubefilter.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

public class YoutubeCommentsAdapter extends BaseAdapter {
    private ArrayList<YoutubeCommentItem> alYoutubeComments = new ArrayList<>();

    public YoutubeCommentsAdapter() {
    }

    @Override
    public int getCount() {
        return alYoutubeComments.size();
    }

    @Override
    public Object getItem(int position) {
        return alYoutubeComments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        final YoutubeCommentItem listViewItem = alYoutubeComments.get(position);

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_youtube_article, parent, false);
        }

        return convertView;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(CommentInfo ci) {
        YoutubeCommentItem item = new YoutubeCommentItem();
        //item.setInfo(vi);
        alYoutubeComments.add(item);
    }

    public void removeAll() {
        alYoutubeComments.clear();
    }
}


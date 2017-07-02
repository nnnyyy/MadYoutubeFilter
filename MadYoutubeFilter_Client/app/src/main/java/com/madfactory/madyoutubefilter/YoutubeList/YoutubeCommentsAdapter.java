package com.madfactory.madyoutubefilter.YoutubeList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.madfactory.madyoutubefilter.R;

import java.util.ArrayList;

public class YoutubeCommentsAdapter extends BaseAdapter {
    private ArrayList<CommentInfo> alYoutubeComments = new ArrayList<>();

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
        final CommentInfo listViewItem = alYoutubeComments.get(position);

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_comment, parent, false);
        }

        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
        tvTitle.setText(listViewItem.sComment);
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        tvName.setText(listViewItem.sAuthName);
        TextView tvDate = (TextView) convertView.findViewById(R.id.tvDate);
        tvDate.setText(listViewItem.sPDate);

        return convertView;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(CommentInfo ci) {
        alYoutubeComments.add(ci);
    }

    public void removeAll() {
        alYoutubeComments.clear();
    }
}


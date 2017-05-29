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

/**
 * Created by nnnyy on 2017-05-21.
 */

public class YoutubeListAdapter extends BaseAdapter {
    private ArrayList<YoutubeArticleItem> alYoutubeList = new ArrayList<>();

    public YoutubeListAdapter() {}

    @Override
    public int getCount() {
        return alYoutubeList.size();
    }

    @Override
    public Object getItem(int position) {
        return alYoutubeList.get(position);
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
        YoutubeArticleItem listViewItem = alYoutubeList.get(position);

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_youtube_article, parent, false);
        }

        if(listViewItem.getID().equals("admob_ads")) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_admob_ads_native, parent, false);
            NativeExpressAdView adView = (NativeExpressAdView)convertView.findViewById(R.id.adView);
            AdRequest request = new AdRequest.Builder().build();
            adView.loadAd(request);
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_youtube_article, parent, false);
            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1) ;
            final TextView titleTextView = (TextView) convertView.findViewById(R.id.tvTitle) ;
            TextView viewCntTextView = (TextView) convertView.findViewById(R.id.tvViewCnt);
            TextView durationTextView = (TextView) convertView.findViewById(R.id.tvDuration);
            final LinearLayout btnFavoriteArea = (LinearLayout)convertView.findViewById(R.id.btnFavoriteArea);
            final ImageView btnFavorite = (ImageView)convertView.findViewById(R.id.btnFavorite);
            if(GVal.isFavorated(titleTextView.getText().hashCode())) {
                btnFavorite.setImageResource(R.drawable.star);
            }
            btnFavoriteArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(GVal.isFavorated(titleTextView.getText().hashCode())) {
                        btnFavorite.setImageResource(R.drawable.star_gray);
                        GVal.removeFavorate(context, titleTextView.getText().hashCode());
                    }
                    else {
                        btnFavorite.setImageResource(R.drawable.star);
                        GVal.setFavorate(context, titleTextView.getText().hashCode());
                    }
                }
            });

            // 아이템 내 각 위젯에 데이터 반영
            Picasso.with(context).load(listViewItem.getThumbnailURL()).into(iconImageView);
            titleTextView.setText(listViewItem.getTitle());
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumIntegerDigits(10); //최대수 지정
            viewCntTextView.setText("view: " + nf.format( Integer.parseInt(listViewItem.getViewCnt()) ));
            durationTextView.setText(listViewItem.getDuration());
        }
        return convertView;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(CategoryFragment.VideoInfo vi) {
        YoutubeArticleItem item = new YoutubeArticleItem();

        item.setInfo(vi);
        alYoutubeList.add(item);
    }

    public void removeAll() {
        alYoutubeList.clear();
    }
}

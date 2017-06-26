package com.madfactory.madyoutubefilter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.madfactory.madyoutubefilter.YoutubeList.YoutubeListAdapter;

public class SearchResultActivity extends AppCompatActivity {

    YoutubeListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        ListView youtubeListView = (ListView)findViewById(R.id.lv_youtube_list);
        this.adapter = new YoutubeListAdapter(false);
        youtubeListView.setAdapter(adapter);
    }
}

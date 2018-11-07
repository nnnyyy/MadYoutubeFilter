package com.madfactory.todaysvideoplayer;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import com.madfactory.todaysvideoplayer.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBind.rvYoutubeList.setHasFixedSize( true );
        mBind.rvYoutubeList.setLayoutManager( new LinearLayoutManager(this));
        mBind.rvYoutubeList.setAdapter( new MainListAdapter() );
    }
}

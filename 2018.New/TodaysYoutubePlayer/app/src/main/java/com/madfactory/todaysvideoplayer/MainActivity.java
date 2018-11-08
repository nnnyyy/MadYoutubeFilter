package com.madfactory.todaysvideoplayer;

import android.databinding.DataBindingUtil;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.madfactory.todaysvideoplayer.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = DataBindingUtil.setContentView(this, R.layout.activity_main);

        YoutubeVideoListFragment yfrag = new YoutubeVideoListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, yfrag);
        fragmentTransaction.commit();
    }
}

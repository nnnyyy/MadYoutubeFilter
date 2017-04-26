package com.madfactory.madyoutubefilter;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager = (ViewPager)findViewById(R.id.viewpager);
        SetupViewPager(viewPager);
        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void SetupViewPager(ViewPager vp) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addItem("Sports" , new SportFragment());
        adapter.addItem("Game" , new GameFragment());
        vp.setAdapter(adapter);
    }
}

class ViewPagerAdapter extends FragmentPagerAdapter {

    private List<PagerItem> mItemList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mItemList.get(position).fragment;
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    public void addItem(String _sTitle, Fragment _fragment) {
        mItemList.add(new PagerItem(_fragment, _sTitle));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mItemList.get(position).sTitle;
    }
}


class PagerItem {
    PagerItem(Fragment _f, String _sTitle) {
        fragment = _f;
        sTitle = _sTitle;
    }
    public Fragment fragment;
    public String sTitle;
}
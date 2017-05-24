package com.madfactory.madyoutubefilter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;

import com.google.firebase.iid.FirebaseInstanceId;
import com.madfactory.madyoutubefilter.Data.GVal;
import com.madfactory.madyoutubefilter.HttpHelper.HttpHelper;
import com.madfactory.madyoutubefilter.HttpHelper.HttpHelperListener;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


public class MainActivity extends AppCompatActivity implements HttpHelperListener {

    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private HttpHelper httpHelper = new HttpHelper();
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ViewPagerAdapter adapter = (ViewPagerAdapter)msg.obj;
            viewPager.setAdapter(adapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        viewPager = (ViewPager)findViewById(R.id.viewpager);
        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        httpHelper.SetListener(this);
        httpHelper.Request(0, "http://4seasonpension.com:4000/list");
    }

    @Override
    public void onResponse(int nType, int nErrorCode, String sResponse) {
        if( !GVal.LoadCategory(sResponse) ) {
            return;
        }

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        ListIterator<GVal.MCategory> iter = GVal.GetCategories().listIterator();
        while(iter.hasNext()) {
            GVal.MCategory mc = iter.next();
            CategoryFragment cf = new CategoryFragment();
            cf.Set(mc);
            adapter.addItem(mc.sName , cf);
        }

        Message completeMsg = mHandler.obtainMessage(0, adapter);
        completeMsg.sendToTarget();
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

    @Override
    public CharSequence getPageTitle(int position) {
        return mItemList.get(position).sTitle;
    }

    public void addItem(String _sTitle, Fragment _fragment) {
        mItemList.add(new PagerItem(_fragment, _sTitle));
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
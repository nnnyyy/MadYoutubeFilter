package com.madfactory.madyoutubefilter;

import android.content.DialogInterface;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.madfactory.madyoutubefilter.AlertManager.AlertManager;
import com.madfactory.madyoutubefilter.Data.GVal;
import com.madfactory.madyoutubefilter.HttpHelper.HttpHelper;
import com.madfactory.madyoutubefilter.HttpHelper.HttpHelperListener;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


public class MainActivity extends AppCompatActivity implements HttpHelperListener {

    // Firebase Analytics
    private FirebaseAnalytics mFirebaseAnalytics;

    private AdView mAdView;

    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private HttpHelper httpHelper = new HttpHelper();
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == -1 ) {
                AlertManager.ShowOk(MainActivity.this, getString(R.string.alert_title), getString(R.string.err_network), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                return;
            }
            if(msg.what == -2) {
                AlertManager.ShowOk(MainActivity.this, getString(R.string.alert_title), getString(R.string.err_parcing), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                return;
            }

            ViewPagerAdapter adapter = (ViewPagerAdapter)msg.obj;
            viewPager.setAdapter(adapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        httpHelper.SetListener(this);
        httpHelper.Request(0, "http://4seasonpension.com:4000/list");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionbarView = inflater.inflate(R.layout.custom_actionbar, null);
        getSupportActionBar().setCustomView(actionbarView);
        Toolbar parentBar = (Toolbar)actionbarView.getParent();
        parentBar.setContentInsetsAbsolute(0,0);

        EditText etSearchText = (EditText)findViewById(R.id.etSearchText);
        etSearchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if(keyCode == event.KEYCODE_ENTER) {

                }
                return false;
            }
        });
        ImageButton btnSearch = (ImageButton)findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return true;
    }

    @Override
    public void onResponse(int nType, int nErrorCode, String sResponse) {
        if(nErrorCode != 0 ) {
            Message completeMsg = mHandler.obtainMessage(nErrorCode);
            completeMsg.sendToTarget();
            return;
        }

        if( !GVal.LoadCategory(sResponse) ) {
            Message completeMsg = mHandler.obtainMessage(-2);
            completeMsg.sendToTarget();
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

    private boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, "한번 더 누르면 종료 됩니다",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }
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
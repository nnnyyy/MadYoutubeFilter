package com.madfactory.madyoutubefilter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.madfactory.madyoutubefilter.AlertManager.AlertManager;
import com.madfactory.madyoutubefilter.Data.GVal;
import com.madfactory.madyoutubefilter.HttpHelper.HttpHelper;
import com.madfactory.madyoutubefilter.HttpHelper.HttpHelperListener;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements HttpHelperListener {

    RelativeLayout toplayout;

    // Firebase Analytics
    private FirebaseAnalytics mFirebaseAnalytics;

    private AdView mAdView;
    private EditText etSearchText;

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

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(),R.color.main));
        }

        toplayout = (RelativeLayout)findViewById(R.id.toplayout);

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        String cCode = Locale.getDefault().getCountry();
        GVal.regionCode = cCode;

        httpHelper.SetListener(this);
        httpHelper.Request(0, "http://4seasonpension.com:4000/list?regionCode=" + GVal.regionCode);
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

        etSearchText = (EditText)findViewById(R.id.etSearchText);
        etSearchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if(keyCode == event.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP ) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etSearchText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    toplayout.setFocusable(true);
                    toplayout.setFocusableInTouchMode(true);
                    Search(etSearchText.getText().toString());
                }
                return false;
            }
        });
        ImageButton btnSearch = (ImageButton)findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etSearchText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                toplayout.setFocusable(true);
                toplayout.setFocusableInTouchMode(true);
                Search(etSearchText.getText().toString());
            }
        });

        return true;
    }

    private void Search(String sSearchWord) {
        if(sSearchWord.isEmpty()) {
            // Error 처리
            return;
        }
        Intent intent = new Intent(getApplicationContext(), SearchResultActivity.class);
        intent.putExtra("SearchWord", etSearchText.getText().toString());
        etSearchText.setText("");
        startActivity(intent);
    }

    @Override
    public void onResponse(int nType, int nErrorCode, String sResponse) {
        switch (nType) {
            case 0: ListProc(nErrorCode, sResponse); break;
            case 1: SearchProc(nErrorCode, sResponse); break;
        }
    }

    private void ListProc(int nErrorCode, String sResponse) {
        if(nErrorCode != 0 ) {
            Message completeMsg = mHandler.obtainMessage(nErrorCode);
            completeMsg.sendToTarget();
            return;
        }

        GVal.loadFavorate(getApplicationContext());
        if( !GVal.LoadCategory(sResponse) ) {
            Message completeMsg = mHandler.obtainMessage(-2);
            completeMsg.sendToTarget();
            return;
        }

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        CategoryFragment cf = null;

        ListIterator<GVal.MCategory> iter = GVal.GetCategories().listIterator();
        while(iter.hasNext()) {
            GVal.MCategory mc = iter.next();
            cf = new CategoryFragment();
            cf.Set(mc);
            adapter.addItem(mc.sName , cf);
        }

        //  즐겨찾기 추가
        GVal.MCategory mcFavorate = new GVal.MCategory();
        mcFavorate.sName = "Favorate";
        mcFavorate.sType = "Favorate";
        cf = new CategoryFragment();
        cf.Set(mcFavorate);
        adapter.addItem(mcFavorate.sName , cf);

        Message completeMsg = mHandler.obtainMessage(0, adapter);
        completeMsg.sendToTarget();
    }
    private void SearchProc(int nErrorCode, String sResponse) {
        if(nErrorCode != 0) {
            Intent intent = new Intent(getApplicationContext(), SearchResultActivity.class);
            intent.putExtra("SearchRet", sResponse);
            startActivity(intent);
        }
        else {
            // Error 처리
        }
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
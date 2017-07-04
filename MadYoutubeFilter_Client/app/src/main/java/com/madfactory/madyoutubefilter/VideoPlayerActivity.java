package com.madfactory.madyoutubefilter;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.madfactory.madyoutubefilter.Data.GVal;
import com.madfactory.madyoutubefilter.HttpHelper.HttpHelper;
import com.madfactory.madyoutubefilter.HttpHelper.HttpHelperListener;
import com.madfactory.madyoutubefilter.YoutubeLib.YouTubeFailureRecoveryActivity;
import com.madfactory.madyoutubefilter.YoutubeList.CommentInfo;
import com.madfactory.madyoutubefilter.YoutubeList.YoutubeCommentsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VideoPlayerActivity extends YouTubeFailureRecoveryActivity implements HttpHelperListener, SwipeRefreshLayout.OnRefreshListener {

    private static final int PORTRAIT_ORIENTATION = Build.VERSION.SDK_INT < 9
            ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            : ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;

    private String videoID = "";
    private YouTubePlayerView playerView;
    private YouTubePlayer player;

    private boolean bLoadingNext = false;
    private HttpHelper httpHelper = new HttpHelper();
    SwipeRefreshLayout srl_youtubeList;
    private String nextToken = "";
    YoutubeCommentsAdapter adapter;
    ResultHandler descRetHandler;

    // Admob Ads
    private AdView mAdView;

    @Override
    public void onResponse(int nType, int nErrorCode, String sResponse) {
        if(nErrorCode != 0) {
            // Error
            Message msg = descRetHandler.obtainMessage(nErrorCode);
            msg.sendToTarget();
            return;
        }
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(sResponse);
            int nRet = jsonObj.getInt("ret");
            if(nRet != 0) {
                Message msg = descRetHandler.obtainMessage(-1);
                msg.sendToTarget();
                return;
            }
            if(!jsonObj.isNull("nextToken"))
                nextToken = jsonObj.getString("nextToken");

            JSONArray arrContents = jsonObj.getJSONArray("contents");
            int len = arrContents.length();
            if(len <= 0) {
                Message msg = descRetHandler.obtainMessage(-1);
                msg.sendToTarget();
                return;
            }
            for(int i = 0 ; i < len ; ++i) {
                CommentInfo ci = new CommentInfo();
                JSONObject content = arrContents.getJSONObject(i);
                ci.sComment = content.getString("comment");
                ci.sAuthName = content.getString("authname");
                ci.sLikeCnt = content.getString("likecnt");
                ci.sPDate = content.getString("pdate");
                ci.sUDate = content.getString("udate");
                adapter.addItem(ci);
            }

            Message msg = descRetHandler.obtainMessage(0);
            msg.sendToTarget();
        } catch (JSONException e) {
            e.printStackTrace();
            Message msg = descRetHandler.obtainMessage(-1);
            msg.sendToTarget();
        }
    }

    @Override
    public void onRefresh() {
        nextToken = "";
        bLoadingNext = false;
        LoadComments();
    }

    class ResultHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what != 0) {
                //Error 처리
                srl_youtubeList.setRefreshing(false);
                return;
            }

            adapter.notifyDataSetChanged();
            bLoadingNext = false;
            srl_youtubeList.setRefreshing(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-8700965243978723~7132164893");

        mAdView = (AdView) findViewById(R.id.adViewMain);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("AEA1198981C8725DFB7C153E9D1F2CFE")
                .build();  // An example device ID
        mAdView.loadAd(adRequest);

        descRetHandler = new ResultHandler();

        videoID = getIntent().getStringExtra("videoID");
        String sTitle = getIntent().getStringExtra("titleString");
        TextView tvVTitle = (TextView)findViewById(R.id.tvVTitle);
        tvVTitle.setText(sTitle);
        ImageButton btnBack = (ImageButton)findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        playerView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        playerView.initialize(GVal.ANDROID_KEY, this);

        srl_youtubeList = (SwipeRefreshLayout)findViewById(R.id.srl_youtube_list);
        srl_youtubeList.setOnRefreshListener(this);

        ListView youtubeListView = (ListView)findViewById(R.id.lv_youtube_comments);
        this.adapter = new YoutubeCommentsAdapter();
        youtubeListView.setAdapter(adapter);

        httpHelper.SetListener(this);

        LoadComments();

        youtubeListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if (!nextToken.isEmpty() && (lastInScreen == totalItemCount) && !(bLoadingNext)) {
                    LoadComments();
                }
            }
        });
    }

    private void LoadComments() {
        if(bLoadingNext) return;
        bLoadingNext = true;

        String urlRet = GVal.URL_Comments + videoID;
        if(!nextToken.isEmpty()) {
            urlRet += "&pageToken=" + nextToken;
        }
        else {
            adapter.removeAll();
        }
        urlRet += "&regionCode=" + GVal.regionCode;
        httpHelper.Request(0, urlRet);
        //Message msg = uiHandler.obtainMessage();
        //msg.sendToTarget();
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return null;
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        player = youTubePlayer;
        int controlFlags = player.getFullscreenControlFlags();
        setRequestedOrientation(PORTRAIT_ORIENTATION);
        controlFlags |= YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE;
        player.setFullscreenControlFlags(controlFlags);

        if (!wasRestored) {
            youTubePlayer.loadVideo(videoID);
            youTubePlayer.play();
        }
    }

    @Override
    protected void onResume() {
        if(mAdView != null) {
            mAdView.resume();
            mAdView.refreshDrawableState();
        }

        if(player != null) {
            player.loadVideo(videoID);
            player.play();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        if(mAdView != null) {
            mAdView.pause();
        }

        if(player != null) {
            player.release();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(mAdView != null) {
            mAdView.destroy();
        }

        if( player != null ) {
            player.release();
        }

        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if( player != null ) {
            player.release();
        }
        player = null;
        super.onStop();
    }
}

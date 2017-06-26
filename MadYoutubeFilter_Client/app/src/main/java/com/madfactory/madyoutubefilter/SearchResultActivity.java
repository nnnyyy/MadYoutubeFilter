package com.madfactory.madyoutubefilter;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.madfactory.madyoutubefilter.AlertManager.AlertManager;
import com.madfactory.madyoutubefilter.Data.GVal;
import com.madfactory.madyoutubefilter.HttpHelper.HttpHelper;
import com.madfactory.madyoutubefilter.HttpHelper.HttpHelperListener;
import com.madfactory.madyoutubefilter.YoutubeList.YoutubeListAdapter;
import com.madfactory.madyoutubefilter.common.LoadingDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ListIterator;
import java.util.Random;

public class SearchResultActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, HttpHelperListener, SwipeRefreshLayout.OnRefreshListener {

    private boolean bLoadingNext = false;
    private HttpHelper httpHelper = new HttpHelper();
    SwipeRefreshLayout srl_youtubeList;
    private String nextToken = "";
    YoutubeListAdapter adapter;
    SearchDescResultHandler descRetHandler;
    String sKey = "";

    @Override
    public void onResponse(int nType, int nErrorCode, String sResponse) {
        switch(nType) {
            case 0: procList(nErrorCode, sResponse); break;
        }
    }

    @Override
    public void onRefresh() {
        ResetLoadInfo();
        LoadList();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //  리스트 아이템 클릭 이벤트
    }

    class SearchDescResultHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what != 0) {
                ResetLoadInfo();
                srl_youtubeList.setRefreshing(false);
                AlertManager.ShowOk(getApplicationContext(), getString(R.string.alert_title), getString(R.string.err_network), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

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
        setContentView(R.layout.activity_search_result);

        sKey = getIntent().getStringExtra("SearchWord");

        descRetHandler = new SearchDescResultHandler();
        srl_youtubeList = (SwipeRefreshLayout)findViewById(R.id.srl_youtube_list);
        srl_youtubeList.setOnRefreshListener(this);

        ListView youtubeListView = (ListView)findViewById(R.id.lv_youtube_list);
        this.adapter = new YoutubeListAdapter(false);
        youtubeListView.setAdapter(adapter);

        httpHelper.SetListener(this);
        youtubeListView.setOnItemClickListener(this);
        youtubeListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if (!nextToken.isEmpty() && (lastInScreen == totalItemCount) && !(bLoadingNext)) {
                    LoadList();
                }
            }
        });

        LoadList();
    }

    private void procList(int nErrorCode, String sResponse) {
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
                Toast.makeText(getApplicationContext(),"리스트 가져오기 실패", Toast.LENGTH_SHORT).show();
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
                CategoryFragment.VideoInfo vi = new CategoryFragment.VideoInfo();
                JSONObject content = arrContents.getJSONObject(i);
                vi.id = content.getString("id");
                vi.title = content.getString("title");
                vi.thumbnailUrl = content.getString("thumnails");
                vi.channelTitle = content.getString("chtitle");
                vi.definition = content.getString("definition");
                vi.duration = content.getString("duration");
                vi.viewCnt = content.getString("viewCnt");
                adapter.addItem(vi);
            }

            Message msg = descRetHandler.obtainMessage(0);
            msg.sendToTarget();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void ResetLoadInfo() {
        nextToken = "";
        bLoadingNext = false;
    }

    private void LoadList() {
        if(bLoadingNext) return;
        bLoadingNext = true;

        String searchKey = sKey;
        try {
            String urlRet = GVal.URL_Search + URLEncoder.encode(searchKey, "utf-8") + "?contentType=search";
            if(!nextToken.isEmpty()) {
                urlRet += "&pageToken=" + nextToken;
            }
            else {
                adapter.removeAll();
            }
            urlRet += "&regionCode=" + GVal.regionCode;
            httpHelper.Request(0, urlRet);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}

package com.madfactory.madyoutubefilter;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.madfactory.madyoutubefilter.AlertManager.AlertManager;
import com.madfactory.madyoutubefilter.Data.GVal;
import com.madfactory.madyoutubefilter.HttpHelper.HttpHelper;
import com.madfactory.madyoutubefilter.HttpHelper.HttpHelperListener;
import com.madfactory.madyoutubefilter.YoutubeLib.ConvertYoutubeApiDate;
import com.madfactory.madyoutubefilter.YoutubeList.YoutubeArticleItem;
import com.madfactory.madyoutubefilter.YoutubeList.YoutubeListAdapter;
import com.madfactory.madyoutubefilter.common.LoadingDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.xml.datatype.Duration;


public class CategoryFragment extends Fragment implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, HttpHelperListener{

    public static class VideoInfo {
        public String id;
        public String title;
        public String thumbnailUrl;
        public String channelTitle;
        public String duration;
        public String definition;
        public String viewCnt;
        public String commentCnt;
    }
    private boolean bLoadingNext = false;
    private boolean bVisible = false;
    LoadingDialog loadingDlg;

    private OnFragmentInteractionListener mListener;
    private HttpHelper httpHelper = new HttpHelper();
    SwipeRefreshLayout srl_youtubeList;
    private String nextToken = "";
    private int randomAdsIndex = 5;
    YoutubeListAdapter adapter;
    DescResultHandler descRetHandler;
    private GVal.MCategory category;
    public int subCategoryIndex = 0;
    private TextView selectedSubCategoryView = null;
    private ArrayList<TextView> liSubCategories = new ArrayList<>();

    ConvertYoutubeApiDate youtubeDateConverter = new ConvertYoutubeApiDate();

    class DescResultHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what != 0) {
                ResetLoadInfo();
                srl_youtubeList.setRefreshing(false);
                HideLoadingDialog();
                if(bVisible) {
                    AlertManager.ShowOk(getContext(), getString(R.string.alert_title), getString(R.string.err_network), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                }
                return;
            }

            adapter.notifyDataSetChanged();
            bLoadingNext = false;
            srl_youtubeList.setRefreshing(false);
            HideLoadingDialog();
        }
    }

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    public void Set(GVal.MCategory mc) {
        category = mc;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ResetLoadInfo();
        descRetHandler = new DescResultHandler();

        LinearLayout llTop = (LinearLayout)view.findViewById(R.id.ll_top);
        liSubCategories.clear();
        if(category.liSubCategories.size() != 0) {
            Iterator<GVal.SubCategory> iter = category.liSubCategories.iterator();
            int subIdx = 0;
            while(iter.hasNext()) {
                GVal.SubCategory sub = iter.next();
                TextView tv = new TextView(getActivity());
                tv.setText(sub.sName);
                tv.setGravity(Gravity.CENTER);
                tv.setTag(subIdx);
                tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(bLoadingNext) return;
                        subCategoryIndex = (int)v.getTag();
                        ResetLoadInfo();
                        LoadList();
                    }
                });
                liSubCategories.add(tv);
                llTop.addView(tv);
                if(subIdx == 0) {
                    selectedSubCategoryView = tv;
                    selectedSubCategoryView.setBackgroundColor(Color.rgb(255,255,255));
                }
                subIdx++;
            }
            llTop.setVisibility(View.VISIBLE);
        }
        else {
            llTop.setVisibility(View.GONE);
        }

        srl_youtubeList = (SwipeRefreshLayout)view.findViewById(R.id.srl_youtube_list);
        srl_youtubeList.setOnRefreshListener(this);

        ListView youtubeListView = (ListView)view.findViewById(R.id.lv_youtube_list);
        boolean bFavorate = false;
        if(category.sType.equals("Favorate")) {
            bFavorate = true;
        }
        this.adapter = new YoutubeListAdapter(bFavorate);
        youtubeListView.setAdapter(adapter);

        httpHelper.SetListener(this);
        LoadList();
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
    }

    private void ResetLoadInfo() {
        nextToken = "";
        bLoadingNext = false;
        randomAdsIndex = (new Random()).nextInt(5) + 4;
    }

    private void LoadList() {
        if(bLoadingNext) return;
        bLoadingNext = true;
        if(category.sType == "Favorate") {
            // Load FavorateList
            LoadFavorateList();
            return;
        }
        String searchKey = category.sKey;
        if(category.liSubCategories.size() != 0) {
            TextView curTV = liSubCategories.get(subCategoryIndex);
            if(selectedSubCategoryView != null) {
                selectedSubCategoryView.setBackgroundColor(Color.rgb(235,235,235));
            }
            selectedSubCategoryView = curTV;
            selectedSubCategoryView.setBackgroundColor(Color.rgb(255,255,255));

            if(searchKey.isEmpty()) {
                searchKey = category.liSubCategories.get(subCategoryIndex).sKey;
            }
            else {
                searchKey += " " + category.liSubCategories.get(subCategoryIndex).sKey;
            }
        }
        try {
            String urlRet = GVal.URL_Search + URLEncoder.encode(searchKey, "utf-8") + "?contentType=" + category.getType(subCategoryIndex);
            if(!nextToken.isEmpty()) {
                urlRet += "&pageToken=" + nextToken;
            }
            else {
                adapter.removeAll();
            }
            urlRet += "&regionCode=" + GVal.regionCode;
            httpHelper.Request(0, urlRet);
            if(bVisible){
                loadingDlg = new LoadingDialog(getContext(), android.R.style.Theme_Translucent_NoTitleBar);
                loadingDlg.show();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            HideLoadingDialog();
        }
    }

    private void LoadFavorateList() {
        String sIDList = GVal.getFavorates(0);
        if(sIDList.isEmpty()) {
            bLoadingNext = false;
            return;
        }

        String urlRet = GVal.URL_FavorateListSearch + sIDList;

        if(!nextToken.isEmpty()) {
            urlRet += "&pageToken=" + nextToken;
        }
        else {
            adapter.removeAll();
        }

        httpHelper.Request(0, urlRet);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        YoutubeArticleItem item = (YoutubeArticleItem) parent.getItemAtPosition(position);
        Intent intent=new Intent(getActivity(),VideoPlayerActivity.class);
        intent.putExtra("videoID", item.getID());
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        ResetLoadInfo();
        LoadList();
    }

    @Override
    public void onResponse(int nType, int nErrorCode, String sResponse) {
        switch(nType) {
            case 0: procList(nErrorCode, sResponse); break;
        }
    }

    void procList(int nErrorCode, String sResponse) {
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
                Toast.makeText(getContext(),"리스트 가져오기 실패", Toast.LENGTH_SHORT).show();
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
                VideoInfo vi = new VideoInfo();
                JSONObject content = arrContents.getJSONObject(i);
                vi.id = content.getString("id");
                vi.title = content.getString("title");
                vi.thumbnailUrl = content.getString("thumnails");
                vi.channelTitle = content.getString("chtitle");
                vi.definition = content.getString("definition");
                vi.duration = youtubeDateConverter.Convert(content.getString("duration"));
                vi.viewCnt = content.getString("viewCnt");
                if( randomAdsIndex == adapter.getCount() -1 ) {
                    VideoInfo vi_temp = new VideoInfo();
                    vi_temp.id = "admob_ads";
                    adapter.addItem(vi_temp);
                }
                adapter.addItem(vi);
            }

            Message msg = descRetHandler.obtainMessage(0);
            msg.sendToTarget();
        } catch (JSONException e) {
            e.printStackTrace();
            HideLoadingDialog();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        bVisible = isVisibleToUser;
    }

    private void HideLoadingDialog() {
        if(loadingDlg != null)
            loadingDlg.hide();
    }
}

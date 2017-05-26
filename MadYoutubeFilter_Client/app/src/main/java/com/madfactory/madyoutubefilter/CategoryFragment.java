package com.madfactory.madyoutubefilter;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

import com.madfactory.madyoutubefilter.Data.GVal;
import com.madfactory.madyoutubefilter.HttpHelper.HttpHelper;
import com.madfactory.madyoutubefilter.HttpHelper.HttpHelperListener;
import com.madfactory.madyoutubefilter.YoutubeList.YoutubeArticleItem;
import com.madfactory.madyoutubefilter.YoutubeList.YoutubeListAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class CategoryFragment extends Fragment implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, HttpHelperListener{

    public class VideoInfo {
        public String id;
        public String title;
        public String thumbnailUrl;
        public String channelTitle;
        public String duration;
        public String definition;
        public String viewCnt;
        public String commentCnt;
    }
    private List<VideoInfo> liVideoInfoListTemp = new ArrayList<>();
    private boolean bLoadingNext = false;

    private OnFragmentInteractionListener mListener;
    private HttpHelper httpHelper = new HttpHelper();
    private String nextToken = "";
    YoutubeListAdapter adapter;
    ListResultHandler listRetHandler;
    DescResultHandler descRetHandler;
    private GVal.MCategory category;
    public int subCategoryIndex = 0;
    private TextView selectedSubCategoryView = null;

    class ListResultHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            httpHelper.Request(1, GVal.URL_Description + (String)msg.obj);
        }
    }

    class DescResultHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            liVideoInfoListTemp.clear();
            adapter.notifyDataSetChanged();
            bLoadingNext = false;
            srl_youtubeList.setRefreshing(false);
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

        listRetHandler = new ListResultHandler();
        descRetHandler = new DescResultHandler();

        LinearLayout llTop = (LinearLayout)view.findViewById(R.id.ll_top);
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
                        selectedSubCategoryView.setBackgroundColor(Color.rgb(235,235,235));
                        subCategoryIndex = (int)v.getTag();
                        ResetLoadInfo();
                        LoadList();
                        selectedSubCategoryView = (TextView)v;
                        selectedSubCategoryView.setBackgroundColor(Color.rgb(255,255,255));
                    }
                });
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
        this.adapter = new YoutubeListAdapter();
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
                if ((lastInScreen == totalItemCount) && !(bLoadingNext)) {
                    LoadList();
                }
            }
        });
    }

    private void ResetLoadInfo() {
        nextToken = "";
    }

    private void LoadList() {
        if(bLoadingNext) return;
        bLoadingNext = true;
        String searchKey = category.sKey;
        if(category.liSubCategories.size() != 0) {
            searchKey += " " + category.liSubCategories.get(subCategoryIndex).sKey;
        }
        try {
            String urlRet = GVal.URL_Search + URLEncoder.encode(searchKey, "utf-8");
            if(!nextToken.isEmpty()) {
                urlRet += "?pageToken=" + nextToken;
            }
            else {
                adapter.removeAll();
            }
            httpHelper.Request(0, urlRet);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        YoutubeArticleItem item = (YoutubeArticleItem) parent.getItemAtPosition(position);
        Intent intent=new Intent(getActivity(),VideoPlayerActivity.class);
        intent.putExtra("videoID", item.getID());
        startActivity(intent);
    }

    SwipeRefreshLayout srl_youtubeList;

    @Override
    public void onRefresh() {
        nextToken = "";
        LoadList();
    }

    @Override
    public void onResponse(int nType, int nErrorCode, String sResponse) {
        switch(nType) {
            case 0: procList(nErrorCode, sResponse); break;
            case 1: procDesc(nErrorCode, sResponse); break;
        }
    }

    void procList(int nErrorCode, String sResponse) {
        if(nErrorCode != 0) {
            // Error
            return;
        }
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(sResponse);
            nextToken = jsonObj.getString("nextToken");
            JSONArray arrContents = jsonObj.getJSONArray("contents");
            int len = arrContents.length();
            liVideoInfoListTemp.clear();
            String sIdList = "";
            for(int i = 0 ; i < len ; ++i) {
                VideoInfo vi = new VideoInfo();
                JSONObject content = arrContents.getJSONObject(i);
                vi.id = content.getString("id");
                vi.title = content.getString("title");
                vi.thumbnailUrl = content.getString("thumnails");
                vi.channelTitle = content.getString("chtitle");
                liVideoInfoListTemp.add(vi);
                sIdList += vi.id + ',';
            }

            Message msg = listRetHandler.obtainMessage(0, sIdList);
            msg.sendToTarget();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void procDesc(int nErrorCode, String sResponse) {
        if(nErrorCode != 0) {
            // Error
            return;
        }
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(sResponse);
            JSONArray arrContents = jsonObj.getJSONArray("contents");
            int len = arrContents.length();
            for(int i = 0 ; i < len ; ++i) {
                JSONObject content = arrContents.getJSONObject(i);
                String id = content.getString("id");
                for(int j = 0 ; j < liVideoInfoListTemp.size() ; ++j) {
                    if( liVideoInfoListTemp.get(j).id.equals(id)) {
                        VideoInfo vi = liVideoInfoListTemp.get(j);
                        vi.definition = content.getString("definition");
                        vi.duration = content.getString("duration");
                        vi.viewCnt = content.getString("viewCnt");
                        if( !content.isNull("commentCnt") ) {
                            vi.commentCnt = content.getString("commentCnt");
                        }
                        else {
                            vi.commentCnt = "0";
                        }
                        adapter.addItem(vi);
                        liVideoInfoListTemp.remove(j);
                        break;
                    }
                }
            }

            Message msg = descRetHandler.obtainMessage(0);
            msg.sendToTarget();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e("!!!!!", "!!!!!!!!");
    }
}

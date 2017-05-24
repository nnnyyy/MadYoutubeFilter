package com.madfactory.madyoutubefilter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;

public class CategoryFragment extends Fragment implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, HttpHelperListener{

    private OnFragmentInteractionListener mListener;
    private HttpHelper httpHelper = new HttpHelper();
    YoutubeListAdapter adapter;
    ListResultHandler listRetHandler;
    private GVal.MCategory category;

    class ListResultHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            adapter.notifyDataSetChanged();
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

        listRetHandler = new ListResultHandler();

        LinearLayout llTop = (LinearLayout)view.findViewById(R.id.ll_top);
        TextView tv = new TextView(getActivity());
        tv.setText("Test!!!!!");
        llTop.addView(tv);

        srl_youtubeList = (SwipeRefreshLayout)view.findViewById(R.id.srl_youtube_list);
        srl_youtubeList.setOnRefreshListener(this);

        ListView youtubeListView = (ListView)view.findViewById(R.id.lv_youtube_list);
        this.adapter = new YoutubeListAdapter();
        youtubeListView.setAdapter(adapter);

        httpHelper.SetListener(this);
        Log.e("Category", category.sKey);
        try {
            httpHelper.Request("http://4seasonpension.com:4000/search/" + URLEncoder.encode(category.sKey, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        youtubeListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        YoutubeArticleItem item = (YoutubeArticleItem) parent.getItemAtPosition(position);
        String titleStr = item.getTitle();
        String descStr = item.getDesc();
    }

    SwipeRefreshLayout srl_youtubeList;

    @Override
    public void onRefresh() {
        srl_youtubeList.setRefreshing(false); 
    }

    @Override
    public void onResponse(int nErrorCode, String sResponse) {
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(sResponse);
            String sNextToken = jsonObj.getString("nextToken");
            JSONArray arrContents = jsonObj.getJSONArray("contents");
            int len = arrContents.length();
            for(int i = 0 ; i < len ; ++i) {
                JSONObject content = arrContents.getJSONObject(i);
                String sID = content.getString("id");
                String sTitle = content.getString("title");
                String sThumbnails = content.getString("thumnails");
                String sChannelTitle = content.getString("chtitle");
                this.adapter.addItem(sThumbnails, sTitle, sID);
            }

            Message msg = new Message();
            msg.arg1 = 0;
            listRetHandler.sendMessage(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

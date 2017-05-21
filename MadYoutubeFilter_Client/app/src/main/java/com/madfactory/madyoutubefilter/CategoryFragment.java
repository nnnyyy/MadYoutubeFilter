package com.madfactory.madyoutubefilter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.madfactory.madyoutubefilter.Data.GVal;
import com.madfactory.madyoutubefilter.YoutubeList.YoutubeArticleItem;
import com.madfactory.madyoutubefilter.YoutubeList.YoutubeListAdapter;

public class CategoryFragment extends Fragment implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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

    private GVal.MCategory category;
    public void Set(GVal.MCategory mc) {
        category = mc;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout llTop = (LinearLayout)view.findViewById(R.id.ll_top);
        TextView tv = new TextView(getActivity());
        tv.setText("Test!!!!!");
        llTop.addView(tv);

        srl_youtubeList = (SwipeRefreshLayout)view.findViewById(R.id.srl_youtube_list);
        srl_youtubeList.setOnRefreshListener(this);

        ListView youtubeListView = (ListView)view.findViewById(R.id.lv_youtube_list);
        YoutubeListAdapter adapter = new YoutubeListAdapter();
        youtubeListView.setAdapter(adapter);
        adapter.addItem(null, "TestItem", "TestItem Description");
        adapter.addItem(null, "TestItem2", "TestItem2 Description");
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
}

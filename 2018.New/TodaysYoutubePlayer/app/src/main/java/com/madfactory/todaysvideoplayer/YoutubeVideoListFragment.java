package com.madfactory.todaysvideoplayer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class YoutubeVideoListFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView view = (RecyclerView) inflater.inflate(R.layout.youtube_video_list_fragment, container, false);

        view.setHasFixedSize( true );
        view.setLayoutManager( new LinearLayoutManager(getContext()));
        view.setAdapter( new MainListAdapter(this) );

        return view;
    }
}

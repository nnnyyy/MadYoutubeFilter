package com.madfactory.todaysvideoplayer;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.flipkart.youtubeview.YouTubePlayerView;
import com.flipkart.youtubeview.models.ImageLoader;
import com.flipkart.youtubeview.models.YouTubePlayerType;
import com.squareup.picasso.Picasso;

public class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.MyViewHolder> {
    private Fragment fragment;

    public MainListAdapter( Fragment fragment ) {
        this.fragment = fragment;
    }

    private ImageLoader imageLoader = new ImageLoader() {
        @Override
        public void loadImage(@NonNull ImageView imageView, @NonNull String url, int height, int width) {
            Picasso.get().load(url).resize(width, height).centerCrop().into(imageView);
        }
    };

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_list_holder, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //  Set Data
        YouTubePlayerView playerView = holder.youtubeView;
        playerView.initPlayer("AIzaSyAgOtMxWNk2NmaCsiBynf8O7kBty9SXPrk", "QfxAd3ZPklY", "", YouTubePlayerType.STRICT_NATIVE, null, fragment, imageLoader);
    }

    @Override
    public int getItemCount() {
        return 6;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        YouTubePlayerView youtubeView;
        // each data item is just a string in this case
        public MyViewHolder(View v) {
            super(v);
            youtubeView = v.findViewById(R.id.yplayer);
        }
    }
}

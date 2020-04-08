package com.technayak.customvideoplayer.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.technayak.customvideoplayer.R;
import com.technayak.customvideoplayer.interfaces.OnVideoPlayListner;
import com.technayak.customvideoplayer.models.VideoData;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder> {
    private ArrayList<VideoData> videoList;
    private Activity activity;
    private OnVideoPlayListner onVideoPlayListner;

    // RecyclerView recyclerView;
    public PlayListAdapter(Activity activity, ArrayList<VideoData> videoList, OnVideoPlayListner onVideoPlayListner) {
        this.videoList = videoList;
        this.activity = activity;
        this.onVideoPlayListner = onVideoPlayListner;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.playlist_view_layout, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvVideoName.setText(videoList.get(position).getName());

        String converted = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(videoList.get(position).getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(videoList.get(position).getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(videoList.get(position).getDuration()))
        );
        holder.tvVideoDuration.setText(converted);
        try {
            Glide
                    .with(activity)
                    .load( videoList.get(position).getUri())
                    .centerCrop()
                    //.placeholder(R.drawable.loading_spinner)
                    .into(holder.ivVideo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(),"click on item: "+myListData.getDescription(),Toast.LENGTH_LONG).show();
            }
        });*/
    }


    @Override
    public int getItemCount() {
        return videoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivVideo;
        public TextView tvVideoDuration, tvVideoName;

        public ViewHolder(View itemView) {
            super(itemView);
            this.ivVideo = (ImageView) itemView.findViewById(R.id.ivVideo);
            this.tvVideoDuration = (TextView) itemView.findViewById(R.id.tvVideoDuration);
            tvVideoName = (TextView) itemView.findViewById(R.id.tvVideoName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onVideoPlayListner.onVideoPlay(getAdapterPosition());
                }
            });
        }
    }
}

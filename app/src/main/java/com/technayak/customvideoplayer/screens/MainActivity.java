package com.technayak.customvideoplayer.screens;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.technayak.customvideoplayer.R;
import com.technayak.customvideoplayer.adapters.PlayListAdapter;
import com.technayak.customvideoplayer.interfaces.OnVideoPlayListner;
import com.technayak.customvideoplayer.models.VideoData;
import com.technayak.customvideoplayer.utils.VerticalSpaceItemDecoration;
import com.technayak.customvideoplayer.viewmodels.MainActivityViewModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnVideoPlayListner {

    private static final int PERMISSION_REQUEST_GALLERY = 1001;
    private static final int initialVideoIndex = 0;
    boolean fullscreen = false;
    private RecyclerView playList;
    private PlayListAdapter playListAdapter;
    private ArrayList<VideoData> videoDataList;
    private MainActivityViewModel mainActivityViewModel;
    private SimpleExoPlayer player;
    private PlayerView playerView;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private int videoIndex = -1;
    private TextView videoName;
    private ImageView ivShareVideo;
    private ImageView fullscreenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_GALLERY);
        } else {
            initView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initView();
            } else {
                Toast.makeText(MainActivity.this, "You denied the permission. Hence we can not proceed futher", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /*private void showPermissionDieniedDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("You denied the permission. Hence we can not proceed futher");
        alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finishAffinity();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.black);
        alertDialog.show();
    }*/

    private void initView() {
        playList = findViewById(R.id.playList);
        videoName = findViewById(R.id.videoName);
        playerView = findViewById(R.id.playerView);
        fullscreenButton = playerView.findViewById(R.id.exo_fullscreen_icon);
        //ivScreenOrientation = findViewById(R.id.ivScreenOrientation);
        ivShareVideo = findViewById(R.id.ivShareVideo);
        playList.setLayoutManager(new LinearLayoutManager(this));
        VerticalSpaceItemDecoration dividerItemDecoration = new VerticalSpaceItemDecoration(30);
        playList.addItemDecoration(dividerItemDecoration);

        videoDataList = new ArrayList<>();
        playListAdapter = new PlayListAdapter(this, videoDataList, this);
        playList.setAdapter(playListAdapter);

        setViewModels();
        setListners();
    }

    private void setListners() {

        ivShareVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, videoDataList.get(videoIndex).getUri());
                shareIntent.setType("video/*");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
            }
        });

        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fullscreen) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });
    }

    private void playVideo(int position) {
        cancelPlayer();
        mainActivityViewModel.setVideoPosition(position);
        MediaSource mediaSource = buildMediaSource(videoDataList.get(position).getUri());

        player = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setPlayer(player);

        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare(mediaSource, false, false);

        videoName.setText(videoDataList.get(position).getName());
        videoName.setVisibility(View.VISIBLE);
        ivShareVideo.setVisibility(View.VISIBLE);
    }

    private void playVideo() {
        if (videoIndex > -1) {
            MediaSource mediaSource = buildMediaSource(videoDataList.get(videoIndex).getUri());
            player = ExoPlayerFactory.newSimpleInstance(this);
            playerView.setPlayer(player);

            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
            player.prepare(mediaSource, false, false);

            videoName.setText(videoDataList.get(videoIndex).getName());
            videoName.setVisibility(View.VISIBLE);
            ivShareVideo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (Util.SDK_INT >= 24) {
            playVideo();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT < 24 || player == null)) {
            if (videoIndex > -1) {
                playVideo();
            }
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        if (playerView != null) {
            playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }

    private void cancelPlayer() {
        if (player != null) {
            playWhenReady = true;
            playbackPosition = 0;
            currentWindow = 0;
            player.release();
            player = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, "exoplayer-customvideo");
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
    }

    private void setViewModels() {
        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        Observer<ArrayList<VideoData>> videoListObserver = new Observer<ArrayList<VideoData>>() {
            @Override
            public void onChanged(ArrayList<VideoData> videoList) {
                videoDataList.clear();
                videoDataList.addAll(videoList);
                System.out.println("videoDataListA: " + videoDataList.size());
                playListAdapter.notifyDataSetChanged();

                if (videoIndex == -1) {
                    playVideo(initialVideoIndex);
                    mainActivityViewModel.setVideoPosition(initialVideoIndex);
                }
            }
        };
        mainActivityViewModel.getVideoDataList().observe(this, videoListObserver);

        Observer<Integer> videoPositionObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer videoPosition) {
                videoIndex = videoPosition;
            }
        };
        mainActivityViewModel.getVideoPosition().observe(this, videoPositionObserver);
    }

    @Override
    public void onVideoPlay(int position) {
        playVideo(position);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checking the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            fullscreenButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.exo_controls_fullscreen_exit));
            fullscreen = true;

            //First Hide other objects (listview or recyclerview), better hide them using Gone.
            playList.setVisibility(View.GONE);
            videoName.setVisibility(View.GONE);
            ivShareVideo.setVisibility(View.GONE);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) playerView.getLayoutParams();
            params.width = params.MATCH_PARENT;
            params.height = params.MATCH_PARENT;
            playerView.setLayoutParams(params);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            fullscreenButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.exo_controls_fullscreen_enter));
            fullscreen = false;

            //unhide your objects here.
            playList.setVisibility(View.VISIBLE);
            videoName.setVisibility(View.VISIBLE);
            ivShareVideo.setVisibility(View.VISIBLE);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) playerView.getLayoutParams();
            params.width = params.MATCH_PARENT;
            params.height = 0;
            params.matchConstraintPercentHeight = (float) 0.35;
            playerView.setLayoutParams(params);
        }
    }

    @Override
    public void onBackPressed() {
        if (fullscreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            super.onBackPressed();
        }

    }
}

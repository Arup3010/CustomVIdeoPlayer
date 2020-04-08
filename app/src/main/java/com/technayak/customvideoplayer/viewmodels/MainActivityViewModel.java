package com.technayak.customvideoplayer.viewmodels;

import android.app.Application;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.technayak.customvideoplayer.models.VideoData;
import java.util.ArrayList;

public class MainActivityViewModel extends AndroidViewModel {
    private Application application;
    private MutableLiveData<ArrayList<VideoData>> videoDataList = new MutableLiveData<>();
    private MutableLiveData<Integer> videoPosition = new MutableLiveData<>();

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        getVideoList();
    }

    private void getVideoList() {

        ArrayList<VideoData> videoList = new ArrayList<VideoData>();

        String[] projection = new String[] {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE
        };
        //String selection = MediaStore.Video.Media.DURATION +" >= ?";
        //String[] selectionArgs = new String[] {String.valueOf(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES))};
        //String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";

        try (Cursor cursor = application.getApplicationContext().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);

                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                videoList.add(new VideoData(contentUri, name, duration, size));
            }
        }


        System.out.println("videoDataListV: "+videoList.size());
        videoDataList.setValue(videoList);

        /*Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Video.VideoColumns.DATA};
        //Cursor cursor = application.getContentResolver().query(uri, projection, null, null, null);
        try {
            Cursor cursor = application.getContentResolver().query(uri, projection, null, null, null);
            ArrayList<VideoData> pathArrList = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    //pathArrList.add(cursor.getString(0));
                    pathArrList.add(new VideoData(cursor.getString(0),"Test",200,25));
                }
                cursor.close();
            }

            videoDataList.setValue(pathArrList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/

    }

    public MutableLiveData<ArrayList<VideoData>> getVideoDataList() {
        if (videoDataList == null) {
            videoDataList = new MutableLiveData<>();
            getVideoList();
        }
        return videoDataList;
    }

    public MutableLiveData<Integer> getVideoPosition() {
        return videoPosition;
    }

    public void setVideoPosition(int position) {
        videoPosition.setValue(position);
    }
}

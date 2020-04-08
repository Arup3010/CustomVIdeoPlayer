package com.technayak.customvideoplayer.models;

import android.net.Uri;

public class VideoData {
    private final Uri uri;
    private final String name;
    private final int duration;
    private final int size;

    public VideoData(Uri uri, String name, int duration, int size) {
        this.uri = uri;
        this.name = name;
        this.duration = duration;
        this.size = size;
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public int getSize() {
        return size;
    }
}

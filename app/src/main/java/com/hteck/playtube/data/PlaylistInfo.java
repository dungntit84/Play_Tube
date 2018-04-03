package com.hteck.playtube.data;

import android.support.annotation.Nullable;

import com.hteck.playtube.R;
import com.hteck.playtube.common.Utils;

import java.util.ArrayList;
import java.util.UUID;

public class PlaylistInfo {
    public UUID id = null;
    public String title;
    public String imageUrl;
    public int playlistType;
    public ArrayList<YoutubeInfo> youtubeList = new ArrayList<>();
}

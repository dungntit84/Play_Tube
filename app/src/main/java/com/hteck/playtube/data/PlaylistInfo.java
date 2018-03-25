package com.hteck.playtube.data;

import java.util.ArrayList;
import java.util.UUID;

public class    PlaylistInfo {
    public UUID id;
    public String title;
    public String imageUrl;
    public int playlistType;
    public ArrayList<YoutubeInfo> youtubeList = new ArrayList<>();
}

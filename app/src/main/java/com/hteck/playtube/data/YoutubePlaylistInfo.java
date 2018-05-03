package com.hteck.playtube.data;

import com.hteck.playtube.common.Utils;
import java.util.ArrayList;

public class YoutubePlaylistInfo {
    public String id;
	public String title;
    public String uploaderId;
    public String uploaderName;
    public int videoCount;
    public boolean isPublic;
    public String imgeUrl;
    public ArrayList<YoutubeInfo> youtubeList = new ArrayList<YoutubeInfo>();
    public boolean hasMoreVideos;
    public String getDisplayNumOfVideos() {
    	return Utils.formatNumber(videoCount, false) + (videoCount > 1 ? " videos" : " video");
    }
    public boolean isMyPlaylist;
}

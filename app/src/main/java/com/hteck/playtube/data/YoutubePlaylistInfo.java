package com.hteck.playtube.data;


import com.hteck.playtube.common.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

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
}

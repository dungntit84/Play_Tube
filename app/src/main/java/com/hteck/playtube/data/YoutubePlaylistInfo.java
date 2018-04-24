package com.venustech.playtube.info;


import java.io.Serializable;
import java.util.Vector;

import com.venustech.playtube.common.Utils;
public class PlaylistInfo {
	public String title;
    public String userId;
    public String userName;
    public int numVideos;
    public boolean isPrivate;
    public String id;
    public String thumbUrl;
    public Vector<VideoInfo> videoList = new Vector<VideoInfo>();
    public boolean hasMoreVideos;
    public String getDisplayNumOfVideos() {
    	return Utils.formatNumber(numVideos, true) + (numVideos > 1 ? " videos" : " video");
    }
}

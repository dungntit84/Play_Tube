package com.hteck.playtube.data;

import com.hteck.playtube.common.Utils;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by admin on 4/24/18.
 */

public class ChannelInfo {
    public String id;
    public String title;
    public String uploadPlaylistId;
    public String favouritePlaylistId;
    public String likePlaylistId;
    public int videoCount;
    public int subscriberCount;
    public String imageUrl;
    public String subscriptionItemId;
    public ArrayList<YoutubePlaylistInfo> playlists = new ArrayList<>();
    public ArrayList<YoutubeInfo> youtubeList = new ArrayList<>();
    public ArrayList<PlaylistItemInfo> activities = new ArrayList<PlaylistItemInfo>();
    public boolean hasMoreVideos;
    public String getDisplayVideoCount() {
        return Utils.formatNumber(videoCount, true) + (videoCount > 1 ? " videos" : " video");
    }

    public String getDisplaySubscriberCount() {
        return Utils.formatNumber(subscriberCount, false) + (videoCount > 1 ? " subscribers" : " subscriber");
    }
    public boolean isLoggedIn;
}

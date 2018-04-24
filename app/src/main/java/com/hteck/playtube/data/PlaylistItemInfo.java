package com.venustech.playtube.info;

import java.util.Vector;

import com.venustech.playtube.common.Constants.ChannelActivityType;
import com.venustech.playtube.common.Constants.YoutubePlaylistItemType;

import android.provider.MediaStore.Video;

public class PlaylistItemInfo {
	public YoutubePlaylistItemType playlistItemType;
	public Object dataInfo;
	public ChannelSectionInfo activityInfo;
	public String time;

	public Vector<VideoInfo> getVideoList() {
		Vector<VideoInfo> results = new Vector<VideoInfo>();
		try {
			if (activityInfo.activityType == ChannelActivityType.SinglePlaylist) {
				return ((PlaylistInfo) activityInfo.dataInfo).videoList;
			} else if (activityInfo.activityType == ChannelActivityType.Uploads) {
				return ((ChannelInfo) activityInfo.dataInfo).videoList;
			} else if (activityInfo.activityType == ChannelActivityType.RecentActiviy) {
				Vector<PlaylistItemInfo> items = ((ChannelInfo) activityInfo.dataInfo).channelActivities;
				for (PlaylistItemInfo item : items) {
					results.add((VideoInfo) item.dataInfo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	public Vector<PlaylistInfo> getplayLists() {
		Vector<PlaylistInfo> results = new Vector<PlaylistInfo>();
		try {
			if (activityInfo.activityType == ChannelActivityType.AllPlaylists) {
				return ((ChannelInfo) dataInfo).playlists;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}
}

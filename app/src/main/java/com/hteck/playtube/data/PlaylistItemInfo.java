package com.hteck.playtube.data;

import com.hteck.playtube.common.Constants;

import java.util.ArrayList;

public class PlaylistItemInfo {
	public int playlistItemType;
	public Object dataInfo;
	public ChannelSectionInfo activityInfo;
	public String time;

	public ArrayList<YoutubeInfo> getYoutubeList() {
		ArrayList<YoutubeInfo> results = new ArrayList<>();
		try {
			switch (activityInfo.activityType) {
				case Constants.UserActivityType.SINGLEPLAYLIST: {
					return ((YoutubePlaylistInfo) activityInfo.dataInfo).youtubeList;
				}
				case Constants.UserActivityType.UPLOADS: {
					return ((ChannelInfo) activityInfo.dataInfo).youtubeList;
				}
				case Constants.UserActivityType.RECENTACTIVIY: {
					ArrayList<PlaylistItemInfo> items = ((ChannelInfo) activityInfo.dataInfo).activities;
					for (PlaylistItemInfo item : items) {
						results.add((YoutubeInfo) item.dataInfo);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}
}

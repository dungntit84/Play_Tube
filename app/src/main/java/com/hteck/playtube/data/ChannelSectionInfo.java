package com.hteck.playtube.data;

import com.hteck.playtube.common.Constants;

public class ChannelSectionInfo implements Cloneable {
	public int activityType;
	public Object dataInfo;
	public String title;
	public int sortBy = Constants.SortBy.NONE;
	public int youtubeState = Constants.YoutubeState.QUEUE;

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}

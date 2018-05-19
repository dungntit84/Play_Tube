package com.hteck.playtube.data;

import com.hteck.playtube.common.Constants;

import java.util.UUID;

public class ChannelSectionInfo implements Cloneable {
    public UUID id;
    public int activityType;
    public Object dataInfo;
    public String title;
    public int sortBy = Constants.SortBy.MOSTRECENT;
    public int youtubeState = Constants.YoutubeState.QUEUE;

    public ChannelSectionInfo() {
        id = UUID.randomUUID();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

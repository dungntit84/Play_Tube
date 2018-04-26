package com.hteck.playtube.service;

import android.annotation.SuppressLint;

import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.data.CommentInfo;
import com.hteck.playtube.data.YoutubeInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import static com.hteck.playtube.common.Constants.ItemConstants.TITLE;
import static com.hteck.playtube.common.Constants.YoutubeField.CONTENTDETAILS;
import static com.hteck.playtube.common.Constants.YoutubeField.Channel.MEDIUM;
import static com.hteck.playtube.common.Constants.YoutubeField.Channel.RELATEDPLAYLISTS;
import static com.hteck.playtube.common.Constants.YoutubeField.Channel.SUBSCRIBERCOUNT;
import static com.hteck.playtube.common.Constants.YoutubeField.Channel.THUMBNAILS;
import static com.hteck.playtube.common.Constants.YoutubeField.Channel.UPLOADS;
import static com.hteck.playtube.common.Constants.YoutubeField.Channel.URL;
import static com.hteck.playtube.common.Constants.YoutubeField.Channel.VIDEOCOUNT;
import static com.hteck.playtube.common.Constants.YoutubeField.ID;
import static com.hteck.playtube.common.Constants.YoutubeField.SNIPPET;
import static com.hteck.playtube.common.Constants.YoutubeField.STATISTICS;
import static com.hteck.playtube.common.Utils.getString;

public class YoutubeHelper {
    public static String getSortByValue(int position) {
        switch (position) {
            case 0: {
                return "relevance";
            }
            case 1: {
                return "rating";
            }
            default: {
                return "viewCount";
            }
        }
    }

    public static String getDateQuery(int position) {
        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        int difDays;
        if (position == 0) {
            difDays = 1;
        } else if (position == 1) {
            difDays = 7;
        } else {
            difDays = 30;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -difDays);
        calendar.add(Calendar.MINUTE, calendar.getTime().getTimezoneOffset());
        Date dtQuery = calendar.getTime();
        return formatter.format(dtQuery);
    }

    public static ArrayList<YoutubeInfo> getAvailableVideos(
            ArrayList<YoutubeInfo> videoList) {
        ArrayList<YoutubeInfo> result = new ArrayList<>();
        for (YoutubeInfo y : videoList) {
            if (!y.isRemoved) {
                result.add(y);
            }
        }
        return result;
    }

    public static AbstractMap.SimpleEntry<String, ArrayList<YoutubeInfo>> getVideoListInfo(
            String data) {
        String nextPageToken = "";
        ArrayList<YoutubeInfo> videoList = new ArrayList<>();
        try {
            JSONObject jObjectData = new JSONObject(data);
            JSONArray items = jObjectData.getJSONArray(Constants.YoutubeField.ITEMS);
            for (int i = 0; i < items.length(); ++i) {
                JSONObject jObjectItem = (JSONObject) items.get(i);

                YoutubeInfo youtubeInfo = new YoutubeInfo();
                youtubeInfo.id = getString(jObjectItem, ID,
                        Constants.YoutubeField.VIDEOID);
                if (!Utils.stringIsNullOrEmpty(youtubeInfo.id)) {
                    videoList.add(youtubeInfo);
                }
            }
            if (jObjectData.has(Constants.YoutubeField.NEXTPAGETOKEN)) {
                nextPageToken = jObjectData.getString(Constants.YoutubeField.NEXTPAGETOKEN);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return new AbstractMap.SimpleEntry<>(nextPageToken,
                videoList);
    }

    public static void populateYoutubeListInfo(ArrayList<YoutubeInfo> youtubeList,
                                               String data) {
        try {
            JSONObject jObjData = new JSONObject(data);
            JSONArray items = jObjData.getJSONArray(Constants.YoutubeField.ITEMS);
            for (int i = youtubeList.size() - 1; i >= 0; --i) {
                boolean isDataReturned = false;
                for (int k = 0; k < items.length(); ++k) {
                    JSONObject jObjectItem = (JSONObject) items.get(k);
                    String id = jObjectItem.getString(ID);

                    if (youtubeList.get(i).id.equals(id)) {
                        if (populateVideoItem(youtubeList.get(i),
                                jObjectItem)) {
                            isDataReturned = true;
                        }
                        break;
                    }
                }
                if (!isDataReturned) {
                    youtubeList.remove(i);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static boolean populateVideoItem(YoutubeInfo youtubeInfo,
                                             JSONObject jObjectItem) {
        try {
            JSONObject jObjectSnippet = jObjectItem
                    .getJSONObject(SNIPPET);
            youtubeInfo.title = jObjectSnippet.getString(Constants.YoutubeField.TITLE);
            if (Utils.stringIsNullOrEmpty(youtubeInfo.title)) {
                return false;
            }

            youtubeInfo.id = jObjectItem.getString(ID);
            youtubeInfo.duration = Utils.getTimeInSeconds(getString(jObjectItem,
                    CONTENTDETAILS, Constants.YoutubeField.DURATION));

            if (jObjectItem.has(STATISTICS)) {
                JSONObject jobjStatistics = jObjectItem
                        .getJSONObject(STATISTICS);
                youtubeInfo.viewsNo = jobjStatistics
                        .getInt(Constants.YoutubeField.VIEWCOUNT);
                if (jobjStatistics.has(Constants.YoutubeField.LIKECOUNT)) {
                    youtubeInfo.likesNo = jobjStatistics
                            .getInt(Constants.YoutubeField.LIKECOUNT);
                }
                if (jobjStatistics.has(Constants.YoutubeField.DISLIKECOUNT)) {
                    youtubeInfo.dislikesNo = jobjStatistics
                            .getInt(Constants.YoutubeField.DISLIKECOUNT);
                }
            }
            youtubeInfo.imageUrl = String.format(
                    "http://i.ytimg.com/vi/%s/mqdefault.jpg", youtubeInfo.id);
            youtubeInfo.uploaderId = jObjectSnippet
                    .getString(Constants.YoutubeField.CHANNELID);
            youtubeInfo.uploaderName = jObjectSnippet
                    .getString(Constants.YoutubeField.CHANNELTITLE);
            String time = jObjectSnippet.getString(Constants.YoutubeField.PUBLISHEDAT);
            youtubeInfo.uploadedDate = Utils.getDisplayTime(time);
            youtubeInfo.isLive = "live".equalsIgnoreCase(jObjectSnippet
                    .getString(Constants.YoutubeField.LIVEBROADCASTCONTENT));
            youtubeInfo.description = jObjectSnippet
                    .getString(Constants.YoutubeField.DESCRIPTION);
            youtubeInfo.isRemoved = false;
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public static AbstractMap.SimpleEntry<String, ArrayList<CommentInfo>> getCommentList(
            String val) {
        ArrayList<CommentInfo> commentList = new ArrayList<>();
        String nextPageToken = "";
        try {

            JSONObject jObjData = new JSONObject(val);
            JSONArray items = jObjData.getJSONArray(Constants.YoutubeField.ITEMS);
            for (int i = 0; i < items.length(); ++i) {

                CommentInfo commentInfo = new CommentInfo();

                JSONObject jObjectTopLevelComment = Utils.getJSONObject(
                        (JSONObject) items.get(i), SNIPPET, Constants.YoutubeField.TOPLEVELCOMMENT);
                if (jObjectTopLevelComment != null) {

                    commentInfo.userName = getString(
                            jObjectTopLevelComment, SNIPPET,
                            Constants.YoutubeField.AUTHORDISPLAYNAME);
                    commentInfo.details = getString(jObjectTopLevelComment,
                            SNIPPET, Constants.YoutubeField.TEXTDISPLAY);
                    commentInfo.commentedDate = Utils
                            .getDisplayDateTime(getString(jObjectTopLevelComment,
                                    SNIPPET, Constants.YoutubeField.PUBLISHEDAT));

                    commentList.add(commentInfo);
                }
            }
            final String NEXTPAGETOKEN = Constants.YoutubeField.NEXTPAGETOKEN;
            if (jObjData.has(NEXTPAGETOKEN)) {
                nextPageToken = jObjData.getString(NEXTPAGETOKEN);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return new AbstractMap.SimpleEntry<>(nextPageToken,
                commentList);
    }

    public static AbstractMap.SimpleEntry<ArrayList<ChannelInfo>, String> getChannelList(
            String data) {
        ArrayList<ChannelInfo> channels = new ArrayList<ChannelInfo>();
        String pageToken = "";
        try {

            JSONObject jObjectData = new JSONObject(data);
            JSONArray items = jObjectData.getJSONArray(Constants.YoutubeField.ITEMS);
            for (int i = 0; i < items.length(); ++i) {
                ChannelInfo channelInfo = populateChannel((JSONObject) items
                        .get(i));
                if (channelInfo != null) {
                    channels.add(channelInfo);
                }
            }
            if (jObjectData.has(Constants.YoutubeField.NEXTPAGETOKEN)) {
                pageToken = jObjectData.getString(Constants.YoutubeField.NEXTPAGETOKEN);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return new AbstractMap.SimpleEntry<>(channels, pageToken);
    }

    public static ChannelInfo populateChannel(JSONObject jObject) {
        ChannelInfo result = new ChannelInfo();

        try {
            JSONObject jObjectSnippet = jObject
                    .getJSONObject(SNIPPET);
            result.title = jObjectSnippet.getString(Constants.YoutubeField.TITLE);
            result.id = jObject.getJSONObject(ID).getString(
                    Constants.YoutubeField.Channel.CHANNELID);
            result.imageUrl = getString(jObjectSnippet, THUMBNAILS,
                    MEDIUM, URL);
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            return result;
        }
    }

    public static ArrayList<ChannelInfo> getChannels(
            String data) {
        ArrayList<ChannelInfo> channels = new ArrayList<>();
        try {
            JSONObject jObjectData = new JSONObject(data);
            JSONArray items = jObjectData.getJSONArray(Constants.YoutubeField.ITEMS);
            for (int i = 0; i < items.length(); ++i) {
                JSONObject jObjectItem = (JSONObject) items.get(i);
                JSONObject jObjectSnippet = jObjectItem
                        .getJSONObject(SNIPPET);
                String id = jObjectItem.getString(ID);
                ChannelInfo channelInfo = new ChannelInfo();
                channelInfo.id = id;
                if (jObjectItem.has(STATISTICS)) {
                    JSONObject jobjStatistics = jObjectItem
                            .getJSONObject(STATISTICS);
                    channelInfo.subscriberCount = jobjStatistics
                            .getInt(SUBSCRIBERCOUNT);
                    channelInfo.videoCount = jobjStatistics
                            .getInt(VIDEOCOUNT);
                }

                channelInfo.uploadPlaylistId = getString(jObjectItem,
                        CONTENTDETAILS,
                        RELATEDPLAYLISTS,
                        UPLOADS);

                channelInfo.imageUrl = getString(jObjectSnippet,
                        THUMBNAILS, MEDIUM,
                        URL);
                channelInfo.title = jObjectSnippet
                        .getString(TITLE);
                channels.add(channelInfo);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return channels;
    }

}

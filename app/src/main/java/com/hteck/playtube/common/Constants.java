package com.hteck.playtube.common;

import com.hteck.playtube.R;

public class Constants {

    public final static String FILE_CATEGORYIES = "categories.xml";
    public final static String FILE_CONFIG = "config.xml";
    public final static String MIN_VERSION = "1";
    public static final int CUSTOM_TAG = R.mipmap.ic_launcher;
    public final static String PLAYTUBE_PREF = "playtube_Pref";
    public final static String POPULAR_TIME_LIST = "popular_time_list";
    public final static String POPULAR_SORT_BY = "popular_sort_by";
    public final static String MAIN_VIEW_ID = "main_view_id";
    public final static String PLAYLIST_DATA = "playlist_data";
    public class ItemConstants {
        public final static String VERSION = "version";
        public final static String ITEM = "item";
        public final static String TITLE = "title";
        public final static String ID = "id";
        public final static String VALUE = "value";
    }

    public enum YoutubeListType {
        Normal, Popular, Playlist, Recent
    }

    public class YoutubeField {
        public final static String COMMENT = "comment";
        public final static String FAVOURITES = "favorites";
        public final static String FAVOURITE = "favorite";
        public final static String LIKES = "likes";
        public final static String LIKE = "like";
        public final static String UPLOAD = "upload";
        public final static String PLAYLISTITEM = "playlistItem";
        public final static String RECOMMENDATION = "recommendation";
        public final static String WATCHHISTORY = "watchHistory";
        public final static String WATCHLATER = "watchLater";
        public final static String STATUS = "status";
        public final static String PRIVACYSTATUS = "privacyStatus";
        public final static String TITLE = "title";
        public final static String ID = "id";
        public final static String CONTENTDETAILS = "contentDetails";
        public final static String DURATION = "duration";
        public final static String STATISTICS = "statistics";
        public final static String VIEWCOUNT = "viewCount";
        public final static String LIVEBROADCASTCONTENT = "liveBroadcastContent";
        public final static String LIKECOUNT = "likeCount";
        public final static String DISLIKECOUNT = "dislikeCount";
        public final static String CHANNELID = "channelId";
        public final static String CHANNELTITLE = "channelTitle";
        public final static String PUBLISHEDAT = "publishedAt";
        public final static String VIDEOPUBLISHEDAT = "videoPublishedAt";
        public final static String DESCRIPTION = "description";
        public final static String ITEMS = "items";
        public final static String NEXTPAGETOKEN = "nextPageToken";
        public final static String VIDEOID = "videoId";
        public final static String SNIPPET = "snippet";
        public final static String AUTHORDISPLAYNAME = "authorDisplayName";
        public final static String TOPLEVELCOMMENT = "topLevelComment";
        public final static String TEXTDISPLAY = "textDisplay";
        public final static String HIGH = "high";
        public final static String URL = "url";
        public final static String RELATEDPLAYLISTS = "relatedPlaylists";
        public final static String SUBSCRIBERCOUNT = "subscriberCount";
        public final static String VIDEOCOUNT = "videoCount";
        public final static String UPLOADS = "uploads";
        public final static String ITEMCOUNT = "itemCount";
        public final static String PLAYLISTS = "playlists";
        public final static String PLAYLISTID = "playlistId";
        public final static String TOTALITEMCOUNT = "totalItemCount";
        public final static String RESOURCEID = "resourceId";
        public final static String TYPE = "type";
        public final static String CHANNELS = "channels";
        public final static String CHANNELITEM = "channelItem";
        public final static String BULLETIN = "bulletin";
        public final static String SUBSCRIPTION = "subscription";
        public final static String SOCIAL = "social";
    }
}

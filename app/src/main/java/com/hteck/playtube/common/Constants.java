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
    public final static String PLAYLIST_DATA = "playlist_data";
    public final static String HISTORY_DATA = "history_data";
    public final static String NO_USES_SETTING = "no_uses_setting";
    public final static int MAX_YOUYUBTE_PAGE_SIZE = 50;
    public final static int PAGE_SIZE = 20;
    public final static String PAGE_ID = "page_id";
    public final static String ACCOUNT_INFO = "account_info";
    public final static int REQUEST_CODE_LOGIN_YOUTUBE = 1;

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

    public enum RightTitleType {
        None, Category
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
        public final static String VIDEOPUBLISHEDAT = "videoPublishedAt";
        public final static String HIGH = "high";
        public final static String ITEMCOUNT = "itemCount";
        public final static String PLAYLISTID = "playlistId";
        public final static String TOTALITEMCOUNT = "totalItemCount";
        public final static String RESOURCEID = "resourceId";
        public final static String CHANNELITEM = "channelItem";
        public final static String BULLETIN = "bulletin";
        public final static String SUBSCRIPTION = "subscription";
        public final static String SOCIAL = "social";

        public final static String TITLE = "title";
        public final static String ID = "id";
        public final static String CONTENTDETAILS = "contentDetails";
        public final static String DURATION = "duration";
        public final static String STATISTICS = "statistics";
        public final static String VIEWCOUNT = "viewCount";
        public final static String VIEWCOUNT_SORTBY = "viewcount";
        public final static String DATE_SORTBY = "date";
        public final static String LIVEBROADCASTCONTENT = "liveBroadcastContent";
        public final static String LIKECOUNT = "likeCount";
        public final static String DISLIKECOUNT = "dislikeCount";
        public final static String CHANNELTITLE = "channelTitle";
        public final static String PUBLISHEDAT = "publishedAt";
        public final static String DESCRIPTION = "description";
        public final static String ITEMS = "items";
        public final static String TYPE = "type";
        public final static String NEXTPAGETOKEN = "nextPageToken";
        public final static String VIDEOID = "videoId";
        public final static String SNIPPET = "snippet";
        public final static String AUTHORDISPLAYNAME = "authorDisplayName";
        public final static String TOPLEVELCOMMENT = "topLevelComment";
        public final static String TEXTDISPLAY = "textDisplay";
        public final static String PLAYLISTS = "playlists";
        public final static String CHANNELID = "channelId";
        public final static String THUMBNAILS = "thumbnails";
        public final static String MEDIUM = "medium";
        public final static String URL = "url";
        public final static String SUBSCRIBERCOUNT = "subscriberCount";
        public final static String VIDEOCOUNT = "videoCount";
        public final static String RELATEDPLAYLISTS = "relatedPlaylists";
        public final static String UPLOADS = "uploads";
        public final static String CHANNELS = "channels";
    }

    public class PlaylistItemType {
        public final static int LIKED = 95;
        public final static int UPLOADEDANDPOSTED = 96;
        public final static int COMMENTED = 97;
        public final static int RECOMMENDED = 98;
        public final static int SUBSCRIBED = 99;
        public final static int NAME = 100;
        public final static int YOUTUBE = 101;
        public final static int PLAYLIST = 102;
        public final static int CHANNEL = 103;
        public final static int SHOWMORE = 104;
        public final static int DIVIDER = 105;
        public final static int UPLOADED = 106;
        public final static int OTHERACTION = 107;
    }

    public class UserActivityType {
        public final static int MULTIPLECHANNELS = 998;
        public final static int RECENTACTIVIY = 999;
        public final static int UPLOADS = 1000;
        public final static int FAVOURITE = 1001;
        public final static int ALLPLAYLISTS = 1002;
        public final static int SINGLEPLAYLIST = 1003;
        public final static int MULTIPLEPLAYLISTS = 1004;
    }

    public class SortBy {
        public final static int MOSTRECENT = 0;
        public final static int MOSTVIEWED = 1;
    }

    public class YoutubeState {
        public final static int QUEUE = 50;
        public final static int WAITINGFORLOADINGITEMCOUNT = 51;
        public final static int ITEMCOUNTLOADED = 52;
        public final static int LOADINGIDS = 53;
        public final static int IDSLOADED = 54;
        public final static int DONE = 55;
    }

    public class AccountViewType {
        public final static int Likes = 22;
        public final static int WhatToWatch = 25;
        public final static int Favourites = 26;
        public final static int Uploads = 23;
        public final static int WatchLater = 24;
    }
}

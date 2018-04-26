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
    public final static String HISTORY_DATA = "history_data";
    public final static String NO_USES_SETTING = "no_uses_setting";
    public final static int PAGE_SIZE = 20;

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
        public final static String DESCRIPTION = "description";
        public final static String ITEMS = "items";
        public final static String NEXTPAGETOKEN = "nextPageToken";
        public final static String VIDEOID = "videoId";
        public final static String SNIPPET = "snippet";
        public final static String AUTHORDISPLAYNAME = "authorDisplayName";
        public final static String TOPLEVELCOMMENT = "topLevelComment";
        public final static String TEXTDISPLAY = "textDisplay";

        public class Channel {
            public final static String CHANNELID = "channelId";
            public final static String THUMBNAILS = "thumbnails";
            public final static String MEDIUM = "medium";
            public final static String URL = "url";
            public final static String SUBSCRIBERCOUNT = "subscriberCount";
            public final static String VIDEOCOUNT = "videoCount";
            public final static String RELATEDPLAYLISTS = "relatedPlaylists";
            public final static String UPLOADS = "uploads";
        }
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
        public final static int NONE = 0;
        public final static int MOSTVIEWED = 1;
        public final static int MOSTRECENT = 2;
    }

    public class YoutubeState {
        public final static int QUEUE = 50;
        public final static int WAITINGFORLOADINGITEMCOUNT = 51;
        public final static int ITEMCOUNTLOADED = 52;
        public final static int LOADINGIDS = 53;
        public final static int IDSLOADED = 54;
        public final static int DONE = 55;
    }
}

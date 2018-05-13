package com.hteck.playtube.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.hteck.playtube.R;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.ChannelInfo;

import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static com.hteck.playtube.common.Constants.YoutubeField.SNIPPET;

public class YoutubeAccountService {
    public interface IYoutubeAccountService {
        void onServiceSuccess(Object userToken, Object data);

        void onServiceFailed(Object userToken, String error);
    }

    IYoutubeAccountService mListener;

    public YoutubeAccountService(IYoutubeAccountService listener) {
        mListener = listener;
    }

    public void loadYoutubeProfile() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                ChannelInfo acc = AccountHelper.getAccountInfo();
                if (acc == null) {
                    acc = AccountContext.getInstance().getAccountTempInfo();
                }
                if (acc == null) {
                    acc = new ChannelInfo();
                }
                try {
                    String profile = AccountContext.getInstance().getOAuthHelper().executeRequestApiCall(PlayTubeController.getConfigInfo().loadUserProfileUrl);
                    System.out.println("user details:" + profile);
                    ArrayList<ChannelInfo> channelList = YoutubeHelper.getChannelList(profile);
                    if (channelList.size() > 0) {
                        ChannelInfo channelInfo = channelList.get(0);
                        acc.id = channelInfo.id;
                        acc.isLoggedIn = true;
                        acc.title = channelInfo.title;
                        acc.imageUrl = channelInfo.imageUrl;
                        acc.uploadPlaylistId = channelInfo.uploadPlaylistId;
                        acc.favouritePlaylistId = channelInfo.favouritePlaylistId;
                        acc.likePlaylistId = channelInfo.likePlaylistId;
                        acc.videoCount = channelInfo.videoCount;
                        acc.subscriberCount = channelInfo.videoCount;
                        mListener.onServiceSuccess(null, acc);
                    } else {
                        mListener.onServiceSuccess(null, null);
                    }
                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (Throwable e) {
                    e.printStackTrace();
                    try {
                        if (e instanceof GoogleJsonResponseException) {
                            JSONObject jObject = new JSONObject(
                                    ((GoogleJsonResponseException) e).getContent());
                            mListener.onServiceFailed(null,
                                    jObject.getString("message"));
                        } else {
                            mListener.onServiceFailed(null, e.getMessage());
                        }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    public void loadWhatToWatch(final String nextPageToken) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String url = String.format(PlayTubeController.getConfigInfo().loadMyWhatToWatchUrl, nextPageToken);
                    String data = AccountContext.getInstance().getOAuthHelper().executeRequestApiCall(url);

                    mListener.onServiceSuccess(null, data);
                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (HttpResponseException e) {
                    mListener.onServiceSuccess(null, null);
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public void loadMyPlaylistVideos(final String playlistId,
                                     final String nextPageToken) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = String.format(PlayTubeController.getConfigInfo().loadVideosInMyPlaylistUrl, nextPageToken,
                            playlistId);
                    String data = AccountContext.getInstance().getOAuthHelper()
                            .executeRequestApiCall(url);

                    mListener.onServiceSuccess(null, data);
                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (HttpResponseException e) {
                    mListener.onServiceSuccess(null, null);
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public void loadMyChannelInfo() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String url = PlayTubeController.getConfigInfo().loadMyChannelDetailsUrl;
                    String data = AccountContext.getInstance().getOAuthHelper()
                            .executeRequestApiCall(url);

                    mListener.onServiceSuccess(null, data);
                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public void loadMySubscriptions(final String nextPageToken) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String url = String.format(PlayTubeController.getConfigInfo().loadMySubscribedChannelsUrl,
                            nextPageToken);
                    String data = AccountContext.getInstance().getOAuthHelper()
                            .executeRequestApiCall(url);

                    mListener.onServiceSuccess(null, data);
                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (HttpResponseException e) {
                    mListener.onServiceSuccess(null, null);
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public void checkChannelSubscribed(final String channelId) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String url = String
                            .format(PlayTubeController.getConfigInfo().checkChannelSubscribedUrl,
                                    channelId);
                    String data = AccountContext.getInstance().getOAuthHelper()
                            .executeRequestApiCall(url);

                    mListener.onServiceSuccess(null, data);
                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (HttpResponseException e) {
                    mListener.onServiceSuccess(null, null);
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public void removeSubscription(final String subscriptionId) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    YouTube youtube = AccountContext.getInstance().getOAuthHelper()
                            .getYoutubeService();
                    YouTube.Subscriptions.Delete playlistItemsDeleteCommand = youtube
                            .subscriptions().delete(subscriptionId);
                    playlistItemsDeleteCommand.execute();

                    mListener.onServiceSuccess(null, null);

                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public void insertSubscription(final String channelId) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    YouTube youtube = AccountContext.getInstance().getOAuthHelper()
                            .getYoutubeService();

                    // Create a resourceId that identifies the channel ID.
                    ResourceId resourceId = new ResourceId();
                    resourceId.setChannelId(channelId);
                    resourceId.setKind("youtube#channel");

                    // Create a snippet that contains the resourceId.
                    SubscriptionSnippet snippet = new SubscriptionSnippet();
                    snippet.setResourceId(resourceId);

                    Subscription subscription = new Subscription();
                    subscription.setSnippet(snippet);

                    YouTube.Subscriptions.Insert subscriptionInsertCommand = youtube
                            .subscriptions().insert("snippet,contentDetails",
                                    subscription);
                    Subscription subscriptionInserted = subscriptionInsertCommand
                            .execute();

                    ChannelInfo subscriptionInfo = YoutubeHelper.getSubscriptionInfo(subscriptionInserted);
                    mListener.onServiceSuccess(null, subscriptionInfo);

                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public void loadMyPlaylists(final String nextPageToken) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String url = String.format(PlayTubeController.getConfigInfo().loadMyPlaylistsUrl, nextPageToken);
                    String data = AccountContext.getInstance().getOAuthHelper()
                            .executeRequestApiCall(url);

                    mListener.onServiceSuccess(null, data);
                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (HttpResponseException e) {
                    mListener.onServiceSuccess(null, null);
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public void insertPlaylist(final String name, final boolean isPrivate) {

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    YouTube youtube = AccountContext.getInstance().getOAuthHelper()
                            .getYoutubeService();

                    PlaylistSnippet playlistSnippet = new PlaylistSnippet();
                    playlistSnippet.setTitle(name);

                    Playlist youTubePlaylist = new Playlist();
                    youTubePlaylist.setSnippet(playlistSnippet);
                    String status = isPrivate ? "private" : "public";

                    PlaylistStatus playlistStatus = new PlaylistStatus();
                    playlistStatus.setPrivacyStatus(status);
                    youTubePlaylist.setStatus(playlistStatus);

                    YouTube.Playlists.Insert playlistInsertCommand = youtube
                            .playlists().insert("snippet,status",
                                    youTubePlaylist);
                    Playlist playlistInserted = playlistInsertCommand.execute();

                    mListener.onServiceSuccess(null, null);
                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public void updatePlaylist(final String id, final String name,
                               final boolean isPrivate) {

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    YouTube youtube = AccountContext.getInstance().getOAuthHelper()
                            .getYoutubeService();

                    PlaylistSnippet playlistSnippet = new PlaylistSnippet();
                    playlistSnippet.setTitle(name);

                    Playlist youTubePlaylist = new Playlist();
                    youTubePlaylist.setId(id);
                    youTubePlaylist.setSnippet(playlistSnippet);
                    String status = isPrivate ? "private" : "public";

                    PlaylistStatus playlistStatus = new PlaylistStatus();
                    playlistStatus.setPrivacyStatus(status);
                    youTubePlaylist.setStatus(playlistStatus);

                    YouTube.Playlists.Update playlistUpdateCommand = youtube
                            .playlists().update("snippet,status",
                                    youTubePlaylist);
                    Playlist playlistupdated = playlistUpdateCommand.execute();

                    mListener.onServiceSuccess(null, null);
                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public void insertPlaylistItem(final String playlistId, final String videoId) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    insertVideoToPlaylist(playlistId, videoId);
                    mListener.onServiceSuccess(null, null);

                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    private void insertVideoToPlaylist(String playlistId, String videoId)
            throws IOException {
        // Define a resourceId that identifies the video being added
        // to the
        // playlist.
        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(videoId);

        // Set fields included in the playlistItem resource's
        // "snippet" part.
        PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
        playlistItemSnippet.setTitle("First video in the test playlist");
        playlistItemSnippet.setPlaylistId(playlistId);
        playlistItemSnippet.setResourceId(resourceId);

        // Create the playlistItem resource and set its snippet to
        // the
        // object created above.
        PlaylistItem playlistItem = new PlaylistItem();
        playlistItem.setSnippet(playlistItemSnippet);

        // Call the API to add the playlist item to the specified
        // playlist.
        // In the API call, the first argument identifies the
        // resource parts
        // that the API response should contain, and the second
        // argument is
        // the playlist item being inserted.
        YouTube youtube = AccountContext.getInstance().getOAuthHelper()
                .getYoutubeService();
        YouTube.PlaylistItems.Insert playlistItemsInsertCommand = youtube
                .playlistItems().insert("snippet,contentDetails", playlistItem);
        PlaylistItem returnedPlaylistItem = playlistItemsInsertCommand
                .execute();

    }

    public void removePlaylistItem(final String playlistId, final String videoId) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // Set fields included in the playlistItem resource's
                    // "snippet" part.
                    PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
                    playlistItemSnippet
                            .setTitle("First video in the test playlist");
                    playlistItemSnippet.setPlaylistId(playlistId);
                    // playlistItemSnippet.setResourceId(resourceId);

                    // Create the playlistItem resource and set its snippet to
                    // the
                    // object created above.
                    PlaylistItem playlistItem = new PlaylistItem();
                    playlistItem.setSnippet(playlistItemSnippet);

                    // Call the API to add the playlist item to the specified
                    // playlist.
                    // In the API call, the first argument identifies the
                    // resource parts
                    // that the API response should contain, and the second
                    // argument is
                    // the playlist item being inserted.
                    YouTube youtube = AccountContext.getInstance().getOAuthHelper()
                            .getYoutubeService();
                    YouTube.PlaylistItems.Delete playlistItemsDeleteCommand = youtube
                            .playlistItems().delete(videoId);
                    playlistItemsDeleteCommand.execute();

                    // Print data from the API response and return the new
                    // playlist
                    // item's unique playlistItem ID.

                    mListener.onServiceSuccess(null, null);

                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public void deletePlaylist(final String id) {

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    YouTube youtube = AccountContext.getInstance().getOAuthHelper()
                            .getYoutubeService();

                    YouTube.Playlists.Delete playlistDeleteCommand = youtube
                            .playlists().delete(id);
                    playlistDeleteCommand.execute();

                    mListener.onServiceSuccess(null, null);
                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public void checkLike(final String id, final String rating) {

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    YouTube youtube = AccountContext.getInstance().getOAuthHelper()
                            .getYoutubeService();
                    boolean isExit = false;
                    int isExisted = 0;
                    while (!isExit) {
                        VideoListResponse response = youtube.videos()
                                .list(SNIPPET).setMyRating(rating)
                                .setMaxResults((long) 50).execute();
                        List<Video> videoList = response.getItems();

                        for (Video videoInfo : videoList) {
                            if (videoInfo.getId().equals(id)) {
                                isExisted = 1;
                                break;
                            }
                        }
                        if (isExisted > 0) {
                            break;
                        }
                        isExit = Utils.stringIsNullOrEmpty(response
                                .getNextPageToken());
                    }

                    mListener.onServiceSuccess(null, isExisted);
                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public void doLike(final String id, final String rating) {

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    YouTube youtube = AccountContext.getInstance().getOAuthHelper()
                            .getYoutubeService();
                    YouTube.Videos.Rate rate = youtube.videos().rate(id, rating);
                    rate.execute();

                    mListener.onServiceSuccess(null, null);
                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public void loadData(final String url) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String data = AccountContext.getInstance().getOAuthHelper()
                            .executeRequestApiCall(url);

                    mListener.onServiceSuccess(null, data);
                } catch (UnknownHostException e) {
                    mListener.onServiceFailed(null,
                            Utils.getString(R.string.network_error));
                } catch (HttpResponseException e) {
                    mListener.onServiceSuccess(null, null);
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleThrowable(mListener, e);
                }
            }
        });
        t.start();
    }

    public static void handleThrowable(IYoutubeAccountService listener,
                                       Throwable throwable) {
        try {
            if (throwable instanceof GoogleJsonResponseException) {
                JSONObject jObject = new JSONObject(
                        ((GoogleJsonResponseException) throwable).getContent());
                listener.onServiceFailed(null,
                        jObject.getString("message"));
            } else {
                listener.onServiceFailed(null, throwable.getMessage());
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}

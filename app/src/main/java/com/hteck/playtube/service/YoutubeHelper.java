package com.hteck.playtube.service;

import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
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
        DateFormat formatter = new SimpleDateFormat(
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

    public static Vector<YoutubeInfo> getAvailableVideos(
            Vector<YoutubeInfo> videoList) {
        Vector<YoutubeInfo> result = new Vector<>();
        for (YoutubeInfo videoInfo : videoList) {
            if (!videoInfo.isRemoved) {
                result.add(videoInfo);
            }
        }
        return result;
    }

    public static AbstractMap.SimpleEntry<String, Vector<YoutubeInfo>> getVideoListInfo(
            String data) {
        String nextPageToken = "";
        Vector<YoutubeInfo> videoList = new Vector<>();
        try {
            JSONObject jObjectData = new JSONObject(data);
            JSONArray items = jObjectData.getJSONArray(Constants.YoutubeField.ITEMS);
            for (int i = 0; i < items.length(); ++i) {
                JSONObject jObjectItem = (JSONObject) items.get(i);

                YoutubeInfo videoInfo = new YoutubeInfo();
                videoInfo.id = Utils.getString(jObjectItem, Constants.YoutubeField.ID,
                        Constants.YoutubeField.VIDEOID);
                if (!Utils.stringIsNullOrEmpty(videoInfo.id)) {
                    videoList.add(videoInfo);
                }
            }
            final String NEXTPAGETOKEN = Constants.YoutubeField.NEXTPAGETOKEN;
            if (jObjectData.has(NEXTPAGETOKEN)) {
                nextPageToken = jObjectData.getString(NEXTPAGETOKEN);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return new AbstractMap.SimpleEntry<>(nextPageToken,
                videoList);
    }

    public static void populateYoutubeListInfo(Vector<YoutubeInfo> youtubeList,
                                               String data) {
        try {
            JSONObject jObjData = new JSONObject(data);
            JSONArray items = jObjData.getJSONArray(Constants.YoutubeField.ITEMS);
            for (int i = youtubeList.size() - 1; i >= 0; --i) {
                boolean isDataReturned = false;
                for (int k = 0; k < items.length(); ++k) {
                    JSONObject jObjectItem = (JSONObject) items.get(k);
                    String id = jObjectItem.getString(Constants.YoutubeField.ID);

                    if (youtubeList.elementAt(i).id.equals(id)) {
                        if (populateVideoItem(youtubeList.elementAt(i),
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

    private static boolean populateVideoItem(YoutubeInfo videoInfo,
                                             JSONObject jObjectItem) {
        try {
            JSONObject jObjectSnippet = jObjectItem
                    .getJSONObject(Constants.YoutubeField.SNIPPET);
            videoInfo.title = jObjectSnippet.getString(Constants.YoutubeField.TITLE);
            if (Utils.stringIsNullOrEmpty(videoInfo.title)) {
                return false;
            }

            videoInfo.id = jObjectItem.getString(Constants.YoutubeField.ID);
            videoInfo.duration = Utils.getTimeInSeconds(Utils.getString(jObjectItem,
                    Constants.YoutubeField.CONTENTDETAILS, Constants.YoutubeField.DURATION));

            if (jObjectItem.has(Constants.YoutubeField.STATISTICS)) {
                JSONObject jobjStatistics = jObjectItem
                        .getJSONObject(Constants.YoutubeField.STATISTICS);
                videoInfo.viewsNo = jobjStatistics
                        .getInt(Constants.YoutubeField.VIEWCOUNT);
                if (jobjStatistics.has(Constants.YoutubeField.LIKECOUNT)) {
                    videoInfo.likesNo = jobjStatistics
                            .getInt(Constants.YoutubeField.LIKECOUNT);
                }
                if (jobjStatistics.has(Constants.YoutubeField.DISLIKECOUNT)) {
                    videoInfo.dislikesNo = jobjStatistics
                            .getInt(Constants.YoutubeField.DISLIKECOUNT);
                }
            }
            videoInfo.imageUrl = String.format(
                    "http://i.ytimg.com/vi/%s/mqdefault.jpg", videoInfo.id);
            videoInfo.uploaderId = jObjectSnippet
                    .getString(Constants.YoutubeField.CHANNELID);
            videoInfo.uploaderName = jObjectSnippet
                    .getString(Constants.YoutubeField.CHANNELTITLE);
            String time = jObjectSnippet.getString(Constants.YoutubeField.PUBLISHEDAT);
            videoInfo.uploadedDate = Utils.getDisplayTime(time);
            videoInfo.isLive = "live".equalsIgnoreCase(jObjectSnippet
                    .getString(Constants.YoutubeField.LIVEBROADCASTCONTENT));
            videoInfo.description = jObjectSnippet
                    .getString(Constants.YoutubeField.DESCRIPTION);
            videoInfo.isRemoved = false;
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
						(JSONObject) items.get(i), Constants.YoutubeField.SNIPPET, Constants.YoutubeField.TOPLEVELCOMMENT);
				if (jObjectTopLevelComment != null) {

					commentInfo.userName = Utils.getString(
							jObjectTopLevelComment, Constants.YoutubeField.SNIPPET,
                            Constants.YoutubeField.AUTHORDISPLAYNAME);
					commentInfo.details = Utils.getString(jObjectTopLevelComment,
                            Constants.YoutubeField.SNIPPET, Constants.YoutubeField.TEXTDISPLAY);
					commentInfo.commentedDate = Utils
							.getDisplayDateTime(Utils.getString(jObjectTopLevelComment,
                                    Constants.YoutubeField.SNIPPET, Constants.YoutubeField.PUBLISHEDAT));

					if (commentInfo != null) {
						commentList.add(commentInfo);
					}
				}
			}
			final String NEXTPAGETOKEN = Constants.YoutubeField.NEXTPAGETOKEN;
			if (jObjData.has(NEXTPAGETOKEN)) {
				nextPageToken = jObjData.getString(NEXTPAGETOKEN);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return new AbstractMap.SimpleEntry<String, ArrayList<CommentInfo>>(nextPageToken,
				commentList);
	}

//	public static KeyPairValue<String, Vector<YoutubeInfo>> getVideosInPlaylist(
//			String data, int maxCount) {
//		Vector<YoutubeInfo> clipList = new Vector<YoutubeInfo>();
//		String nextPageToken = "";
//		try {
//
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = 0; i < items.length(); ++i) {
//
//				YoutubeInfo videoInfo = new YoutubeInfo();
//
//				videoInfo.playlistItemId = ((JSONObject) items.get(i))
//						.getString(YoutubeField.ID);
//				videoInfo.id = getString((JSONObject) items.get(i),
//						YoutubeField.CONTENTDETAILS, YoutubeField.VIDEOID);
//				String publishedAt = getString((JSONObject) items.get(i),
//						YoutubeField.CONTENTDETAILS, YoutubeField.VIDEOPUBLISHEDAT);
//				if (!Utils.isNullOrEmpty(videoInfo.id) && !Utils.isNullOrEmpty(publishedAt)) {
//					if (maxCount != 0 && clipList.size() >= maxCount) {
//						break;
//					}
//					clipList.add(videoInfo);
//				}
//			}
//			final String NEXTPAGETOKEN = YoutubeField.NEXTPAGETOKEN;
//			if (jObjectData.has(NEXTPAGETOKEN)) {
//				nextPageToken = jObjectData.getString(NEXTPAGETOKEN);
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//
//		return new KeyPairValue<String, Vector<YoutubeInfo>>(nextPageToken,
//				clipList);
//	}
//
//	public static KeyPairValue<String, Vector<YoutubeInfo>> getVideos(String data) {
//		Vector<YoutubeInfo> clipList = new Vector<YoutubeInfo>();
//		String nextPageToken = "";
//		try {
//
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = 0; i < items.length(); ++i) {
//
//				YoutubeInfo videoInfo = new YoutubeInfo();
//
//				videoInfo.id = getString((JSONObject) items.get(i),
//						YoutubeField.ID, YoutubeField.VIDEOID);
//				if (!Utils.isNullOrEmpty(videoInfo.id)) {
//					clipList.add(videoInfo);
//				}
//			}
//			final String NEXTPAGETOKEN = YoutubeField.NEXTPAGETOKEN;
//			if (jObjectData.has(NEXTPAGETOKEN)) {
//				nextPageToken = jObjectData.getString(NEXTPAGETOKEN);
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//
//		return new KeyPairValue<String, Vector<YoutubeInfo>>(nextPageToken,
//				clipList);
//	}
//
//	public static KeyPairValue<String, Vector<YoutubeInfo>> getVideosInAccount(
//			String data) {
//		Vector<YoutubeInfo> clipList = new Vector<YoutubeInfo>();
//		String nextPageToken = "";
//		try {
//
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = 0; i < items.length(); ++i) {
//				try {
//					YoutubeInfo videoInfo = new YoutubeInfo();
//					JSONObject jObjectContentDetails = ((JSONObject) items
//							.get(i)).getJSONObject(YoutubeField.CONTENTDETAILS);
//
//					JSONObject objResourceDetails = getResourceDetails(jObjectContentDetails);
//					if (objResourceDetails != null) {
//						if (!objResourceDetails.isNull(YoutubeField.VIDEOID)) {
//							videoInfo.id = objResourceDetails
//									.getString(YoutubeField.VIDEOID);
//							clipList.add(videoInfo);
//						} else if (!objResourceDetails
//								.isNull(YoutubeField.RESOURCEID)) {
//							videoInfo.id = objResourceDetails.getJSONObject(
//									YoutubeField.RESOURCEID).getString(
//									YoutubeField.VIDEOID);
//							clipList.add(videoInfo);
//						}
//					}
//
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
//
//			}
//
//			final String NEXTPAGETOKEN = YoutubeField.NEXTPAGETOKEN;
//			if (jObjectData.has(NEXTPAGETOKEN)) {
//				nextPageToken = jObjectData.getString(NEXTPAGETOKEN);
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//
//		return new KeyPairValue<String, Vector<YoutubeInfo>>(nextPageToken,
//				clipList);
//	}
//
//	public static Vector<YoutubeInfo> getVideoList(String data) {
//		Vector<YoutubeInfo> result = new Vector<YoutubeInfo>();
//		try {
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = 0; i < items.length(); ++i) {
//				JSONObject jObjectItem = (JSONObject) items.get(i);
//
//				YoutubeInfo videoInfo = new YoutubeInfo();
//				if (populateVideoItem(videoInfo, jObjectItem)) {
//					result.add(videoInfo);
//				}
//			}
//
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		return result;
//	}
//

//	public static int convertToSeconds(String duration) {
//		try {
//			duration = duration.replaceAll("[PT]", "");
//			if (!duration.contains("H")) {
//				duration = "0H" + duration;
//			}
//			if (!duration.contains("M")) {
//				int indexOfH = duration.indexOf('H');
//				duration = duration.substring(0, indexOfH + 1) + "0M"
//						+ duration.substring(indexOfH + 1);
//			}
//
//			if (!duration.contains("S")) {
//				duration = duration + "0S";
//			}
//			duration = duration.replaceAll("[HMS]", " ");
//			String[] elements = duration.trim().split("[ ]");
//			if (elements.length == 1) {
//				return Utils.parseInt(elements[0]);
//			}
//			if (elements.length == 2) {
//				return Utils.parseInt(elements[0]) * 60
//						+ Utils.parseInt(elements[1]);
//			}
//			if (elements.length == 3) {
//				return Utils.parseInt(elements[0]) * 3600
//						+ Utils.parseInt(elements[1]) * 60
//						+ Utils.parseInt(elements[2]);
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		return 0;
//	}
//
//	public static void populateVideosInfo(Vector<YoutubeInfo> clipList,
//			String data) {
//		try {
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = clipList.size() - 1; i >= 0; --i) {
//				boolean isDataReturned = false;
//				for (int k = 0; k < items.length(); ++k) {
//					JSONObject jObjectItem = (JSONObject) items.get(k);
//					String id = jObjectItem.getString(YoutubeField.ID);
//
//					if (clipList.elementAt(i).id.equals(id)) {
//						if (populateVideoItem(clipList.elementAt(i),
//								jObjectItem)) {
//							isDataReturned = true;
//						}
//						break;
//					}
//				}
//				if (!isDataReturned) {
//					clipList.remove(i);
//				}
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static String getDisplayTime(String time) {
//		DateFormat formatter = new SimpleDateFormat(
//				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//		try {
//			Date dt = formatter.parse(time);
//
//			Calendar calendar = Calendar.getInstance();
//			calendar.add(Calendar.MINUTE, calendar.getTime()
//					.getTimezoneOffset());
//			Date dtNow = calendar.getTime();
//			int diffInHour = (int) ((dtNow.getTime() - dt.getTime()) / (1000 * 60 * 60));
//
//			if (diffInHour < 24) {
//				return String.format("%s %s ago", diffInHour,
//						diffInHour > 1 ? "hours" : "hour");
//			}
//			int days = diffInHour / 24;
//			if (days < 30) {
//				return String.format("%s %s ago", days, days > 1 ? "days"
//						: "day");
//			}
//			int months = days / 30;
//			if (months < 12) {
//				return String.format("%s %s ago", months, months > 1 ? "months"
//						: "month");
//			}
//
//			int years = months / 12;
//			return String.format("%s %s ago", years, years > 1 ? "years"
//					: "year");
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//		return "";
//	}
//
//
//	public static KeyPairValue<String, Vector<ChannelInfo>> getChannels(
//			String data) {
//		Vector<ChannelInfo> channels = new Vector<ChannelInfo>();
//		String nextPageToken = "";
//		try {
//
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = 0; i < items.length(); ++i) {
//				ChannelInfo channelInfo = populateChannel((JSONObject) items
//						.get(i));
//				if (channelInfo != null) {
//					channels.add(channelInfo);
//				}
//			}
//			final String NEXTPAGETOKEN = YoutubeField.NEXTPAGETOKEN;
//			if (jObjectData.has(NEXTPAGETOKEN)) {
//				nextPageToken = jObjectData.getString(NEXTPAGETOKEN);
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//
//		return new KeyPairValue<String, Vector<ChannelInfo>>(nextPageToken,
//				channels);
//	}
//
//	public static ChannelInfo populateChannel(JSONObject jObject) {
//		ChannelInfo result = new ChannelInfo();
//
//		try {
//			JSONObject jObjectSnippet = jObject
//					.getJSONObject(YoutubeField.SNIPPET);
//			result.title = jObjectSnippet.getString(YoutubeField.TITLE);
//			result.id = jObject.getJSONObject(YoutubeField.ID).getString(
//					YoutubeField.CHANNELID);
//			result.imageUrl = getString(jObjectSnippet, YoutubeField.THUMBNAILS,
//					YoutubeField.MEDIUM, YoutubeField.URL);
//			return result;
//		} catch (Throwable e) {
//			e.printStackTrace();
//			return result;
//		}
//	}
//
//	public static void populateChannelsInfo(Vector<ChannelInfo> channels,
//			String data) {
//		try {
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = 0; i < items.length(); ++i) {
//				JSONObject jObjectItem = (JSONObject) items.get(i);
//				JSONObject jObjectSnippet = jObjectItem
//						.getJSONObject(YoutubeField.SNIPPET);
//				String id = jObjectItem.getString(YoutubeField.ID);
//				for (ChannelInfo channelInfo : channels) {
//					if (channelInfo.id.equals(id)) {
//						if (jObjectItem.has(YoutubeField.STATISTICS)) {
//							JSONObject jobjStatistics = jObjectItem
//									.getJSONObject(YoutubeField.STATISTICS);
//							channelInfo.numSubscribers = jobjStatistics
//									.getInt(YoutubeField.SUBSCRIBERCOUNT);
//							channelInfo.numVideos = jobjStatistics
//									.getInt(YoutubeField.VIDEOCOUNT);
//						}
//
//						channelInfo.uploadPlaylistId = getString(jObjectItem,
//								YoutubeField.CONTENTDETAILS,
//								YoutubeField.RELATEDPLAYLISTS,
//								YoutubeField.UPLOADS);
//
//						channelInfo.imageUrl = getString(jObjectSnippet,
//								YoutubeField.THUMBNAILS, YoutubeField.MEDIUM,
//								YoutubeField.URL);
//						channelInfo.title = jObjectSnippet
//								.getString(YoutubeField.TITLE);
//						break;
//					}
//				}
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static Vector<ChannelInfo> populateChannelsInfo(String data) {
//		Vector<ChannelInfo> channels = new Vector<ChannelInfo>();
//		try {
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = 0; i < items.length(); ++i) {
//				ChannelInfo channelInfo = new ChannelInfo();
//				JSONObject jObjectItem = (JSONObject) items.get(i);
//				String id = jObjectItem.getString(YoutubeField.ID);
//				JSONObject jObjectSnippet = jObjectItem
//						.getJSONObject(YoutubeField.SNIPPET);
//				channelInfo.title = jObjectSnippet.getString(YoutubeField.TITLE);
//				channelInfo.id = id;
//				if (jObjectItem.has(YoutubeField.STATISTICS)) {
//					JSONObject jobjStatistics = jObjectItem
//							.getJSONObject(YoutubeField.STATISTICS);
//					channelInfo.numSubscribers = jobjStatistics
//							.getInt(YoutubeField.SUBSCRIBERCOUNT);
//					channelInfo.numVideos = jobjStatistics
//							.getInt(YoutubeField.VIDEOCOUNT);
//				}
//
//				channelInfo.uploadPlaylistId = getString(jObjectItem,
//						YoutubeField.CONTENTDETAILS,
//						YoutubeField.RELATEDPLAYLISTS, YoutubeField.UPLOADS);
//
//				channelInfo.likePlaylistId = getString(jObjectItem,
//						YoutubeField.CONTENTDETAILS,
//						YoutubeField.RELATEDPLAYLISTS, YoutubeField.LIKES);
//
//				channelInfo.imageUrl = getString(jObjectSnippet,
//						YoutubeField.THUMBNAILS, YoutubeField.MEDIUM,
//						YoutubeField.URL);
//				channels.add(channelInfo);
//
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		return channels;
//	}
//
//	public static Vector<ChannelInfo> getChannelList(String data, boolean isMine) {
//		Vector<ChannelInfo> result = new Vector<ChannelInfo>();
//		try {
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = 0; i < items.length(); ++i) {
//				JSONObject jObjectItem = (JSONObject) items.get(i);
//				String id = jObjectItem.getString(YoutubeField.ID);
//				ChannelInfo channelInfo = new ChannelInfo();
//				channelInfo.id = id;
//				if (jObjectItem.has(YoutubeField.STATISTICS)) {
//					JSONObject jobjStatistics = jObjectItem
//							.getJSONObject(YoutubeField.STATISTICS);
//					channelInfo.numSubscribers = jobjStatistics
//							.getInt(YoutubeField.SUBSCRIBERCOUNT);
//					channelInfo.numVideos = jobjStatistics
//							.getInt(YoutubeField.VIDEOCOUNT);
//				}
//
//				channelInfo.uploadPlaylistId = getString(jObjectItem,
//						YoutubeField.CONTENTDETAILS,
//						YoutubeField.RELATEDPLAYLISTS, YoutubeField.UPLOADS);
//
//				channelInfo.likePlaylistId = getString(jObjectItem,
//						YoutubeField.CONTENTDETAILS,
//						YoutubeField.RELATEDPLAYLISTS, YoutubeField.LIKES);
//
//				channelInfo.imageUrl = getString(jObjectItem,
//						YoutubeField.SNIPPET, YoutubeField.THUMBNAILS,
//						YoutubeField.MEDIUM, YoutubeField.URL);
//
//				channelInfo.title = getString(jObjectItem, YoutubeField.SNIPPET,
//						YoutubeField.TITLE);
//				if (isMine) {
//					channelInfo.watchHistoryPlaylistId = getString(jObjectItem,
//							YoutubeField.CONTENTDETAILS,
//							YoutubeField.RELATEDPLAYLISTS,
//							YoutubeField.WATCHHISTORY);
//					channelInfo.watchLaterPlaylistId = getString(jObjectItem,
//							YoutubeField.CONTENTDETAILS,
//							YoutubeField.RELATEDPLAYLISTS,
//							YoutubeField.WATCHLATER);
//					channelInfo.favouritesPlaylistId = getString(jObjectItem,
//							YoutubeField.CONTENTDETAILS,
//							YoutubeField.RELATEDPLAYLISTS,
//							YoutubeField.FAVOURITES);
//				}
//				result.add(channelInfo);
//
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		return result;
//	}
//
//	public static ChannelInfo PopulateSubscription(JSONObject jObject) {
//		ChannelInfo result = new ChannelInfo();
//
//		try {
//			JSONObject jObjectSnippet = jObject
//					.getJSONObject(YoutubeField.SNIPPET);
//			result.title = jObjectSnippet.getString(YoutubeField.TITLE);
//			result.subscriptionId = jObject.getString(YoutubeField.ID);
//			result.id = getString(jObjectSnippet, YoutubeField.RESOURCEID,
//					YoutubeField.CHANNELID);
//			result.imageUrl = getString(jObjectSnippet, YoutubeField.THUMBNAILS,
//					YoutubeField.HIGH, YoutubeField.URL);
//			result.numVideos = getInt(jObject, YoutubeField.CONTENTDETAILS,
//					YoutubeField.TOTALITEMCOUNT);
//			return result;
//		} catch (Throwable e) {
//			e.printStackTrace();
//			return result;
//		}
//	}
//
//	public static ChannelInfo PopulateSubscription(Subscription subscription) {
//		ChannelInfo result = new ChannelInfo();
//
//		try {
//			SubscriptionSnippet jObjectSnippet = subscription.getSnippet();
//			result.title = jObjectSnippet.getTitle();
//			result.subscriptionId = subscription.getId();
//			result.id = jObjectSnippet.getChannelId();
//			if (jObjectSnippet.getThumbnails().getHigh() != null) {
//				result.imageUrl = jObjectSnippet.getThumbnails().getHigh()
//						.getUrl();
//			} else {
//				result.imageUrl = jObjectSnippet.getThumbnails().getDefault()
//						.getUrl();
//			}
//			result.numVideos = subscription.getContentDetails()
//					.getTotalItemCount().intValue();
//			return result;
//		} catch (Throwable e) {
//			e.printStackTrace();
//			return result;
//		}
//	}
//
//	public static KeyPairValue<String, Vector<PlaylistInfo>> getPlaylists(
//			String data, boolean isCustomPlaylist, boolean isMine) {
//		Vector<PlaylistInfo> playlists = new Vector<PlaylistInfo>();
//		String nextPageToken = "";
//		try {
//
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = 0; i < items.length(); ++i) {
//				PlaylistInfo playlistInfo = isCustomPlaylist ? populateCustomPlaylist(
//						(JSONObject) items.get(i), isMine)
//						: populatePlaylist((JSONObject) items.get(i));
//				if (playlistInfo != null) {
//					playlists.add(playlistInfo);
//				}
//			}
//			final String NEXTPAGETOKEN = YoutubeField.NEXTPAGETOKEN;
//			if (jObjectData.has(NEXTPAGETOKEN)) {
//				nextPageToken = jObjectData.getString(NEXTPAGETOKEN);
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//
//		return new KeyPairValue<String, Vector<PlaylistInfo>>(nextPageToken,
//				playlists);
//	}
//
//	public static Vector<PlaylistInfo> getPlaylists(String data,
//			boolean isFullData) {
//		Vector<PlaylistInfo> playlists = new Vector<PlaylistInfo>();
//		try {
//
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = 0; i < items.length(); ++i) {
//				PlaylistInfo playlistInfo = isFullData ? populateCustomPlaylist(
//						(JSONObject) items.get(i), false)
//						: populatePlaylistInChannelSection((JSONObject) items
//								.get(i));
//				if (playlistInfo != null) {
//					playlists.add(playlistInfo);
//				}
//			}
//
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//
//		return playlists;
//	}
//
//	public static PlaylistInfo populatePlaylistInChannelSection(
//			JSONObject jObject) {
//		PlaylistInfo result = new PlaylistInfo();
//
//		try {
//			JSONObject jObjectContentDetails = jObject
//					.getJSONObject(YoutubeField.CONTENTDETAILS);
//			JSONArray jArray = jObjectContentDetails
//					.getJSONArray(YoutubeField.PLAYLISTS);
//			if (jArray.length() > 0) {
//				result.id = jArray.get(0).toString();
//			}
//			return result;
//		} catch (Throwable e) {
//			e.printStackTrace();
//			return result;
//		}
//	}
//
//	public static PlaylistInfo populateCustomPlaylist(JSONObject jObject,
//			boolean isMine) {
//		PlaylistInfo result = new PlaylistInfo();
//
//		try {
//			JSONObject jObjectSnippet = jObject
//					.getJSONObject(YoutubeField.SNIPPET);
//			result.title = jObjectSnippet.getString(YoutubeField.TITLE);
//			result.id = jObject.getString(YoutubeField.ID);
//			result.imageUrl = getString(jObjectSnippet, YoutubeField.THUMBNAILS,
//					YoutubeField.MEDIUM, YoutubeField.URL);
//			result.numVideos = getInt(jObject, YoutubeField.CONTENTDETAILS,
//					YoutubeField.ITEMCOUNT);
//			if (isMine) {
//				String status = getString(jObject, YoutubeField.STATUS,
//						YoutubeField.PRIVACYSTATUS);
//				result.isPrivate = status.equalsIgnoreCase("private");
//			}
//			return result;
//		} catch (Throwable e) {
//			e.printStackTrace();
//			return result;
//		}
//	}
//
//	public static PlaylistInfo populatePlaylist(JSONObject jObject) {
//		PlaylistInfo result = new PlaylistInfo();
//
//		try {
//			JSONObject jObjectSnippet = jObject
//					.getJSONObject(YoutubeField.SNIPPET);
//			result.title = jObjectSnippet.getString(YoutubeField.TITLE);
//			result.id = getString(jObject, YoutubeField.ID,
//					YoutubeField.PLAYLISTID);
//			result.imageUrl = getString(jObjectSnippet, YoutubeField.THUMBNAILS,
//					YoutubeField.MEDIUM, YoutubeField.URL);
//			return result;
//		} catch (Throwable e) {
//			e.printStackTrace();
//			return result;
//		}
//	}
//
//	public static void populatePlaylistsInfo(Vector<PlaylistInfo> playlists,
//			String data) {
//		try {
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = 0; i < items.length(); ++i) {
//				JSONObject jObjectItem = (JSONObject) items.get(i);
//				String id = jObjectItem.getString(YoutubeField.ID);
//				for (PlaylistInfo playlistInfo : playlists) {
//					if (playlistInfo.id.equals(id)) {
//						playlistInfo.numVideos = getInt(jObjectItem,
//								YoutubeField.CONTENTDETAILS,
//								YoutubeField.ITEMCOUNT);
//						playlistInfo.title = getString(jObjectItem,
//								YoutubeField.SNIPPET, YoutubeField.TITLE);
//						playlistInfo.imageUrl = getString(jObjectItem,
//								YoutubeField.SNIPPET, YoutubeField.THUMBNAILS,
//								YoutubeField.MEDIUM, YoutubeField.URL);
//						break;
//					}
//				}
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//	}
//

//	public static Vector<ChannelSectionInfo> getActivityInfos(String data) {
//		Vector<ChannelSectionInfo> results = new Vector<ChannelSectionInfo>();
//		try {
//
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = 0; i < items.length(); ++i) {
//				JSONObject jObjectItem = (JSONObject) items.get(i);
//				JSONObject jObjectSnippet = ((JSONObject) jObjectItem
//						.getJSONObject(YoutubeField.SNIPPET));
//				String type = getString(jObjectSnippet, YoutubeField.TYPE);
//				if (isValidForMark(type,
//						MainContext.getDevKeyInfo().channelSectionPlaylistMark)) {
//					PlaylistInfo playlistInfo = new PlaylistInfo();
//					JSONObject jObjectContentDetails = jObjectItem
//							.getJSONObject(YoutubeField.CONTENTDETAILS);
//					JSONArray jArray = jObjectContentDetails
//							.getJSONArray(YoutubeField.PLAYLISTS);
//					if (jArray.length() > 0) {
//						playlistInfo.id = jArray.get(0).toString();
//						ChannelSectionInfo channelSectionInfo = new ChannelSectionInfo();
//						channelSectionInfo.activityType = ChannelActivityType.SinglePlaylist;
//						channelSectionInfo.dataInfo = playlistInfo;
//						channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.pendingForLoadingItemCount;
//						results.add(channelSectionInfo);
//					}
//				} else if (isValidForMark(type,
//						MainContext.getDevKeyInfo().channelSectionLikesMark)) {
//					ChannelInfo channelInfo = new ChannelInfo();
//					String channelId = jObjectSnippet
//							.getString(YoutubeField.CHANNELID);
//					channelInfo.id = channelId;
//
//					ChannelSectionInfo channelSectionInfo = new ChannelSectionInfo();
//					channelSectionInfo.activityType = ChannelActivityType.Likes;
//					channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.NotStarted;
//					channelSectionInfo.dataInfo = channelInfo;
//
//					results.add(channelSectionInfo);
//				} else if (isValidForMark(type,
//						MainContext.getDevKeyInfo().channelSectionChannelMark)) {
//					ChannelInfo channelInfo = new ChannelInfo();
//					String channelId = jObjectSnippet
//							.getString(YoutubeField.CHANNELID);
//					channelInfo.id = channelId;
//
//					ChannelSectionInfo channelSectionInfo = new ChannelSectionInfo();
//					channelSectionInfo.activityType = ChannelActivityType.Uploads;
//					channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.pendingForLoadingItemCount;
//					channelSectionInfo.dataInfo = channelInfo;
//
//					if (type.equals("recentUploads")) {
//						channelSectionInfo.sortBy = SortBy.MostRecent;
//					} else if (type.equals("popularUploads")) {
//						channelSectionInfo.sortBy = SortBy.MostViewed;
//					}
//
//					results.add(channelSectionInfo);
//				} else if (isValidForMark(
//						type,
//						MainContext.getDevKeyInfo().channelSectionRecentActivityMark)) {
//					ChannelInfo channelInfo = new ChannelInfo();
//					String channelId = jObjectSnippet
//							.getString(YoutubeField.CHANNELID);
//					channelInfo.id = channelId;
//
//					ChannelSectionInfo channelSectionInfo = new ChannelSectionInfo();
//					channelSectionInfo.activityType = ChannelActivityType.RecentActiviy;
//					channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.pendingForLoadingItemCount;
//					channelSectionInfo.dataInfo = channelInfo;
//
//					results.add(channelSectionInfo);
//				} else if (isValidForMark(
//						type,
//						MainContext.getDevKeyInfo().channelSectionAllPlaylistsMark)) {
//					ChannelInfo channelInfo = new ChannelInfo();
//					String channelId = jObjectSnippet
//							.getString(YoutubeField.CHANNELID);
//					channelInfo.id = channelId;
//
//					ChannelSectionInfo channelSectionInfo = new ChannelSectionInfo();
//					channelSectionInfo.activityType = ChannelActivityType.AllPlaylists;
//					channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.pendingForLoadingItemCount;
//					channelSectionInfo.dataInfo = channelInfo;
//
//					results.add(channelSectionInfo);
//				}
//
//				else if (isValidForMark(
//						type,
//						MainContext.getDevKeyInfo().channelSectionMultiPlaylistsMark)) {
//					Vector<PlaylistInfo> playlists = new Vector<PlaylistInfo>();
//					JSONObject jObjectContentDetails = jObjectItem
//							.getJSONObject(YoutubeField.CONTENTDETAILS);
//					JSONArray jArray = jObjectContentDetails
//							.getJSONArray(YoutubeField.PLAYLISTS);
//					for (int k = 0; k < jArray.length(); ++k) {
//						PlaylistInfo playlistInfo = new PlaylistInfo();
//						playlistInfo.id = jArray.get(k).toString();
//
//						playlists.add(playlistInfo);
//					}
//					ChannelSectionInfo channelSectionInfo = new ChannelSectionInfo();
//					channelSectionInfo.activityType = ChannelActivityType.MultiplePlaylists;
//					channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.loadedItemCount;
//					channelSectionInfo.dataInfo = playlists;
//					channelSectionInfo.title = jObjectSnippet
//							.getString(YoutubeField.TITLE);
//					results.add(channelSectionInfo);
//				} else if (isValidForMark(
//						type,
//						MainContext.getDevKeyInfo().channelSectionMultiChannelsMark)) {
//					Vector<ChannelInfo> channels = new Vector<ChannelInfo>();
//					JSONObject jObjectContentDetails = jObjectItem
//							.getJSONObject(YoutubeField.CONTENTDETAILS);
//					JSONArray jArray = jObjectContentDetails
//							.getJSONArray(YoutubeField.CHANNELS);
//					for (int k = 0; k < jArray.length(); ++k) {
//						ChannelInfo channelInfo = new ChannelInfo();
//						channelInfo.id = jArray.get(k).toString();
//
//						channels.add(channelInfo);
//					}
//					ChannelSectionInfo channelSectionInfo = new ChannelSectionInfo();
//					channelSectionInfo.activityType = ChannelActivityType.MultipleChannels;
//					channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.loadedItemCount;
//					channelSectionInfo.dataInfo = channels;
//					channelSectionInfo.title = jObjectSnippet
//							.getString(YoutubeField.TITLE);
//					results.add(channelSectionInfo);
//				}
//			}
//
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//
//		return results;
//	}
//
//	private static boolean isValidForMark(String val, String mark) {
//		try {
//			String[] elements = mark.split("[,]");
//			for (String item : elements) {
//				if (item.equals(val)) {
//					return true;
//				}
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		return false;
//	}
//
//	public static KeyPairValue<String, Vector<PlaylistItemInfo>> getChannelActivities(
//			String data) {
//		Vector<PlaylistItemInfo> activityList = new Vector<PlaylistItemInfo>();
//		String nextPageToken = "";
//		try {
//
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = 0; i < items.length(); ++i) {
//				PlaylistItemInfo itemInfo = new PlaylistItemInfo();
//
//				JSONObject jObjectContentDetails = ((JSONObject) items.get(i))
//						.getJSONObject(YoutubeField.CONTENTDETAILS);
//				YoutubeInfo videoInfo = new YoutubeInfo();
//
//				JSONObject objResourceDetails = getResourceDetails(jObjectContentDetails);
//				if (objResourceDetails != null) {
//					if (!objResourceDetails.isNull(YoutubeField.VIDEOID)) {
//						videoInfo.id = getString(objResourceDetails,
//								YoutubeField.VIDEOID);
//					} else if (!objResourceDetails
//							.isNull(YoutubeField.RESOURCEID)) {
//						videoInfo.id = getString(objResourceDetails,
//								YoutubeField.RESOURCEID, YoutubeField.VIDEOID);
//					}
//				}
//
//				if (!Utils.isNullOrEmpty(videoInfo.id)) {
//					itemInfo.dataInfo = videoInfo;
//					JSONObject jObjectSnippet = ((JSONObject) ((JSONObject) items
//							.get(i)).getJSONObject(YoutubeField.SNIPPET));
//					String time = getString(jObjectSnippet,
//							YoutubeField.PUBLISHEDAT);
//					itemInfo.time = Utils.getDisplayTime(time);
//
//					if (jObjectContentDetails.has(YoutubeField.LIKE)) {
//						itemInfo.playlistItemType = YoutubePlaylistItemType.Liked;
//					} else if (jObjectContentDetails.has(YoutubeField.UPLOAD)) {
//						itemInfo.playlistItemType = YoutubePlaylistItemType.Uploaded;
//					} else if (jObjectContentDetails.has(YoutubeField.BULLETIN)) {
//						itemInfo.playlistItemType = YoutubePlaylistItemType.UploadedAndPosted;
//					} else if (jObjectContentDetails.has(YoutubeField.COMMENT)) {
//						itemInfo.playlistItemType = YoutubePlaylistItemType.Commented;
//					} else if (jObjectContentDetails
//							.has(YoutubeField.RECOMMENDATION)) {
//						itemInfo.playlistItemType = YoutubePlaylistItemType.Recommended;
//					} else if (jObjectContentDetails
//							.has(YoutubeField.SUBSCRIPTION)) {
//						itemInfo.playlistItemType = YoutubePlaylistItemType.Subscribed;
//					} else {
//						itemInfo.playlistItemType = YoutubePlaylistItemType.OtherAction;
//					}
//					activityList.add(itemInfo);
//				}
//			}
//			final String NEXTPAGETOKEN = YoutubeField.NEXTPAGETOKEN;
//			if (jObjectData.has(NEXTPAGETOKEN)) {
//				nextPageToken = jObjectData.getString(NEXTPAGETOKEN);
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//
//		return new KeyPairValue<String, Vector<PlaylistItemInfo>>(
//				nextPageToken, activityList);
//	}
//
//	private static JSONObject getResourceDetails(JSONObject obj) {
//		String[] fields = { YoutubeField.BULLETIN, YoutubeField.CHANNELITEM,
//				YoutubeField.COMMENT, YoutubeField.FAVOURITE, YoutubeField.LIKE,
//				YoutubeField.PLAYLISTITEM, YoutubeField.RECOMMENDATION,
//				YoutubeField.SOCIAL, YoutubeField.SUBSCRIPTION,
//				YoutubeField.UPLOAD };
//		for (String s : fields) {
//			if (!obj.isNull(s)) {
//				try {
//					return obj.getJSONObject(s);
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		return null;
//	}
//
//	public static KeyPairValue<String, Vector<ChannelInfo>> getSubscriptions(
//			String data) {
//		Vector<ChannelInfo> playlists = new Vector<ChannelInfo>();
//		String nextPageToken = "";
//		try {
//
//			JSONObject jObjectData = new JSONObject(data);
//			JSONArray items = jObjectData.getJSONArray(YoutubeField.ITEMS);
//			for (int i = 0; i < items.length(); ++i) {
//				ChannelInfo channelInfo = PopulateSubscription((JSONObject) items
//						.get(i));
//				if (channelInfo != null) {
//					playlists.add(channelInfo);
//				}
//			}
//			final String NEXTPAGETOKEN = YoutubeField.NEXTPAGETOKEN;
//			if (jObjectData.has(NEXTPAGETOKEN)) {
//				nextPageToken = jObjectData.getString(NEXTPAGETOKEN);
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//
//		return new KeyPairValue<String, Vector<ChannelInfo>>(nextPageToken,
//				playlists);
//	}
//
//	public static String getString(JSONObject jObj, String... propNames) {
//		try {
//			JSONObject item = null;
//			if (propNames.length == 1) {
//				item = jObj;
//			}
//			for (int i = 0; i < propNames.length - 1; ++i) {
//				JSONObject tmp = item == null ? jObj : item;
//				if (tmp.has(propNames[i])) {
//					item = tmp.getJSONObject(propNames[i]);
//				} else {
//					return "";
//				}
//			}
//			if (item != null && item.has(propNames[propNames.length - 1])) {
//				return item.getString(propNames[propNames.length - 1]);
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		return "";
//	}
//
//	public static int getInt(JSONObject jObj, String... propNames) {
//		try {
//			JSONObject item = null;
//			if (propNames.length == 1) {
//				item = jObj;
//			}
//			for (int i = 0; i < propNames.length - 1; ++i) {
//				JSONObject tmp = item == null ? jObj : item;
//				if (tmp.has(propNames[i])) {
//					item = tmp.getJSONObject(propNames[i]);
//				} else {
//					return 0;
//				}
//			}
//			if (item != null && item.has(propNames[propNames.length - 1])) {
//				return item.getInt(propNames[propNames.length - 1]);
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		return 0;
//	}
//
//	public static JSONObject getJSONObject(JSONObject jObj, String... propNames) {
//		try {
//			JSONObject item = null;
//			for (int i = 0; i < propNames.length; ++i) {
//				JSONObject tmp = item == null ? jObj : item;
//				if (tmp.has(propNames[i])) {
//					item = tmp.getJSONObject(propNames[i]);
//				} else {
//					return null;
//				}
//			}
//			return item;
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
}

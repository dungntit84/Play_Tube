package com.hteck.playtube.service;

import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.data.YoutubeInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class PlaylistService {
    public static void addPlaylist(PlaylistInfo playlistInfo) {
        try {
            ArrayList<PlaylistInfo> playlists = getAllPlaylists();

            Gson gson = new Gson();
            PlaylistArrayInfo playlistArrayInfo = Utils.stringIsNullOrEmpty(data) ? new PlaylistArrayInfo() : new Gson().fromJson(data, PlaylistArrayInfo.class);
            playlistArrayInfo.playlists.add(playlistInfo);
            data = gson.toJson(playlistArrayInfo);
            Utils.savePref(Constants.PLAYLIST_DATA, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void addPlaylist(PlaylistInfo playlistInfo, YoutubeInfo youtubeInfo) {
        try {
            String data = Utils.getPrefValue(Constants.PLAYLIST_DATA, "");
            Gson gson = new Gson();
            PlaylistArrayInfo playlistArrayInfo = Utils.stringIsNullOrEmpty(data) ? new PlaylistArrayInfo() : new Gson().fromJson(data, PlaylistArrayInfo.class);
            if (playlistInfo.youtubeList == null) {
                playlistInfo.youtubeList = new ArrayList<>();
            }
            if (!isExisted(playlistInfo.youtubeList, youtubeInfo)) {
                playlistArrayInfo.playlists.add(playlistInfo);
            }
            data = gson.toJson(playlistArrayInfo);
            Utils.savePref(Constants.PLAYLIST_DATA, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isExisted(ArrayList<YoutubeInfo> youtubeList, YoutubeInfo youtubeInfo) {
        for (YoutubeInfo y : youtubeList) {
            if (y.id.equals(youtubeInfo.id)) {
                return true;
            }
        }
        return false;
    }

    public static void removePlaylist(PlaylistInfo playlistInfo) {
        try {
            String data = Utils.getPrefValue(Constants.PLAYLIST_DATA, "");
            Gson gson = new Gson();
            PlaylistArrayInfo playlistArrayInfo = Utils.stringIsNullOrEmpty(data) ? new PlaylistArrayInfo() : new Gson().fromJson(data, PlaylistArrayInfo.class);
            if (playlistInfo.youtubeList == null) {
                playlistInfo.youtubeList = new ArrayList<>();
            }
            for (int i = playlistArrayInfo.playlists.size() - 1; i >= 0; --i) {
                if (playlistArrayInfo.playlists.get(i).id.equals(playlistInfo.id)) {
                    playlistArrayInfo.playlists.remove(i);
                }
            }
            data = gson.toJson(playlistArrayInfo);
            Utils.savePref(Constants.PLAYLIST_DATA, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void removeYoutube(PlaylistInfo playlistInfo, YoutubeInfo youtubeInfo) {
        try {
            String data = Utils.getPrefValue(Constants.PLAYLIST_DATA, "");
            Gson gson = new Gson();
            PlaylistArrayInfo playlistArrayInfo = Utils.stringIsNullOrEmpty(data) ? new PlaylistArrayInfo() : new Gson().fromJson(data, PlaylistArrayInfo.class);
            if (playlistInfo.youtubeList == null) {
                playlistInfo.youtubeList = new ArrayList<>();
            }
            for (int i = playlistArrayInfo.playlists.size() - 1; i >= 0; --i) {
                boolean isExit = false;
                if (playlistArrayInfo.playlists.get(i).id.equals(playlistInfo.id)) {
                    for (int k = playlistArrayInfo.playlists.get(i).youtubeList.size() - 1; k >= 0; --k) {
                        if (playlistArrayInfo.playlists.get(i).youtubeList.get(k).id.equals(youtubeInfo.id)) {
                            playlistArrayInfo.playlists.get(i).youtubeList.remove(k);
                            isExit = true;
                            break;
                        }
                    }
                }
                if (isExit) {
                    break;
                }
            }
            data = gson.toJson(playlistArrayInfo);
            Utils.savePref(Constants.PLAYLIST_DATA, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static PlaylistInfo getPlaylistInfo(JSONObject jObject) {
        PlaylistInfo playlistInfo = new PlaylistInfo();

        Class<?> clazz = playlistInfo.getClass();
        try {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isPrivate(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (field.getName().equals("youtubeList")) {
                    JSONArray youtubeItems = jObject.getJSONArray(field.getName());
                    for (int i = 0; i < youtubeItems.length(); ++i) {
                        playlistInfo.youtubeList.add(YoutubeService.getYoutubeInfo(youtubeItems.getJSONObject(i)));
                    }
                } else {
                    if (jObject.has(field.getName()) && !jObject.isNull(field.getName())) {
                        Object propValue = Utils.toObject(field.getType(), jObject, field.getName());

                        field.set(playlistInfo, propValue);
                    }
                }
            }
            return playlistInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<PlaylistInfo> getAllPlaylists() {
        ArrayList<PlaylistInfo> result = new ArrayList<>();
        try {
            String data = Utils.getPrefValue(Constants.PLAYLIST_DATA, "");
            if (Utils.stringIsNullOrEmpty(data)) {
                JSONArray jItems = new JSONArray(data);
                for (int i = 0; i < jItems.length(); ++i) {
                    JSONObject jObject = jItems.getJSONObject(i);
                    PlaylistInfo playlistInfo = getPlaylistInfo(jObject);
                    result.add(playlistInfo);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}

package com.hteck.playtube.service;

import com.google.gson.Gson;
import com.hteck.playtube.R;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.data.YoutubeInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.UUID;

public class PlaylistService {
    public static void addPlaylist(PlaylistInfo playlistInfo) {
        try {
            ArrayList<PlaylistInfo> playlists = getAllPlaylists();

            Gson gson = new Gson();
            playlists.add(playlistInfo);
            String data = gson.toJson(playlists);
            Utils.savePref(Constants.PLAYLIST_DATA, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void updatePlaylist(PlaylistInfo playlistInfo) {
        try {
            ArrayList<PlaylistInfo> playlists = getAllPlaylists();


            for (int i = 0; i < playlists.size(); ++i) {
                PlaylistInfo p = playlists.get(i);
                if (p.id.equals(playlistInfo.id)) {
                    playlists.set(i, playlistInfo);
                }
            }
            Gson gson = new Gson();
            String data = gson.toJson(playlists);
            Utils.savePref(Constants.PLAYLIST_DATA, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void addPlaylist(PlaylistInfo playlistInfo, YoutubeInfo youtubeInfo) {
        try {
            Gson gson = new Gson();
            ArrayList<PlaylistInfo> playlists = getAllPlaylists();
            if (playlistInfo.youtubeList == null) {
                playlistInfo.youtubeList = new ArrayList<>();
            }

            if (!isExisted(playlistInfo.youtubeList, youtubeInfo)) {
                playlistInfo.youtubeList.add(youtubeInfo);
            }
            boolean isExisted = false;
            for (int i = 0; i < playlists.size(); ++i) {
                if (playlists.get(i).id.equals(playlistInfo.id)) {
                    playlists.set(i, playlistInfo);
                    isExisted = true;
                }
            }
            if (!isExisted) {
                playlists.add(playlistInfo);
            }

            String data = gson.toJson(playlists);
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

    public static boolean isExisted(ArrayList<PlaylistInfo> playlists, PlaylistInfo playlistInfo) {
        for (PlaylistInfo p : playlists) {
            if (p.id.equals(playlistInfo.id)) {
                return true;
            }
        }
        return false;
    }

    public static void removePlaylist(PlaylistInfo playlistInfo) {
        try {
            Gson gson = new Gson();
            ArrayList<PlaylistInfo> playlists = getAllPlaylists();
            if (playlistInfo.youtubeList == null) {
                playlistInfo.youtubeList = new ArrayList<>();
            }
            for (int i = playlists.size() - 1; i >= 0; --i) {
                if (playlists.get(i).id.equals(playlistInfo.id)) {
                    playlists.remove(i);
                }
            }
            String data = gson.toJson(playlists);
            Utils.savePref(Constants.PLAYLIST_DATA, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void removeYoutube(PlaylistInfo playlistInfo, YoutubeInfo youtubeInfo) {
        try {
            Gson gson = new Gson();
            ArrayList<PlaylistInfo> playlists = getAllPlaylists();
            if (playlistInfo.youtubeList == null) {
                playlistInfo.youtubeList = new ArrayList<>();
            }
            for (int i = playlists.size() - 1; i >= 0; --i) {
                boolean isExit = false;
                if (playlists.get(i).id.equals(playlistInfo.id)) {
                    for (int k = playlists.get(i).youtubeList.size() - 1; k >= 0; --k) {
                        if (playlists.get(i).youtubeList.get(k).id.equals(youtubeInfo.id)) {
                            playlists.get(i).youtubeList.remove(k);
                            isExit = true;
                            break;
                        }
                    }
                    if (playlists.get(i).youtubeList.size() == 0) {
                        playlists.get(i).imageUrl = "";
                    }
                }
                if (isExit) {
                    break;
                }
            }
            String data = gson.toJson(playlists);
            Utils.savePref(Constants.PLAYLIST_DATA, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static PlaylistInfo getPlaylistInfo(JSONObject jObject) {
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
            if (playlistInfo.youtubeList.size() > 0) {
                playlistInfo.imageUrl = playlistInfo.youtubeList.get(0).imageUrl;
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
            boolean isFavouritesExisted = false;
            if (!Utils.stringIsNullOrEmpty(data)) {
                JSONArray jItems = new JSONArray(data);
                for (int i = 0; i < jItems.length(); ++i) {
                    JSONObject jObject = jItems.getJSONObject(i);
                    PlaylistInfo playlistInfo = getPlaylistInfo(jObject);
                    if (playlistInfo != null && playlistInfo.id.equals(Utils.buildFavouritesUUID())) {
                        isFavouritesExisted = true;
                        result.add(playlistInfo);
                    }
                }
            }
            if (!isFavouritesExisted) {
                PlaylistInfo playlistInfo = new PlaylistInfo();
                playlistInfo.youtubeList = new ArrayList<>();
                playlistInfo.title = Utils.getString(R.string.favourites);
                playlistInfo.id = Utils.buildFavouritesUUID();
                result.add(playlistInfo);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static PlaylistInfo getPlaylistInfoById(UUID id) {
        try {
            String data = Utils.getPrefValue(Constants.PLAYLIST_DATA, "");
            if (!Utils.stringIsNullOrEmpty(data)) {
                JSONArray jItems = new JSONArray(data);
                for (int i = 0; i < jItems.length(); ++i) {
                    JSONObject jObject = jItems.getJSONObject(i);
                    PlaylistInfo playlistInfo = getPlaylistInfo(jObject);
                    if (playlistInfo != null && playlistInfo.id.equals(id)) {
                        return playlistInfo;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void addYoutubeToFavouritePlaylist(YoutubeInfo youtubeInfo) {
        try {
            Gson gson = new Gson();
            ArrayList<PlaylistInfo> playlists = getAllPlaylists();
            for (int i = 0; i < playlists.size(); ++i) {
                PlaylistInfo playlistInfo = playlists.get(i);
                if (playlistInfo.id.equals(Utils.buildFavouritesUUID())) {
                    if (!isExisted(playlistInfo.youtubeList, youtubeInfo)) {
                        playlistInfo.youtubeList.add(youtubeInfo);
                    }
                }
            }

            String data = gson.toJson(playlists);
            Utils.savePref(Constants.PLAYLIST_DATA, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

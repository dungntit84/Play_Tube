package com.hteck.playtube.service;

import com.google.gson.Gson;
import com.hteck.playtube.R;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.data.YoutubeInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class HistoryService {

    public static ArrayList<YoutubeInfo> getAllHistory() {
        ArrayList<YoutubeInfo> result = new ArrayList<>();
        try {
            String data = Utils.getPrefValue(Constants.HISTORY_DATA, "");
            if (!Utils.stringIsNullOrEmpty(data)) {
                JSONArray jItems = new JSONArray(data);
                for (int i = 0; i < jItems.length(); ++i) {
                    JSONObject jObject = jItems.getJSONObject(i);
                    YoutubeInfo youtubeInfo = YoutubeService.getYoutubeInfo(jObject);
                    if (youtubeInfo != null) {
                        result.add(youtubeInfo);
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static void addYoutubeToHistory(YoutubeInfo youtubeInfo) {

        try {
            ArrayList<YoutubeInfo> result = getAllHistory();
            for (int i = result.size() - 1; i >= 0; --i) {
                if (result.get(i).id.equals(youtubeInfo.id)) {
                    result.remove(i);
                }
            }
            result.add(0, youtubeInfo);

            Gson gson = new Gson();
            String data = gson.toJson(result);
            Utils.savePref(Constants.HISTORY_DATA, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void removeYoutubeHistory(String id) {

        try {
            ArrayList<YoutubeInfo> result = getAllHistory();
            for (int i = result.size() - 1; i >= 0; --i) {
                if (result.get(i).id.equals(id)) {
                    result.remove(i);
                }
            }

            Gson gson = new Gson();
            String data = gson.toJson(result);
            Utils.savePref(Constants.HISTORY_DATA, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void clearHistory() {

        try {
            Utils.removePref(Constants.HISTORY_DATA);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

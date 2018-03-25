package com.hteck.playtube.service;

import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.YoutubeInfo;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by dungnt84 on 23/03/2018.
 */

public class YoutubeService {
    public static YoutubeInfo getYoutubeInfo(JSONObject jObject) {
        YoutubeInfo youtubeInfo = new YoutubeInfo();
        Class<?> clazz = youtubeInfo.getClass();
        try {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isPrivate(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                if (jObject.has(field.getName()) && !jObject.isNull(field.getName())) {
                    Object propValue = Utils.toObject(field.getType(), jObject, field.getName());

                    field.set(youtubeInfo, propValue);
                }
            }
            return youtubeInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

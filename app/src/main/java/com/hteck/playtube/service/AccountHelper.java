package com.hteck.playtube.service;

import com.google.gson.Gson;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.ChannelInfo;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class AccountHelper {

    public static boolean saveAccountInfo(ChannelInfo accountInfo) {
        try {
            if (accountInfo == null) {
                Utils.removePref(Constants.ACCOUNT_INFO);
            } else {
                Gson gson = new Gson();
                String json = gson.toJson(accountInfo);
                Utils.savePref(Constants.ACCOUNT_INFO, json);
            }
            return true;

        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ChannelInfo getAccountInfo() {
        try {
            String s = Utils.getPrefValue(Constants.ACCOUNT_INFO, "");
            if (Utils.stringIsNullOrEmpty(s)) {
                JSONObject jObject = new JSONObject(s);

                return getChannelInfo(jObject);
            }
        } catch (Throwable e) {
            e.printStackTrace();

        }
        return null;
    }

    public static ChannelInfo getChannelInfo(JSONObject jObject) {
        ChannelInfo channelInfo = new ChannelInfo();
        Class<?> clazz = channelInfo.getClass();
        try {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isPrivate(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                if (jObject.has(field.getName()) && !jObject.isNull(field.getName())) {
                    Object propValue = Utils.toObject(field.getType(), jObject, field.getName());

                    field.set(channelInfo, propValue);
                }
            }
            return channelInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

package com.hteck.playtube.common;

import com.hteck.playtube.service.CustomCallback;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;

public class CustomHttpOk extends Thread {
    private String _url;
    private CustomCallback _callback;
    private Call _call;
    public List<AbstractMap.SimpleEntry<String, String>> mHeaders;

    public CustomHttpOk(String url, CustomCallback callback) {
        System.out.println("url=" + url);
        this._url = url;
        this._callback = callback;
    }

    @Override
    public void run() {
        try {
            OkHttpClient client = new OkHttpClient();

            Request.Builder builder = new Request.Builder();
            if (mHeaders != null) {
                for (int i = 0; i < mHeaders.size(); ++i) {
                    builder.addHeader(mHeaders.get(i).getKey(),
                            mHeaders.get(i).getValue());
                }
            }
            Request request = builder.url(_url).build();
            _call = client.newCall(request);
            _call.enqueue(_callback);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        _call.cancel();
    }
}

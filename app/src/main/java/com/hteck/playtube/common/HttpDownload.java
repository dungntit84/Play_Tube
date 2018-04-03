package com.hteck.playtube.common;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.List;

public class HttpDownload extends Thread {
    private String _url;
    private IHttplistener _httpListener;
    private boolean _isExited;
    public List<AbstractMap.SimpleEntry<String, String>> mHeaders;
    private HttpURLConnection _ucon;
    private Object _dataContext;

    public HttpDownload(String url, IHttplistener httpListener) {
        _url = url;
        _httpListener = httpListener;
    }

    public HttpDownload(String url, IHttplistener httpListener,
                        List<AbstractMap.SimpleEntry<String, String>> headers) {
        _url = url;
        _httpListener = httpListener;
        mHeaders = headers;
    }

    public void exit() {
        _isExited = true;
        try {
            if (_ucon != null) {
                _ucon.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private final String USER_AGENT = "Mozilla/5.0";
    @Override
    public void run() {
        try {
            URL url = new URL(_url);
            _ucon = (HttpURLConnection) url.openConnection();
            if (mHeaders != null) {
                for (int i = 0; i < mHeaders.size(); ++i) {
                    _ucon.setRequestProperty(mHeaders.get(i).getKey(),
                            mHeaders.get(i).getValue());
                }
            }
            _ucon.setRequestProperty("User-Agent", USER_AGENT);
            _ucon.setRequestProperty("Accept-Charset", "UTF-8");
            _ucon.connect();
            int responseCode = _ucon.getResponseCode();

            if (responseCode == 404) {
                _httpListener.returnResult(HttpDownload.this, null,
                        IHttplistener.ResultType.ConnectionError);
                return;
            }
            InputStream stream = _ucon.getInputStream();
            byte[] bytes = readFully(stream);
            _ucon.disconnect();
            if (_isExited) {
                return;
            }
            _httpListener.returnResult(HttpDownload.this,
                    bytes, IHttplistener.ResultType.Done);
        } catch (Exception e) {
            e.printStackTrace();
            if (_isExited) {
                return;
            }
            _httpListener.returnResult(HttpDownload.this,
                    null, IHttplistener.ResultType.Error);
        } finally {
            try {
                if (_ucon != null) {
                    _ucon.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] readFully(InputStream input) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }
}

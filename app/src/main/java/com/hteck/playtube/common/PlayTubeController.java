package com.hteck.playtube.common;

import android.os.Environment;

import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.data.ConfigInfo;
import com.hteck.playtube.data.PlayingInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.service.ConfigService;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

public class PlayTubeController {
    private static String _mainDirectory;
    private static PlayingInfo _playingInfo;
    private static ConfigInfo _configInfo;

    public static String getMainDirectory() {
        if (Utils.stringIsNullOrEmpty(_mainDirectory)) {
            String state = Environment.getExternalStorageState();
            File path;

            try {
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    // We can read and write the media
                    path = MainActivity.getInstance().getExternalFilesDir(null);
                    _mainDirectory = path.getAbsolutePath() + "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return _mainDirectory;
    }

    public static void initImageLoader() {
        try {
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisk(true).cacheInMemory(true)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .displayer(new FadeInBitmapDisplayer(300)).build();

            ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(
                    MainActivity.getInstance());
            config.defaultDisplayImageOptions(defaultOptions);
            config.threadPriority(Thread.NORM_PRIORITY - 2);
            config.denyCacheImageMultipleSizesInMemory();
            config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
            config.diskCacheSize(100 * 1024 * 1024);
            config.tasksProcessingOrder(QueueProcessingType.LIFO);
            ImageLoader.getInstance().init(config.build());
        } catch (Throwable e) {
            e.printStackTrace();

        }
    }

    public static ConfigInfo getConfigInfo() {
        if (_configInfo == null) {
            _configInfo = ConfigService.getConfigInfo();
        }
        return _configInfo;
    }

    public static void setConfig(ConfigInfo configInfo) {
        _configInfo = configInfo;
    }

    public static void setPlayingInfo(YoutubeInfo youtubeInfo, ArrayList<YoutubeInfo> youtubeList) {
        _playingInfo = new PlayingInfo(youtubeInfo, youtubeList);
    }

    public static PlayingInfo getPlayingInfo() {
        return _playingInfo;
    }
}

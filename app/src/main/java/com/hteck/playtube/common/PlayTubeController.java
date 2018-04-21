package com.hteck.playtube.common;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

import com.hteck.playtube.R;
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
import java.util.List;

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
            config.diskCacheSize(50 * 1024 * 1024);
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

    public static int getNoOfUses() {
        return Utils.getPrefValue(Constants.NO_USES_SETTING, 0);
    }

    public static void saveNumOfUses(int numOfUses) {
        try {
            Utils.savePref(Constants.NO_USES_SETTING, numOfUses);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static boolean showRateAndReview() {
        if (!isValidContext()) {
            return false;
        }
        boolean result = false;
        try {
            int numOfUses = getNoOfUses();
            numOfUses++;
            if (numOfUses > 8) {
                return false;
            }

            if (numOfUses == 5 || numOfUses == 8) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        final AlertDialog.Builder alert = new AlertDialog.Builder(
                                MainActivity.getInstance(),
                                AlertDialog.THEME_HOLO_LIGHT);
                        alert.setTitle(MainActivity.getInstance().getString(
                                R.string.rate_app_title));
                        alert.setMessage(MainActivity.getInstance().getString(
                                R.string.rate_app_content));

                        alert.setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        try {
                                            dialog.cancel();
                                            Utils.rateApp();
                                        } catch (Throwable e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                        alert.setNegativeButton(android.R.string.no,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        dialog.cancel();
                                    }
                                });
                        alert.show();
                    }
                });
                result = true;
            }
            saveNumOfUses(numOfUses);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isValidContext() {
        try {
            ActivityManager manager = (ActivityManager) MainActivity
                    .getInstance().getSystemService(Context.ACTIVITY_SERVICE);

            List<ActivityManager.RunningTaskInfo> task = manager
                    .getRunningTasks(1);

            // Get the info we need for comparison.
            if (!task.get(0).topActivity.getClassName().equals(
                    MainActivity.class.getName())) {
                return false;
            }
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}

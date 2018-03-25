package com.hteck.playtube.data;

import com.hteck.playtube.common.PlayTubeController;

import java.util.Vector;

/**
 * Created by dungnt84 on 10/03/2018.
 */

public class PlayingInfo {
    private YoutubeInfo _currentYoutubeInfo;
    private Vector<YoutubeInfo> _youtubeList;

    public PlayingInfo(YoutubeInfo youtubeInfo, Vector<YoutubeInfo> youtubeList) {
        _currentYoutubeInfo = youtubeInfo;
        _youtubeList = youtubeList;
    }

    public void setYoutubeInfo(YoutubeInfo youtubeInfo) {
        _currentYoutubeInfo = youtubeInfo;
    }

    public int getCurrentIndex() {
        try {
            for (int i = 0; i < _youtubeList.size(); ++i) {
                YoutubeInfo youtubeInfo = _youtubeList.elementAt(i);
                if (_currentYoutubeInfo.id.equals(youtubeInfo.id)) {
                    return i;
                }
            }
            return 0;
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Vector<YoutubeInfo> getYoutubeList() {
        return _youtubeList;
    }

    public YoutubeInfo getCurrentYoutubeInfo() {
        return _currentYoutubeInfo;
    }

    public void doPrevious() {
        try {
            int nextIndex = PlayTubeController.getPlayingInfo().getCurrentIndex() - 1;
            if (nextIndex >= 0
                    && nextIndex < PlayTubeController.getPlayingInfo().getYoutubeList()
                    .size()) {
                YoutubeInfo youtubeInfo = PlayTubeController.getPlayingInfo().getYoutubeList()
                        .elementAt(nextIndex);
                PlayTubeController.getPlayingInfo().setYoutubeInfo(youtubeInfo);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void doNext() {
        int nextIndex = PlayTubeController.getPlayingInfo().getCurrentIndex() + 1;
        if (nextIndex < PlayTubeController.getPlayingInfo().getYoutubeList()
                .size()) {
            YoutubeInfo youtubeInfo = PlayTubeController.getPlayingInfo().getYoutubeList()
                    .elementAt(nextIndex);
            PlayTubeController.getPlayingInfo().setYoutubeInfo(youtubeInfo);
        }

    }
}

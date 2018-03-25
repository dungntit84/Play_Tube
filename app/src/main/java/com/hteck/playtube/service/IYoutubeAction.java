package com.hteck.playtube.service;

import com.hteck.playtube.data.YoutubeInfo;

public interface IYoutubeAction {
	public enum ActionName {
		DELETE
	}
	
	public void onEvent(YoutubeInfo videoInfo, ActionName actionName);
}

package com.hteck.playtube.auth;

import com.hteck.playtube.common.PlayTubeController;
//import com.google.api.services.plus.PlusScopes;

/**
 * 
 * Enum that encapsulates the various OAuth2 connection parameters for the
 * different providers
 * 
 * We capture the following properties for the demo application
 * 
 * clientId clientSecret scopes rederictUri apiUrl tokenUrl
 * authUrl accessMethod
 * 
 * @author davydewaele
 * 
 */
public class OauthParams {
	private String clientId;
	private String clientSecret;
	private String scopes;
	private String rederictUri;
	private String userId;
	private String tokenUrl;
	private String authUrl;

	public OauthParams() {
		this.clientId = PlayTubeController.getConfigInfo().clientID;
		this.clientSecret = PlayTubeController.getConfigInfo().secretID;
		this.tokenUrl = PlayTubeController.getConfigInfo().tokenUrl;
		this.authUrl = PlayTubeController.getConfigInfo().authUrl;
		this.rederictUri = PlayTubeController.getConfigInfo().redirectUrl;
		this.scopes = PlayTubeController.getConfigInfo().scopes;
		this.userId = "hteck_auth";
	}

	public String getClientId() {
		if (this.clientId == null || this.clientId.length() == 0) {
			throw new IllegalArgumentException(
					"Please provide a valid clientId in the Oauth2Params class");
		}
		return clientId;
	}

	public String getClientSecret() {
		if (this.clientSecret == null || this.clientSecret.length() == 0) {
			throw new IllegalArgumentException(
					"Please provide a valid clientSecret in the Oauth2Params class");
		}
		return clientSecret;
	}

	public String getScopes() {
		return scopes;
	}

	public String getRederictUri() {
		return rederictUri;
	}

	public String getTokenUrl() {
		return tokenUrl;
	}

	public String getAuthUrl() {
		return authUrl;
	}


	public String getUserId() {
		return userId;
	}
}

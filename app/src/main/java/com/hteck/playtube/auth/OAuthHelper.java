package com.hteck.playtube.auth;


import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.hteck.playtube.activity.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class OAuthHelper {
    private CredentialStore credentialStore;
    private AuthorizationCodeFlow flow;

    private OauthParams oauth2Params;

    /**
     * Global instance of the HTTP transport.
     */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();


    public OAuthHelper(OauthParams oauth2Params) {
        try {
            this.credentialStore = new SharedPreferencesCredentialStore();
            this.oauth2Params = oauth2Params;

            ClientParametersAuthentication clientParams = new ClientParametersAuthentication(
                    oauth2Params.getClientId(), oauth2Params.getClientSecret());
            String authServerURL = oauth2Params.getAuthUrl();
            String clientID = oauth2Params.getClientId();

            AuthorizationCodeFlow.Builder builder = new AuthorizationCodeFlow.Builder(
                    FoursquareQueryParameterAccessMethod.getInstance(),
                    HTTP_TRANSPORT, JSON_FACTORY, new GenericUrl(
                    oauth2Params.getTokenUrl()), clientParams,
                    clientID, authServerURL);

            builder.setCredentialStore(this.credentialStore);
            this.flow = builder.build();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public String executeRequestApiCall(String url) throws IOException {

        return HTTP_TRANSPORT.createRequestFactory(loadCredential())
                .buildGetRequest(new GenericUrl(url)).execute()
                .parseAsString();

    }

    public void clearCredentials() throws IOException {
        CookieSyncManager.createInstance(MainActivity.getInstance());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        if (credentialStore != null) {
            credentialStore.delete(oauth2Params.getUserId(), null);
        }
    }

    public YouTube getYoutubeService() {
        try {
            // This object is used to make YouTube Data API requests.
            return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                    loadCredential()).build();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public Credential loadCredential() throws IOException {
        return flow.loadCredential(oauth2Params.getUserId());
    }

    public boolean loadSavedCredential() {
        try {
            return credentialStore.load(oauth2Params.getUserId(),
                    loadCredential());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return false;
    }

    public void retrieveAndStoreAccessToken(String authorizationCode)
            throws IOException {
        try {
            Log.i("oauth", "retrieveAndStoreAccessToken for code "
                    + authorizationCode);

            TokenResponse tokenResponse = flow.newTokenRequest(authorizationCode)
                    .setScopes(convertScopesToString(oauth2Params.getScopes()))
                    .setRedirectUri(oauth2Params.getRederictUri()).execute();
            Log.i("oauth", "Found tokenResponse :");
            Log.i("oauth", "Access Token : " + tokenResponse.getAccessToken());
            Log.i("oauth", "Refresh Token : " + tokenResponse.getRefreshToken());

            flow.createAndStoreCredential(tokenResponse, oauth2Params.getUserId());

            credentialStore.store(oauth2Params.getUserId(), loadCredential());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private Collection<String> convertScopesToString(String scopesConcat) {
        String[] scopes = scopesConcat.split(",");
        Collection<String> collection = new ArrayList<String>();
        Collections.addAll(collection, scopes);
        return collection;
    }
}

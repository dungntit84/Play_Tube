package com.hteck.playtube.service;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.auth.OAuthHelper;
import com.hteck.playtube.auth.OauthParams;
import com.hteck.playtube.common.CustomHttpOk;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.ChannelInfo;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;

public class AccountContext {
    private static AccountContext _instance;
    public boolean mIsReloadMyPlaylist;
    public boolean mIsReloadMySubscriptions;
    private OAuthHelper _oAuth2Helper;
    private ChannelInfo _accountInfo;
    private ChannelInfo _currentAccount;

    static public AccountContext getInstance() {
        if (_instance == null) {
            _instance = new AccountContext();
        }
        return _instance;
    }

    public void loadYoutubeAccount() {
        _accountInfo = AccountHelper.getAccountInfo();

        try {
            OauthParams mOAuthParams = new OauthParams();

            // =====================================================
            OAuthHelper auth2Helper = new OAuthHelper(mOAuthParams);
            if (_accountInfo != null) {
                if (auth2Helper.loadSavedCredential()) {
                    _oAuth2Helper = auth2Helper;
                    if (_accountInfo != null) {
                        _accountInfo.isLoggedIn = true;
                    }
                    loadUserProfile();
                }

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public ChannelInfo getAccountInfo() {
        return _accountInfo;
    }

    public ChannelInfo getAccountTempInfo() {
        return _currentAccount;
    }

    public OAuthHelper getOAuthHelper() {
        if (_oAuth2Helper == null) {
            OauthParams oAuthParams = new OauthParams();
            _oAuth2Helper = new OAuthHelper(oAuthParams);
        }
        return _oAuth2Helper;
    }

    public void setAccountInfo(ChannelInfo accountInfo) {
        _accountInfo = accountInfo;
        AccountHelper.saveAccountInfo(accountInfo);
    }

    public void loadUserProfile() {

        YoutubeAccountService youtubeService = new YoutubeAccountService(
                new YoutubeAccountService.IYoutubeAccountService() {

                    @Override
                    public void onServiceSuccess(Object userToken, Object data) {
                        try {
                            _currentAccount = (ChannelInfo) data;

                            _currentAccount.isLoggedIn = true;
                            validateChanel(_currentAccount);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            MainActivity.getInstance().dismissProgress();
                        }
                    }

                    @Override
                    public void onServiceFailed(Object userToken,
                                                    String error) {
                        MainActivity.getInstance().dismissProgress();
                    }
                });
        youtubeService.loadYoutubeProfile();
    }

    private void validateChanel(ChannelInfo channelInfo) {
        if (channelInfo == null) {
            return;
        }
        String url = String.format(
                PlayTubeController.getConfigInfo().loadChannelsInfoUrl,
                channelInfo.id);

        CustomCallback callback = new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                            Utils.showMessage(Utils
                                    .getString(R.string.network_error));

                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String s = response.body().string();
                            ArrayList<ChannelInfo> channels = YoutubeHelper
                                    .getChannelList(s);
                            if (channels.size() == 0) {
                                confirmSignUpYoutube();
                            } else {
                                setAccountInfo(_currentAccount);

                                accountSignedIn();
                            }

                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        CustomHttpOk httpOk = new CustomHttpOk(url, callback);
        httpOk.start();
    }

    public boolean isYoutubeLoggedIn() {
        if (_accountInfo == null) {
            return false;
        }
        return _accountInfo.isLoggedIn;
    }

    public void youtubeSignout() {
        try {
            _accountInfo = null;
            _currentAccount = null;
            if (_oAuth2Helper != null) {
                _oAuth2Helper.clearCredentials();
            }
            AccountHelper.saveAccountInfo(_accountInfo);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void confirmSignUpYoutube() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.getInstance(), AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle(Utils.getString(R.string.sign_up_title));
        builder.setMessage(Utils.getString(R.string.sign_up_confirm_msg));
        builder.setPositiveButton(android.R.string.yes,
                new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showSignUp();
                    }
                });

        builder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        signOutYoutube();
                    }
                });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });
        builder.show();
    }

    private WebView _wvConfirmSignUp;
    private void showSignUp() {
        try {
            final Dialog dlg = new Dialog(MainActivity.getInstance());

            LayoutInflater inflater = MainActivity.getInstance()
                    .getLayoutInflater();
            FrameLayout v = (FrameLayout) inflater.inflate(R.layout.webbrowser,
                    null);
            _wvConfirmSignUp = v.findViewById(R.id.webview);
            cleanWebView();
            dlg.setContentView(v);
            dlg.setTitle(Utils.getString(R.string.register_channel));
            dlg.show();
            WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
            lWindowParams.copyFrom(dlg.getWindow().getAttributes());
            lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            lWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            dlg.getWindow().setAttributes(lWindowParams);
            _wvConfirmSignUp.getSettings().setJavaScriptEnabled(true);
            _wvConfirmSignUp.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView wView, String url) {
                    if (url.indexOf(PlayTubeController.getConfigInfo().signedUpYoutubeUrl) == 0) {
                        try {
                            if (_wvConfirmSignUp != null) {
                                synchronized (_wvConfirmSignUp) {
                                    _wvConfirmSignUp.getSettings()
                                            .setJavaScriptEnabled(false);
                                    _wvConfirmSignUp.setWebChromeClient(null);
                                    _wvConfirmSignUp.setWebViewClient(null);

                                    _wvConfirmSignUp.clearCache(true);
                                    _wvConfirmSignUp.stopLoading();
                                    _wvConfirmSignUp = null;
                                }
                            }
                            loadUserProfile();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        dlg.dismiss();
                        return true;
                    }
                    wView.loadUrl(url);
                    return false;
                }
            });
            WebChromeClient chrome = new WebChromeClient();
            _wvConfirmSignUp.setWebChromeClient(chrome);
            _wvConfirmSignUp.setVerticalScrollBarEnabled(true);
            _wvConfirmSignUp.setHorizontalScrollBarEnabled(true);
            _wvConfirmSignUp.getSettings()
                    .setJavaScriptCanOpenWindowsAutomatically(true);
            _wvConfirmSignUp
                    .loadUrl(PlayTubeController.getConfigInfo().confirmRegisterYoutubeAccountUrl);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void cleanWebView() {
        try {
            CookieSyncManager.createInstance(MainActivity.getInstance());
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void signOutYoutube() {
        try {
            AccountContext.getInstance().youtubeSignout();
//            _homeView.refreshAccountData();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void accountSignedIn() {
        try {
//            doPendingAction();
//            _homeView.refreshAccountData();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}

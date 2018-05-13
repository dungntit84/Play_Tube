package com.hteck.playtube.activity;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.SearchView;

import com.hteck.playtube.R;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.databinding.ActivityMainBinding;
import com.hteck.playtube.fragment.BaseFragment;
import com.hteck.playtube.fragment.HistoryView;
import com.hteck.playtube.fragment.PlaylistVideosView;
import com.hteck.playtube.fragment.PlaylistsView;
import com.hteck.playtube.fragment.PopularView;
import com.hteck.playtube.fragment.SettingsView;
import com.hteck.playtube.fragment.YoutubePlayerBottomView;
import com.hteck.playtube.fragment.YoutubePlayerView;
import com.hteck.playtube.service.HistoryService;
import com.hteck.playtube.view.CustomRelativeLayout;

import java.util.ArrayList;
import java.util.Date;

import static com.hteck.playtube.common.PlayTubeController.showRateAndReview;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static MainActivity _this;

    public static MainActivity getInstance() {
        return _this;
    }

    private PopularView _popularView;
    long _timeExitPressed;
    public boolean mIsOrientationChanged = false;
    private float _oldX = 0;
    private float _oldY = 0;
    private float _prevX = 0;
    private float _prevY = 0;
    private boolean _isPlayerLayoutInitiated = false;
    private boolean _isPlayerShowing, _isSmallPlayer;
    private YoutubePlayerView _youtubePlayerApiView;
    private YoutubePlayerBottomView _youtubePlayerBottomView;
    private SearchView _searchView;
    private boolean _isInSearchMode;
    private Menu _menu;

    private boolean _isOrientationChanged = false;
    private ActivityMainBinding _binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        _this = this;
        setSupportActionBar(_binding.toolbar);
        setTitle(getTitle().toString());
        _binding.mainActivityTextViewExplore.setText(Utils.getString(R.string.explore));
        _binding.mainActivityTextViewSearch.setText(Utils.getString(R.string.search));
        _binding.mainActivityTextViewPlaylists.setText(Utils.getString(R.string.playlists));
        _binding.mainActivityTextViewHistory.setText(Utils.getString(R.string.history));
        _binding.mainActivityTextViewSettings.setText(Utils.getString(R.string.settings));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
        }
        init();

        showRateAndReview();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_main_layout_hot: {
                selectPopularView();
                break;
            }
            case R.id.activity_main_layout_search: {
                selectSearchView();
                break;
            }
            case R.id.activity_main_layout_playlists: {
                selectPlaylistsView();
                break;
            }
            case R.id.activity_main_layout_history: {
                selectHistoryView();
                break;
            }
            case R.id.activity_main_layout_setting: {
                selectSettingsView();
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isRootLevel()) {
                    Utils.hideKeyboard();
                    zoomImageFromThumb();
                } else {
                    doBackStep();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        try {
            PlayTubeController.initImageLoader();

            initView();

            _binding.activityMainLayoutHot.setOnClickListener(this);
            _binding.activityMainLayoutSearch.setOnClickListener(this);
            _binding.activityMainLayoutPlaylists.setOnClickListener(this);
            _binding.activityMainLayoutHistory.setOnClickListener(this);
            _binding.activityMainLayoutSetting.setOnClickListener(this);
            _binding.activityMainLayoutSearch.setOnClickListener(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void zoomImageFromThumb() {
        Animation animation = AnimationUtils.loadAnimation(getInstance(), R.anim.zoom_out_animation);
        animation.setRepeatMode(0);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                _binding.activityMainLayoutMain1Overlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        _binding.activityMainLayoutMain1.startAnimation(animation);
        _binding.activityMainLayoutMain10Overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _binding.activityMainLayoutMain1.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(getInstance(), R.anim.zoom_in_animation);
                animation.setDuration(200);
                animation.setFillAfter(true);
                _binding.activityMainLayoutMain1Overlay.setVisibility(View.GONE);

                _binding.activityMainLayoutMain1.startAnimation(animation);
            }
        });
    }

    private void selectPopularView() {
        try {
            restoreMainAnimation();

            initView();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void selectSearchView() {
        try {
            restoreMainAnimation();

            com.hteck.playtube.fragment.SearchView searchView = com.hteck.playtube.fragment.SearchView.newInstance(_searchView);
            replaceFragment(searchView);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void selectPlaylistsView() {
        try {
            restoreMainAnimation();

            showPlaylists();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void selectHistoryView() {
        try {
            restoreMainAnimation();

            showHistory();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void selectSettingsView() {
        try {
            restoreMainAnimation();

            showSettings();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void refreshPlaylistData() {
        try {
            for (int i = 0; i < getSupportFragmentManager().getFragments()
                    .size(); ++i) {
                Fragment fragment = getSupportFragmentManager().getFragments().get(i);
                if (fragment != null) {
                    if (fragment instanceof PlaylistsView) {
                        ((PlaylistsView) fragment).refreshData();
                    } else if (fragment instanceof PlaylistVideosView) {
                        ((PlaylistVideosView) fragment).refreshData();
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void refreshHistoryData() {
        try {
            for (int i = 0; i < getSupportFragmentManager().getFragments()
                    .size(); ++i) {
                Fragment fragment = getSupportFragmentManager().getFragments().get(i);
                if (fragment != null) {
                    if (fragment instanceof HistoryView) {
                        ((HistoryView) fragment).refreshData();
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void restoreMainAnimation() {
        _binding.activityMainLayoutMain1.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getInstance(), R.anim.zoom_in_animation);
        animation.setDuration(200);
        animation.setFillAfter(true);
        _binding.activityMainLayoutMain1Overlay.setVisibility(View.GONE);

        _binding.activityMainLayoutMain1.startAnimation(animation);
    }

    public Fragment addFragment(Fragment fragment) {
        String loadingFragment = fragment.getClass().getName();
        if (fragment.getArguments() != null
                && fragment.getArguments().containsKey(
                Constants.MAIN_VIEW_ID)) {
            loadingFragment += fragment.getArguments().getString(
                    Constants.MAIN_VIEW_ID);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment existedfragment = fragmentManager
                .findFragmentByTag(loadingFragment);
        if (existedfragment == null) {

            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.add(R.id.activity_main_layout_content, fragment,
                    loadingFragment);

            fragmentTransaction.addToBackStack(loadingFragment);

            fragmentTransaction.commit();
        } else {
            fragmentManager.popBackStack(loadingFragment, 0);
        }

        return existedfragment;
    }

    public void replaceFragment(Fragment fragment) {
        Fragment currentFragment = getCurrentFrag();
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            return;
        }
        FragmentManager fragmentManager = MainActivity.getInstance()
                .getSupportFragmentManager();
        fragmentManager.popBackStack(null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        fragmentTransaction.replace(R.id.activity_main_layout_content, fragment, fragment
                .getClass().getName());

        fragmentTransaction.addToBackStack(fragment.getClass().getName());
        fragmentTransaction.commit();
    }

    private void replaceFragment(Fragment fragment, int controlId) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        fragmentTransaction.replace(controlId, fragment);
        fragmentTransaction.commit();
    }

    private Fragment getCurrentFrag() {
        Object obj = getSupportFragmentManager().findFragmentById(
                R.id.activity_main_layout_content);
        if (obj != null) {
            return (Fragment) obj;
        }
        return null;
    }

    private void initView() {
        if (_popularView == null) {
            _popularView = PopularView.newInstance();
        }
        replaceFragment(_popularView);
    }

    private void showPlaylists() {
        PlaylistsView playlistsView = PlaylistsView.newInstance();
        replaceFragment(playlistsView);
    }

    private void showHistory() {
        HistoryView historyView = HistoryView.newInstance();
        replaceFragment(historyView);
    }

    private void showSettings() {
        SettingsView settingsView = SettingsView.newInstance();
        replaceFragment(settingsView);
    }

    @Override
    public void onBackPressed() {
        if (!doBackStep()) {
            exit();
        }
    }

    public boolean doBackStep() {

        if (isPlayerPlaying()) {
            minimizePlayer();
            return true;
        }
        boolean result = false;
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStackImmediate();
            result = true;
            setHeader();
        }

        return result;
    }

    private void exit() {
        try {
            boolean isOKToQuit = (new Date().getTime() - _timeExitPressed) <= 3000;
            if (isOKToQuit) {
                finish();
                System.exit(1);
                return;
            }
            _timeExitPressed = new Date().getTime();
            Utils.showMessage(Utils.getString(R.string.quit_msg));

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void playYoutube(YoutubeInfo youtubeInfo, ArrayList<YoutubeInfo> youtubeList, boolean isOutside) {
        try {
            PlayTubeController.setPlayingInfo(youtubeInfo, youtubeList);
            _isPlayerShowing = true;
            if (isOutside) {
                _isSmallPlayer = false;
            }
            if (_youtubePlayerApiView == null) {
                _youtubePlayerBottomView = YoutubePlayerBottomView.newInstance();

                _youtubePlayerApiView = YoutubePlayerView
                        .newInstance(_youtubePlayerBottomView);

                replaceFragment(_youtubePlayerApiView, R.id.activity_main_player_video);
                replaceFragment(_youtubePlayerBottomView,
                        R.id.layout_player_bottom_fragment);

            } else {
                _youtubePlayerApiView.reinitPlayer();
                if (_youtubePlayerBottomView == null) {
                    _youtubePlayerBottomView = YoutubePlayerBottomView.newInstance();
                } else {
                    _youtubePlayerBottomView.refreshData();
                }

            }
            if (isOutside) {
                switchPlayerLayout(false);
            }
            HistoryService.addYoutubeToHistory(youtubeInfo);
            refreshHistoryData();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void switchPlayerLayout(boolean isMiniPlayer) {
        _isSmallPlayer = isMiniPlayer;
        try {
            FrameLayout.LayoutParams layoutParamsBody = (FrameLayout.LayoutParams) _binding.layoutPlayerVideo
                    .getLayoutParams();
            ViewGroup.LayoutParams layoutParams = _binding.activityMainPlayerVideo.getLayoutParams();
            boolean isLandscapeScreen = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            if (isMiniPlayer) {
                _binding.layoutPlayerBottom.setVisibility(View.GONE);
                _binding.mainActivityPlayerHeader.setVisibility(View.VISIBLE);

                layoutParams.height = (int) getResources().getDimension(R.dimen.mini_player_height);
                layoutParams.width = (int) getResources().getDimension(R.dimen.mini_player_width);

                layoutParamsBody.height = layoutParams.height + Utils.convertPointToPixels(32);
                layoutParamsBody.width = layoutParams.width;
                if (_prevX != 0 && _prevY != 0) {
                    layoutParamsBody.leftMargin = (int) _prevX;
                    layoutParamsBody.topMargin = (int) _prevY;
                }
                _binding.layoutPlayerVideo.setLayoutParams(layoutParamsBody);

                _binding.layoutPlayer.setSizeChangedListener(onSizeChangedListener);
                _binding.layoutPlayer.setVisibility(View.VISIBLE);

                _binding.layoutPlayerVideo.setClickable(true);
                _binding.layoutPlayerVideo
                        .setOnTouchListener(_ontouchListener);

                initPlayerEvents();

                if (!_isPlayerLayoutInitiated) {
                    initPlayerLayout(_binding.layoutPlayer.getMeasuredWidth(),
                            _binding.layoutPlayer.getMeasuredHeight(),
                            _binding.layoutPlayer.getMeasuredWidth(),
                            _binding.layoutPlayer.getMeasuredHeight());
                }
                _youtubePlayerApiView.setFullScreen(false);
                _binding.layoutPlayerBottom.setPadding(0, 0, 0, 0);
            } else {
                _binding.layoutPlayerBottom.setVisibility(View.VISIBLE);
                _binding.mainActivityPlayerHeader.setVisibility(View.GONE);

                if (isLandscapeScreen) {
                    _youtubePlayerApiView.setFullScreen(true);
                } else {
                    _youtubePlayerApiView.setFullScreen(false);
                }

                layoutParams.height = isLandscapeScreen ? ViewGroup.LayoutParams.MATCH_PARENT
                        : Utils.getPlayerHeightPortrait();
                layoutParams.width = isLandscapeScreen ? ViewGroup.LayoutParams.MATCH_PARENT
                        : Utils.getScreenWidth();


                layoutParamsBody.width = isLandscapeScreen ? ViewGroup.LayoutParams.MATCH_PARENT
                        : Utils.getScreenWidth();
                layoutParamsBody.height = isLandscapeScreen ? ViewGroup.LayoutParams.MATCH_PARENT
                        : Utils.getPlayerHeightPortrait();
                _binding.layoutPlayerBottom.setPadding(0,
                        Utils.getPlayerHeightPortrait(), 0, 0);
//                _binding.layoutPlayer.mEventListener = null;
                _binding.layoutPlayerVideo.setOnTouchListener(null);
                layoutParamsBody.leftMargin = 0;
                layoutParamsBody.topMargin = 0;
                if (isLandscapeScreen) {
                    setNavVisibility(false);
                } else {
                    setNavVisibility(true);
                }
            }

            _youtubePlayerApiView.visibleFullScreenButton(!isMiniPlayer);
            _binding.layoutPlayer.setVisibility(View.VISIBLE);
            _binding.activityMainPlayerVideo.setLayoutParams(layoutParams);
            _binding.layoutPlayerVideo.setLayoutParams(layoutParamsBody);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void initPlayerEvents() {
        _binding.buttonPlayerMaximize.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                switchPlayerLayout(false);
            }
        });

        _binding.buttonPlayerClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                closePlayer();

            }
        });
    }

    public void closePlayer() {
        try {
            _isSmallPlayer = false;
            _isPlayerShowing = false;

            _binding.layoutPlayer.setVisibility(View.GONE);
            if (_youtubePlayerApiView != null) {
                _youtubePlayerApiView.stop();
                _youtubePlayerApiView = null;
                _binding.activityMainPlayerVideo.removeAllViews();
            }

            handleLockScreenEvent();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void handleLockScreenEvent() {
        try {
            if (_isPlayerShowing) {
                getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void onYoutubeApiOrientationChanged(Configuration newConfig) {
        try {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (_youtubePlayerApiView != null && !_isSmallPlayer) {
                    _youtubePlayerApiView.setFullScreen(true);
                }
            } else {
                if (_youtubePlayerApiView != null && !_isSmallPlayer) {
                    _youtubePlayerApiView.setFullScreen(false);
                }
            }
            switchPlayerLayout(_isSmallPlayer);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    CustomRelativeLayout.ISizeChangedListener onSizeChangedListener = new CustomRelativeLayout.ISizeChangedListener() {

        @Override
        public void onLayoutChanged(int xNew, int yNew, int xOld, int yOld) {
            initPlayerLayout(xNew, yNew, xOld, yOld);

        }
    };

    private void initPlayerLayout(int xNew, int yNew, int xOld, int yOld) {
        if (!_isSmallPlayer) {
            return;
        }
        try {
            FrameLayout.LayoutParams layout = (FrameLayout.LayoutParams) _binding.layoutPlayerVideo
                    .getLayoutParams();
            if (!_isPlayerLayoutInitiated) {
                _oldX = _binding.layoutPlayer.getMeasuredWidth()
                        - getResources().getDimensionPixelSize(R.dimen.mini_player_width) - Utils.convertPointToPixels(24);
                _oldY = _binding.layoutPlayer.getMeasuredHeight()
                        - getResources().getDimensionPixelSize(R.dimen.mini_player_height) - Utils.convertPointToPixels(36)
                        - Utils.convertPointToPixels(112);

                layout.leftMargin = (int) _oldX;
                layout.topMargin = (int) _oldY;
                _binding.layoutPlayerVideo.setLayoutParams(layout);
                return;
            }

            int x = layout.leftMargin;
            int y = layout.topMargin;

            if (x == 0 && y == 0) {
                layout.leftMargin = (int) _prevX;
                layout.topMargin = (int) _prevY;
            } else {
                if (_isOrientationChanged) {
                    layout.leftMargin = (x + _binding.layoutPlayerVideo.getWidth() / 2)
                            * xNew / xOld
                            - _binding.layoutPlayerVideo.getWidth() / 2;
                    layout.topMargin = (y + _binding.layoutPlayerVideo.getHeight() / 2)
                            * yNew / yOld
                            - _binding.layoutPlayerVideo.getHeight() / 2;
                }
                _isOrientationChanged = false;
            }
            if (_binding.layoutPlayerVideo.getWidth() + layout.leftMargin > _binding.layoutPlayer
                    .getMeasuredWidth()
                    && _binding.layoutPlayerVideo.getWidth() < _binding.layoutPlayer
                    .getMeasuredWidth()) {
                layout.leftMargin = _binding.layoutPlayer.getMeasuredWidth()
                        - _binding.layoutPlayerVideo.getWidth();
            }
            if (layout.leftMargin < 0) {
                layout.leftMargin = 0;
            }

            if (_binding.layoutPlayerVideo.getHeight() + layout.topMargin > _binding.layoutPlayer
                    .getMeasuredHeight()
                    && _binding.layoutPlayer.getMeasuredHeight() > _binding.layoutPlayerVideo
                    .getHeight()) {
                layout.topMargin = _binding.layoutPlayer.getMeasuredHeight()
                        - _binding.layoutPlayerVideo.getHeight();
            }
            if (layout.topMargin < 0) {
                layout.topMargin = 0;
            }

            _binding.layoutPlayerVideo.setLayoutParams(layout);
            _prevX = layout.leftMargin;
            _prevY = layout.topMargin;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    View.OnTouchListener _ontouchListener = new View.OnTouchListener() {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:

                    _oldX = event.getX();
                    _oldY = event.getY();
                    break;
                case MotionEvent.ACTION_UP:

                case MotionEvent.ACTION_POINTER_UP:
                    break;

                case MotionEvent.ACTION_MOVE:
                    float _dX = event.getX() - _oldX;
                    float _dY = event.getY() - _oldY;

                    float _posX = _prevX + _dX;
                    float _posY = _prevY + _dY;
                    if (_posX < 0) {
                        _posX = 0;
                    }
                    if (_posY < 0) {
                        _posY = 0;
                    }
                    if ((_posX + v.getWidth()) > _binding.layoutPlayer.getWidth()) {
                        _posX = _binding.layoutPlayer.getWidth() - v.getWidth();
                    }
                    if ((_posY + v.getHeight()) > _binding.layoutPlayer.getHeight()) {
                        _posY = _binding.layoutPlayer.getHeight() - v.getHeight();
                    }

                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) v
                            .getLayoutParams();
                    layoutParams.leftMargin = (int) _posX;
                    layoutParams.topMargin = (int) _posY;
                    v.setLayoutParams(layoutParams);
                    _prevX = _posX;
                    _prevY = _posY;
                    _isPlayerLayoutInitiated = true;
                    break;
            }
            return false;
        }
    };

    public boolean isPlayerPlaying() {
        return _isPlayerShowing && !_isSmallPlayer;
    }

    public void minimizePlayer() {
        try {
            switchPlayerLayout(true);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mIsOrientationChanged = true;

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setNavVisibility(false);
        } else {
            setNavVisibility(true);
        }

        if (!_isPlayerShowing) {
            return;
        }
        if (!_isSmallPlayer) {
            _isPlayerLayoutInitiated = false;
        }
        _isOrientationChanged = true;

        onYoutubeApiOrientationChanged(newConfig);
    }

    public void setNavVisibility(boolean visible) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (!visible) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        this.getWindow().setAttributes(attrs);
    }

    private boolean isRootLevel() {
        try {
            if (getSupportFragmentManager().getFragments() == null) {
                return true;
            }
            int count = 0;
            for (int i = 0; i < getSupportFragmentManager().getFragments()
                    .size(); ++i) {
                if (getSupportFragmentManager().getFragments().get(i) != null && getSupportFragmentManager().getFragments().get(i) instanceof BaseFragment) {
                    count++;
                }
            }
            return count <= 1;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setHeader() {
        boolean isRoot = isRootLevel();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (isRoot) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
        } else {
            getSupportActionBar().setHomeAsUpIndicator(0);
        }
        Fragment fragment = getCurrentFrag();
        if (fragment instanceof BaseFragment) {
            String title = ((BaseFragment) fragment).getTitle();
            setTitle((BaseFragment) fragment);
            if (_searchView != null) {
                _isInSearchMode = title.equals(Utils.getString(R.string.search));
                visibleSearchView();
            }
        }
    }

    public void setTitle(BaseFragment baseFragment) {
        _binding.toolbarText.setText(baseFragment.getTitle());
        Constants.RightTitleType rightTitleType = baseFragment.getRightTitleType();
        _binding.toolbarRight.setVisibility(rightTitleType == Constants.RightTitleType.Category ? View.VISIBLE : View.GONE);
        _binding.toolbarRight.setText(baseFragment.getRightTitle());
        View.OnClickListener clickListener = baseFragment.getGetRightEventListener();
        _binding.toolbarRight.setOnClickListener(clickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        _menu = menu;
        getMenuInflater().inflate(R.menu.main_search, menu);
        _searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        _searchView.setIconified(false);

        visibleSearchView();
        return true;
    }

    private void visibleSearchView() {
        if (_isInSearchMode) {
            _searchView.setVisibility(View.VISIBLE);
            _menu.findItem(R.id.action_search).setVisible(true);
        } else {
            _searchView.setVisibility(View.INVISIBLE);
            _menu.findItem(R.id.action_search).setVisible(false);
        }
    }
}

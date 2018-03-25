package com.hteck.playtube.fragment;

import java.util.AbstractMap;
import java.util.Vector;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;

import com.hteck.playtube.R;
import com.hteck.playtube.adapter.CustomArrayAdapter;
import com.hteck.playtube.adapter.YoutubeByPageAdapter;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.HttpDownload;
import com.hteck.playtube.common.IHttplistener;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.service.CategoryService;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.CategoryInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.service.YoutubeHelper;
import com.hteck.playtube.view.LoadingView;

public class PopularView extends Fragment implements
        AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    private ViewGroup _contentView, _mainView;
    GridView _gridView;
    private Vector<YoutubeInfo> _youtubeList = new Vector<>(),
            _youtubeListLoading = new Vector<>();
    private Vector<CategoryInfo> _categoryList;
    YoutubeByPageAdapter _adapter;
    private LoadingView _busyView;
    View _viewNoItem;
    private String _nextPageToken = "";
    private boolean _isLoading = false;
    private View _viewReload;
    private final String[] TIMELIST = Utils.getString(R.string.time_list).split("[,]");
    private final String[] ORDERBYLIST = Utils.getString(R.string.order_by_list).split("[,]");
    private Spinner _spinnerOrderBy;
    private Spinner _spinnerTimeList;
    private int _selectedIndexOrderBy;
    private int _selectedIndexTime;
    private HttpDownload _httpGetFile;
    private int _selectedCategoryIndex = 0;

    public static PopularView newInstance() {
        PopularView popularView = new PopularView();
        return popularView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (_categoryList == null) {
            _categoryList = CategoryService.getGenreListInfo().getValue();
        }
        return createView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        _gridView.setColumnWidth(Utils.getYoutubeWidth());
    }

    private View createView() {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        _mainView = (ViewGroup) inflater.inflate(R.layout.polular_view, null);

        setupSpinner();

        _gridView = (GridView) _mainView.findViewById(R.id.popular_gridview);
        _contentView = (ViewGroup) _mainView.findViewById(R.id.popular_layout_content);
        _adapter = new YoutubeByPageAdapter(_youtubeList);
        _gridView.setAdapter(_adapter);
        _gridView.setColumnWidth(Utils.getYoutubeWidth());
        _gridView.setOnItemClickListener(this);
        _gridView.setOnScrollListener(this);

        loadData();
        return _mainView;
    }

    void setupSpinner() {
        _spinnerTimeList = (Spinner) _mainView
                .findViewById(R.id.popular_spn_time);

        ArrayAdapter<String> adapter = new CustomArrayAdapter(
                MainActivity.getInstance(), TIMELIST);

        _selectedIndexTime = Utils.getPrefValue(Constants.POPULAR_TIME_LIST,
                TIMELIST.length - 1);

        _spinnerTimeList.setAdapter(adapter);
        _spinnerTimeList
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int index, long arg3) {
                        if (index != _selectedIndexTime) {
                            _selectedIndexTime = index;
                            Utils.savePref(Constants.POPULAR_SORT_BY,
                                    _selectedIndexTime);
                            loadData();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub

                    }
                });
        _spinnerTimeList.setSelection(_selectedIndexTime);

        _spinnerOrderBy = (Spinner) _mainView
                .findViewById(R.id.popular_spn_sortBy);

        ArrayAdapter<String> adapterOrderBy = new CustomArrayAdapter(
                MainActivity.getInstance(), ORDERBYLIST);

        _spinnerOrderBy.setAdapter(adapterOrderBy);
        _spinnerOrderBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View view,
                                       int index, long id) {
                if (index != _selectedIndexOrderBy) {
                    _selectedIndexOrderBy = index;
                    Utils.savePref(Constants.POPULAR_SORT_BY,
                            _selectedIndexOrderBy);
                    loadData();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        _selectedIndexOrderBy = Utils.getPrefValue(
                Constants.POPULAR_SORT_BY, 0);
        _spinnerOrderBy.setSelection(_selectedIndexOrderBy);
    }

    public void setDataSource(Vector<YoutubeInfo> videoList, boolean isClearAction) {
        if (!isClearAction && videoList.size() == 0) {
            if (_viewNoItem == null) {
                _viewNoItem = Utils.createNoItemView(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.no_youtube_found));
            }
            _contentView.addView(_viewNoItem);
        } else {
            if (_viewNoItem != null) {
                _contentView.removeView(_viewNoItem);
            }
        }
        _youtubeList = videoList;
        _adapter.setIsNetworkError(false);
        _adapter.setDataSource(_youtubeList);
        _adapter.notifyDataSetChanged();
    }

    public void loadData() {
        cancelAllRequests();

        _isLoading = true;
        _nextPageToken = "";
        _youtubeList = new Vector<>();
        setDataSource(_youtubeList, true);
        loadVideoData();
        showProgressBar();
    }

    public void loadMore() {
        if (_isLoading || Utils.stringIsNullOrEmpty(_nextPageToken)) {
            return;
        }

        _isLoading = true;
        loadVideoData();
    }

    private void loadVideoData() {
        String url;
        if (_spinnerTimeList.getSelectedItemPosition() == _spinnerTimeList
                .getCount() - 1) {
            if (_selectedCategoryIndex == 0) {
                url = String
                        .format(PlayTubeController.getConfigInfo().loadYoutubeVideosByAllUrl,
                                _nextPageToken, YoutubeHelper.getSortByValue(_spinnerOrderBy
                                        .getSelectedItemPosition()));
            } else {
                url = String
                        .format(PlayTubeController.getConfigInfo().loadYoutubeVideosByCategoryUrl,
                                _categoryList.elementAt(_selectedCategoryIndex).id, _nextPageToken, YoutubeHelper.getSortByValue(_spinnerOrderBy
                                        .getSelectedItemPosition()));
            }
        } else {
            if (_selectedCategoryIndex == 0) {
                url = String
                        .format(PlayTubeController.getConfigInfo().loadYoutubeVideosByAllCategoriesAndTimeUrl,
                                _nextPageToken, YoutubeHelper
                                        .getSortByValue(_spinnerOrderBy
                                                .getSelectedItemPosition()),
                                YoutubeHelper.getDateQuery(_spinnerTimeList
                                        .getSelectedItemPosition()));
            } else {
                url = String
                        .format(PlayTubeController.getConfigInfo().loadYoutubeVideosByCategoryAndTimeUrl,
                                _categoryList.elementAt(_selectedCategoryIndex).id, _nextPageToken, YoutubeHelper
                                        .getSortByValue(_spinnerOrderBy
                                                .getSelectedItemPosition()),
                                YoutubeHelper.getDateQuery(_spinnerTimeList
                                        .getSelectedItemPosition()));
            }
        }

        IHttplistener eventHandler = new IHttplistener() {

            @Override
            public void returnResult(Object sender,
                                     final byte[] data, final ResultType resultType) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (resultType == IHttplistener.ResultType.Done) {
                            try {
                                String s = new String(data);

                                AbstractMap.SimpleEntry<String, Vector<YoutubeInfo>> searchResult = YoutubeHelper.getVideoListInfo(s);
                                _youtubeListLoading = searchResult.getValue();
                                _nextPageToken = searchResult.getKey();
                                if (_youtubeListLoading.size() == 0) {
                                    if (_youtubeList.size() > 0
                                            && _youtubeList.elementAt(_youtubeList
                                            .size() - 1) == null) {
                                        _youtubeList.remove(_youtubeList.size() - 1);
                                    }

                                    setDataSource(_youtubeList, false);
                                    hideProgressBar();
                                    _isLoading = false;
                                } else {
                                    loadVideosInfo();
                                }

                            } catch (Throwable e) {
                                e.printStackTrace();
                                hideProgressBar();
                                _isLoading = false;
                            }
                        }

                        if (resultType != ResultType.Done) {
                            Utils.showMessage(Utils.getString(R.string.network_error));
                            hideProgressBar();
                            _isLoading = false;
                            if (_youtubeList.size() == 0) {
                                handleNetworkError();
                            } else {
                                _adapter.setIsNetworkError(true);
                                _adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }
        };
        _httpGetFile = new HttpDownload(url, eventHandler);
        _httpGetFile.start();
    }

    private void loadVideosInfo() {
        String videoIds = "";
        for (YoutubeInfo videoInfo : _youtubeListLoading) {
            if (videoIds == "") {
                videoIds = videoInfo.id;
            } else {
                videoIds = videoIds + "," + videoInfo.id;
            }
        }
        String url = String.format(PlayTubeController.getConfigInfo().loadVideosInfoUrl,
                videoIds);
        _httpGetFile = new HttpDownload(url,
                eventListenerDownloadVideosInfoCompleted);
        _httpGetFile.start();

    }

    private void cancelAllRequests() {
        if (_httpGetFile != null) {
            _httpGetFile.exit();
        }
    }

    IHttplistener eventListenerDownloadVideosInfoCompleted = new IHttplistener() {

        @Override
        public void returnResult(Object sender, final byte[] data, final ResultType resultType) {
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (resultType == ResultType.Done) {
                        try {
                            String s = new String(data);

                            YoutubeHelper.populateYoutubeListInfo(
                                    _youtubeListLoading, s);

                            if (_youtubeList.size() > 0
                                    && _youtubeList.lastElement() == null) {
                                _youtubeList.remove(_youtubeList.size() - 1);
                            }

                            _youtubeList.addAll(YoutubeHelper
                                    .getAvailableVideos(_youtubeListLoading));
                            if (!Utils.stringIsNullOrEmpty(_nextPageToken)) {
                                _youtubeList.add(null);
                            }
                            setDataSource(_youtubeList, false);

                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        hideProgressBar();
                        _isLoading = false;
                    }

                    if (resultType == ResultType.ConnectionError) {
                        Utils.showMessage(Utils
                                .getString(R.string.network_error));
                        hideProgressBar();
                        _isLoading = false;
                        if (_youtubeList.size() == 0) {
                            handleNetworkError();
                        } else {
                            _adapter.setIsNetworkError(true);
                            _adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    };

    private void hideProgressBar() {
        Utils.hideProgressBar(_contentView, _busyView);
    }

    private void showProgressBar() {
        _busyView = Utils.showProgressBar(_contentView, _busyView);
    }

    private void handleNetworkError() {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        if (_viewReload == null) {
            _viewReload = (ViewGroup) inflater.inflate(R.layout.retry_view, null);
            _contentView.addView(_viewReload);
        }
        _viewReload.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (_viewReload != null) {
                    _contentView.removeView(_viewReload);
                    _viewReload = null;
                }
                loadData();
                return false;
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
        try {
            if (index == _youtubeList.size() - 1 && _youtubeList.lastElement() == null) {
                if (_adapter.getIsNetworkError()) {
                    _adapter.setIsNetworkError(false);
                    _adapter.notifyDataSetChanged();
                    loadMore();
                }
            } else {
                Vector<YoutubeInfo> youtubeList = new Vector<>();
                for (YoutubeInfo youtubeInfo : _youtubeList) {
                    if (youtubeInfo != null) {
                        youtubeList.add(youtubeInfo);
                    }
                }

                MainActivity.getInstance().playYoutube(_youtubeList.elementAt(index), youtubeList, true, false);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (_gridView.getLastVisiblePosition() == _gridView.getAdapter()
                    .getCount() - 1) {
                if (_youtubeList.size() > 0
                        && _youtubeList.elementAt(_youtubeList.size() - 1) == null) {
                    if (!_adapter.getIsNetworkError()) {
                        loadMore();
                    }
                }
            }
        }

    }

//        @Override
//        public View.OnClickListener getOnClickListener() {
//            return new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    selectCategory();
//                }
//            };
//        }
//
//        @Override
//        public String getLeftTitle() {
//            try {
//                if (_categoryList == null) {
//                    _categoryList = CategoryHelper.getGenreInfos().getValue();
//                }
//                return _categoryList.elementAt(_selectedCategoryIndex).title;
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//            return "";
//        }
//
//        @Override
//        public String getTitle() {
//            return MainActivity.getInstance().getString(R.string.popular);
//        }
//
//        @Override
//        public LeftHomeTitleType getLeftTitleType() {
//            return LeftHomeTitleType.Text;
//        }

//        public void selectCategory() {
//            final CategoryListView v = new CategoryListView(
//                    MainActivity.getInstance(), _selectedCategoryIndex);
//            AlertDialog.Builder builder = new AlertDialog.Builder(
//                    MainActivity.getInstance());
//            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int whichButton) {
//                    _selectedCategoryIndex = v.getSelectedIndex();
//                    MainActivity.getInstance().updateHomeTitle(null);
//                    loadData();
//                }
//            });
//
//            builder.setNegativeButton("Cancel",
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int whichButton) {
//                            dialog.cancel();
//                        }
//                    });
//            builder.setTitle(MainActivity.getInstance().getString(
//                    R.string.select_category));
//
//            builder.setView(v);
//            final AlertDialog dialog = builder.create();
//
//            dialog.show();
//        }
}

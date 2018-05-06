package com.hteck.playtube.fragment;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import com.hteck.playtube.R;
import com.hteck.playtube.adapter.CustomArrayAdapter;
import com.hteck.playtube.adapter.YoutubeByPageAdapter;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.CustomHttpOk;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.databinding.PopularViewBinding;
import com.hteck.playtube.service.CategoryService;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.CategoryInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.service.CustomCallback;
import com.hteck.playtube.service.YoutubeHelper;
import com.hteck.playtube.view.CategoryListView;
import com.hteck.playtube.view.LoadingView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class PopularView extends BaseFragment implements
        AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    private ArrayList<YoutubeInfo> _youtubeList = new ArrayList<>(),
            _youtubeListLoading = new ArrayList<>();
    private Vector<CategoryInfo> _categoryList;
    YoutubeByPageAdapter _adapter;
    private LoadingView _busyView;
    View _viewNoItem;
    private String _nextPageToken = "";
    private boolean _isLoading = false;
    private View _viewReload;
    private final String[] TIMELIST = Utils.getString(R.string.time_list).split("[,]");
    private final String[] ORDERBYLIST = Utils.getString(R.string.order_by_list).split("[,]");
    private int _selectedIndexOrderBy;
    private int _selectedIndexTime;
    private CustomHttpOk _httpOk;
    private int _selectedCategoryIndex = 0;
    private PopularViewBinding _binding;
    private static View _mainView;

    public static PopularView newInstance() {
        PopularView popularView = new PopularView();
        return popularView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity.getInstance().setHeader();

        if (_mainView != null) {
            return _mainView;
        }
        if (_categoryList == null) {
            _categoryList = CategoryService.getGenreListInfo().getValue();
        }

        _mainView = createView(container);
        return _mainView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        _binding.popularGridview.setColumnWidth(Utils.getYoutubeWidth());
    }

    private View createView(ViewGroup container) {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        _binding = DataBindingUtil.inflate(inflater, R.layout.popular_view, container, false);
        setupSpinner();

        _adapter = new YoutubeByPageAdapter(_youtubeList, Constants.YoutubeListType.Popular);
        _binding.popularGridview.setAdapter(_adapter);
        _binding.popularGridview.setColumnWidth(Utils.getYoutubeWidth());
        _binding.popularGridview.setOnItemClickListener(this);
        _binding.popularGridview.setOnScrollListener(this);

        loadData();
        return _binding.getRoot();
    }

    void setupSpinner() {
        ArrayAdapter<String> adapter = new CustomArrayAdapter(
                MainActivity.getInstance(), TIMELIST);

        _selectedIndexTime = Utils.getPrefValue(Constants.POPULAR_TIME_LIST,
                TIMELIST.length - 1);

        _binding.popularSpnTime.setAdapter(adapter);
        _binding.popularSpnTime
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
        _binding.popularSpnTime.setSelection(_selectedIndexTime);

        ArrayAdapter<String> adapterOrderBy = new CustomArrayAdapter(
                MainActivity.getInstance(), ORDERBYLIST);

        _binding.popularSpnSortBy.setAdapter(adapterOrderBy);
        _binding.popularSpnSortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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
        _binding.popularSpnSortBy.setSelection(_selectedIndexOrderBy);
    }

    public void setDataSource(ArrayList<YoutubeInfo> videoList, boolean isClearAction) {
        if (!isClearAction && videoList.size() == 0) {
            if (_viewNoItem == null) {
                _viewNoItem = Utils.createNoItemView(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.no_youtube_found));
            }
            ((ViewGroup) _binding.getRoot()).addView(_viewNoItem);
        } else {
            if (_viewNoItem != null) {
                ((ViewGroup) _binding.getRoot()).removeView(_viewNoItem);
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
        _youtubeList = new ArrayList<>();
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
        if (_binding.popularSpnTime.getSelectedItemPosition() == _binding.popularSpnTime
                .getCount() - 1) {
            if (_selectedCategoryIndex == 0) {
                url = String
                        .format(PlayTubeController.getConfigInfo().loadYoutubeVideosByAllUrl,
                                _nextPageToken, YoutubeHelper.getSortByValue(_binding.popularSpnSortBy
                                        .getSelectedItemPosition()));
            } else {
                url = String
                        .format(PlayTubeController.getConfigInfo().loadYoutubeVideosByCategoryUrl,
                                _categoryList.elementAt(_selectedCategoryIndex).id, _nextPageToken, YoutubeHelper.getSortByValue(_binding.popularSpnSortBy
                                        .getSelectedItemPosition()));
            }
        } else {
            if (_selectedCategoryIndex == 0) {
                url = String
                        .format(PlayTubeController.getConfigInfo().loadYoutubeVideosByAllCategoriesAndTimeUrl,
                                _nextPageToken, YoutubeHelper
                                        .getSortByValue(_binding.popularSpnSortBy
                                                .getSelectedItemPosition()),
                                YoutubeHelper.getDateQuery(_binding.popularSpnTime
                                        .getSelectedItemPosition()));
            } else {
                url = String
                        .format(PlayTubeController.getConfigInfo().loadYoutubeVideosByCategoryAndTimeUrl,
                                _categoryList.elementAt(_selectedCategoryIndex).id, _nextPageToken, YoutubeHelper
                                        .getSortByValue(_binding.popularSpnSortBy
                                                .getSelectedItemPosition()),
                                YoutubeHelper.getDateQuery(_binding.popularSpnTime
                                        .getSelectedItemPosition()));
            }
        }

        _httpOk = new CustomHttpOk(url, new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                });

            }

            @Override
            public void onResponse(final Response response) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String s = response.body().string();

                            AbstractMap.SimpleEntry<String, ArrayList<YoutubeInfo>> searchResult = YoutubeHelper.getVideoListInfo(s);
                            _youtubeListLoading = searchResult.getValue();
                            _nextPageToken = searchResult.getKey();
                            if (_youtubeListLoading.size() == 0) {
                                if (_youtubeList.size() > 0
                                        && _youtubeList.get(_youtubeList
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
                });

            }
        });
        _httpOk.start();
    }

    private void loadVideosInfo() {
        String videoIds = "";
        for (YoutubeInfo y : _youtubeListLoading) {
            if (videoIds == "") {
                videoIds = y.id;
            } else {
                videoIds = videoIds + "," + y.id;
            }
        }
        String url = String.format(PlayTubeController.getConfigInfo().loadVideosInfoUrl,
                videoIds);

        _httpOk = new CustomHttpOk(url, new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                });

            }

            @Override
            public void onResponse(final Response response) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String s = response.body().string();

                            YoutubeHelper.populateYoutubeListInfo(
                                    _youtubeListLoading, s);

                            if (_youtubeList.size() > 0
                                    && _youtubeList.get(_youtubeList.size() - 1) == null) {
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
                });

            }
        });
        _httpOk.start();
    }

    private void cancelAllRequests() {
        if (_httpOk != null) {
            _httpOk.cancel();
        }
    }

    private void hideProgressBar() {
        Utils.hideProgressBar(_binding.popularLayoutContent, _busyView);
    }

    private void showProgressBar() {
        _busyView = Utils.showProgressBar(_binding.popularLayoutContent, _busyView);
    }

    private void handleNetworkError() {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        if (_viewReload == null) {
            _viewReload = (ViewGroup) inflater.inflate(R.layout.retry_view, null);
            _binding.popularLayoutContent.addView(_viewReload);
        }
        _viewReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_viewReload != null) {
                    _binding.popularLayoutContent.removeView(_viewReload);
                    _viewReload = null;
                }
                loadData();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
        try {
            if (index == _youtubeList.size() - 1 && _youtubeList.get(index) == null) {
                if (_adapter.getIsNetworkError()) {
                    _adapter.setIsNetworkError(false);
                    _adapter.notifyDataSetChanged();
                    loadMore();
                }
            } else {
                ArrayList<YoutubeInfo> youtubeList = new ArrayList<>();
                for (YoutubeInfo youtubeInfo : _youtubeList) {
                    if (youtubeInfo != null) {
                        youtubeList.add(youtubeInfo);
                    }
                }

                MainActivity.getInstance().playYoutube(_youtubeList.get(index), youtubeList, true);
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
            if (_binding.popularGridview.getLastVisiblePosition() == _binding.popularGridview.getAdapter()
                    .getCount() - 1) {
                if (_youtubeList.size() > 0
                        && _youtubeList.get(_youtubeList.size() - 1) == null) {
                    if (!_adapter.getIsNetworkError()) {
                        loadMore();
                    }
                }
            }
        }

    }

    @Override
    public String getTitle() {
        return Utils.getString(R.string.explore);
    }

    @Override
    public String getRightTitle() {
        if (_categoryList == null) {
            _categoryList = CategoryService.getGenreListInfo().getValue();
        }
        return _categoryList.get(_selectedCategoryIndex).title;
    }

    @Override
    public Constants.RightTitleType getRightTitleType() {
        return Constants.RightTitleType.Category;
    }

    @Override
    public View.OnClickListener getGetRightEventListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCategory();
            }
        };
    }
    public void selectCategory() {
        final CategoryListView v = new CategoryListView(
                MainActivity.getInstance(), _selectedCategoryIndex);
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.getInstance(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                _selectedCategoryIndex = v.getSelectedIndex();
                MainActivity.getInstance().setHeader();
                loadData();
            }
        });

        builder.setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        builder.setTitle(Utils.getString(
                R.string.select_a_category));

        builder.setView(v);
        final AlertDialog dialog = builder.create();

        dialog.show();
    }
}

package com.hteck.playtube.fragment;

import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.SearchView.OnQueryTextListener;
import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.SearchCursorAdapter;
import com.hteck.playtube.common.HttpDownload;
import com.hteck.playtube.common.IHttplistener;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.view.YoutubeListView;

import org.json.JSONArray;

public class SearchView extends BaseFragment {
    private android.support.v7.widget.SearchView _editTextSearch;
    private static String _query = "";
    private static YoutubeListView _youtubeListView;
    private String[] _suggestions = new String[]{};
    private MatrixCursor mSearchCursor;
    SearchCursorAdapter _adapter;
    public static SearchView newInstance(
            android.support.v7.widget.SearchView editTextSearch) {
        SearchView v = new SearchView();
        v._editTextSearch = editTextSearch;
        return v;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        MainActivity.getInstance().updateHomeIcon();
        if (_youtubeListView == null) {
            _youtubeListView = new YoutubeListView(getContext());
        } else {
            ViewGroup parent = (ViewGroup) _youtubeListView.getParent();
            if (parent != null) {
                parent.removeView(_youtubeListView);
            }
        }
        initView();
        return _youtubeListView;
    }

    private void initView() {
        _editTextSearch.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    if (Utils.stringIsNullOrEmpty(query)) {
                        return false;
                    }

                    _query = query;

                    doSearch(true);
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    if (newText.equalsIgnoreCase(_query)) {
                        return true;
                    }
                    loadSuggestions(newText);
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                return true;
            }
        });

        _editTextSearch.setOnSuggestionListener(new android.support.v7.widget.SearchView.OnSuggestionListener() {

            @Override
            public boolean onSuggestionSelect(int position) {
                if (position >= 0 && position < mSearchCursor.getCount())
                    return true;

                return false;
            }

            @Override
            public boolean onSuggestionClick(int arg0) {
                try {
                    String query = _suggestions[arg0];
                    //
                    _editTextSearch.setQuery(query, true);
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                return false;
            }
        });
        _editTextSearch.setOnCloseListener(new android.support.v7.widget.SearchView.OnCloseListener() {

            @Override
            public boolean onClose() {
                try {
                    LayoutParams layoutParams = _editTextSearch
                            .getLayoutParams();
                    layoutParams.width = LayoutParams.WRAP_CONTENT;
                    _editTextSearch.setLayoutParams(layoutParams);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        if (!Utils.stringIsNullOrEmpty(_query)) {
            _editTextSearch.setQuery(_query, false);
        }

    }

    private void doSearch(boolean isNewSearch) {
        if (Utils.stringIsNullOrEmpty(_query.trim())) {
            return;
        }
        if (isNewSearch) {
            // Check if no view has focus:
            Utils.closeKeyboard();

            try {
                _suggestions = new String[]{};
                if (_xHttpGetFile != null) {
                    _xHttpGetFile.exit();
                }
                setSuggestionForSearchView();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        _youtubeListView.search(_query);
    }

    private void setSuggestionForSearchView() {
        // closeSearchCursor();

        String[] columnNames = { "_id", "text" };
        mSearchCursor = new MatrixCursor(columnNames);

        String[] temp = new String[2];
        int id = 0;
        for (String item : _suggestions) {
            temp[0] = Integer.toString(id++);
            temp[1] = item;
            mSearchCursor.addRow(temp);
        }
        if (_adapter == null) {
            _adapter = new SearchCursorAdapter(getActivity(), mSearchCursor,
                    this);
            _editTextSearch.setSuggestionsAdapter(_adapter);
        } else {
            _adapter.setDataSource(mSearchCursor);
        }
    }

    private void loadSuggestions(final String query) {
        String url = String.format(PlayTubeController.getConfigInfo().loadSuggestionsUrl,
                Utils.urlEncode(query));

        if (_xHttpGetFile != null) {
            _xHttpGetFile.exit();
        }
        _xHttpGetFile = new HttpDownload(url, new IHttplistener() {
            @Override
            public void returnResult(Object sender, final byte[] data, final ResultType resultType) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (resultType == ResultType.Done) {
                            try {

                                String result = new String(data);

                                int startIndex = result.indexOf('(');
                                result = result.substring(startIndex + 1,
                                        result.length() - 1);
                                JSONArray objResult = new JSONArray(result);
                                if (objResult.length() > 1) {
                                    JSONArray items = (JSONArray) objResult
                                            .get(1);
                                    int count = items.length() < 5 ? items
                                            .length() : 5;
                                    String[] suggestions = new String[count];
                                    for (int i = 0; i < count; ++i) {
                                        suggestions[i] = ((JSONArray) items
                                                .get(i)).get(0).toString()
                                                .replace("\"", "");
                                    }
                                    _suggestions = suggestions;
                                    setSuggestionForSearchView();

                                } else {
                                    _suggestions = new String[] {};
                                    setSuggestionForSearchView();
                                }

                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
        _xHttpGetFile.start();

    }

    HttpDownload _xHttpGetFile;
    public void selectQuery(String query) {
        try {
            _editTextSearch.setQuery(query, false);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    @Override
    public String getTitle() {
        return Utils.getString(R.string.search);
    }
}

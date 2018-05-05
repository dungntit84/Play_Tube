package com.hteck.playtube.service;

import com.squareup.okhttp.Callback;
public abstract class CustomCallback implements Callback {
    private Object _dataContext;
    public CustomCallback() { }

    public CustomCallback(Object dataContext) {
        _dataContext = dataContext;
    }

    public void setDataContext(Object dataContext) {
        this._dataContext = dataContext;
    }

    public Object getDataContext() {
        return this._dataContext;
    }
}

package com.hteck.playtube.service;

import com.squareup.okhttp.Callback;
public abstract class CustomCallback implements Callback {
    private Object _dataContext;
    public Exception _test;
    public CustomCallback() { }

    public CustomCallback(Object dataContext) {
        _dataContext = dataContext;
    }
    public CustomCallback(Object dataContext, Exception test) {
        _dataContext = dataContext;
        _test = test;
    }

    public void setDataContext(Object dataContext) {
        this._dataContext = dataContext;
    }

    public Object getDataContext() {
        return this._dataContext;
    }
}

package com.wtf.utils;

import android.app.Activity;

import com.litesuits.http.exception.HttpException;
import com.litesuits.http.listener.HttpListener;
import com.litesuits.http.response.Response;

/**
 * Created by liyan on 2016/12/15.
 */

public class MyHttpListener<T> extends HttpListener<T> {
    private Activity activity;
    public MyHttpListener(Activity activity) {
        this.activity = activity;
    }
    // disable listener when activity is null or be finished.
    @Override
    public boolean disableListener() {
        return activity == null || activity.isFinishing();
    }
    // handle by this by call super.onFailure()
    @Override
    public void onFailure(HttpException e, Response response) {
        // handle exception
        new MyHttpExceptHandler(activity).handleException(e);
    }
}

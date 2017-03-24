package com.wtf.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.wtf.WTFApplication;


/**
 * Activity抽象基类
 * Created by Hailey on 2016/3/24.
 */
public abstract class SBaseActivity extends FragmentActivity {
    // 上下文实例
    public Context _context;
    // 应用全局的实例
    public WTFApplication _application;
    // 核心层的Action实例


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _application = (WTFApplication) this.getApplication();
        _context = _application._context;
    }
}

package com.wtf.core;

import com.wtf.model.UpdateData;

import java.util.List;

/**
 * Created by liyan on 2016/11/22.
 */

public interface SystemAction {
    /**
     * 监测更新
     * @param versionCode
     * @param versionName
     * @param listener
     */
    void sysUpdate(int versionCode,String versionName, ActionCallbackListener<List<UpdateData>> listener);
}

package com.wtf.api.utils;

import com.wtf.model.UpdateData;

import java.util.List;

/**
 * Created by liyan on 2016/11/22.
 */

public interface  SystemApi {
    //检测更新
    String UPDATE="update";
    //String SERVER=" http://139.224.54.233:4567/v1/sys/apps/";
    String SERVER=" https://smartmattress.lesmarthome.com/v1/sys/apps/";

    /**
     * 检测更新
     * @param versionCode
     * @param versionName
     * @return
     */
    ApiResponse<List<UpdateData>> sysUpdate(int versionCode,String versionName);
}

package com.wtf.api.utils;

import com.google.gson.reflect.TypeToken;
import com.wtf.api.net.HttpUtil;
import com.wtf.model.UpdateData;
import com.wtf.model.UserData;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liyan on 2016/11/22.
 */

public class SystemApiImpl implements SystemApi {
    private final static int TIME_OUT_EVENT = 0;
    private HttpUtil httpUtil;

    public SystemApiImpl() {
        httpUtil = HttpUtil.getInstance();
    }
    @Override
    public ApiResponse<List<UpdateData>> sysUpdate(int versionCode,String versionName) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("version_code", String.valueOf(versionCode));
        paramMap.put("version_name",versionName);
        String method = SERVER + UPDATE;
        Type type = new TypeToken<ApiResponse<List<UpdateData>>>() {
        }.getType();
        try {
            ApiResponse<List<UpdateData>> response = httpUtil.getHandle(method, paramMap, type);
            if (response == null) {
                return new ApiResponse(TIME_OUT_EVENT);
            } else {
                return response;
            }
        } catch (IOException e) {
            return new ApiResponse(TIME_OUT_EVENT);
        }
    }
}

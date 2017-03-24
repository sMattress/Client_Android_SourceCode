package com.wtf.api.utils;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.wtf.api.net.HttpUtil;
import com.wtf.model.DeviceData;
import com.wtf.model.UserData;


import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hailey on 2016/5/4.
 */
public class UserApiImpl implements UserApi {
    private final static int TIME_OUT_EVENT = 0;
    private HttpUtil httpUtil;
    //private final static String SERVER_2 = "http://139.224.54.233:4567/v1/user/";
    private final static String SERVER_2 = "https://smartmattress.lesmarthome.com/v1/user/";
    private final static String SERVER_TEST = "http://192.168.1.103:4567/v1/user/";
    public UserApiImpl() {
        httpUtil = HttpUtil.getInstance();
    }

    @Override
    public ApiResponse<List<UserData>> userGetCode(String account) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("account", account);
        String method = SERVER_2 + CODE;
        Type type = new TypeToken<ApiResponse<List<UserData>>>() {
        }.getType();
        try {
            ApiResponse<List<UserData>> response = httpUtil.getHandle(method, paramMap, type);
            if (response == null) {
                return new ApiResponse(TIME_OUT_EVENT);
            } else {
                return response;
            }
        } catch (IOException e) {
            return new ApiResponse(TIME_OUT_EVENT);
        }
    }

    @Override
    public ApiResponse<List<UserData>> userLogin(String account, String password) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("password", password);
        paramMap.put("account", account);

        String method = SERVER_2 + LOGIN;
        Type type = new TypeToken<ApiResponse<List<UserData>>>() {
        }.getType();
        try {
            ApiResponse<List<UserData>> response = httpUtil.getHandle(method, paramMap, type);
            if (response == null) {
                return new ApiResponse(TIME_OUT_EVENT);
            } else {
                return response;
            }
        } catch (IOException e) {
            return new ApiResponse(TIME_OUT_EVENT);
        }
    }

    @Override
    public ApiResponse<Void> userRegister(String account, String password) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("password", password);
        paramMap.put("account", account);

        Log.i("paramMap",paramMap.get("password"));
        String method = SERVER_2 + REGISTER;
        Type type = new TypeToken<ApiResponse<Void>>() {
        }.getType();
        try {
            ApiResponse<Void> response = httpUtil.postHandle(1, method, paramMap, type);
            if (response == null) {
                return new ApiResponse(TIME_OUT_EVENT);
            } else {
                return response;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ApiResponse(TIME_OUT_EVENT);
        }
    }

    @Override
    public ApiResponse<List<DeviceData>> deviceList(String account, Long timestamp, String sign) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("timestamp", String.valueOf(timestamp));
        paramMap.put("account", account);
        paramMap.put("sign", sign);

        String method = SERVER_2 + DEVICES_LIST;
        Type type = new TypeToken<ApiResponse<List<DeviceData>>>() {
        }.getType();
        try {
            ApiResponse<List<DeviceData>> response = httpUtil.getHandle(method, paramMap, type);
            if (response == null) {
                return new ApiResponse(TIME_OUT_EVENT);
            } else {
                return response;
            }
        } catch (IOException e) {
            return new ApiResponse(TIME_OUT_EVENT);
        }
    }

    @Override
    public ApiResponse<List<UserData>> getBaseInfo(String account, Long timestamp, String sign) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("timestamp", String.valueOf(timestamp));
        paramMap.put("account", account);
        paramMap.put("sign", sign);

        String method = SERVER_2 + GET_BASE_INFO;
        Type type = new TypeToken<ApiResponse<List<UserData>>>() {
        }.getType();
        try {
            ApiResponse<List<UserData>> response = httpUtil.getHandle(method, paramMap, type);
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

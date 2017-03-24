package com.wtf.api.utils;

import com.alibaba.fastjson.JSONArray;

/**
 * Api响应结果的封装类
 * Created by Hailey on 2016/3/24.
 */

public class ApiResponse<T> {
    private int flag;    // 返回码，1为成功
    private T params;
    private String imgUrl;
    private int err_code;
    private JSONArray cause;

    // 构造函数，初始化flag
    public ApiResponse(int flag) {
        this.flag = flag;
    }

    // 判断结果是否成功
    public boolean isSuccess() {
        return flag==1;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getErrCode() {
        return err_code;
    }

    public void setErrCode(int err_code) {
        this.err_code = err_code;
    }

    public JSONArray getCause() {
        return cause;
    }

    public void setCause(JSONArray cause) {
        this.cause = cause;
    }

    public T getParams() {
        return params;
    }

    public void setParams(T params) {
        this.params = params;
    }
}



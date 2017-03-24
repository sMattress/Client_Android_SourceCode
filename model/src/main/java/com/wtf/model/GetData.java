package com.wtf.model;

import com.alibaba.fastjson.JSONArray;

/**
 * Created by liyan on 2016/11/15.
 */

public class GetData {
    private int flag;
    private JSONArray params;
    private int errCode;
    private String cause;

    public int getFlag() {
        return flag;
    }

    public GetData setFlag(int flag) {
        this.flag = flag;
        return this;
    }

    public JSONArray getParams() {
        return params;
    }


    public GetData setParams(JSONArray params) {
        this.params = params;
        return this;
    }

    public GetData addParams(Object param) {
        if (params==null){
            params=new JSONArray();
        }
        params.add(param);
        return this;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }
}

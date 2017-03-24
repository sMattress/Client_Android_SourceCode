package com.wtf.model;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by Hailey on 2016/4/25.
 */
public class UpdateData {

    private JSONObject latest;
    private String download;
    private  int versionCode;
    private String versionName;

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public JSONObject getLatest() {
        return latest;
    }

    public void setLatest(JSONObject latest) {
        this.latest = latest;
    }

    public int getVersionCode(){
        versionCode = latest.getInteger("version_code");
        return versionCode;
    }
    public String getVersionName(){
        versionName = latest.getString("version_name");
        return versionName;
    }
}

package com.wtf.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户的业务模型类，封装了用户的基本信息
 * Created by liyan on 2016/11/15.
 */

public class UserData implements Serializable {
    private int user_id;
    private String account;
    //使用验证码加密后的密码
    private String password;
    private String token;
    private long expires_in;
    private int code;
    private long tokenExpireTime;

    private String birthday;
    private String name;
    private int sex;
    private String img_data;
    private String img_type;
    private String img_url;

//    private boolean isHaveDevice=false;

    public List<DeviceData> mData = new ArrayList<>();
    private int selDeviceIndex = 0;

    public int getSelDeviceIndex() {
        return selDeviceIndex;
    }

    public void setSelDeviceIndex(int selDeviceIndex) {
        if(selDeviceIndex < mData.size())
            this.selDeviceIndex = selDeviceIndex;
        else
            this.selDeviceIndex = 0;
    }
    public DeviceData getSelDeviceData() {
        this.setSelDeviceIndex(this.selDeviceIndex);
        if(this.mData.size() > this.selDeviceIndex)
            return this.mData.get(this.selDeviceIndex);
        return null;
    }
    public String getSelDeviceName() {
        DeviceData sel = this.getSelDeviceData();
        if(sel != null)
            return sel.getDeviceName();
        return "unkownName";
    }
    public String getSelDeviceAlias() {
        DeviceData sel = this.getSelDeviceData();
        if(sel != null)
            return sel.getAlias();
        return "unkownAlias";
    }
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUserId() {
        return user_id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public String getToken() {
        return token;
    }


    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresIn() {
        return expires_in;
    }

    public void setExpiresIn(long expires_in) {
        this.expires_in = expires_in;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getImgUrl() {
        return img_url;
    }

    public void setImgUrl(String imgUrl) {
        this.img_url = imgUrl;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

//    public boolean getHaveDevice() {
//        return isHaveDevice;
//    }
//
//    public void setHaveDevice(boolean haveDevice) {
//        isHaveDevice = haveDevice;
//
//    }

    public long getTokenExpireTime() {
        return tokenExpireTime;
    }

    public void setTokenExpireTime(long tokenExpireTime) {
        this.tokenExpireTime = tokenExpireTime;
    }

    public boolean isHaveDevice() {
        if(mData.size() > 0)
            return true;
        return false;
    }
    public boolean isTokenExpire(long time) {
        if(tokenExpireTime > time)
            return true;
        return false;
    }
}

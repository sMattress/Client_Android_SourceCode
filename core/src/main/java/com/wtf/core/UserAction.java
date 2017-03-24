package com.wtf.core;


import com.wtf.model.DeviceData;
import com.wtf.model.GetData;
import com.wtf.model.UserData;

import java.util.List;

/**
 * 接收用户Api的Action
 * Created by Hailey on 2016/5/4.
 */
public interface UserAction {
    /**
     * 获取验证码
     * @param account
     * @param listener
     */
    void userGetCode(String account, ActionCallbackListener<List<UserData>> listener);
    /**
     * 用户登录
     * @param account
     * @param password
     * @param listener
     */
    void userLogin(String account, String password, ActionCallbackListener<List<UserData>> listener);

    /**
     * 用户注册
     * @param account
     * @param password
     * @param listener
     */
    void userRegister(String account, String password, ActionCallbackListener<Void> listener);

    /**
     * 获取设备列表
     * @param account
     * @param timestamp
     * @param sign
     */
    void deviceList(String account, Long timestamp, String sign,ActionCallbackListener<List<DeviceData>> listener);

    /**
     * 获取用户基本信息
     * @param account
     * @param timestamp
     * @param sign
     * @param listener
     */
    void getBaseInfo(String account, Long timestamp, String sign,ActionCallbackListener<List<UserData>> listener);

}

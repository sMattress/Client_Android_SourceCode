package com.wtf.api.utils;



import com.wtf.model.DeviceData;
import com.wtf.model.GetData;
import com.wtf.model.UserData;

import java.util.List;

/**
 * 用户模块接口
 * Created by Hailey on 2016/5/4.
 */
public interface UserApi {
    //获取验证码
    String CODE ="code";
    // 登录
    String LOGIN = "login";
    // 注册
    String REGISTER = "register";
    //获取用户个人信息
    String GET_BASE_INFO ="get/base_info";
    //获取设备列表
    String DEVICES_LIST ="device/list";
    /**
     * 获取验证码
     * @param account
     * @return
     */
    ApiResponse<List<UserData>> userGetCode(String account);

    /**
     * 用户登录
     * @param account
     * @param password
     * @return
     */
    ApiResponse<List<UserData>> userLogin(String account, String password);

    /**
     * 用户注册
     * @param account
     * @param password
     * @return
     */
    ApiResponse<Void> userRegister(String account, String password);


    /**
     * 获取设备列表
     * @param account
     * @param timestamp
     * @param sign
     * @return
     */
    ApiResponse<List<DeviceData>> deviceList(String account , Long timestamp, String sign);

    /**
     * 获取用户基本信息
     * @param account
     * @param timestamp
     * @param sign
     * @return
     */
    ApiResponse<List<UserData>> getBaseInfo(String account , Long timestamp, String sign);



}

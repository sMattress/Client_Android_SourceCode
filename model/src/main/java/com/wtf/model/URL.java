package com.wtf.model;

/**
 * Created by liyan on 2016/12/16.
 */

public interface URL {
    //检测更新
    String UPDATE=URL.SERVER_SYS+"apps/update";
    //获取验证码
    String CODE =URL.SERVER_USER + "code";
    // 登录
    String LOGIN = URL.SERVER_USER +"login";
    // 注册
    String REGISTER = URL.SERVER_USER +"register";
    //获取用户个人信息
    String GET_BASE_INFO =URL.SERVER_USER +"get/base_info";
    //更新个人信息
    String UPDATE_BASE_INFO =URL.SERVER_USER +"update/base_info";
    //忘记密码
    String FORGET_SECURE_INFO =URL.SERVER_USER +"forget/secure_info";
    //修改密码
    String UPDATE_SECURE_INFO =URL.SERVER_USER +"update/secure_info";

    //获取设备列表
    String DEVICES_LIST =URL.SERVER_USER +"device/list";
    //设备绑定
    String DEVICE_BIND =URL.SERVER_USER +"device/bind";
    //解绑设备
    String DEVICE_UNBIND =URL.SERVER_USER + "device/unbind";
    //更新设备名
    String DEVICE_UPDATE =URL.SERVER_USER + "device/update";
    //验证token
    String VALIDATE_TOKEN=URL.SERVER_SYS+"validate/token";

    //系统接口
    String SERVER_SYS="https://smartmattress.lesmarthome.com/v1/sys/";
    //用户接口
    String SERVER_USER = "https://smartmattress.lesmarthome.com/v1/user/";
}

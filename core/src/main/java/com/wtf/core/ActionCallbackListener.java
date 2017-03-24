package com.wtf.core;

import com.alibaba.fastjson.JSONArray;

/**
 * Action的处理结果回调监听器
 * Created by Hailey on 2016/3/24.
 */
public interface ActionCallbackListener<T> {
    /**
     * 成功时调用
     * @param data 返回的数据
     */
    public void onSuccess(T data);

    /**
     * 验证时调用
     * @param errorEvent 错误码
     * @param message 错误信息
     */
    public void onValidate(String errorEvent, String message);

    /**
     * 失败时调用
     * @param flag 标识码
     * @param errCode 错误码
     * @param cause 错误信息
     */
    public void onFailure(int flag,int errCode,JSONArray cause);
}

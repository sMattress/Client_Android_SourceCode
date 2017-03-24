package com.wtf.core;

import android.content.Context;
import android.os.AsyncTask;

import com.wtf.api.utils.ApiResponse;
import com.wtf.api.utils.UserApi;
import com.wtf.api.utils.UserApiImpl;
import com.wtf.model.DeviceData;
import com.wtf.model.UserData;

import java.util.List;

/**
 * Created by Hailey on 2016/5/4.
 */
public class UserActionImpl implements UserAction {
    private UserApi api;
    private Context context;

    public UserActionImpl(Context context) {
        this.context = context;
        this.api = new UserApiImpl();
    }

    @Override
    public void userGetCode(final String account, final ActionCallbackListener<List<UserData>> listener) {
        // 请求Api
        new AsyncTask<Void, Void, ApiResponse<List<UserData>>>() {
            @Override
            protected ApiResponse<List<UserData>> doInBackground(Void... voids) {
                return api.userGetCode(account);
            }

            @Override
            protected void onPostExecute(ApiResponse<List<UserData>> response) {
                if (listener != null && response != null) {
                    if (response.isSuccess()) {
                        listener.onSuccess(response.getParams());
                    } else {
                        listener.onFailure(response.getFlag(), response.getErrCode(), response.getCause());
                    }
                }
            }
        }.execute();
    }

    @Override
    public void userLogin(final String cellphone, final String password, final ActionCallbackListener<List<UserData>> listener) {
        // 请求Api
        new AsyncTask<Void, Void, ApiResponse<List<UserData>>>() {


            @Override
            protected ApiResponse<List<UserData>> doInBackground(Void... voids) {
                return api.userLogin(cellphone, password);
            }

            @Override
            protected void onPostExecute(ApiResponse<List<UserData>> response) {
                if (listener != null && response != null) {
                    if (response.isSuccess()) {
                        listener.onSuccess(response.getParams());
                    } else {
                        listener.onFailure(response.getFlag(), response.getErrCode(), response.getCause());
                    }
                }
            }
        }.execute();
    }

    @Override
    public void userRegister(final String account, final String password, final ActionCallbackListener<Void> listener) {
        // 请求Api
        new AsyncTask<Void, Void, ApiResponse<Void>>() {
            @Override
            protected ApiResponse<Void> doInBackground(Void... voids) {
                return api.userRegister(account, password);
            }

            @Override
            protected void onPostExecute(ApiResponse<Void> response) {
                if (listener != null && response != null) {
                    if (response.isSuccess()) {
                        listener.onSuccess(null);
                    } else {
                        listener.onFailure(response.getFlag(), response.getErrCode(), response.getCause());
                    }
                }
            }
        }.execute();
    }

    @Override
    public void deviceList(final String account, final Long timestamp, final String sign, final ActionCallbackListener<List<DeviceData>> listener) {
        // 请求Api
        new AsyncTask<Void, Void, ApiResponse<List<DeviceData>>>() {


            @Override
            protected ApiResponse<List<DeviceData>> doInBackground(Void... voids) {
                return api.deviceList(account, timestamp, sign);
            }

            @Override
            protected void onPostExecute(ApiResponse<List<DeviceData>> response) {
                if (listener != null && response != null) {
                    if (response.isSuccess()) {
                        listener.onSuccess(response.getParams());
                    } else {
                        listener.onFailure(response.getFlag(), response.getErrCode(), response.getCause());
                    }
                }
            }
        }.execute();
    }

    @Override
    public void getBaseInfo(final String account, final Long timestamp, final String sign, final ActionCallbackListener<List<UserData>> listener) {
        // 请求Api
        new AsyncTask<Void, Void, ApiResponse<List<UserData>>>() {


            @Override
            protected ApiResponse<List<UserData>> doInBackground(Void... voids) {
                return api.getBaseInfo(account, timestamp, sign);
            }

            @Override
            protected void onPostExecute(ApiResponse<List<UserData>> response) {
                if (listener != null && response != null) {
                    if (response.isSuccess()) {
                        listener.onSuccess(response.getParams());
                    } else {
                        listener.onFailure(response.getFlag(), response.getErrCode(), response.getCause());
                    }
                }
            }
        }.execute();
    }

}

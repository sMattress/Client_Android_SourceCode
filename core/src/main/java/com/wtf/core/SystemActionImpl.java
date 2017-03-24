package com.wtf.core;


import android.content.Context;
import android.os.AsyncTask;

import com.wtf.api.utils.ApiResponse;
import com.wtf.api.utils.SystemApi;
import com.wtf.api.utils.SystemApiImpl;
import com.wtf.model.UpdateData;

import java.util.List;

/**
 * Created by liyan on 2016/11/22.
 */

public class SystemActionImpl implements SystemAction {
    private SystemApi api;
    private Context context;

    public SystemActionImpl(Context context) {
        this.context = context;
        this.api = new SystemApiImpl();
    }

    @Override
    public void sysUpdate(final int versionCode, final String versionName, final ActionCallbackListener<List<UpdateData>> listener) {
        // 请求Api
        new AsyncTask<Void, Void, ApiResponse<List<UpdateData>>>() {
            @Override
            protected ApiResponse<List<UpdateData>> doInBackground(Void... voids) {
                return api.sysUpdate(versionCode,versionName);
            }

            @Override
            protected void onPostExecute(ApiResponse<List<UpdateData>> response) {
                if (listener != null && response != null) {
                    if (response.isSuccess()) {
                        listener.onSuccess(response.getParams());
                    } else {
                        listener.onFailure(response.getFlag(),response.getErrCode(),response.getCause());
                    }
                }
            }
        }.execute();
    }
}

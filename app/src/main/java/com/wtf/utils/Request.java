package com.wtf.utils;

import android.widget.TextView;

import com.litesuits.http.request.JsonAbsRequest;
import com.litesuits.http.request.param.HttpRichParamModel;
import com.wtf.model.AppMsg;

/**
 * Created by liyan on 2016/11/23.
 */

public class Request extends JsonAbsRequest<AppMsg> {
    public Request(String url) {
        super(url);
    }

}

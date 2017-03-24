package com.wtf.model;

import com.alibaba.fastjson.JSONArray;

/**
 * Created by liyan on 2016/11/15.
 */

public class PostData {
    JSONArray body;

    public JSONArray getBody() {
        return body;
    }

    public PostData setBody(JSONArray body) {
        this.body = body;
        return this;
    }
    public PostData addBody(Object object) {
        if (body==null){
            body=new JSONArray();
        }
        body.add(object);
        return this;
    }
}

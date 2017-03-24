package com.wtf.model;



import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by liyan on 2016/11/7.
 */

public class AppMsg {
    private Integer flag;
    private Integer cmd;
    private Integer errCode;
    private JSONArray params;
    private JSONArray cause;
    private String version = "1.0";
    private Integer err_code;

    public Integer getFlag() {
        return flag;
    }

    public AppMsg setFlag(Integer flag) {
        this.flag = flag;
        return this;
    }

    public Integer getErrCode() {
        return errCode;
    }

    public AppMsg setErrCode(Integer errCode) {
        this.errCode = errCode;
        return this;
    }

    public JSONArray getParams() {
        return params;
    }

    public AppMsg addParam(Object param) {
        if (params == null) {
            params = new JSONArray();
        }
        params.add(param);
        return this;
    }

    public AppMsg setParams(JSONArray params) {
        this.params = params;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public AppMsg setVersion(String version) {
        this.version = version;
        return this;
    }

    public Integer getCmd() {
        return cmd;
    }

    public AppMsg setCmd(Integer cmd) {
        this.cmd = cmd;
        return this;
    }

    public JSONArray getCause() {
        return cause;
    }

    public void setCause(JSONArray cause) {
        this.cause = cause;
    }

    public Integer getErr_code() {
        return err_code;
    }

    public void setErr_code(Integer err_code) {
        this.err_code = err_code;
    }
}

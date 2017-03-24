package com.wtf.model;

import java.io.Serializable;

/**
 * Created by liyan on 2016/11/22.
 */

public class DeviceData implements Serializable {
    private int device_id;
    private String device_name;
    private String alias = "新设备";

    public int getDeviceId() {
        return device_id;
    }

    public void setDeviceId(int device_id) {
        this.device_id = device_id;
    }

    public String getDeviceName() {
        return device_name;
    }

    public void setDeviceName(String device_name) {
        this.device_name = device_name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}

package com.wtf.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wtf.R;
import com.wtf.model.DeviceData;

/**
 * Created by liyan on 2016/11/23.
 */

public class DeviceListAdapter extends SBaseAdapter<DeviceData>{

    public DeviceListAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view==null){
            view= LayoutInflater.from(context).inflate(R.layout.device_list_item,null);
            holder=new ViewHolder();
            holder.tv_device_name=(TextView)view.findViewById(R.id.tv_device_name);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }

        String deviceName = itemList.get(pos).getDeviceName();
        String alias = itemList.get(pos).getAlias();
        if (alias!=null&&!alias.equals("")){
            holder.tv_device_name.setText(alias);
        }else {
            holder.tv_device_name.setText(deviceName);
        }

        return view;
    }

    static class ViewHolder{
        TextView tv_device_name;
    }
}

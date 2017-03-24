package com.wtf.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.wtf.R;

/**
 * Created by liyan on 2016/12/5.
 */

public class DeviceFragment extends Fragment {

    private TextView tv_title;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
/*
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
       /* LinearLayout view = (LinearLayout) inflater.inflate(
                R.layout.wifi_fragment_connect, container, false);*/

        View view = inflater.inflate(R.layout.wifi_fragment_connect, container, false);
        //View view = inflater.inflate(R.layout.wifi_fragment_connect, container);

        tv_title = (TextView) view.findViewById(R.id.tv_main_title);
        tv_title.setText("请绑定设备");

        return view;
    }

}

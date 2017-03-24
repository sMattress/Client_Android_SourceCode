package com.wtf.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wtf.R;
import com.wtf.activity.HelpActivity;

/**
 * Created by liyan on 2016/10/10.
 */
public class HelpFragment extends Fragment implements View.OnClickListener {
    private TextView tv_title;
    private LinearLayout ll_download;
    private LinearLayout ll_device;
    private LinearLayout ll_heating;
    private LinearLayout ll_therapy;
    private LinearLayout ll_problem;
    private LinearLayout ll_control;
    private LinearLayout ll_far_infrared_ray;
    private LinearLayout ll_tmp_curve;
    //private LinearLayout ll_tmp_curve;
    //private static final String URL = "http://help.lesmarthome.com/smartmattress/content/";
    private static final String URL = "https://haitunsasa.gitbooks.io/smartmattress/content/";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_knowledge, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        tv_title = (TextView) view.findViewById(R.id.tv_main_title);
        ll_download = (LinearLayout) view.findViewById(R.id.ll_download);
        ll_device = (LinearLayout) view.findViewById(R.id.ll_device);
        ll_control = (LinearLayout) view.findViewById(R.id.ll_control);
        ll_heating = (LinearLayout) view.findViewById(R.id.ll_heating);
        ll_therapy = (LinearLayout) view.findViewById(R.id.ll_therapy);
        ll_problem = (LinearLayout) view.findViewById(R.id.ll_problem);
        ll_far_infrared_ray = (LinearLayout) view.findViewById(R.id.ll_far_infrared_ray);
        ll_tmp_curve=(LinearLayout)view.findViewById(R.id.ll_tmp_curve);

        tv_title.setText(R.string.icon_knowledge);
        ll_download.setOnClickListener(this);
        ll_device.setOnClickListener(this);
        ll_control.setOnClickListener(this);
        ll_therapy.setOnClickListener(this);
        ll_heating.setOnClickListener(this);
        ll_problem.setOnClickListener(this);
        ll_far_infrared_ray.setOnClickListener(this);
        ll_tmp_curve.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), HelpActivity.class);
        switch (view.getId()) {
            case R.id.ll_download:
                intent.putExtra("url", URL + "app下载及说明.html");
                break;
            case R.id.ll_device:
                intent.putExtra("url", URL + "添加新设备的流程.html");
                break;
            case R.id.ll_control:
                intent.putExtra("url", URL + "远程控制.html");
                break;
            case R.id.ll_heating:
                intent.putExtra("url", URL + "加热预约.html");
                break;
            case R.id.ll_therapy:
                intent.putExtra("url", URL + "理疗预约.html");
                break;
            case R.id.ll_problem:
                intent.putExtra("url", URL + "常见问题及解决办法.html");
                break;
            case R.id.ll_tmp_curve:
                intent.putExtra("url", URL + "智能温度曲线.html");
                break;
            case R.id.ll_far_infrared_ray:
                intent.putExtra("url",  URL + "远红外.html");
                break;
        }
        startActivity(intent);
    }
}

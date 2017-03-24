package com.wtf.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.wtf.R;
import com.wtf.WTFApplication;
import com.wtf.activity.DeviceActivity;
import com.wtf.activity.LoginActivity;
import com.wtf.activity.MainActivity;
import com.wtf.activity.UserInfoActivity;
import com.wtf.activity.VersionActivity;
import com.wtf.model.AppMsg;
import com.wtf.model.Param;
import com.wtf.model.URL;
import com.wtf.model.UserData;
import com.wtf.utils.ConnectDirect;
import com.wtf.utils.HttpUtil;
import com.wtf.utils.MD5Util;
import com.wtf.utils.MyHttpListener;
import com.wtf.utils.Request;
import com.wtf.utils.SharedPreUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyan on 2016/10/10.
 */
public class PersonageFragment extends Fragment implements OnClickListener{

    // TODO: 2016/11/28 用户头像
    private FragmentActivity mActivity;
    private TextView tv_user_name;
    private ImageView iv_user_image;
    private RelativeLayout ll_user_setting;
    private RelativeLayout ll_device_manage;
    private RelativeLayout ll_version;
    private RelativeLayout ll_user_exit;
    private ProgressDialog mDialog;
    private personage_handler personageHandler;
    private Long timestamp;
    private String sign;
    private String account;
    private String token;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personage, container, false);
        initViews(view);
        getData(view);
        return view;
    }

    private void initViews(View view){
        mActivity = getActivity();
        personageHandler=new personage_handler(this);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_main_title);
        tv_user_name=(TextView)view.findViewById(R.id.tv_user_name);
        iv_user_image =(ImageView) view.findViewById(R.id.iv_user_image);
        ll_user_setting=(RelativeLayout)view.findViewById(R.id.ll_user_setting);
        ll_device_manage=(RelativeLayout)view.findViewById(R.id.ll_device_manage);
        ll_version=(RelativeLayout)view.findViewById(R.id.ll_version);
        ll_user_exit=(RelativeLayout)view.findViewById(R.id.ll_user_exit);
        tv_title.setText(R.string.icon_personage);
        ll_user_setting.setOnClickListener(this);
        ll_device_manage.setOnClickListener(this);
        ll_version.setOnClickListener(this);
        ll_user_exit.setOnClickListener(this);
    }
    private void getData(View view) {
        account=WTFApplication.userData.getAccount();
        token=WTFApplication.userData.getToken();

        if (WTFApplication.userData.getBirthday()==null) {
            WTFApplication.userData.setName("NoName");
            WTFApplication.userData.setBirthday("2010-12-05");
            WTFApplication.userData.setSex(1);
            WTFApplication.putUserData();
        }
        if (!WTFApplication.isConnectingToInternet()) {
            personageHandler.sendEmptyMessage(2);
        } else {
            // 开始获取数据线程
            mDialog = new ProgressDialog(getContext());
            mDialog.setMessage("获取信息中...");
            mDialog.show();
            timestamp = WTFApplication.getTimeStamp();
            sign = MD5Util.encrypt("/v1/user/get/base_info" +
                    "?account=" + account + "&timestamp=" + timestamp + "&token=" + token);

            String uploadUrl = URL.GET_BASE_INFO + "?account=" + account +
                    "&timestamp=" + timestamp + "&sign=" + sign;

            Request request = new Request(uploadUrl);
            request.setHttpListener(new MyHttpListener<AppMsg>(mActivity) {
                @Override
                public void onSuccess(AppMsg s, Response<AppMsg> response) {
                    if (s.getFlag() == 1) {
                        if (s.getParams() != null && !s.getParams().isEmpty()) {
                            List<UserData> data = new ArrayList<>();
                            data = JSONObject.parseArray(s.getParams().toJSONString(), UserData.class);
                            WTFApplication.userData.setImgUrl(data.get(0).getImgUrl());
                            WTFApplication.userData.setBirthday(data.get(0).getBirthday());
                            WTFApplication.userData.setName(data.get(0).getName());
                            WTFApplication.userData.setSex(data.get(0).getSex());
                            String baseDir = Environment.getExternalStorageDirectory()
                                    .getAbsolutePath();
                            Bitmap bitmap = BitmapFactory.decodeFile(baseDir + Param.FILE_NAME);
                            if (bitmap != null) {
                                iv_user_image.setImageBitmap(bitmap);
                            }
                            WTFApplication.putUserData();
                            personageHandler.sendEmptyMessage(0);
                        }else {
                            personageHandler.sendEmptyMessage(3);
                        }
                    }else {
                        Log.i("err",""+s.getErrCode());
                        Log.i("cause",""+s.getCause());
                        personageHandler.sendEmptyMessage(1);
                    }
                }

                @Override
                public void onFailure(HttpException e, Response response) {
                    personageHandler.sendEmptyMessage(1);
                    super.onFailure(e, response);
                }
            });
            HttpUtil.liteHttp.executeAsync(request);
        }
    }

    // Handler
    static class personage_handler extends Handler {
        WeakReference<PersonageFragment> mActivity;

        personage_handler(PersonageFragment activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PersonageFragment theFragment = mActivity.get();
            switch (msg.what) {
                case 0:
                    theFragment.mDialog.cancel();
                    theFragment.tv_user_name.setText(WTFApplication.userData.getName());
                    Toast.makeText(theFragment.getContext(), "获取成功！", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    theFragment.mDialog.cancel();
                    Toast.makeText(theFragment.getContext(), "网络连接超时了，请重试", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case 2:
                    Toast.makeText(theFragment.getContext(), "未检测到网络，请打开网络连接",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    theFragment.mDialog.cancel();
                    Toast.makeText(theFragment.getContext(), "无用户信息", Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.ll_user_setting:
                Intent intent1 = new Intent();
                intent1.setClass(mActivity, UserInfoActivity.class);
                startActivity(intent1);
                break;
            case R.id.ll_device_manage:
                // TODO: 2016/11/24 wifi
                Intent intent2 = new Intent();
                intent2.setClass(mActivity, DeviceActivity.class);
                startActivity(intent2);
                break;
            case R.id.ll_version:
                Intent intent3 = new Intent();
                intent3.setClass(mActivity, VersionActivity.class);
                startActivity(intent3);
                break;
            case R.id.ll_user_exit:
                AlertDialog.Builder build = new AlertDialog.Builder(mActivity);
                build.setTitle("退出")
                        .setMessage("确定要退出吗？")
                        .setPositiveButton("确认",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        MainActivity.state=0;
                                        WTFApplication.userData = new UserData();
                                        // 用户名，密码保存在SharedPreferences
                                        WTFApplication.putUserData();
                                        Intent intent = new Intent();
                                        intent.setClass(mActivity, LoginActivity.class);
                                        startActivity(intent);
                                        mActivity.finish();

                                    }
                                })
                        .setNegativeButton("取消", null).show();
                break;
        }
    }

    public void onResume(){
        tv_user_name.setText(WTFApplication.userData.getName());
        super.onResume();
    }
}

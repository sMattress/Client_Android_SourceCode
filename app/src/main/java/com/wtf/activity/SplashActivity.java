package com.wtf.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.wtf.R;
import com.wtf.WTFApplication;
import com.wtf.model.AppMsg;
import com.wtf.model.Param;
import com.wtf.model.URL;
import com.wtf.model.UpdateData;
import com.wtf.model.UserData;
import com.wtf.utils.HttpUtil;
import com.wtf.utils.MD5Util;
import com.wtf.utils.MyHttpListener;
import com.wtf.utils.Request;
import com.wtf.utils.SharedPreUtil;
import com.wtf.utils.VersionUtil;

import static com.wtf.WTFApplication.userData;

//todo 过期?
public class SplashActivity extends SBaseActivity {
    private final String TAG = "info";
    private UpdateData info;
    /* 记录进度条数量 */
    private int flag = 2;

    private String err = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        updateData();
    }

    /**
     * 检测更新
     */
    private synchronized void updateData() {

        int versionCode = VersionUtil.getVersionCode(_context);
        String versionName = VersionUtil.getVersionName(_context);
        String uploadUrl = URL.UPDATE + "?version_code=" + versionCode +
                "&version_name=" + versionName;

        Request request = new Request(uploadUrl);
        request.setHttpListener(new MyHttpListener<AppMsg>(this) {
            @Override
            public void onSuccess(AppMsg s, Response<AppMsg> response) {
                if (s.getFlag() == 1) {
                    if (s.getParams() != null && !s.getParams().isEmpty()) {
                        info = JSONObject.parseArray(s.getParams().toJSONString(), UpdateData.class).get(0);
                        delayed();
                    } else {
                        delayed();
                    }
                } else {
                    //最新版本
                    delayed();
                    Log.i("err", "" + s.getErr_code());
                    Log.i("cause", "" + s.getCause());
                }
            }

            public void onFailure(HttpException e, Response response) {
                // handle by this by call super.onFailure()
                super.onFailure(e, response);
                delayed();
                // 通过调用父类的处理方法，来调用 MyHttpExceptHandler 来处理异常。
            }
        });

        HttpUtil.liteHttp.executeAsync(request);
    }

    private void delayed() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (userData.getAccount() != null) {
                    validateToken(userData.getToken(),userData.getAccount());
                } else {
                    Intent intent = new Intent();
                    if(info!=null) {
                        Bundle bundle = new Bundle();
                        Log.i(TAG, "versionCode" + info.getVersionCode());
                        bundle.putInt("versionCode", info.getVersionCode());
                        bundle.putString("versionName", info.getVersionName());
                        bundle.putString("download", info.getDownload());
                        intent.putExtra("flag", flag);
                        intent.putExtra("updateInfo", bundle);
                    }
                    intent.setClass(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    SplashActivity.this.finish();
                }
            }
        }, 2000);
    }

    //验证token
    public void validateToken(String token, String account){

        Long timestamp = WTFApplication.getTimeStamp();
        String sign = MD5Util.encrypt("/v1/sys/validate/token" +
                "?account=" + account + "&timestamp=" + timestamp + "&token=" + token);
        String uploadUrl = URL.VALIDATE_TOKEN + "?account=" + account +
                "&timestamp=" + timestamp+"&sign="+sign;

        Request request = new Request(uploadUrl);
        request.setHttpListener(new MyHttpListener<AppMsg>(this) {
            @Override
            public void onSuccess(AppMsg s, Response<AppMsg> response) {
                if (s.getFlag() == 1) {
                    //未过期
                    Log.i("TokenExpireTime","unExpireTime");
                    Intent intent = new Intent();
                    intent.putExtra("isHaveDevice", userData.isHaveDevice());
                    if(info!=null) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("versionCode", info.getVersionCode());
                        bundle.putString("versionName", info.getVersionName());
                        bundle.putString("download", info.getDownload());
                        intent.putExtra("flag", flag);
                        intent.putExtra("updateInfo", bundle);
                    }
                    intent.setClass(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    SplashActivity.this.finish();
                } else {
                    Log.i("validateTokenerr", "" + s.getErr_code());
                    Log.i("validateTokencause", "" + s.getCause());
                    //已过期，跳转到登录页面
                    Intent intent = new Intent();

                    if(info!=null) {
                        intent.putExtra("flag", flag);
                        Bundle bundle = new Bundle();
                        Log.i(TAG, "versionCode" + info.getVersionCode());
                        bundle.putInt("versionCode", info.getVersionCode());
                        bundle.putString("versionName", info.getVersionName());
                        bundle.putString("download", info.getDownload());
                        intent.putExtra("updateInfo", bundle);
                    }
                    intent.setClass(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    SplashActivity.this.finish();
                }
            }

            public void onFailure(HttpException e, Response response) {
                // handle by this by call super.onFailure()
                super.onFailure(e, response);
                // 通过调用父类的处理方法，来调用 MyHttpExceptHandler 来处理异常。
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        });

        HttpUtil.liteHttp.executeAsync(request);
    }

}

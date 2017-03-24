package com.wtf.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.wtf.R;
import com.wtf.WTFApplication;
import com.wtf.model.AppMsg;
import com.wtf.model.URL;
import com.wtf.model.UpdateData;
import com.wtf.ui.UpdateDialog;
import com.wtf.utils.ConnectDirect;
import com.wtf.utils.HttpUtil;
import com.wtf.utils.MyHttpListener;
import com.wtf.utils.Request;
import com.wtf.utils.VersionUtil;

import java.lang.ref.WeakReference;

/**
 * Created by Hailey on 2016/4/25.
 */
public class VersionActivity extends SBaseActivity implements OnClickListener {
    private int versionCode = 0;
    private String versionName = "";
    private ProgressDialog mDialog;
    private final String TAG = "info";
    private UpdateData info;
    private ProgressBar mProgress;
    private TextView tv_percent;
    /* 记录进度条数量 */
    private int progress;
    private AlertDialog alertDialog;
    private boolean cancelUpdate = false;
    private String savePath;
    private SettingsHandler settingsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);
        initViews();
    }

    private void initViews() {
        TextView tv_title = (TextView) findViewById(R.id.tv_main_title);
        tv_title.setText(R.string.settings_tv_title);
        ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);

        versionCode = VersionUtil.getVersionCode(_context);
        TextView tv_version = (TextView) findViewById(R.id.tv_settings_version);
        versionName = VersionUtil.getVersionName(_context);
        tv_version.setText("V" + VersionUtil.getVersionName(_context));

        RelativeLayout rl_update = (RelativeLayout) findViewById(R.id.rl_settings_update);
        rl_update.setOnClickListener(this);
        settingsHandler = new SettingsHandler(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.rl_settings_update:
                toCheck();
                break;
            default:
                break;
        }
    }

    public void toCheck() {
        // 判断网络连接
        if (!WTFApplication.isConnectingToInternet()) {
            Toast.makeText(this, "未检测到网络，请打开网络连接", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "versionCode1...");
            mDialog = new ProgressDialog(this);
            mDialog.setMessage("正在获取最新版本信息...");
            mDialog.show();
            Log.i(TAG, "versionCode2...");

            String uploadUrl = URL.UPDATE+"?version_code=" + versionCode +
                    "&version_name=" + versionName;

            Request request = new Request(uploadUrl);
            request.setHttpListener(new MyHttpListener<AppMsg>(this) {
                @Override
                public void onSuccess(AppMsg s, Response<AppMsg> response) {
                    if (s.getFlag() == 1) {
                        if (s.getParams() != null && !s.getParams().isEmpty()) {
                            info = JSONObject.parseArray(s.getParams().toJSONString(), UpdateData.class).get(0);
                            settingsHandler.sendEmptyMessage(0);
                        }
                    } else {
                        if (s.getErr_code() == 0) {
                            settingsHandler.sendEmptyMessage(1);
                        } else {
                            settingsHandler.sendEmptyMessage(2);
                        }
                    }
                }

                public void onFailure(HttpException e, Response response) {
                    mDialog.cancel();
                    super.onFailure(e, response);
                }
            });
            HttpUtil.liteHttp.executeAsync(request);
        }
    }

    static class SettingsHandler extends Handler {
        WeakReference<VersionActivity> mActivity;

        SettingsHandler(VersionActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            VersionActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 0:
                    theActivity.mDialog.cancel();
                    //对话框通知用户升级程序
                    UpdateDialog updateDialog = new UpdateDialog(theActivity.info.getVersionName(), theActivity.info.getDownload(), theActivity);
                    updateDialog.showUpdateDialog();
                    break;
                case 1:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "当前已是最新版本",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity, "网络连接超时了，请重试",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}

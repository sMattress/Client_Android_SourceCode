package com.wtf.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.wtf.R;
import com.wtf.WTFApplication;
import com.wtf.fragment.DeviceFragment;
import com.wtf.fragment.HelpFragment;
import com.wtf.fragment.PersonageFragment;
import com.wtf.fragment.SettingFragment;
import com.wtf.model.AppMsg;
import com.wtf.model.Param;
import com.wtf.ui.UpdateDialog;
import com.wtf.utils.ConnectDirect;
import com.wtf.utils.HttpUtil;
import com.wtf.utils.NetWorkStateReceiver;
import com.wtf.utils.SocketConnection;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import wtf.socket.*;


public class MainActivity extends FragmentActivity implements
        OnClickListener {
    //用于对Fragment进行管理
    private FragmentManager fragmentManager;
    // 用于返回是存储选择Fragment的状态
    int selectionState = 0;

    private WIFIReceiver wifiReceiver;

    private HelpFragment knowledgeFragment;
    private SettingFragment settingFragment;
    private PersonageFragment personageFragment;
    private DeviceFragment deviceFragment;

    private ImageView settingImage;
    private ImageView knowledgeImage;
    private ImageView personageImage;
    private View settingView;
    private View knowledgeView;
    private View personageView;
    private TextView settingText;
    private TextView knowledgeText;
    private TextView personageText;
    public static int state = 0;

    private static final int white = Color.WHITE;//修改时的颜色
    private static final int no_select = Color.rgb(76, 84, 103);//修改后的颜色

    private int request = 1;

    private int versionCode = 0;
    private String versionName = "";
    private String download = "";
    private MainHandler mainHandler;
    public static MainActivity MainActivity;

    public static boolean breinit = false;
    private IntentFilter intentFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity = this;
        //标志从哪个页面跳转
        int flag = getIntent().getIntExtra("flag", 0);
        Log.i("flag", "" + flag);
        if (flag == 2) {
            //SplashActivity跳转
            mainHandler = new MainHandler(this);
            Bundle bundle = getIntent().getBundleExtra("updateInfo");
            versionName = bundle.getString("versionName");
            versionCode = bundle.getInt("versionCode");
            download = bundle.getString("download");
            mainHandler.sendEmptyMessage(0);
        }

        initViews();

            new Thread() {
                @Override
                public void run() {
                    SocketConnection.registerSocket();
                }
            }.start();
        wifiReceiver = new WIFIReceiver();
        IntentFilter filter = new IntentFilter();
       // filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        //filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        //filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        //filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
       // filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(wifiReceiver, filter);

    }

    private void initViews() {
        settingView = findViewById(R.id.ll_main_setting);
        knowledgeView = findViewById(R.id.ll_main_knowledge);
        personageView = findViewById(R.id.ll_main_personage);
        settingImage = (ImageView) findViewById(R.id.iv_main_setting);
        knowledgeImage = (ImageView) findViewById(R.id.iv_main_knowledge);
        personageImage = (ImageView) findViewById(R.id.iv_main_personage);
        settingText = (TextView) findViewById(R.id.tv_main_setting);
        knowledgeText = (TextView) findViewById(R.id.tv_main_knowledge);
        personageText = (TextView) findViewById(R.id.tv_main_personage);
        settingView.setOnClickListener(this);
        knowledgeView.setOnClickListener(this);
        personageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_main_setting:
                state = 0;
                setTabSelection(0);
                break;
            case R.id.ll_main_knowledge:
                state = 1;
                setTabSelection(1);
                break;
            case R.id.ll_main_personage:
                state = 2;
                setTabSelection(2);
                break;
        }
    }

    /**
     * 根据传入的index参数来设置选中的tab页。
     */
    private void setTabSelection(int index) {
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);
        // fragmentManager.beginTransaction().remove(deviceFragment);

        /*if(deviceFragment != null) {
            transaction.detach(deviceFragment);
            deviceFragment.onDestroy();
            deviceFragment = null;
        }*/
        switch (index) {
            case 0:
                // 当点击了设置时，改变控件的图片
                settingView.setBackgroundColor(Color.BLACK);
                knowledgeView.setBackgroundResource(R.drawable.bottom_bg);
                personageView.setBackgroundResource(R.drawable.bottom_bg);

                settingText.setTextColor(white);
                knowledgeText.setTextColor(no_select);
                personageText.setTextColor(no_select);

                settingImage.setImageResource(R.drawable.setting_selected_icon);
                knowledgeImage.setImageResource(R.drawable.help_icon);
                personageImage.setImageResource(R.drawable.me_icon);

                if (WTFApplication.userData.isHaveDevice()) {
                    if (settingFragment == null) {
                        // 如果settingFragment为空，则创建一个并添加到界面上
                        settingFragment = new SettingFragment();
                        transaction.add(R.id.body_frame, settingFragment);
                    } else {
                        // 如果settingFragment不为空，则直接将它显示出来
                        transaction.show(settingFragment);
                    }
                } else {
                    if (deviceFragment == null) {
                        // 如果settingFragment为空，则创建一个并添加到界面上
                        deviceFragment = new DeviceFragment();
                        transaction.add(R.id.body_frame, deviceFragment);
                    } else {
                        // 如果settingFragment不为空，则直接将它显示出来
                        transaction.show(deviceFragment);
                    }
//                    Intent intent2 = new Intent();
//                    intent2.setClass(MainActivity.this, DeviceActivity.class);
//                    startActivity(intent2);
                }
                break;
            case 1:
                // 当点击了知识时，改变控件的图片
                settingView.setBackgroundResource(R.drawable.bottom_bg);
                knowledgeView.setBackgroundColor(Color.BLACK);
                personageView.setBackgroundResource(R.drawable.bottom_bg);

                settingText.setTextColor(no_select);
                knowledgeText.setTextColor(white);
                personageText.setTextColor(no_select);

                settingImage.setImageResource(R.drawable.setting_icon);
                knowledgeImage.setImageResource(R.drawable.help_light_icon);
                personageImage.setImageResource(R.drawable.me_icon);

                if (knowledgeFragment == null) {
                    // 如果knowledgeFragment为空，则创建一个并添加到界面上
                    knowledgeFragment = new HelpFragment();
                    transaction.add(R.id.body_frame, knowledgeFragment);
                } else {
                    // 如果knowledgeFragment不为空，则直接将它显示出来
                    transaction.show(knowledgeFragment);
                }
                break;
            case 2:
                // 当点击了我时，改变控件的图片
                settingView.setBackgroundResource(R.drawable.bottom_bg);
                knowledgeView.setBackgroundResource(R.drawable.bottom_bg);
                personageView.setBackgroundColor(Color.BLACK);


                settingText.setTextColor(no_select);
                knowledgeText.setTextColor(no_select);
                personageText.setTextColor(white);

                settingImage.setImageResource(R.drawable.setting_icon);
                knowledgeImage.setImageResource(R.drawable.help_icon);
                personageImage.setImageResource(R.drawable.me_selected_icon);

                if (personageFragment == null) {
                    // 如果personageFragment为空，则创建一个并添加到界面上
                    personageFragment = new PersonageFragment();
                    transaction.add(R.id.body_frame, personageFragment);
                } else {
                    // 如果personageFragment不为空，则直接将它显示出来
                    transaction.show(personageFragment);
                }
                break;

        }
        transaction.commit();
    }

    /**
     * 将所有的Fragment都置为隐藏状态。
     *
     * @param transaction 用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (settingFragment != null) {
            transaction.hide(settingFragment);
        }
        if (knowledgeFragment != null) {
            transaction.hide(knowledgeFragment);
        }
        if (personageFragment != null) {
            transaction.hide(personageFragment);
        }
        if (deviceFragment != null) {
            transaction.hide(deviceFragment);
        }
    }

    // 定义一个变量，来标识是否退出
    private static boolean isExit = false;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == request) {
            Fragment f = fragmentManager.findFragmentByTag("child");
            //然后在碎片中调用重写的onActivityResult方法
            f.onActivityResult(requestCode, resultCode, data);
        }
    }

    //放止fragment重叠
    protected void onSaveInstanceState(Bundle outState) {

    }

    static class MainHandler extends Handler {
        WeakReference<MainActivity> mActivity;

        MainHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 0:
                    //对话框通知用户升级程序
                    UpdateDialog updateDialog = new UpdateDialog(theActivity.versionName, theActivity.download, theActivity);
                    updateDialog.showUpdateDialog();
                    break;
            }
        }
    }

    public void onResume() {
        super.onResume();

        fragmentManager = getSupportFragmentManager();
        // 返回时获取选择Fragment的状态
        selectionState = state;
        // 第一次启动时选中第0个tab
        setTabSelection(selectionState);
    }

    class NetworkChangeReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
           /* if(MainActivity.breinit == true) {
                if (WTFApplication.isConnectingToInternet()) {
                    WTFSocketSessionFactory.reInit();
                } else {
                    Toast.makeText(MainActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
                }
            }
            MainActivity.breinit = true;*/
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiReceiver!=null){
            unregisterReceiver(wifiReceiver);
            wifiReceiver = null;
        }
    }



    private class WIFIReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 判断网络连接
            ConnectDirect cd = new ConnectDirect(context);
            Boolean isInternetPresent = cd.isConnectingToInternet();
            if (!isInternetPresent) {
                /*Toast.makeText(context, "未检测到网络，请打开网络连接",
                        Toast.LENGTH_SHORT).show();*/
            }else{
                new Thread() {

                    @Override
                    public void run() {
                        SocketConnection.registerSocket();
                    }
                }.start();
            }
        }
    }
}

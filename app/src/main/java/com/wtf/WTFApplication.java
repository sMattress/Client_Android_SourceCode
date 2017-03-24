package com.wtf;

import android.app.Application;
import android.content.Context;

import com.realtek.simpleconfiglib.SCLibrary;
import com.wtf.model.UserData;
import com.wtf.utils.ConnectDirect;
import com.wtf.utils.HttpUtil;
import com.wtf.utils.SharedPreUtil;
//import com.wtf.fragment.WifiFragment;


/**
 * Created by liyan on 2016/10/10.
 */
public class WTFApplication extends Application{

    public static SCLibrary SCLib = new SCLibrary();
    public static Context _context;
    public static ConnectDirect _connectDirect;
    public static UserData userData;
    //public static WifiFragment wifiFragment = new WifiFragment();
    static {
        System.loadLibrary("simpleconfiglib");
    }
    @Override
    public void onCreate() {
        super.onCreate();

        _context = this.getApplicationContext();
        SharedPreUtil.initSharedPreference(_context);
        this.upUserData();
        _connectDirect = new ConnectDirect(_context);

        HttpUtil httpUtil=new HttpUtil();
        httpUtil.initLiteHttp(_context);

        SCLib.rtk_sc_init();
    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        putUserData();
        SCLib.rtk_sc_exit();
    }

    public static boolean  isConnectingToInternet() {
        return _connectDirect.isConnectingToInternet();
    }
    public static long getTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    public static void upUserData() {//应该不调用
        userData=SharedPreUtil.getInstance().getUser();
    }

    public static void putUserData() {
        SharedPreUtil.getInstance().putUser(userData);
    }

}

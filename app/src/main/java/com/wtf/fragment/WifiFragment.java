package com.wtf.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.request.content.multi.MultipartBody;
import com.litesuits.http.request.content.multi.StringPart;
import com.litesuits.http.request.param.HttpMethods;
import com.litesuits.http.response.Response;
import com.realtek.simpleconfiglib.SCLibrary;
import com.wtf.R;
import com.wtf.WTFApplication;
import com.wtf.activity.MainActivity;
import com.wtf.adapter.DeviceListAdapter;
import com.wtf.model.AppMsg;
import com.wtf.model.DeviceData;
import com.wtf.model.URL;
import com.wtf.ui.RefreshView;
import com.wtf.utils.ConnectDirect;
import com.wtf.utils.EncodingUtils;
import com.wtf.utils.HttpUtil;
import com.wtf.utils.MD5Util;
import com.wtf.utils.MyHttpListener;
import com.wtf.utils.Request;
import com.wtf.utils.camera.util.CaptureActivity;
import com.wtf.utils.wifi.ConfigurationDevice;
import com.wtf.utils.wifi.ConfigurationSecuritiesV8;
import com.wtf.utils.wifi.FileOps;
import com.wtf.utils.wifi.SCCtlOps;
import com.wtf.utils.wifi.Wifi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wtf.socket.WTFSocketMsg;
import wtf.socket.WTFSocketSession;
import wtf.socket.WTFSocketSessionFactory;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission_group.CAMERA;
import static android.Manifest.permission_group.LOCATION;
import static android.content.Context.WIFI_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.v4.content.PermissionChecker.PERMISSION_DENIED;

/**
 * Created by liyan on 2016/12/5.
 */

public class WifiFragment extends Fragment implements View.OnClickListener, RefreshView.OnHeaderRefreshListener {
    private RelativeLayout rl_wifi;
    private RelativeLayout rl_scan;
    private ListView lv_device;
    private RefreshView mPullToRefreshView;
    private RefreshDataAsynTask mRefreshAsynTask;
    private LinearLayout load;
    private LinearLayout error;
    private TextView tv_error_info;
    private TextView tv_refresh;
    private PopupWindow mPopWindow;
    private ImageView iv_scan;

    private Long timestamp;
    private String sign;
    private String token;
    private String account;
    private String deviceName = "";
    private String deviceAlias = "";
    private int renamePos = 0;

    //    private int WIFI_FLAG = 1;
    private DeviceListAdapter adapter;
    //    private List<DeviceData> mData = new ArrayList<>();
// private DeviceData deviceData;
    private ProgressDialog mDialog;
    private int deletePos = 0;
    private int data_pos = 0;
    private int flag;
    private static final int REQUEST_CODE=1;

    private SCLibrary SCLib = WTFApplication.SCLib;

    private Activity mActivity;

    private int popLocationWidth;
    private int popLocationHeight;

    private static final int MY_PERMISSIONS_REQUEST_CALL_CAMERA = 1;//请求码，自己定义
    private static final int MY_PERMISSIONS_REQUEST_CALL_LOCATION = 2;//请求码，自己定义

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = getActivity();
       /* getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        View view = inflater.inflate(R.layout.wifi_fragment, container, false);

        //initWifi();
        deviceHandler = new handler(this);
        account = WTFApplication.userData.getAccount();
        token = WTFApplication.userData.getToken();
        flag = getActivity().getIntent().getIntExtra("flag", 0);
        if (flag == 1) {
            deviceName = getActivity().getIntent().getStringExtra("deviceName");
            if (deviceName != null)
                addDevice();
        }
        initViews(view);
        getData();
        return view;
    }

    public void onResume() {
        super.onResume();
        initWifi();
    }

    private void initViews(View view) {

        rl_wifi = (RelativeLayout) view.findViewById(R.id.rl_wifi);
        rl_scan = (RelativeLayout) view.findViewById(R.id.rl_scan);

        rl_wifi.setOnClickListener(this);
        rl_scan.setOnClickListener(this);

        load = (LinearLayout) view.findViewById(R.id.device_load);
        error = (LinearLayout) view.findViewById(R.id.device_error);
        tv_refresh = (TextView) error.findViewById(R.id.tv_refresh);
        //tv_title = (TextView) view.findViewById(R.id.tv_main_title);
        //tv_title.setText("请绑定设备");
        tv_refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                error.setVisibility(View.GONE);
                load.setVisibility(View.VISIBLE);
                getData();
            }
        });
        tv_error_info = (TextView) error.findViewById(R.id.tv_error_info);

        mPullToRefreshView = (RefreshView) view.findViewById(R.id.pull_to_refresh_device);
        mPullToRefreshView.setOnHeaderRefreshListener(this);
        mPullToRefreshView.setEnablePullLoadMoreDataStatus(false);
        mPullToRefreshView.showFooterView(false);
        mPullToRefreshView.setLastUpdated(new Date().toLocaleString());

        lv_device = (ListView) view.findViewById(R.id.lv_device);

        adapter = new DeviceListAdapter(getActivity());
        lv_device.setAdapter(adapter);
        lv_device.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                longClickItem(position);
                return true;
            }
        });
        lv_device.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                WTFApplication.userData.setSelDeviceIndex(position);
                WTFApplication.putUserData();

                //if(MainActivity.MainActivity != null) MainActivity.MainActivity.finish();
                Intent intent = new Intent();
                // TODO: 2016/12/5 跳转
                intent.setClass(getActivity(), MainActivity.class);
                MainActivity.state = 0;
                startActivity(intent);
                getActivity().finish();
            }
        });

    }

    /**
     * 长点击事件
     *
     * @param position
     */
    public void longClickItem(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] array = new String[]{
                "重命名",
                "删除此设备",
                "复位",
                "产生二维码"
        };
        builder.setItems(array, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                deviceName = WTFApplication.userData.mData.get(position).getDeviceName();
                switch (which) {
                    case 0:
                        final EditText rename = new EditText(getActivity());
                        AlertDialog.Builder build1 = new AlertDialog.Builder(mActivity);
                        build1.setTitle("重命名")
                                .setView(rename)
                                .setPositiveButton(
                                        "确认",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                renameDevice(position, rename.getText().toString());
                                            }
                                        })
                                .setNegativeButton("取消", null).show();
                        break;
                    case 1:// delete
                        AlertDialog.Builder build = new AlertDialog.Builder(mActivity);
                        build.setTitle("删除")
                                .setMessage("确定要删除该设备吗？")
                                .setPositiveButton(
                                        "确认",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                delete(position, deviceName);
                                                //getData();
                                            }
                                        })
                                .setNegativeButton("取消", null).show();
                        break;
                    case 2:
                        reset();
                        break;
                    case 3:
                        make();
                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    //生成二维码 可以设置Logo
    public void make() {
        if (deviceName.equals("") && deviceName == null) {
            Toast.makeText(getActivity(), "客户端异常", Toast.LENGTH_SHORT).show();
        } else {
                    /*BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)*///CheckBox选中就设置Logo
            showPopupWindow();

               // mPopWindow.showAsDropDown(iv_scan, Gravity.CENTER,0, 0);

                mPopWindow.showAsDropDown(iv_scan, popLocationWidth, popLocationHeight);

        }
    }

    private void showPopupWindow() {
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.pop_scan, null);
        mPopWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        iv_scan = (ImageView) contentView.findViewById(R.id.iv_scan);
        WindowManager manager = mActivity.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int popWidth = outMetrics.widthPixels;
        int popHeight = outMetrics.heightPixels;
        Bitmap qrCode = EncodingUtils.createQRCode(deviceName, 1000, 1000, null);
        iv_scan.setImageBitmap(qrCode);
        mPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopWindow.setOutsideTouchable(true);
        mPopWindow.getContentView().getWidth();
       // mPopWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
       /* int popWidth = mPopWindow.getContentView().getMeasuredWidth();
        int popHeight = mPopWindow.getContentView().getMeasuredHeight();*/
        popLocationWidth = popWidth / 2 - qrCode.getWidth() / 2;
        popLocationHeight = popHeight / 2 - qrCode.getHeight() / 2;
    }

    /**
     * 复位
     */
    private void reset() {
        ConnectDirect cd = new ConnectDirect(this.getActivity().getApplicationContext());
        Boolean isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent != true) {
            deviceHandler.sendEmptyMessage(4);
        } else {
            WTFSocketMsg currentMsg = new WTFSocketMsg().setBody(new AppMsg().setCmd(19));

            WTFSocketSession session1 = WTFSocketSessionFactory.getSession(deviceName);
            session1.sendMsg(currentMsg);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_wifi:
                configNewDevice_OnClick();
                break;
            case R.id.rl_scan:
                toScan();
                break;
        }
    }

    /*判断是否开启摄像头权限*/
    public boolean isCameraGranted() {

        return ContextCompat.checkSelfPermission(mActivity, CAMERA) == PERMISSION_GRANTED;
    }

    /*判断是否开启WIFI权限*/
    public boolean isWIFIGranted() {
        return ContextCompat.checkSelfPermission(mActivity, LOCATION) == PERMISSION_GRANTED;
    }

    private void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", mActivity.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", mActivity.getPackageName());
        }
        startActivity(localIntent);
    }
    private void callCamera() {
        Intent intent = new Intent();
        intent.putExtra("deviceFlag", 1);
        intent.setClass(mActivity, CaptureActivity.class);
        startActivityForResult(intent,REQUEST_CODE);
        //mActivity.finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode==REQUEST_CODE)
        {
            if (resultCode==CaptureActivity.RESULT_CODE)
            {
                Bundle bundle=data.getExtras();
                deviceName=bundle.getString("deviceName");
                addDevice();
                //Toast.makeText(mActivity, str, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void toScan() {
//        if (isCameraGranted()) {
//            callCamera();
//        } else {
//            getAppDetailSettingIntent(mActivity);
//            Toast.makeText(mActivity, "请开启摄像头权限", Toast.LENGTH_SHORT).show();
//        }
        int re = WTFApplication._context.checkCallingPermission(Manifest.permission.CAMERA);

        //检查权限
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //如果没有授权，则请求授权
            Toast.makeText(mActivity, "获取开启摄像头权限", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CALL_CAMERA);
        } else {
            //有授权，直接开启摄像头
            callCamera();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //判断请求码
        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_CAMERA) {
            //grantResults授权结果
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //成功，开启摄像头
                callCamera();
            } else {
                //授权失败
                Toast.makeText(mActivity, "你拒绝了打开相机,不能进行设备二维码扫描", Toast.LENGTH_SHORT).show();
            }
            return;
        } else if(requestCode == MY_PERMISSIONS_REQUEST_CALL_LOCATION) {
            //grantResults授权结果
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //成功，开启摄像头
                configNewDevice();
            } else {
                //授权失败
                Toast.makeText(mActivity, "你拒绝了定位,不能进行设备配网操作", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    /**
     * 添加设备
     */
    private void addDevice() {
        // 判断网络连接
        if (!WTFApplication.isConnectingToInternet()) {
            deviceHandler.sendEmptyMessage(2);
        } else {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage("正在添加...");
            mDialog.show();

            timestamp = WTFApplication.getTimeStamp();
            sign = MD5Util.encrypt("/v1/user/device/bind" +
                    "?account=" + account + "&timestamp=" + timestamp + "&token=" + token);
            String uploadUrl = URL.DEVICE_BIND + "?account=" + account +
                    "&timestamp=" + timestamp + "&sign=" + sign;
            final Request postRequest = new Request(uploadUrl)
                    .setMethod(HttpMethods.Post)
                    .setHttpListener(new MyHttpListener<AppMsg>(mActivity) {
                        @Override
                        public void onSuccess(AppMsg s, Response<AppMsg> response) {
                            Log.i("flag", String.valueOf(s.getFlag()));
                            if (s.getFlag() == 1) {
                                DeviceData deviceData = new DeviceData();
                                deviceData.setDeviceName(deviceName);
                                deviceData.setAlias("新设备");
                                WTFApplication.userData.mData.add(WTFApplication.userData.mData.size(), deviceData);
                                WTFApplication.putUserData();
                                adapter.setItems(WTFApplication.userData.mData);
                                deviceHandler.sendEmptyMessage(67);
                            } else {
                                Log.i("err", String.valueOf(s.getErr_code()));
                                Log.i("cause", String.valueOf(s.getCause()));
                                if (s.getErr_code() == 19) {
                                    //设备不存在
                                    deviceHandler.sendEmptyMessage(68);
                                } else {
                                    //设备已绑定
                                    deviceHandler.sendEmptyMessage(69);
                                }
                            }
                        }

                        public void onFailure(HttpException e, Response response) {
                            deviceHandler.sendEmptyMessage(66);
                            super.onFailure(e, response);
                        }
                    });

            MultipartBody body = new MultipartBody();
            body.addPart(new StringPart("device_name", deviceName));
            postRequest.setHttpBody(body);
            HttpUtil.liteHttp.executeAsync(postRequest);
        }
    }

    /**
     * 获取数据
     */
    private void getData() {
        // 判断网络连接
        if (!WTFApplication.isConnectingToInternet()) {
            deviceHandler.sendEmptyMessage(2);
        } else {
            timestamp = WTFApplication.getTimeStamp();
            Log.i("timestamp", String.valueOf(timestamp));
            sign = MD5Util.encrypt("/v1/user/device/list" +
                    "?account=" + account + "&timestamp=" + timestamp + "&token=" + token);
            String uploadUrl = URL.DEVICES_LIST + "?account=" + account +
                    "&timestamp=" + timestamp + "&sign=" + sign;

            Request request = new Request(uploadUrl);
            request.setHttpListener(new MyHttpListener<AppMsg>(mActivity) {
                @Override
                public void onSuccess(AppMsg s, Response<AppMsg> response) {

                    if (s.getFlag() == 1) {
                        if (s.getParams() != null && !s.getParams().isEmpty()) {
                            WTFApplication.userData.mData = JSONObject.parseArray(s.getParams().toJSONString(), DeviceData.class);
                            WTFApplication.putUserData();
                            deviceHandler.sendEmptyMessage(15);
                        } else {
                            deviceHandler.sendEmptyMessage(16);
                        }
                    } else {
                        if(s.getErr_code()==2){
                            deviceHandler.sendEmptyMessage(19);
                        }
                        Log.i("error",s.getErr_code()+"");
                        Log.i("cause",s.getCause()+"");
                        deviceHandler.sendEmptyMessage(17);
                    }
                }

                public void onFailure(HttpException e, Response response) {
                    super.onFailure(e, response);
                    deviceHandler.sendEmptyMessage(18);

                }
            });
            HttpUtil.liteHttp.executeAsync(request);
        }
    }


    /**
     * 删除设备
     *
     * @param pos
     * @param item_device
     */
    private void delete(int pos, String item_device) {
        // 判断网络连接
        if (!WTFApplication.isConnectingToInternet()) {
            deviceHandler.sendEmptyMessage(2);
        } else {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage("正在删除...");
            mDialog.show();

            deletePos = pos;
            deviceName = item_device;

            timestamp = WTFApplication.getTimeStamp();
            sign = MD5Util.encrypt("/v1/user/device/unbind" +
                    "?account=" + account + "&timestamp=" + timestamp + "&token=" + token);
            String uploadUrl = URL.DEVICE_UNBIND + "?account=" + account +
                    "&timestamp=" + timestamp + "&sign=" + sign;
            final Request postRequest = new Request(uploadUrl)
                    .setMethod(HttpMethods.Post)
                    .setHttpListener(new MyHttpListener<AppMsg>(mActivity) {
                        @Override
                        public void onSuccess(AppMsg s, Response<AppMsg> response) {
                            Log.i("flag", String.valueOf(s.getFlag()));
                            if (s.getFlag() == 1) {
                                WTFApplication.userData.mData.remove(deletePos);
                                WTFApplication.putUserData();
                                adapter.setItems(WTFApplication.userData.mData);//update display
                                deviceHandler.sendEmptyMessage(53);
                            } else {
                                deviceHandler.sendEmptyMessage(54);
                            }
                        }

                        public void onFailure(HttpException e, Response response) {
                            deviceHandler.sendEmptyMessage(55);
                            super.onFailure(e, response);
                        }
                    });

            MultipartBody body = new MultipartBody();
            body.addPart(new StringPart("device_name", deviceName));
            postRequest.setHttpBody(body);
            HttpUtil.liteHttp.executeAsync(postRequest);


        }
    }

    /**
     * 重命名
     */
    private void renameDevice(int _pos, final String item_device) {
        // 判断网络连接
        if (item_device.isEmpty()) {
            deviceHandler.sendEmptyMessage(73);
            return;
        }
        if (!WTFApplication.isConnectingToInternet()) {
            deviceHandler.sendEmptyMessage(2);
        } else {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage("正在修改...");
            mDialog.show();

            deviceAlias = item_device;
            renamePos = _pos;
            timestamp = WTFApplication.getTimeStamp();
            sign = MD5Util.encrypt("/v1/user/device/update" +
                    "?account=" + account + "&timestamp=" + timestamp + "&token=" + token);
            String uploadUrl = URL.DEVICE_UPDATE + "?account=" + account +
                    "&timestamp=" + timestamp + "&sign=" + sign;
            final Request request = new Request(uploadUrl)
                    .setMethod(HttpMethods.Post)
                    .setHttpListener(new MyHttpListener<AppMsg>(mActivity) {
                        @Override
                        public void onSuccess(AppMsg s, Response<AppMsg> response) {
                            if (s.getFlag() == 1) {
                                WTFApplication.userData.mData.get(renamePos).setAlias(deviceAlias);
                                WTFApplication.putUserData();
                                adapter.setItems(WTFApplication.userData.mData);//update display/
                                deviceHandler.sendEmptyMessage(71);
                            } else {
                                deviceHandler.sendEmptyMessage(72);
                            }
                        }

                        public void onFailure(HttpException e, Response response) {
                            deviceHandler.sendEmptyMessage(72);
                            super.onFailure(e, response);
                        }
                    });

            MultipartBody body = new MultipartBody();
            body.addPart(new StringPart("alias", deviceAlias));
            body.addPart(new StringPart("device_name", WTFApplication.userData.mData.get(renamePos).getDeviceName()));
            request.setHttpBody(body);
            HttpUtil.liteHttp.executeAsync(request);
        }
    }

    public void onActivityResult() {

    }


    private String deviceId = null;

    private handler deviceHandler;


    static class handler extends Handler {
        WeakReference<WifiFragment> mActivity;

        handler(WifiFragment activity) {
            mActivity = new WeakReference<>(activity);
        }

        public void handleMessage(Message msg) {
            WifiFragment theActivity = mActivity.get();
            switch (msg.what) {
                case 0://配网成功后绑定设备
                    theActivity.deviceName = theActivity.configuredDevices[0].getmacAdrress().replaceAll(":", "");
                    Log.i("mac", theActivity.deviceName);
                    //theActivity.deviceName= theActivity.configuredDevices[0].getmacAdrress();
                    if (theActivity.deviceName != null) theActivity.addDevice();
                    break;
                case 1:
                    Toast.makeText(theActivity.mActivity, "设备绑定成功", Toast.LENGTH_SHORT)
                            .show();
                    theActivity.adapter.setItems(WTFApplication.userData.mData);
                    break;
                case 2:
                    theActivity.adapter.setItems(WTFApplication.userData.mData);
                    theActivity.load.setVisibility(View.GONE);
                    theActivity.tv_error_info.setText("未联网，请先联网");
                    theActivity.error.setVisibility(View.VISIBLE);
                    Toast.makeText(theActivity.mActivity, "未联网，请先联网", Toast.LENGTH_SHORT).show();
                    break;
                case 53:
                    theActivity.mDialog.cancel();
                    if (WTFApplication.userData.isHaveDevice() == false) {
                        theActivity.tv_error_info.setText("你还没有添加设备哦");
                        theActivity.error.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(theActivity.mActivity, "删除成功！", Toast.LENGTH_SHORT).show();
                    break;
                case 54:
                case 55:
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity.mActivity, "删除失败，请重试", Toast.LENGTH_SHORT).show();
                    break;
                case 15:
                    theActivity.load.setVisibility(View.GONE);
                    theActivity.error.setVisibility(View.GONE);
                    theActivity.adapter.setItems(WTFApplication.userData.mData);
                    break;
                case 16:
                    theActivity.load.setVisibility(View.GONE);
                    theActivity.adapter.setItems(WTFApplication.userData.mData);
                    if (WTFApplication.userData.isHaveDevice() == false) {
                        theActivity.tv_error_info.setText("你还没有添加设备哦");
                        theActivity.error.setVisibility(View.VISIBLE);
                    } else {
                        theActivity.error.setVisibility(View.GONE);
                        Toast.makeText(theActivity.mActivity,
                                "没有获取到最新的设备信息", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 17:
                    theActivity.adapter.setItems(WTFApplication.userData.mData);
                    theActivity.load.setVisibility(View.GONE);
                    theActivity.tv_error_info.setText("网络超时，请重试");
                    theActivity.error.setVisibility(View.VISIBLE);
                    //  Toast.makeText(theActivity.mActivity, "网络超时，请重试", Toast.LENGTH_SHORT).show();
                    break;
                case 18:
                    theActivity.adapter.setItems(WTFApplication.userData.mData);
                    theActivity.load.setVisibility(View.GONE);
                    theActivity.tv_error_info.setText("网络异常，请重试");
                    theActivity.error.setVisibility(View.VISIBLE);
                    //  Toast.makeText(theActivity.mActivity, "网络超时，请重试", Toast.LENGTH_SHORT).show();
                    break;
                case 19:
                    theActivity.adapter.setItems(WTFApplication.userData.mData);
                    theActivity.load.setVisibility(View.GONE);
                    theActivity.tv_error_info.setText("手机上的时间与服务器上的时间不同，\n请把手机时间与服务器时间同步");
                    theActivity.error.setVisibility(View.VISIBLE);
                    //  Toast.makeText(theActivity.mActivity, "手机上的时间与服务器上的时间不同，\n请把时间调成自动校时", Toast.LENGTH_SHORT).show();
                    break;
                case 66://adddevice
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity.mActivity, "网络超时，请重新配网", Toast.LENGTH_SHORT).show();
                    break;
                case 67://adddevice
                    theActivity.mDialog.cancel();
                    theActivity.load.setVisibility(View.GONE);
                    theActivity.error.setVisibility(View.GONE);
                    Toast.makeText(theActivity.mActivity, "添加设备成功", Toast.LENGTH_SHORT).show();
                    break;
                case 68://adddevice
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity.mActivity, "非法设备，请确认设备", Toast.LENGTH_SHORT).show();
                    break;
                case 69://adddevice
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity.mActivity, "设备已绑定", Toast.LENGTH_SHORT).show();
                    break;
                case 71://rename message
                    theActivity.mDialog.cancel();
                    theActivity.load.setVisibility(View.GONE);
                    theActivity.error.setVisibility(View.GONE);
                    Toast.makeText(theActivity.mActivity,
                            "重命名操作成功", Toast.LENGTH_SHORT).show();
//                    theActivity.userData.mData.add(theActivity.userData.mData.size(), theActivity.deviceData);
//                    theActivity.adapter.setItems(theActivity.userData.mData);
//                    if (theActivity.userData.mData.size() == 1) {
//                        theActivity.userData.setHaveDevice(true);
//                        SharedPreUtil.getInstance().putUser(theActivity.userData);
//                        SharedPreferences preferences = theActivity.mActivity.getSharedPreferences("device", Context.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = preferences.edit();
//                        editor.putString("deviceName", theActivity.deviceData.getDeviceName());
//                        editor.putString("alias", "新设备");
//                        editor.commit();
//                    }
                    //theActivity.getData();
                    break;
                case 72://rename message
                    theActivity.mDialog.cancel();
                    Toast.makeText(theActivity.mActivity, "重命名操作失败", Toast.LENGTH_SHORT).show();
                    break;
                case 73://rename message
                    Toast.makeText(theActivity.mActivity, "别名不能为空", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public void onHeaderRefresh(RefreshView view) {
        mRefreshAsynTask = new RefreshDataAsynTask();
        mRefreshAsynTask.execute(null, null);
    }

    // 下拉刷新的实现
    class RefreshDataAsynTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                getData();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            mPullToRefreshView.onHeaderRefreshComplete();
        }
    }


    /**
     * WIFI模块
     */
    public enum SECURITY_TYPE {
        SECURITY_NONE,
        SECURITY_WEP,
        SECURITY_PSK,
        SECURITY_EAP
    }

    private static final String pwfileName = "preAPInfoFile";
    private static final String pinfileName = "prePINFile";
    private static final String TAG = "<simple config wizard>";
    private static final String defaultPINcode = "";
    private static final String backdoor_PINCODE = "00000000";
    private static final String aboutMsg = "uncheck device if any unwanted";

    private static final int discoveryTimeout = 3000; //3s
    private static final int configTimeout = 120000;//120s
    private static final int deviceNumber = 32;
    private static final int APNumber = 100;

    private int CurrentItem;
    private String CurrentControlIP;
    private String QRCodeScanResult;

    //global

    EditText edittxt_PINcode;
    View wifiPW_EntryView;
    LayoutInflater factory;

    String pinCodeText = "";
    String presave_pinCode = "";//pre file saved
    String ssid_name = "";
    String AP_password = "";
    String[] delConfirmIP;

    boolean ConnectAPProFlag = false;//user need to connect ap
    boolean ConfigureAPProFlag = false;//user need to connect ap
    boolean isWiFiEnable = false;
    boolean DiscovEnable = false;
    boolean isDeletedDevice = false;
    boolean isControlSingleDevice = false;
    boolean TimesupFlag_cfg = true;
    boolean TimesupFlag_rename = false;
    boolean TimesupFlag_remove = false;
    boolean ShowCfgSteptwo = false;

    private int mSingleChoiceID = -1;
    private String PINGet = null;
    private String PINSet = null;
    private SimpleAdapter adapter_deviceInfo;
    private ProgressDialog pd;

    private List<HashMap<String, Object>> DevInfo;
    private List<HashMap<String, Object>> wifiArrayList = new ArrayList<HashMap<String, Object>>();
    private ScanResult mScanResult;
    private List<ScanResult> mScanResults;

    private boolean WifiConnected = false;
    protected WifiManager mWifiManager;

    AlertDialog APList_alert;
    AlertDialog.Builder APList_builder;
    ConfigurationDevice.DeviceInfo[] configuredDevices;
    ConfigurationDevice.DeviceInfo[] APInfo;
    ConfigurationDevice.DeviceInfo SelectedAPInfo;
    ConfigurationDevice.DeviceInfo[] deviceList;

    Thread connectAPThread = null;
    Thread backgroundThread = null;

    private FileOps fileOps = new FileOps();

    /**
     * 初始化wifi
     */
    private void initWifi() {
        //SCLib.rtk_sc_init();
        SCLib.TreadMsgHandler = new MsgHandler();

        SCLib.WifiInit(mActivity);
        fileOps.SetKey(SCLib.WifiGetMacStr());

        initData();
        initComponent();

        initComponentAction();

        mWifiManager = (WifiManager) mActivity.getSystemService(WIFI_SERVICE);

    }

    private void initData() {
        configuredDevices = new ConfigurationDevice.DeviceInfo[deviceNumber];
        System.out.println("deviceNumber:" + deviceNumber);
        for (int i = 0; i < deviceNumber; i++) {
            configuredDevices[i] = new ConfigurationDevice.DeviceInfo();
            configuredDevices[i].setaliveFlag(1);
            configuredDevices[i].setName("");
            configuredDevices[i].setIP("");
            configuredDevices[i].setmacAdrress("");
            configuredDevices[i].setimg(null);
            configuredDevices[i].setconnectedflag(false);
        }

        APInfo = new ConfigurationDevice.DeviceInfo[APNumber];
        for (int i = 0; i < APNumber; i++) {
            APInfo[i] = new ConfigurationDevice.DeviceInfo();
        }

        SelectedAPInfo = new ConfigurationDevice.DeviceInfo();
        APList_Clear();


        SCCtlOps.ConnectedSSID = null;
        SCCtlOps.ConnectedPasswd = null;

    }

    private void initComponent() {

        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //int s_height = metrics.heightPixels;
        //int s_width = metrics.widthPixels;
        //Log.d(TAG, "initComponent: " + String.valueOf(s_height));
        //Log.d(TAG, "initComponent: " + String.valueOf(s_width));


    }

    /**
     * MsgHandler
     */
    private class MsgHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            //Log.d(TAG, "msg.what: " + msg.what);
            switch (msg.what) {
                case ~SCCtlOps.Flag.CfgSuccessACK://Config Timeout
                    //Log.d("MsgHandler","Config Timeout");
                    SCLib.rtk_sc_stop();

                    break;
                case SCCtlOps.Flag.CfgSuccessACK: //Not Showable
                    //Log.d("MsgHandler","Config SuccessACK");
                    //Toast.makeText(getApplication(), "Get CfgSuccessACK", Toast.LENGTH_SHORT).show();
                    SCLib.rtk_sc_stop();
//				Log.d(TAG, "CurrentView: " + CurrentView);
                    TimesupFlag_cfg = true;

                    if (ShowCfgSteptwo == true)
                        mActivity.runOnUiThread(Cfg_changeMessage);

                    List<HashMap<String, Object>> InfoList = new ArrayList<HashMap<String, Object>>();
                    SCLib.rtk_sc_get_connected_sta_info(InfoList);
                    String ip = InfoList.get(0).get("IP").toString();
                    if (!ip.equals("0.0.0.0")) {// Client Got IP
                        //Log.d(TAG, "Client Got IP");
                        //SendCtlDevPacket(SCCtlOps.Flag.RenameDev, PINSet, ip, ReNameStr);
                        //RenameOfConfig = false;
                    }
                    break;
                case SCCtlOps.Flag.DiscoverACK:
                    //Log.d("MsgHandler","DiscoverACK");
                    //DiscovEnable = false;
                    SCCtlOps.handle_discover_ack((byte[]) msg.obj);
                    if (SCCtlOps.DiscoveredNew) {
                        show_discoverDevice();
                        //handler_pd.sendEmptyMessage(0);
                    }

                    break;
                case ~SCCtlOps.Flag.DiscoverACK:
                    //Log.d("MsgHandler","Discovery timeout.");
                    DiscovEnable = false;
                    //handler_pd.sendEmptyMessage(0);
                    break;
                case SCCtlOps.Flag.DelProfACK:

                    //Toast.makeText(getApplication(), "Get DelProfACK", Toast.LENGTH_SHORT).show();

                    //Log.d("MsgHandler","Del Profile ACK");
                    rtk_sc_send_confirm_packet(SCCtlOps.Flag.DelProf);
                    isDeletedDevice = true;

                    SCCtlOps.rtk_sc_control_reset();

                    DiscovEnable = true;
                    TimesupFlag_remove = true;

                    new Thread(new Runnable() {
                        public void run() {
                            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                            byte[] DiscovCmdBuf = SCCtlOps.rtk_sc_gen_discover_packet(SCLib.rtk_sc_get_default_pin());

                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }

                            SCLib.rtk_sc_send_discover_packet(DiscovCmdBuf, "255.255.255.255");
                            // Update Status
                            Message msg = Message.obtain();
                            msg.obj = null;
                            msg.what = ~SCCtlOps.Flag.DiscoverACK; //timeout
                            SCLib.TreadMsgHandler.sendMessage(msg);
                            //handler_pd.sendEmptyMessage(0);
                        }
                    }).start();

                    show_discoverDevice();

                    break;
                case SCCtlOps.Flag.RenameDevACK:

                    rtk_sc_send_confirm_packet(SCCtlOps.Flag.RenameDev);

                    SCCtlOps.rtk_sc_control_reset();
                    TimesupFlag_rename = true;

                    break;
                default:
                    //Log.d("MsgHandler","default");
                    break;
            }
        }
    }

    /**
     * 发送确认数据包
     *
     * @param flag
     */
    private void rtk_sc_send_confirm_packet(final int flag) {
        new Thread(new Runnable() {
            String pin;

            public void run() {
                byte[] buf;
                Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);

                pin = PINSet;
                //for confirm to delete
                if (flag == SCCtlOps.Flag.DelProf && delConfirmIP != null && isControlSingleDevice != true) {
                    int DelArraySize = delConfirmIP.length;

                    for (int i = 0; i < DelArraySize; i++) {
                        if (delConfirmIP[i].length() > 0) {
                            //Log.d(TAG,"DelProf rtk_sc_send_confirm_packet :"+delConfirmIP[i]);
                            buf = SCCtlOps.rtk_sc_gen_control_confirm_packet(flag,
                                    SCLib.rtk_sc_get_default_pin(), pin);
                            if (delConfirmIP[i].equals("0.0.0.0")) {
                                //Toast.makeText(WifiConnectActivity.this, "rtk_sc_send_confirm_packet1:"+CurrentControlIP, Toast.LENGTH_SHORT).show();
                                //Log.d(TAG,"rtk_sc_send_confirm_packet1: "+CurrentControlIP);
                                for (int retry = 0; retry < 5; retry++) {
                                    try {
                                        Thread.sleep(1);
                                        SCLib.rtk_sc_send_control_packet(buf, CurrentControlIP);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else {
                                //Toast.makeText(WifiConnectActivity.this, "rtk_sc_send_confirm_packet2:"+delConfirmIP[i], Toast.LENGTH_SHORT).show();
                                //Log.d(TAG,"rtk_sc_send_confirm_packet2"+delConfirmIP[i]);
                                for (int retry = 0; retry < 5; retry++) {
                                    try {
                                        Thread.sleep(1);

                                        SCLib.rtk_sc_send_control_packet(buf, delConfirmIP[i]);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                } else {
                    //Log.d(TAG,"rtk_sc_send_confirm_packet3"+CurrentControlIP);
                    //Toast.makeText(WifiConnectActivity.this, "rtk_sc_send_confirm_packet3:"+CurrentControlIP, Toast.LENGTH_SHORT).show();
                    buf = SCCtlOps.rtk_sc_gen_control_confirm_packet(flag,
                            SCLib.rtk_sc_get_default_pin(), pin);
                    for (int retry = 0; retry < 5; retry++) {
                        try {
                            Thread.sleep(1);
                            SCLib.rtk_sc_send_control_packet(buf, CurrentControlIP);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                }

            }
        }).start();
    }

    private Runnable Cfg_changeMessage = new Runnable() {
        @Override
        public void run() {
            //Log.v(TAG, strCharacters);
            pd.setMessage("Waiting for the device");
        }
    };

    @SuppressLint("SdCardPath")
    private void initComponentAction() {
        //scan configured devices
        SCCtlOps.rtk_sc_control_reset();

        ConnectivityManager connManager = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            DiscovEnable = true;
            DiscoveryDevice(discoveryTimeout);
            show_discoverDevice();
        }

    }


    public void configNewDevice_OnClick() {
        Log.i(TAG, "configNewDevice_OnClick");
        //int itmeNum = 0;

        mSingleChoiceID = -1;
        WifiManager wifi = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);

        //check wifi is disable
        if (!wifi.isWifiEnabled()) {
            // SCLib.WifiOpen();
            Toast.makeText(mActivity, "请打开WIFI", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 0);

        } else {
            //检查权限
            if ((ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)  ||
                    (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED)){
                //如果没有授权，则请求授权
                Toast.makeText(mActivity, "获取开启定位权限", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_CALL_LOCATION);
            } else {
                //有授权，直接开启摄像头
                configNewDevice();
            }
        }
    }
    //显示WIFI
    public void configNewDevice() {
//        Log.i(TAG, "configNewDevice_OnClick");
//        //int itmeNum = 0;
//
//        Log.i("isWIFIGranted", isWIFIGranted() + "");
//        mSingleChoiceID = -1;
//        WifiManager wifi = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
//
//        //check wifi is disable
//        if (!wifi.isWifiEnabled()) {
//            // SCLib.WifiOpen();
//            Toast.makeText(mActivity, "请打开WIFI", Toast.LENGTH_SHORT).show();
//            startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
//
//        } else if (!isWIFIGranted()) {
//            getAppDetailSettingIntent(mActivity);
//            Toast.makeText(mActivity, "请开启网络位置权限", Toast.LENGTH_SHORT).show();
//        } else
        {

            GetAllWifiList();

            fileOps.SetKey(WTFApplication.SCLib.WifiGetMacStr());


            LayoutInflater inflater = LayoutInflater.from(mActivity.getApplicationContext());
            View customTitle = inflater.inflate(R.layout.wifi_custom_title_bar, null);
            final ImageView addNetworkkBtn = (ImageView) customTitle.findViewById(R.id.addNewNetworkBtn);
            addNetworkkBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    SCCtlOps.addNewNetwork = true;

                    addNetworkPopup();
                }

            });

            int itmeNum = 0;
            for (int num = 0; num < mScanResults.size(); num++) {
                if (num >= APNumber)
                    break;
                if (APInfo[num].getconnectedflag()) {
                    itmeNum++;
                    deviceList = new ConfigurationDevice.DeviceInfo[itmeNum];
                    deviceList[0] = new ConfigurationDevice.DeviceInfo();
                    deviceList[0] = APInfo[num];
                    break;
                }
            }

            //Log.d(TAG, "configNewDevice_OnClick itmeNum:" + String.valueOf(itmeNum));

            if (itmeNum > 0) {

                Log.i(TAG, itmeNum + "");

                //set foucs connect item
                int foucsIndex = 0;

                SCCtlOps.isHiddenSSID = false;
                SCCtlOps.addNewNetwork = false;
                ListAdapter adapter = new DeviceAdapter(mActivity, deviceList);


                APList_builder = new AlertDialog.Builder(mActivity);

                APList_builder.setCustomTitle(customTitle);

                APList_builder.setCancelable(false);


                APList_builder.setSingleChoiceItems(adapter, foucsIndex, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // no use "which" due to sort name

                        //convert to origin index
                        mSingleChoiceID = which;

                        fileOps.ParseSsidPasswdFile(deviceList[which].getName());
                        dialog.dismiss();

                        SCCtlOps.ConnectedSSID = deviceList[which].getName();

                        String content = "";
                        byte[] buff = new byte[256]; //input stream buffer
                        try {
                            FileInputStream reader = mActivity.openFileInput(pwfileName);
                            while ((reader.read(buff)) != -1) {
                                content += new String(buff).trim();
                            }
                            reader.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            //Log.e("deviceinfo FileNotFoundException", content);
                        } catch (IOException e) {
                            Log.e("deviceinfo IOException", content);
                        }
                        //Log.d(TAG, "APinfo:" + content);
                        SCCtlOps.ConnectedPasswd = "";
                        AP_password = "";
                        if (content.length() > 0) {
                            String[] APitem = content.split(";");
                            for (String splitString : APitem) {
                                String[] array = splitString.split(",");
                                if (deviceList[which].getName().equals(array[0]) == true) {//ssid is the same

                                    if (array.length > 1)
                                        AP_password = array[1];
                                    else
                                        AP_password = "";

                                    SCCtlOps.StoredPasswd = AP_password;
                                    SCCtlOps.ConnectedPasswd = SCCtlOps.StoredPasswd;

                                    //isPasswordExist = true;
                                    break;
                                }
                            }
                        }

                        //check it if it need password

                        if (deviceList[which].getsecurityType() == 0) {//connect directly
                            //do connect ap action
                            AP_password = "";
                            ConnectAPProFlag = true;

                            pd = new ProgressDialog(mActivity);
                            pd.setTitle("连接中");
                            pd.setMessage("请等待...");
                            pd.setIndeterminate(true);
                            pd.setCancelable(false);
                            pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    ConnectAPProFlag = false;
                                }
                            });
                            pd.show();

                            connectAPThread = new Thread() {
                                @Override
                                public void run() {

                                    try {
                                        if (connect_action(mSingleChoiceID) == true) {

                                            handler_pd.sendEmptyMessage(0);
                                            //Log.d(TAG, "connect AP: "+ APInfo[mSingleChoiceID].getName()+ "success");
                                            mActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    if (ConnectAPProFlag) {
                                                        //show "start to configure"
                                                        ConfigPINcode();
                                                        ConnectAPProFlag = false;
                                                    }
                                                }
                                            });
                                        } else {
                                            if (ConnectAPProFlag) {
                                                Log.e(TAG, "connect AP:" + SelectedAPInfo.getName() + "fail");

                                                mActivity.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        AlertDialog.Builder errorAlert = new AlertDialog.Builder(mActivity);

                                                        errorAlert.setTitle("连接失败");
                                                        errorAlert.setMessage("请确认输入的密码是否正确\n");
                                                        errorAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                                            @Override
                                                            public void onClick(DialogInterface arg0, int arg1) {
                                                                //// TODO: 2016/12/17 配置wifi
                                                                startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 0);

                                                            }
                                                        });
                                                        errorAlert.show();
                                                    }
                                                });
                                            }
                                            handler_pd.sendEmptyMessage(0);
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            connectAPThread.start();

                        } else {//ask user to key in password
                            LayoutInflater factory = LayoutInflater.from(mActivity);
                            final View wifiPW_EntryView = factory.inflate(R.layout.wifi_password_entry, null);
                            final EditText edittxt_apPassword = (EditText) wifiPW_EntryView.findViewById(R.id.id_ap_password);
                            CheckBox password_checkbox;
                            password_checkbox = (CheckBox) wifiPW_EntryView.findViewById(R.id.checkBox_password);

                            if (AP_password.length() > 0)
                                edittxt_apPassword.setText(AP_password, TextView.BufferType.EDITABLE);
                            else
                                edittxt_apPassword.setText("", TextView.BufferType.EDITABLE);

                            password_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                                    if (isChecked)
                                        edittxt_apPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                    else
                                        edittxt_apPassword.setInputType(129);
                                }

                            });

                            AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
                            alert.setCancelable(false);
                            //switch password input type
                            alert.setTitle("请输入密码:");
                            alert.setCancelable(false);
                            alert.setView(wifiPW_EntryView);
                            alert.setPositiveButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {

                                        }
                                    });
                            alert.setNegativeButton("连接",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {

                                            //Log.d(TAG, "AP Password:" + edittxt_apPassword.getText().toString());

                                            //AP_password;
                                            if (edittxt_apPassword.getText().toString().length() > 0) {
                                                AP_password = edittxt_apPassword.getText().toString();
                                            } else {
                                                AlertDialog.Builder msgAlert = new AlertDialog.Builder(mActivity);
                                                msgAlert.setTitle("错误");
                                                msgAlert.setMessage("请确认输入的密码是否正确\n");
                                                msgAlert.setPositiveButton("确定", null);
                                                msgAlert.show();
                                                return;
                                            }

                                            //do connect ap action
                                            ConnectAPProFlag = true;

                                            pd = new ProgressDialog(mActivity);
                                            pd.setTitle("连接");
                                            pd.setMessage("请等待...");
                                            pd.setIndeterminate(true);
                                            pd.setCancelable(false);
                                            pd.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    ConnectAPProFlag = false;
                                                }
                                            });
                                            pd.show();

                                            connectAPThread = new Thread() {
                                                @Override
                                                public void run() {

                                                    try {
                                                        if (connect_action(mSingleChoiceID) == true) {

                                                            //Log.d(TAG, "connect AP: " + APInfo[mSingleChoiceID].getName()+ "sucess");
                                                            mActivity.runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {

                                                                    if (ConnectAPProFlag == true) {
                                                                        //show "start to configure"
                                                                        ConfigPINcode();
                                                                        ConnectAPProFlag = false;
                                                                    }
                                                                }
                                                            });

                                                        } else {
                                                            if (ConnectAPProFlag != false) {
                                                                //Log.e(TAG, "connect AP: "+APInfo[mSingleChoiceID].getName()+ "fail");

                                                                mActivity.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        AlertDialog.Builder errorAlert = new AlertDialog.Builder(mActivity);

                                                                        errorAlert.setTitle("连接WIFI失败");
                                                                        errorAlert.setMessage("\"请确认输入的密码是否正确\\n\"");
                                                                        errorAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                                                            @Override
                                                                            public void onClick(DialogInterface arg0, int arg1) {
                                                                                startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 0);

                                                                            }
                                                                        });
                                                                        errorAlert.show();
                                                                    }
                                                                });
                                                            }
                                                            handler_pd.sendEmptyMessage(0);
                                                        }
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            };
                                            connectAPThread.start();
                                        }
                                    });
                            alert.show();
                        }
                    }

                });

                APList_builder.setPositiveButton("取消", null);

                APList_alert = APList_builder.create();
                APList_alert.show();
            } else {
                Toast.makeText(mActivity, "请连接您期望配置的wifi", Toast.LENGTH_SHORT).show();
                startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
                // startActivity(new Intent( android.provider.Settings.ACTION_WIRELESS_SETTINGS))
            }
        }
    }

    /**
     * 获取所有wifi的列表
     */
    private void GetAllWifiList() {

        ConnectivityManager connManager = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        String tmp = "";
        String connected_ssid = "";
        String connected_bssid = "";
        String connected_ip = "";
        WifiConnected = false;

        WifiManager wifiManager = (WifiManager) mActivity.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED || mWifi.isConnected()) {

            connected_ssid = wifiInfo.getSSID();
            connected_bssid = wifiInfo.getBSSID();
            if (connected_ssid.indexOf("\"") == 0)
                tmp = connected_ssid.substring(1, connected_ssid.length() - 1);
            else
                tmp = connected_ssid;
            int myIp = wifiInfo.getIpAddress();
            int intMyIp3 = myIp / 0x1000000;
            int intMyIp3mod = myIp % 0x1000000;

            int intMyIp2 = intMyIp3mod / 0x10000;
            int intMyIp2mod = intMyIp3mod % 0x10000;

            int intMyIp1 = intMyIp2mod / 0x100;
            int intMyIp0 = intMyIp2mod % 0x100;

            connected_ip = String.valueOf(intMyIp0)
                    + "." + String.valueOf(intMyIp1)
                    + "." + String.valueOf(intMyIp2)
                    + "." + String.valueOf(intMyIp3);

            WifiConnected = true;
            Log.i(TAG, "Connected AP:" + tmp);
        }

        mScanResults = SCLib.WifiGetScanResults();
        //Log.d(TAG, "mScanResults: "+ String.valueOf(mScanResults.size()) );

        APList_Clear();
        wifiArrayList.clear();

        if (mScanResults != null) {

            boolean checkSameSSID = false;

            int i = 0;

            for (int iScan = 0; iScan < mScanResults.size() && i < APNumber; iScan++) {
                checkSameSSID = false;
                if (iScan < APNumber) {
                    mScanResult = mScanResults.get(iScan);
                    //Log.d(TAG, "AP"+String.valueOf(i)  +" : " + mScanResult.SSID + "("+ mScanResult.capabilities+")" + String.valueOf(mScanResult.level));
                } else
                    continue;

                if (mScanResult.SSID.length() == 0) continue;

                for (int numAP = 0; numAP < APNumber; numAP++) {
                    if (APInfo[numAP].getaliveFlag() == 1) {
                        if (mScanResult.SSID.equals(APInfo[numAP].getName()))
                            checkSameSSID = true;
                    }
                }
                if (checkSameSSID) {
                    Log.d(TAG, "checkSameSSID");
                    continue;
                } else {

                    if ((SCCtlOps.ConnectedSSID != null) &&
                            (SCCtlOps.ConnectedSSID.length() > 0) &&
                            (SCCtlOps.ConnectedSSID.equals(mScanResult.SSID)) &&
                            SCLib.isWifiConnected(SCCtlOps.ConnectedSSID)) {
                        //Log.d(TAG, "AP"+String.valueOf(i)  +" : " + mScanResult.SSID );
                    }

                    //Log.d(TAG,"=====================");
                    //Log.d(TAG,"mScanResult.SSID:" + mScanResult.SSID);
                    //Log.d(TAG,"mScanResult.SSID len:" + mScanResult.SSID.getBytes().length);
                    //Log.d(TAG,String.valueOf(getSecurity(mScanResult)));
                    //Log.d(TAG,mScanResult.capabilities);

                    //APInfo[i].setsecurityType(mScanResult.capabilities);
                    APInfo[i].setsecurityType(getSecurity(mScanResult));
                    APInfo[i].setaliveFlag(1);
                    APInfo[i].setName(mScanResult.SSID);
                    APInfo[i].setmacAdrress(mScanResult.BSSID);
                    APInfo[i].setconnectedflag(false);
                    APInfo[i].setIP("");

                    if (mScanResult.level > -50)
                        APInfo[i].setimg(getResources().getDrawable(R.drawable.signal5));
                    else if (mScanResult.level > -60)
                        APInfo[i].setimg(getResources().getDrawable(R.drawable.signal4));
                    else if (mScanResult.level > -70)
                        APInfo[i].setimg(getResources().getDrawable(R.drawable.signal3));
                    else if (mScanResult.level > -80)
                        APInfo[i].setimg(getResources().getDrawable(R.drawable.signal2));
                    else
                        APInfo[i].setimg(getResources().getDrawable(R.drawable.signal1));

                    //tmp = "\"" + mScanResult.SSID + "\"";
                    if (WifiConnected == true) {
                        if (connected_bssid.equals(APInfo[i].getmacAdrress()) &&
                                APInfo[i].getName().length() > 0) {
                            APInfo[i].setIP(connected_ip);
                            APInfo[i].setconnectedflag(true);

                            SelectedAPInfo.setconnectedflag(true);
                            SelectedAPInfo.setName(APInfo[i].getName());
                            SelectedAPInfo.setaliveFlag(1);
                            SelectedAPInfo.setimg(APInfo[i].getimg());
                            SelectedAPInfo.setIP(APInfo[i].getIP());
                            SelectedAPInfo.setmacAdrress(APInfo[i].getmacAdrress());
                            SelectedAPInfo.setsecurityType(APInfo[i].getsecurityType());

                            //Log.d(TAG,SelectedAPInfo.getName());
                        }
                    }
                }
                i++;
            }
        }

    }

    /**
     * 清除AP列表
     */
    private void APList_Clear() {
        for (int i = 0; i < APNumber; i++) {
            APInfo[i].setconnectedflag(false);
            APInfo[i].setaliveFlag(0);
            APInfo[i].setName("");
            APInfo[i].setIP("");
            APInfo[i].setmacAdrress("");
            APInfo[i].setsecurityType(0);
            APInfo[i].setimg(null);
        }

        SelectedAPInfo = new ConfigurationDevice.DeviceInfo();
        SelectedAPInfo.setconnectedflag(false);
        SelectedAPInfo.setaliveFlag(0);
        SelectedAPInfo.setName("");
        SelectedAPInfo.setIP("");
        SelectedAPInfo.setmacAdrress("");
        SelectedAPInfo.setsecurityType(0);
        SelectedAPInfo.setimg(null);

    }

    /**
     * 手动添加网络的弹出框
     */
    private void addNetworkPopup() {
        if (APList_alert != null) {
            APList_alert.cancel();
        }


        LayoutInflater inflater = mActivity.getLayoutInflater();
        View addNetworkView = inflater.inflate(R.layout.wifi_add_network_content,
                (ViewGroup) mActivity.findViewById(R.id.add_network_dialog));

        final EditText network_name_edit;
        final EditText network_pw_edit;
        final Spinner encrypt_type_spinner;
        final ArrayAdapter<String> encrypt_adapter;
        final String[] encryption_types = {"NONE", "WEP", "WAPI", "WPA-PSK", "WPA2-PSK", "WPA_EAP"};
        CheckBox password_checkbox;


        network_name_edit = (EditText) addNetworkView.findViewById(R.id.network_name_edit);
        network_pw_edit = (EditText) addNetworkView.findViewById(R.id.id_ap_password);
        password_checkbox = (CheckBox) addNetworkView.findViewById(R.id.checkBox_password);

        encrypt_type_spinner = (Spinner) addNetworkView.findViewById(R.id.encrypt_type);
        encrypt_adapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, encryption_types);
        encrypt_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        encrypt_type_spinner.setAdapter(encrypt_adapter);
        encrypt_type_spinner.setOnItemSelectedListener(null);
        encrypt_type_spinner.setVisibility(View.VISIBLE);

        password_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked)
                    network_pw_edit.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                else
                    network_pw_edit.setInputType(129);
            }

        });

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("手动添加网络:")
                .setIcon(R.drawable.ic_dialog_icon)
                .setView(addNetworkView)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ssid_name = network_name_edit.getText().toString();
                        AP_password = network_pw_edit.getText().toString();
                        final String encrypt_type = encrypt_type_spinner.getSelectedItem().toString();
                        SCCtlOps.isHiddenSSID = true;
//	 			Log.d(TAG, "network_name_edit: " + ssid_name);
//	 			Log.d(TAG, "encrypt_type: " + encrypt_type);
//	 			Log.d(TAG, "is_hidden_ssid: " + SCCtlOps.isHiddenSSID);

	 			/*if(encrypt_type.equals("NONE")) {
                     encrypt_type = "";
	 			} else {
	 				encrypt_type = "[" + encrypt_type + "]";
	 			}
	 			Log.d(TAG, "encrypt_type: " + encrypt_type);*/
                        ConnectAPProFlag = true;
                        pd = new ProgressDialog(mActivity);
                        pd.setTitle("连接中");
                        pd.setMessage("请等待");
                        pd.setIndeterminate(true);
                        pd.setCancelable(false);
                        pd.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ConnectAPProFlag = false;
                            }
                        });
                        pd.show();

                        connectAPThread = new Thread() {
                            public void run() {

                                String jsonSsidStr = "{" +
                                        "\"SSID\":\"" + ssid_name + "\"" +
                                        ",\"BSSID\":\"" + "\"" +
                                        ",\"capabilities\":\"" + encrypt_type + "[ESS]\"" +
                                        ",\"level\":" + 0 +
                                        ",\"frequency\":" + 0 +
                                        "}";

                                Log.d(TAG, "jsonSsidStr: " + jsonSsidStr + " pw:" + AP_password);
                                Gson gson = new Gson();
                                SCCtlOps.reBuiltScanResult = gson.fromJson(jsonSsidStr,
                                        new TypeToken<ScanResult>() {
                                        }.getType());

//	 		 			Log.i(TAG, "reBuiltScanResult: " + SCCtlOps.reBuiltScanResult);
//	 		 			Log.d(TAG, "reBuiltScanResult.SSID: " + SCCtlOps.reBuiltScanResult.SSID);
//	 		 			Log.d(TAG, "reBuiltScanResult.BSSID: " + SCCtlOps.reBuiltScanResult.BSSID);
//	 		 			Log.d(TAG, "reBuiltScanResult.capabilities: " + SCCtlOps.reBuiltScanResult.capabilities);
//	 		 			Log.d(TAG, "reBuiltScanResult.level: " + SCCtlOps.reBuiltScanResult.level);
//	 		 			Log.d(TAG, "reBuiltScanResult.frequency: " + SCCtlOps.reBuiltScanResult.frequency);

                                //launchWifiConnecter(WifiConnectActivity.this,SCCtlOps.reBuiltScanResult);

                                //String security = Wifi.ConfigSec.getScanResultSecurity(SCCtlOps.reBuiltScanResult);
                                @SuppressWarnings("deprecation")
                                int mNumOpenNetworksKept = Settings.Secure.getInt(mActivity.getContentResolver(),
                                        Settings.Secure.WIFI_NUM_OPEN_NETWORKS_KEPT, 10);
                                boolean connResult = false;
                                connResult = Wifi.connectToNewNetwork(mActivity, mWifiManager
                                        , SCCtlOps.reBuiltScanResult
                                        , AP_password
                                        , mNumOpenNetworksKept);

                                Log.d(TAG, "connResult: " + String.valueOf(connResult));

                                //check wifi connected
                                WifiManager wifiManager = (WifiManager) mActivity.getSystemService(WIFI_SERVICE);
                                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                ConnectivityManager connManager = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                                String connected_ssid = "";
                                int retry = 60;
                                do {

                                    try {
                                        Thread.sleep(500);
                                        wifiInfo = wifiManager.getConnectionInfo();
                                        mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                                        if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED || mWifi.isConnected()) {
                                            connected_ssid = wifiInfo.getSSID();
                                            if (connected_ssid.indexOf("\"") == 0)
                                                connected_ssid = connected_ssid.substring(1, connected_ssid.length() - 1);
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    Log.d(TAG, "wifi connect :" + connected_ssid);
                                } while (!ssid_name.equals(connected_ssid) && retry-- > 0);

                                //wait for android system
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (ConnectAPProFlag == true) {
                                    handler_pd.sendEmptyMessage(0);
                                    ConnectAPProFlag = false;
                                }

                                if (retry > 0) {
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            configNewDevice_OnClick();
                                        }
                                    });
                                } else {
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertDialog.Builder errorAlert = new AlertDialog.Builder(mActivity);

                                            errorAlert.setTitle("连接网络失败");
                                            errorAlert.setMessage("Please check the password or other problem.\nYou can go to System Settings/Wi-Fi, select the Wi-Fi network!");
                                            errorAlert.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface arg0, int arg1) {
                                                    startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 0);

                                                }
                                            });
                                            errorAlert.show();
                                        }
                                    });
                                }

                            }

                            ;

                        };
                        connectAPThread.start();

                    }
                })
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    /**
     * 连接
     *
     * @param choiceID
     * @return
     * @throws InterruptedException
     */
    private boolean connect_action(int choiceID) throws InterruptedException {
        WifiManager wifiManager = (WifiManager) mActivity.getSystemService(WIFI_SERVICE);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        ScanResult scanResult = null;
        WifiConfiguration wifiConfig = null;
        ConfigurationSecuritiesV8 conf = new ConfigurationSecuritiesV8();
        ConnectivityManager connManager = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = null;

        boolean isConnectedConfig = false;
        boolean active = false;
        int networkId = -1;
        String s_SSID = "";

        if (!wifiManager.isWifiEnabled()) {
            if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
                wifiManager.setWifiEnabled(true);
            ;
        }

        mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {

            for (int i = 0; i < APNumber; i++) {
                scanResult = mScanResults.get(i);

                //Log.d("SC_CONNECTION","BSSID: " + deviceList[choiceID].getmacAdrress() + " vs " + scanResult.BSSID + "("+ scanResult.SSID +")");

                if (deviceList[choiceID].getmacAdrress().equals(scanResult.BSSID) && scanResult.SSID.length() > 0) {

                    if (deviceList[choiceID].getName().endsWith(scanResult.SSID)) {

                        for (int i_conf = 0; i_conf < list.size(); i_conf++) {

                            wifiConfig = list.get(i_conf);

                            if (wifiConfig.SSID != null)
                                s_SSID = wifiConfig.SSID.indexOf("\"") == 0 ? wifiConfig.SSID.substring(1, wifiConfig.SSID.length() - 1) : wifiConfig.SSID;

                            if (wifiConfig.BSSID == null || wifiConfig.BSSID.equalsIgnoreCase("any")) {
                                if (s_SSID.length() > 0 && s_SSID.equals(scanResult.SSID)) {
                                    isConnectedConfig = true;
                                    break;
                                }
                            } else {
                                if (wifiConfig.BSSID.equalsIgnoreCase(scanResult.BSSID)) {

                                    //Log.d("SC_CONNECTION","the same BSSID: " + s_SSID + " v.s. " + scanResult.SSID);

                                    if (s_SSID.length() > 0) {

                                        if (s_SSID.equals(scanResult.SSID)) {
                                            isConnectedConfig = true;
                                            break;
                                        }

                                    } else {
                                        isConnectedConfig = true;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    }

                }
            }
        } else {

            for (int i = 0; i < APNumber; i++) {
                scanResult = mScanResults.get(i);

                //Log.d("SC_CONNECTION","BSSID: " + deviceList[choiceID].getmacAdrress() + " vs " + scanResult.BSSID + "("+ scanResult.SSID +")");

                if (deviceList[choiceID].getmacAdrress().equals(scanResult.BSSID) && scanResult.SSID.length() > 0) {

                    if (deviceList[choiceID].getName().endsWith(scanResult.SSID)) {
                        break;
                    }
                }
            }
        }

        ShowCfgSteptwo = true;

        if (isConnectedConfig == true && wifiConfig != null) {//check that it is already connected
            //Log.d("SC_CONNECTION","it is already connected");
            /*runOnUiThread(new Runnable() {
                @Override
				public void run() {

					Toast toast = Toast.makeText(WifiConnectActivity.this,
							"take original"
							,Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 100);
					LinearLayout toastLayout = (LinearLayout) toast.getView();
					TextView toastTV = (TextView) toastLayout.getChildAt(0);
					toastTV.setTextSize(15);
					toast.show();
				}
			});*/

            conf.setupSecurity(wifiConfig, conf.getScanResultSecurity(scanResult), AP_password);

            networkId = wifiConfig.networkId;

            if (Build.MANUFACTURER.equalsIgnoreCase("HUAWEI")) {//for HUAWEI
                if (Build.MODEL.equalsIgnoreCase("HUAWEI G610-T00") ||
                        Build.MODEL.indexOf("H60") > 0) {
                    //Log.d(TAG,Build.MODEL + " connecting");

                    if (!scanResult.BSSID.equals(wifiManager.getConnectionInfo().getBSSID())) {
                        networkId = wifiManager.addNetwork(wifiConfig);
                        wifiManager.disconnect();
                        active = wifiManager.enableNetwork(networkId, true);
                    }
                } else {
                    networkId = wifiManager.addNetwork(wifiConfig);
                    wifiManager.disconnect();
                    active = wifiManager.enableNetwork(networkId, true);
                }
            } else {

                if (networkId <= 0) {
                    networkId = wifiManager.addNetwork(wifiConfig);
                    wifiManager.disconnect();
                    active = wifiManager.enableNetwork(networkId, true);
                }
            }

            active = wifiManager.reconnect();

        } else {                        // new wifi config, and connect it, if networkId is invalid
            //Log.d("SC_CONNECTION","new wifi config");
            /*runOnUiThread(new Runnable() {
                @Override
				public void run() {

					Toast toast = Toast.makeText(WifiConnectActivity.this,
							"new wifi_conf"
							,Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 100);
					LinearLayout toastLayout = (LinearLayout) toast.getView();
					TextView toastTV = (TextView) toastLayout.getChildAt(0);
					toastTV.setTextSize(15);
					toast.show();
				}
			});*/

            conf = new ConfigurationSecuritiesV8();
            wifiConfig = new WifiConfiguration();
            conf.setupSecurity(wifiConfig, conf.getScanResultSecurity(scanResult), AP_password);

            wifiConfig.SSID = "\"" + scanResult.SSID + "\"";
            wifiConfig.priority = getMaxPriority(wifiManager) + 1;

            //Log.d(TAG,"=======connect AP======= Security type: " +conf.getScanResultSecurity(scanResult));

            // Dependent on the security type of the selected network
            // we set the security settings for the configuration
            networkId = wifiManager.addNetwork(wifiConfig);

            if (networkId == -1)
                return false;

            wifiManager.disconnect();

            active = wifiManager.enableNetwork(networkId, true);
            active = wifiManager.reconnect();
        }

        //Log.d(TAG,"=======connect AP======= enableNetwork: " +String.valueOf(active) + " networkId:" + networkId);
        //Log.d("SC_CONNECTION","=======connect AP======= enableNetwork: " +String.valueOf(active) + " networkId:" + networkId);
        int retry = 0;
        try {
            do {
                Thread.sleep(500);
                mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                retry++;
                //Log.d(TAG,"=======connect AP======= wait"+ String.valueOf(retry));
                Log.d("SC_CONNECTION", "=======connect AP======= wait" + String.valueOf(retry) + " : " + mWifi.getDetailedState());

                if (ConnectAPProFlag == false)
                    return false;

            } while (mWifi.isConnected() == false && retry <= 60);

        } catch (InterruptedException e) {
            Log.e("SC_CONNECTION", "=======connect AP======= wait" + String.valueOf(retry));
            e.printStackTrace();
        }

        WifiConnected = false;

        //check connect AP correctly
        if (wifiManager.getConnectionInfo().getSSID() != null) {
            s_SSID = wifiManager.getConnectionInfo().getSSID();
            s_SSID = s_SSID.indexOf("\"") == 0 ? s_SSID.substring(1, s_SSID.length() - 1) : s_SSID;

            if (!scanResult.SSID.equals(s_SSID)) {
                active = false;
                //Log.d("SC_CONNECTION","SSID is different!!!! " + scanResult.SSID + " vs " + wifiManager.getConnectionInfo().getSSID());
            }
            //Log.d("SC_CONNECTION","BSSID " + scanResult.BSSID + " vs " + wifiManager.getConnectionInfo().getBSSID());
        }

        //Log.d("SC_CONNECTION","Result: " + active);

        if (active == true && mWifi.isConnected() == true) {
            WifiConnected = true;

            SelectedAPInfo.setconnectedflag(true);
            SelectedAPInfo.setaliveFlag(1);
            SelectedAPInfo.setName(deviceList[choiceID].getName());
            SelectedAPInfo.setIP(deviceList[choiceID].getIP());
            SelectedAPInfo.setmacAdrress(deviceList[choiceID].getmacAdrress());
            SelectedAPInfo.setsecurityType(deviceList[choiceID].getsecurityType());
            SelectedAPInfo.setimg(null);

            SCCtlOps.ConnectedSSID = scanResult.SSID;
            SCCtlOps.ConnectedPasswd = AP_password;

        } else {
            WifiConnected = false;

            SelectedAPInfo.setconnectedflag(false);
            SelectedAPInfo.setaliveFlag(0);
            SelectedAPInfo.setName("");
            SelectedAPInfo.setIP("");
            SelectedAPInfo.setmacAdrress("");
            SelectedAPInfo.setsecurityType(0);
            SelectedAPInfo.setimg(null);

            //writeLogtoSys("/sdcard/Log.txt","connection fail:" + "networkId("+networkId+") " + scanResult.SSID);
        }

        //check password if it is not exist
        //store the password
        //content: ssid,password;ssid,password;.....
        if (WifiConnected == true) {

            String content = "";
            byte[] buff = new byte[256]; //input stream buffer
            try {
                FileInputStream reader = mActivity.openFileInput(pwfileName);
                while ((reader.read(buff)) != -1) {
                    content += new String(buff).trim();
                }
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "deviceinfo FileNotFoundException " + content);
            } catch (IOException e) {
                Log.e(TAG, "deviceinfo IOException " + content);
            }
            //Log.d(TAG,"APinfo: "+content);

            if (content.length() > 0) {
                String[] APitem = content.split(";");
                int itemNumber = APitem.length;
                int compareAPNumber = 0;
                String[] array;

                for (int i = 0; i < itemNumber; i++) {
                    array = APitem[i].split(",");
                    if (array.length > 0) {
                        if (SelectedAPInfo.getName().equals(array[0]) == false) { //ssid is different , no store
                            compareAPNumber++;
                        }
                    }
                }

                if (itemNumber == compareAPNumber) // add new password
                {
                    //store password into file
                    String spilt = ",";
                    String spiltEnd = ";";
                    FileOutputStream writer;
                    try {
                        writer = mActivity.openFileOutput(pwfileName, Context.MODE_APPEND);
                        writer.write(SelectedAPInfo.getName().getBytes());
                        writer.write(spilt.getBytes());
                        if (SCCtlOps.ConnectedPasswd.length() > 0)
                            writer.write(SCCtlOps.ConnectedPasswd.getBytes());
                        else
                            writer.write("".getBytes());
                        writer.write(spiltEnd.getBytes());
                        writer.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else { //update password
                    FileOutputStream writer;
                    String tmpData = content;
                    String newData = "";
                    String[] tmpAPitem = tmpData.split(";");

                    //refine data
                    for (int i = 0; i < tmpAPitem.length; i++) {
                        array = tmpAPitem[i].split(",");
                        if (SelectedAPInfo.getName().equals(array[0]) == false) { //ssid is different
                            newData = newData + tmpAPitem[i] + ";";
                        } else {
                            tmpAPitem[i] = array[0] + "," + SCCtlOps.ConnectedPasswd;
                            newData = newData + tmpAPitem[i] + ";";
                        }
                    }


                    try {
                        writer = mActivity.openFileOutput(pwfileName, Context.MODE_PRIVATE);
                        writer.write(newData.getBytes());
                        writer.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            } else {
                //store password into file directly if origin is empty
                String spilt = ",";
                String spiltEnd = ";";
                FileOutputStream writer;
                try {
                    writer = mActivity.openFileOutput(pwfileName, Context.MODE_APPEND);
                    writer.write(SelectedAPInfo.getName().getBytes());
                    writer.write(spilt.getBytes());
                    writer.write(SCCtlOps.ConnectedPasswd.getBytes());
                    writer.write(spiltEnd.getBytes());
                    writer.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return WifiConnected;
    }

    Handler progressHandler = new Handler() {
        public void handleMessage(Message msg) {
            pd.incrementProgressBy(1);
        }
    };

    Handler handler_pd = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //Log.d(TAG,"handleMessage msg.what: " + String.valueOf(msg.what));
            switch (msg.what) {
                case 0:
                    pd.dismiss();
                    break;
                case 1:
                    int timeout = 10;
                    int coutDown = timeout;

                    while (coutDown > 0) {

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        coutDown--;
                        if (coutDown == 0) {
                            pd.dismiss();
                        }
                    }

                    break;

                default:
                    break;
            }
        }
    };


    /**
     * 配置
     */
    public void ConfigPINcode() {
        factory = LayoutInflater.from(mActivity);
        wifiPW_EntryView = factory.inflate(R.layout.wifi_confirm_pincode_entry, null);
        edittxt_PINcode = (EditText) wifiPW_EntryView.findViewById(R.id.id_ap_password);

        edittxt_PINcode.setText("", TextView.BufferType.EDITABLE);
        edittxt_PINcode.setInputType(InputType.TYPE_CLASS_NUMBER);

        //get last pin code
        pinCodeText = edittxt_PINcode.getText().toString();
        PINGet = edittxt_PINcode.getText().toString();
        Log.i("pinCodeText",pinCodeText);
        PINSet = null;
        pd.dismiss();
        startToConfigure();
    }

    /**
     * 开始配置
     */
    public void startToConfigure() {

        ConfigureAPProFlag = true;

        pd = new ProgressDialog(mActivity);
        pd.setCancelable(true);
        pd.setTitle("配置新设备");
        pd.setCancelable(false);
        pd.setMessage("正在配置");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setProgress(0);
        pd.setMax(100);
        pd.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ConfigureAPProFlag = false;
                TimesupFlag_cfg = true;
                SCLib.rtk_sc_stop();
                backgroundThread.interrupt();
            }
        });
        pd.show();
        // create a thread for updating the progress bar
        backgroundThread = new Thread(new Runnable() {
            public void run() {
                try {

                    //int c = 0;

                    while (pd.getProgress() <= pd.getMax()) {

                	   /*if(c==0){
                		   runOnUiThread(new Runnable() {
	       						@Override
	       						public void run() {
	       							WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	       				        	   WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	       				        	   String bssid = wifiInfo.getBSSID();
	       				        	   String ssid = wifiInfo.getSSID();
	       							   Toast.makeText(WifiConnectActivity.this, "AP: "+ssid+" ("+bssid+")", Toast.LENGTH_LONG).show();
	       						}
       		           		});
                	   }

                	   if(c++>5)c=0;*/

                        Thread.sleep(1200);
                        progressHandler.sendMessage(progressHandler.obtainMessage());
                    }
                } catch (InterruptedException e) {
                }
            }
        });
        backgroundThread.start();

        Thread ConfigDeviceThread = new Thread() {
            @Override
            public void run() {

                Configure_action();

                //wait dialog cancel
                if (ConnectAPProFlag == false) {
                    pd.setProgress(100);
                    backgroundThread.interrupt();
                    handler_pd.sendEmptyMessage(0);
                }

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ConfigureAPProFlag == true) {
                            //show "start to configure"
                            ConfigureAPProFlag = false;

                            showConfiguredList();
                        }
                    }
                });
            }
        };
        ConfigDeviceThread.start();
    }

    private void Configure_action() {
        int stepOneTimeout = 30000;

        //check wifi connected
        if (SCCtlOps.ConnectedSSID == null) {
            return;
        }
        int connect_count = 200;

        //get wifi ip
        int wifiIP = SCLib.WifiGetIpInt();
        while (connect_count > 0 && wifiIP == 0) {
            wifiIP = SCLib.WifiGetIpInt();
            connect_count--;
        }
        if (wifiIP == 0) {
            Toast.makeText(mActivity, "Allocating IP, please wait a moment", Toast.LENGTH_SHORT).show();
            return;
        }

        SCLib.rtk_sc_reset();
        if (PINSet == null) {
            SCLib.rtk_sc_set_default_pin(defaultPINcode);
        } else if (PINSet.length() > 0) {
            SCLib.rtk_sc_set_default_pin(defaultPINcode);
        }

        //Log.d("=== Configure_action ===","rtk_sc_set_pin: " + pinCodeText + "," + PINSet);
        if (pinCodeText.length() > 0) {
            //Log.d("=== Configure_action ===","pinCodeText: " + pinCodeText);
            SCLib.rtk_sc_set_pin(pinCodeText);
        } else
            SCLib.rtk_sc_set_pin(PINSet);

        //Log.d("=== Configure_action ===","rtk_sc_set_ssid"+ SCCtlOps.ConnectedSSID);
        SCLib.rtk_sc_set_ssid(SCCtlOps.ConnectedSSID);

        if (!SCCtlOps.IsOpenNetwork) {
        	/*if(SCCtlOps.ConnectedPasswd == null) {
    	        Log.e(TAG, "Please Enter Password");
        	    Toast.makeText(WifiConnectActivity.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
        	    return;
        	}*/
            SCLib.rtk_sc_set_password(SCCtlOps.ConnectedPasswd);
            //if(SCCtlOps.ConnectedPasswd!=null)
            //	Log.d("=== Configure_action ===",SCCtlOps.ConnectedPasswd);
        }

        TimesupFlag_cfg = false;

        SCLib.rtk_sc_set_ip(wifiIP);
        SCLib.rtk_sc_build_profile();

		/* Profile(SSID+PASSWORD, contain many packets) sending total time(ms). */
        SCLibrary.ProfileSendTimeMillis = configTimeout;

        //==================== 1 ========================= 30s
		/* Time interval(ms) between sending two profiles. */
        SCLibrary.ProfileSendTimeIntervalMs = 50; //50ms
		/* Time interval(ms) between sending two packets. */
        SCLibrary.PacketSendTimeIntervalMs = 5; //0ms
		/* Each packet sending counts. */
        SCLibrary.EachPacketSendCounts = 1;

        Log.d(TAG, "Build.MANUFACTURER " + Build.MANUFACTURER);
        Log.d(TAG, "Build.MODEL " + Build.MODEL);

        //exception action
        exception_action();

        SCLib.rtk_sc_start();
        int watchCount = 0;
        try {
            do {
                Thread.sleep(1000);
                watchCount += 1000;
            } while (TimesupFlag_cfg == false && watchCount < stepOneTimeout);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //==================== 2 =========================
        if (TimesupFlag_cfg == false) {
            int count = 0;
			/* Time interval(ms) between sending two profiles. */
            SCLibrary.ProfileSendTimeIntervalMs = 200; //200ms
			/* Time interval(ms) between sending two packets. */
            SCLibrary.PacketSendTimeIntervalMs = 10; //10ms
			/* Each packet sending counts. */
            SCLibrary.EachPacketSendCounts = 1;

            //exception action
            //exception_action();

            try {
                do {
                    Thread.sleep(1000);
                    count++;
                    if ((((configTimeout - stepOneTimeout) / 1000) - count) < 0)
                        break;
                } while (TimesupFlag_cfg == false);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            TimesupFlag_cfg = true;

            //Log.d("=== Configure_action ===","rtk_sc_stop 2");
            SCLib.rtk_sc_stop();
        }

    }

    /**
     * 显示配置成功的设备列表，点击确定就可以绑定
     */
    private void showConfiguredList() {
        ShowCfgSteptwo = false;
        ConfigureAPProFlag = false;

        handler_pd.sendEmptyMessage(0);

        final List<HashMap<String, Object>> InfoList = new ArrayList<HashMap<String, Object>>();
        String[] deviceList = null;
        SCLib.rtk_sc_stop();
        final int itemNum = SCLib.rtk_sc_get_connected_sta_num();

        SCLib.rtk_sc_get_connected_sta_info(InfoList);

        final boolean[] isSelectedArray = new boolean[itemNum];
        Arrays.fill(isSelectedArray, Boolean.TRUE);

        //input data
        if (itemNum > 0) {
            deviceList = new String[itemNum];
            for (int i = 0; i < itemNum; i++) {

                configuredDevices[i].setaliveFlag(1);

                if (InfoList.get(i).get("Name") == null)
                    configuredDevices[i].setName((String) InfoList.get(i).get("MAC"));
                else
                    configuredDevices[i].setName((String) InfoList.get(i).get("Name"));

                configuredDevices[i].setmacAdrress((String) InfoList.get(i).get("MAC"));
                configuredDevices[i].setIP((String) InfoList.get(i).get("IP"));

                deviceList[i] = configuredDevices[i].getName();
            }
        } else {
            if (TimesupFlag_cfg == true) {
                AlertDialog.Builder alert_timeout = new AlertDialog.Builder(mActivity);
                alert_timeout.setCancelable(false);
                //switch password input type
                alert_timeout.setTitle("连接超时");
                alert_timeout.setCancelable(false);
                alert_timeout.setPositiveButton("确定", null);
                alert_timeout.show();
            }

            handler_pd.sendEmptyMessage(0);
            return;
        }

        /** Refresh PIN of MAC and store in file */
		/*if(InfoList.size()>0) {
			fileOps.UpdateCtlPinFile(InfoList.get(0).get("MAC").toString(),
				(PINSet!=null && PINSet.length()>0) ? PINSet : "null");
		}*/

        //selectedItemIndexList = new ArrayList();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setCancelable(false);

        //Toast toast = Toast.makeText(mActivity, aboutMsg, Toast.LENGTH_LONG);
        //toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 100);
        //LinearLayout toastLayout = (LinearLayout) toast.getView();
        //TextView toastTV = (TextView) toastLayout.getChildAt(0);
        //toastTV.setTextSize(20);
        //toast.show();

        builder.setTitle("配置成功设备列表");
        builder.setIcon(android.R.drawable.ic_dialog_info);

        //Log.d(TAG,"show Configured itmeNum:"+itemNum);

        builder.setMultiChoiceItems(deviceList, isSelectedArray,
                new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        //多选择
						/*if(isChecked){
							selectedItemIndexList.add(which);
						}else if(selectedItemIndexList.contains(which)){
							selectedItemIndexList.remove(Integer.valueOf(which));
						}*/
                        isSelectedArray[which] = isChecked;
                    }
                });
        builder.setPositiveButton("绑定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                Log.i(TAG, "start to configure");

                //String selectedStr = "un-configured state:\n";
                int delDeviceNumber = 0;
                pinCodeText = backdoor_PINCODE;
                PINGet = backdoor_PINCODE;

                byte[] pinget = PINGet.getBytes();
                byte[] pinset;
                if (pinget.length > 0) {
                    System.out.println("pinget.length===" + pinget.length);

                    if (pinget.length < 8) {
                        pinset = new byte[8];
                        System.arraycopy(pinget, 0, pinset, 0, pinget.length);
                        for (int i = pinget.length; i < 8; i++) {
                            pinset[i] = '0';
                        }
                    } else if (pinget.length >= 8 && pinget.length <= 64) {
                        pinset = new byte[pinget.length];
                        System.arraycopy(pinget, 0, pinset, 0, pinget.length);
                    } else {
                        pinset = new byte[64];
                        System.arraycopy(pinget, 0, pinset, 0, 64);
                    }
                    PINSet = new String(pinset);
                } else {
                    PINSet = new String(pinget);
                }
                fileOps.UpdateCfgPinFile((PINSet != null && PINSet.length() > 0) ? PINSet : "null");

                //store the pin code
                //content: bssid,pin;bssid,pin;.....
                if (presave_pinCode.length() > 0) {
                    String content = "";
                    byte[] buff = new byte[256]; //input stream buffer
                    try {
                        FileInputStream reader = mActivity.openFileInput(pinfileName);
                        while ((reader.read(buff)) != -1) {
                            content += new String(buff).trim();
                        }
                        reader.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.e(TAG, "pin code FileNotFoundException " + content);
                    } catch (IOException e) {
                        Log.e(TAG, "pin code IOException " + content);
                    }
                    //Log.d(TAG,"PIN Code info: "+content);

                    //Write pin into file
                    if (content.length() > 0) {
                        String[] DeviceItem = content.split(";");
                        boolean isDiffBSSID = false; //check all different SSID
                        int itemNumber = DeviceItem.length;
                        int CompearNumber = 0;

                        for (int i = 0; i < itemNumber; i++) {
                            String[] array = DeviceItem[i].split(",");
                            if (configuredDevices[0].getmacAdrress().equals(array[0]) == false) {//bssid is different , no store
                                CompearNumber++;
                            }
                            /*if (configuredDevices[0].getIP().equals(array[0]) == false) {//bssid is different , no store
                                CompearNumber++;
                            }*/
                        }

                        if (itemNumber == CompearNumber)
                            isDiffBSSID = true;

                        if (isDiffBSSID == true) {// new bssid
                            //store password into file
                            String spilt = ",";
                            String spiltEnd = ";";
                            FileOutputStream writer;
                            try {
                                System.out.println(configuredDevices);
                                writer = mActivity.openFileOutput(pinfileName, Context.MODE_APPEND);
                                writer.write(configuredDevices[0].getmacAdrress().getBytes());
                                writer.write(configuredDevices[0].getIP().getBytes());
                                writer.write(spilt.getBytes());
                                writer.write(presave_pinCode.getBytes());
                                writer.write(spiltEnd.getBytes());
                                writer.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else { //update bssid
                            String tmpData = content;
                            String[] tmpDeviceItem = tmpData.split(";");
                            String newData = "";
                            String[] array;
                            for (int i = 0; i < itemNumber; i++) {
                                array = tmpDeviceItem[i].split(",");
                                if (configuredDevices[0].getmacAdrress().equals(array[0]) == true) {
                                    newData = array[0] + "," + presave_pinCode + ";";
                                } else {
                                    newData = array[0] + "," + array[1] + ";";
                                }
                                /*if (configuredDevices[0].getIP().equals(array[0]) == true) {
                                    newData = array[0] + "," + presave_pinCode + ";";
                                } else {
                                    newData = array[0] + "," + array[1] + ";";
                                }*/
                            }

                            FileOutputStream writer;
                            try {
                                writer = mActivity.openFileOutput(pinfileName, Context.MODE_PRIVATE);
                                writer.write(newData.getBytes());
                                writer.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                    } else {
                        //store password into file directly if origin is empty
                        String spilt = ",";
                        String spiltEnd = ";";
                        FileOutputStream writer;
                        try {
                            writer = mActivity.openFileOutput(pinfileName, Context.MODE_APPEND);
                            writer.write(configuredDevices[0].getmacAdrress().getBytes());
                            writer.write(configuredDevices[0].getIP().getBytes());
                            writer.write(spilt.getBytes());
                            writer.write(presave_pinCode.getBytes());
                            writer.write(spiltEnd.getBytes());
                            writer.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    //check content
                    try {
                        FileInputStream reader = mActivity.openFileInput(pinfileName);
                        while ((reader.read(buff)) != -1) {
                            content += new String(buff).trim();
                        }
                        reader.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.e(TAG, "pin code FileNotFoundException: " + content);
                    } catch (IOException e) {
                        Log.e(TAG, "pin code IOException: " + content);
                    }
                    //Log.d(TAG,"PIN Code info: "+content);
                }


                for (int _i = 0; _i < itemNum; _i++) {
                    if (isSelectedArray[_i] == false) {
                        delDeviceNumber++;
                    }
                }
                System.out.println("delDeviceNumber=====" + delDeviceNumber);
                if (delDeviceNumber > 0) {
                    delConfirmIP = new String[delDeviceNumber];
                    for (int _i = 0; _i < delDeviceNumber; _i++)
                        delConfirmIP[_i] = "";
                }

                int j = 0;
                boolean isDelDevice = false;
                for (int _i = 0; _i < itemNum; _i++) {
                    if (isSelectedArray[_i] == false) {
                        //selectedStr = selectedStr+"\n"+configuredDevices[_i];

                        delConfirmIP[j++] = InfoList.get(_i).get("IP").toString();

                        CurrentControlIP = InfoList.get(_i).get("IP").toString();
                        System.out.println(CurrentControlIP);
                        //Log.d(TAG,"DelProf SendCtlDevPacket :"+CurrentControlIP);


                        //confirm that uncheck device
                        SendCtlDevPacket(SCCtlOps.Flag.DelProf, pinCodeText, CurrentControlIP, null);
                        isDelDevice = true;

                    }
                }

                //Toast.makeText(WifiConnectActivity.this, selectedStr, 3000).show();
                if (isDelDevice == false) {

                    SCCtlOps.rtk_sc_control_reset();

                    DiscovEnable = true;
                    DiscoveryDevice(3000);
                    show_discoverDevice();
                }

                deviceHandler.sendEmptyMessage(0);


            }
        });
        builder.create();
        builder.show();
    }


    /**
     * 发现设备
     *
     * @param counts
     */
    private void DiscoveryDevice(final int counts) {

        pd = new ProgressDialog(mActivity);
//
//		pd.setTitle("Scan Configured Devices by " + SCLib.getConnectedWifiSSID());
//
//		pd.setMessage("Discovering devices ...");
//		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		pd.setIndeterminate(true);
//		pd.setCancelable(false);
//		pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
//		    @Override
//		    public void onClick(DialogInterface dialog, int which) {
//		        dialog.dismiss();
//		        DiscovEnable = false;
//		    }
//		});
//		pd.show();


        DiscovEnable = false;


        new Thread(new Runnable() {
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                byte[] DiscovCmdBuf = SCCtlOps.rtk_sc_gen_discover_packet(SCLib.rtk_sc_get_default_pin());
                long startTime = System.currentTimeMillis();
                long endTime = System.currentTimeMillis();
                while (DiscovEnable && (endTime - startTime) < counts) {

                    try {
                        Thread.sleep(200);
                        SCLib.rtk_sc_send_discover_packet(DiscovCmdBuf, "255.255.255.255");
                        Log.d(TAG, "scan-discovery: 255.255.255.255");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    endTime = System.currentTimeMillis();
                }
                handler_pd.sendEmptyMessage(0);
                Log.i(TAG, "Discover Time Elapsed: " + (endTime - startTime) + "ms");

                // Update Status
                Message msg = Message.obtain();
                msg.obj = null;
                msg.what = ~SCCtlOps.Flag.DiscoverACK; //timeout
                SCLib.TreadMsgHandler.sendMessage(msg);
            }
        }).start();
    }

    /**
     * 显示发现的设备
     *
     * @return
     */
    private boolean show_discoverDevice() {
        adapter_deviceInfo = new SimpleAdapter(mActivity, getData_Device(),
                R.layout.wifi_device_list, new String[]{"main_title", "info"},
                new int[]{R.id.title, R.id.info});

        System.out.println("adapter_deviceInfo.getCount()=======" + adapter_deviceInfo.getCount());
        if (adapter_deviceInfo.getCount() > 0) {


        } else if (adapter_deviceInfo.getCount() == 0) {

        }
        return true;
    }

    /**
     * 发送wifi信息包
     *
     * @param flag
     * @param pin
     * @param ip
     * @param new_name
     */
    public void SendCtlDevPacket(final int flag, final String pin, final String ip, final String new_name) {
//		Log.d(TAG, "ip: " + ip);
//		Log.d(TAG, "pin: " + pin);
//		Log.d(TAG, "name: " + new_name);

        new Thread(new Runnable() {
            int count = 0;

            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                byte[] buf = SCCtlOps.rtk_sc_gen_control_packet(flag, SCLib.rtk_sc_get_default_pin(), pin, new_name);

                while (count < 6) {
                    try {
                        Thread.sleep(1);
                        SCLib.rtk_sc_send_control_packet(buf, ip);
                        //Log.d("control packet","flag: " + String.valueOf(flag));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    count++;
                }
            }
        }).start();
    }

    /**
     * 获取设备数据
     *
     * @return
     */
    private List<? extends Map<String, ?>> getData_Device() {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        String tmp = "";
        int i = 0;

        DevInfo = new ArrayList<HashMap<String, Object>>();
        SCCtlOps.rtk_sc_get_discovered_dev_info(DevInfo);
        System.out.println("WifiConnectActivityDevInfo====" + DevInfo);
        for (i = 0; i < SCCtlOps.rtk_sc_get_discovered_dev_num(); i++) {
            map = new HashMap<String, Object>();
            if (DevInfo.get(i).get("Name") == null) {

                map.put("main_title", DevInfo.get(i).get("MAC"));
                map.put("info", DevInfo.get(i).get("MAC") + "   " + DevInfo.get(i).get("Status"));

            } else {
                //Log.d(TAG,"getData_Device "+(String) DevInfo.get(i).get("Name"));
                map.put("main_title", DevInfo.get(i).get("Name"));
                map.put("info", DevInfo.get(i).get("MAC") + "   " + DevInfo.get(i).get("Status"));
            }

            tmp = (String) DevInfo.get(i).get("IP");
            if (DevInfo.get(i).get("IP") != null && tmp.length() > 0)
                list.add(map);

        }

        return list;
    }

    /**
     * 设备适配器
     */
    static class DeviceAdapter extends ArrayAdapter {

        private static final int RESOURCE = R.layout.wifi_ap_list;
        private LayoutInflater inflater;

        static class ViewHolder {
            TextView nameTxVw;
            TextView deviceInfo;
            ImageView deviceImg;
            RadioGroup selected;
        }

        @SuppressWarnings("unchecked")
        public DeviceAdapter(Context context, ConfigurationDevice.DeviceInfo[] objects) {
            super(context, RESOURCE, objects);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                // inflate a new view and setup the view holder for future use
                convertView = inflater.inflate(RESOURCE, null);

                holder = new ViewHolder();
                holder.nameTxVw = (TextView) convertView.findViewById(R.id.title_aplist);
                holder.deviceInfo = (TextView) convertView.findViewById(R.id.info_aplist);
                holder.deviceImg = (ImageView) convertView.findViewById(R.id.signalImg);
                holder.selected = (RadioGroup) convertView.findViewById(R.id.radioButton1);

                convertView.setTag(holder);
            } else {
                // view already defined, retrieve view holder
                holder = (ViewHolder) convertView.getTag();
            }

            ConfigurationDevice.DeviceInfo cat = (ConfigurationDevice.DeviceInfo) getItem(position);
            if (cat == null) {
                //Log.e( TAG,"error_getView_neil "+ String.valueOf(position) );
            }

            holder.nameTxVw.setText(cat.getName());

            if (cat.getconnectedflag()) {
                holder.deviceInfo.setText("已连接");
            } else {
                holder.deviceInfo.setText("");
            }


            //holder.nameTxVw.setCompoundDrawables( cat.getimg(), null, null, null );
            holder.deviceImg.setImageDrawable(cat.getimg());

            return convertView;
        }
    }

    /**
     * 获取
     *
     * @param result
     * @return
     */
    static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_TYPE.SECURITY_WEP.ordinal();
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_TYPE.SECURITY_PSK.ordinal();
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_TYPE.SECURITY_EAP.ordinal();
        }
        return SECURITY_TYPE.SECURITY_NONE.ordinal();
    }

    /**
     * 获取最大优先
     *
     * @param wifiManager
     * @return
     */
    private static int getMaxPriority(final WifiManager wifiManager) {
        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        int pri = 0;
        for (final WifiConfiguration config : configurations) {
            if (config.priority > pri) {
                pri = config.priority;
            }
        }
        return pri;
    }

    /**
     * 适配手机
     */
    private void exception_action() {
        if (Build.MANUFACTURER.equalsIgnoreCase("Samsung")) {
            //SCLibrary.PacketSendTimeIntervalMs  = 5;
            if (Build.MODEL.equalsIgnoreCase("G9008")) { //Samsung Galaxy S5 SM-G9008
                SCLibrary.PacketSendTimeIntervalMs = 10;
            } else if (Build.MODEL.contains("SM-G9208")) { //samsun Galaxy S6
                SCLibrary.PacketSendTimeIntervalMs = 10;
            } else if (Build.MODEL.contains("N900")) { //samsun Galaxy note 3
                SCLibrary.PacketSendTimeIntervalMs = 5;
            } else if (Build.MODEL.contains("SM-N910U")) { //samsun Galaxy note 4
                SCLibrary.PacketSendTimeIntervalMs = 5;
            }

        } else if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {//for MI
            if (Build.MODEL.equalsIgnoreCase("MI 4W")) {
                SCLibrary.PacketSendTimeIntervalMs = 5;    //MI 4
            }
        } else if (Build.MANUFACTURER.equalsIgnoreCase("Sony")) {//for Sony
            if (Build.MODEL.indexOf("Xperia") > 0) {
                SCLibrary.PacketSendTimeIntervalMs = 5;    //Z3
            }
        } else if (Build.MANUFACTURER.equalsIgnoreCase("HUAWEI")) {//HUAWEI
            if (Build.MODEL.indexOf("GEM-702L") > 0) {
                SCLibrary.PacketSendTimeIntervalMs = 10;    //GEM-702L
            } else {
                SCLibrary.PacketSendTimeIntervalMs = 5;
            }
        }

        //check link rate
        WifiManager wifi_service = (WifiManager) mActivity.getSystemService(WIFI_SERVICE);
        WifiInfo wifiinfo = wifi_service.getConnectionInfo();
        if (wifiinfo.getLinkSpeed() > 78) {//MCS8 , 20MHZ , NOSGI
            SCLibrary.ProfileSendTimeIntervalMs = 100; //50ms
            SCLibrary.PacketSendTimeIntervalMs = 15;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //SCLib.rtk_sc_exit();
        SCCtlOps.ConnectedSSID = null;
        SCCtlOps.ConnectedPasswd = null;
        DiscovEnable = false;
    }
}

package com.wtf.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.alibaba.fastjson.JSON;
import com.wtf.R;
import com.wtf.WTFApplication;
import com.wtf.model.AppMsg;
import com.wtf.model.HeatReservation;
import com.wtf.model.Param;
import com.wtf.model.QueryData;
import com.wtf.ui.RefreshLayout;
import com.wtf.ui.time_picker.WLQQTimePicker;
import com.wtf.ui.time_picker.WheelView;
import com.wtf.utils.WifiThread;

import java.lang.ref.WeakReference;
import java.util.Date;

import wtf.socket.WTFSocketException;
import wtf.socket.WTFSocketHandler;
import wtf.socket.WTFSocketMsg;
import wtf.socket.WTFSocketSession;
import wtf.socket.WTFSocketSessionFactory;


/**
 * Created by liyan on 2016/10/12.
 * 加热预约
 */
public class ConstantFragment extends BaseFragment implements OnClickListener, RefreshLayout.OnHeaderRefreshListener {
    private FragmentActivity mActivity;

    //private ToggleButton tb_switch;
    private ToggleButton tb_tmp_control;
    private TextView tv_tmp;
    private ImageView iv_tmp_minus;
    private ImageView iv_tmp_plus;
    private ImageView iv_tmp_cancel;
    private ScrollView sv_constant;
    private WheelView minute;
    private WheelView hour;
    private WLQQTimePicker timePicker;
    private LinearLayout load_progress;
    private ImageView iv_heating_off;
    private PopupWindow mPopWindow;
    private TextView tv_pop;
    private TextView tv_tmp_info;
    private TextView tv_tmp_control;
    private TextView tv_open_time_info;
    private TextView tv_side;
    private ToggleButton tb_switch;
    private Button btn_is_change;

    private Integer targetTemperature = 35;
    private Integer protectTime = 2100;

    private Integer _switch = 0;
    private Integer originalProtectTime = 2100;
    private Integer startTime = 28800;
    private Integer autoTemperatureControl = 0;
    public static Integer side = 1;
    private Integer originalTemperature = 25;
    private Integer originalHour = 0;
    private Integer originalMin = 0;
    private int popLocation;

    private HeatReservation heatReservation;
    private String packet = null;
    private ConstantHandler constantHandler;
    private QueryData queryData;

    private RefreshLayout mPullToRefreshView;
    private RefreshDataAsynTask mRefreshAsynTask;

    /**
     * 是否已被加载过一次，第二次就不再去请求数据了
     */
    private boolean mHasLoadedOnce;


    private static final int light_grey = Color.rgb(0x8c, 0x8c, 0x8c);

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_constant, container, false);
        constantHandler = new ConstantHandler(this);
        heatReservation = new HeatReservation();

        initViews(view);//create interface
        isPrepared = true;
        lazyLoad();

        return view;
    }

    private void initTitle() {
        mActivity = this.getActivity();

        SettingFragment settingFragment = (SettingFragment) ConstantFragment.this.getParentFragment();
        settingFragment.toChangeVISIBLE();
        Log.i("settingFragment", "" + String.valueOf(settingFragment == null));
        View settingView = settingFragment.getView();

        tv_side = (TextView) settingView.findViewById(R.id.tv_side);
        tb_switch = (ToggleButton) settingView.findViewById(R.id.tb_switch);
        btn_is_change = (Button) settingView.findViewById(R.id.btn_is_change);

        tb_switch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_switch != 0) {
                    btn_is_change.setVisibility(View.VISIBLE);
                } else {
                    updateCurrentData();
                }
            }
        });
        tb_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                tb_switch.setChecked(isChecked);

                if (isChecked) {
                    _switch = 1;
                    iv_heating_off.setVisibility(View.GONE);
                    iv_heating_off.setClickable(false);
                } else {
                    _switch = 0;
                    iv_heating_off.setVisibility(View.VISIBLE);
                    iv_heating_off.setClickable(true);
                }
                /*if (_switch != 0) {
                    isChange = 1;
                    btn_is_change.setVisibility(View.VISIBLE);
                } else {
                    updateCurrentData();
                }*/
            }
        });
        tv_side.setOnClickListener(this);
        btn_is_change.setOnClickListener(this);
    }

    private void initViews(View view) {
        load_progress = (LinearLayout) view.findViewById(R.id.constant_load_progress);

        sv_constant = (ScrollView) view.findViewById(R.id.sv_constant);

        tb_tmp_control = (ToggleButton) view.findViewById(R.id.tb_tmp_control);
        tv_tmp = (TextView) view.findViewById(R.id.tv_tmp);

        iv_tmp_minus = (ImageView) view.findViewById(R.id.iv_tmp_minus);
        iv_tmp_plus = (ImageView) view.findViewById(R.id.iv_tmp_plus);
        iv_tmp_cancel = (ImageView) view.findViewById(R.id.iv_tmp_cancel);
        tv_tmp_info = (TextView) view.findViewById(R.id.tv_tmp_info);
        tv_tmp_control = (TextView) view.findViewById(R.id.tv_tmp_control);

        iv_heating_off = (ImageView) view.findViewById(R.id.iv_heating_off);
        tv_open_time_info = (TextView) view.findViewById(R.id.tv_open_time_info);

        minute = (WheelView) view.findViewById(R.id.minute);
        hour = (WheelView) view.findViewById(R.id.hour);
        timePicker = (WLQQTimePicker) view.findViewById(R.id.time_picker);

        timePicker.setDate(startTime);

        sv_constant.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                // Log.v(TAG, "PARENT TOUCH");
                v.findViewById(R.id.time_picker).getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
        hour.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                //  Log.v(TAG,"CHILD TOUCH");
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if (timePicker.getHour() != originalHour) {
                    if (btn_is_change.getVisibility() == View.GONE) {
                        btn_is_change.setVisibility(View.VISIBLE);
                    }
                }
                return false;
            }
        });
        minute.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                //  Log.v(TAG,"CHILD TOUCH");
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if (timePicker.getMin() != originalMin) {
                    if (btn_is_change.getVisibility() == View.GONE) {
                        btn_is_change.setVisibility(View.VISIBLE);
                    }
                }
                return false;
            }
        });


        tb_tmp_control.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                tb_tmp_control.setChecked(isChecked);

                if (isChecked) {
                    autoTemperatureControl = 1;
                } else {
                    autoTemperatureControl = 0;
                }
                btn_is_change.setVisibility(View.VISIBLE);
            }
        });


        iv_tmp_plus.setOnClickListener(this);
        iv_tmp_minus.setOnClickListener(this);
        iv_tmp_cancel.setOnClickListener(this);
        tv_tmp_info.setOnClickListener(this);
        tv_tmp_control.setOnClickListener(this);
        tv_open_time_info.setOnClickListener(this);


        mPullToRefreshView = (RefreshLayout) view
                .findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnHeaderRefreshListener(this);
        mPullToRefreshView.setEnablePullLoadMoreDataStatus(false);
        mPullToRefreshView.showFooterView(false);
        mPullToRefreshView.setLastUpdated(new Date().toLocaleString());
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    private void showPopupWindow() {
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.pop_up, null);
        mPopWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tv_pop = (TextView) contentView.findViewById(R.id.tv_pop);
        mPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopWindow.setOutsideTouchable(true);
        mPopWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popWidth = mPopWindow.getContentView().getMeasuredWidth();
        popLocation = popWidth / 2 - tv_tmp_info.getWidth() / 2;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_side:
                if (side == 0) {
                    tv_side.setText("左侧");
                    side = 1;
                } else {
                    tv_side.setText("右侧");
                    side = 0;
                }
                getCurrentData();
                break;
            case R.id.btn_is_change:
                if (btn_is_change.getVisibility() == View.VISIBLE) {
                    updateCurrentData();
                } else {
                    btn_is_change.setVisibility(View.GONE);
                }
                break;
            case R.id.iv_tmp_cancel:
                if (targetTemperature != originalTemperature) {
                    targetTemperature = originalTemperature;
                    tv_tmp.setText(targetTemperature + "℃");
                    tv_tmp.setTextColor(light_grey);
                    iv_tmp_cancel.setVisibility(View.GONE);
                }
                break;
            case R.id.tv_tmp_info:
                showPopupWindow();
                tv_pop.setText(R.string.heating_info);
                mPopWindow.showAsDropDown(tv_tmp_info, -popLocation, 0);
                break;
            case R.id.tv_tmp_control:
                showPopupWindow();
                tv_pop.setText(R.string.open_time_info);
                mPopWindow.showAsDropDown(tv_tmp_control, -popLocation, 0);
                break;
            case R.id.tv_allow_time_info:
                showPopupWindow();
                tv_pop.setText(R.string.open_time_info);
                mPopWindow.showAsDropDown(tv_open_time_info, -popLocation, 0);
                break;
            case R.id.iv_tmp_plus:
                if (targetTemperature < 45) {
                    targetTemperature++;
                    if (btn_is_change.getVisibility() == View.GONE) {
                        btn_is_change.setVisibility(View.VISIBLE);
                    }
                    tv_tmp.setTextColor(Color.WHITE);
                    tv_tmp.setText(targetTemperature + "℃");
                    if (targetTemperature != originalTemperature) {
                        if (iv_tmp_cancel.getVisibility() == View.GONE) {
                            iv_tmp_cancel.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (iv_tmp_cancel.getVisibility() == View.VISIBLE) {
                            iv_tmp_cancel.setVisibility(View.GONE);
                        }
                    }
                } else {
                    showToast("温度不能超过45摄氏度");
                }
                break;
            case R.id.iv_tmp_minus:
                if (targetTemperature > 25) {
                    targetTemperature--;
                    if (btn_is_change.getVisibility() == View.GONE) {
                        btn_is_change.setVisibility(View.VISIBLE);
                    }
                    tv_tmp.setTextColor(Color.WHITE);
                    tv_tmp.setText(targetTemperature + "℃");
                    if (!targetTemperature.equals(originalTemperature)) {
                        if (iv_tmp_cancel.getVisibility() == View.GONE) {
                            iv_tmp_cancel.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (iv_tmp_cancel.getVisibility() == View.VISIBLE) {
                            iv_tmp_cancel.setVisibility(View.GONE);
                        }
                    }
                } else {
                    showToast("温度不能低于25摄氏度");
                }
                break;

        }
    }

    private void updateCurrentData() {
        if (!WTFApplication.isConnectingToInternet()) {
            constantHandler.sendEmptyMessage(4);
        } else {
            load_progress.setVisibility(View.VISIBLE);

            startTime = timePicker.getHour() * 60 * 60 + timePicker.getMin() * 60;
            heatReservation.setSide(side);
            heatReservation.setModeSwitch(_switch);
            heatReservation.setStartTime(startTime);
            heatReservation.setAutoTemperatureControl(autoTemperatureControl);
            heatReservation.setProtectTime(protectTime);
            heatReservation.setTargetTemperature(targetTemperature);
            if (WTFSocketSessionFactory.isAvailable()) {

                new Thread() {
                    @Override
                    public void run() {
                        WTFSocketMsg updateMsg = new WTFSocketMsg().setBody(new AppMsg().setCmd(17).addParam(heatReservation));
                        WTFSocketSession session1 = WTFSocketSessionFactory.getSession(WTFApplication.userData.getSelDeviceName());
                        session1.sendMsg(updateMsg, new WTFSocketHandler() {
                            @Override
                            public boolean onReceive(WTFSocketSession session, WTFSocketMsg msg) {
                                if (msg.getState() != 1) {
                                    constantHandler.sendEmptyMessage(5);
                                } else {
                                    //  currentTemperature = Integer.valueOf(session.getMsg().getParams().getJSONObject(0).getString("currentTemperature"));
                                    AppMsg appMsg = msg.getBody(AppMsg.class);
                                    if (appMsg.getFlag() == 1) {
                                        System.out.println("更新数据成功");
                                        originalTemperature = targetTemperature;
                                        constantHandler.sendEmptyMessage(2);

                                    } else {
                                        System.out.println("更新数据失败");
                                        constantHandler.sendEmptyMessage(5);

                                    }
                                }
                                return true;
                            }

                            public boolean onException(WTFSocketSession session, WTFSocketMsg msg, WTFSocketException e) {
                                constantHandler.sendEmptyMessage(1);
                                return true;
                            }
                        }, Param.TCP_TIMEOUT);
                    }
                }.start();

            }else {
                constantHandler.sendEmptyMessage(6);
            }
        }
    }

    private void getCurrentData() {
        if (getActivity() == null) return;

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                tv_side.setVisibility(View.VISIBLE);
                tb_switch.setVisibility(View.VISIBLE);
                btn_is_change.setVisibility(View.GONE);
            }
        });

        if (!WTFApplication.isConnectingToInternet()) {
            constantHandler.sendEmptyMessage(4);
        } else {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    load_progress.setVisibility(View.VISIBLE);
                }
            });
            queryData = new QueryData();
            queryData.setSide(side);
            System.out.println("side:" + side);
            if(WTFSocketSessionFactory.isAvailable()) {
                new Thread() {
                    @Override
                    public void run() {
                        WTFSocketMsg currentMsg = new WTFSocketMsg().setBody(new AppMsg().addParam(queryData).setCmd(33));

                        WTFSocketSession session1 = WTFSocketSessionFactory.getSession(WTFApplication.userData.getSelDeviceName());
                        session1.sendMsg(currentMsg, new WTFSocketHandler() {
                            @Override
                            public boolean onReceive(WTFSocketSession session, WTFSocketMsg msg) {
                                if (msg.getState() != 1) {
                                    constantHandler.sendEmptyMessage(5);
                                } else {
                                    //  currentTemperature = Integer.valueOf(session.getMsg().getParams().getJSONObject(0).getString("currentTemperature"));
                                    AppMsg appMsg = msg.getBody(AppMsg.class);

                                    if (appMsg.getFlag() == 1) {
                                        //System.out.println("获取数据成功");
                                        packet = appMsg.getParams().getString(0);
                                        constantHandler.sendEmptyMessage(0);
                                    } else {
                                        //System.out.println("获取数据失败");
                                        constantHandler.sendEmptyMessage(5);

                                    }
                                }
                                return true;
                            }

                            public boolean onException(WTFSocketSession session, WTFSocketMsg msg, WTFSocketException e) {
                                constantHandler.sendEmptyMessage(1);
                                return true;
                            }
                        }, Param.TCP_TIMEOUT);
                    }
                }.start();
            }else {
                constantHandler.sendEmptyMessage(6);
            }
        }
    }

    @Override
    public void onHeaderRefresh(RefreshLayout view) {
        mRefreshAsynTask = new RefreshDataAsynTask();
        mRefreshAsynTask.execute(null, null);
    }

    // 下拉刷新的实现
    class RefreshDataAsynTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                getCurrentData();
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

    // Handler
    static class ConstantHandler extends Handler {
        WeakReference<ConstantFragment> mFragment;

        ConstantHandler(ConstantFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            ConstantFragment theFragment = mFragment.get();
            switch (msg.what) {
                case 0:
                    theFragment.load_progress.setVisibility(View.GONE);
                    theFragment.heatReservation = JSON.parseObject(theFragment.packet, HeatReservation.class);
                    System.out.println("heatReservation:" + theFragment.heatReservation);
                    theFragment.originalTemperature = theFragment.heatReservation.getTargetTemperature();
                    theFragment._switch = theFragment.heatReservation.getModeSwitch();
                    theFragment.originalProtectTime = theFragment.heatReservation.getProtectTime();
                    theFragment.startTime = theFragment.heatReservation.getStartTime();
                    theFragment.autoTemperatureControl = theFragment.heatReservation.getAutoTemperatureControl();

                    theFragment.originalHour = theFragment.startTime / 60 / 60;
                    theFragment.originalMin = theFragment.startTime / 60 % 60;

                    if (theFragment.originalTemperature < 25 || theFragment.originalTemperature > 45) {
                        theFragment.originalTemperature = 35;
                    }
                    theFragment.targetTemperature = theFragment.originalTemperature;
                    theFragment.tv_tmp.setText(theFragment.targetTemperature + "℃");

                    if (theFragment.originalProtectTime < 30 * 60 || theFragment.originalProtectTime > 180 * 60) {
                        theFragment.originalProtectTime = 30 * 60;
                    }
                    theFragment.protectTime = theFragment.originalProtectTime;

                    if (theFragment._switch == 1) {
                        theFragment.tb_switch.setChecked(true);
                        theFragment.iv_heating_off.setVisibility(View.GONE);
                        theFragment.iv_heating_off.setClickable(false);
                    } else {
                        theFragment.tb_switch.setChecked(false);
                        theFragment.iv_heating_off.setVisibility(View.VISIBLE);
                        theFragment.iv_heating_off.setClickable(true);
                    }
                    if (theFragment.autoTemperatureControl == 1) {
                        theFragment.tb_tmp_control.setChecked(true);
                    } else {
                        theFragment.tb_tmp_control.setChecked(false);
                    }

                    theFragment.timePicker.setDate(theFragment.startTime);

                    theFragment.btn_is_change.setVisibility(View.GONE);
                    theFragment.tv_tmp.setTextColor(light_grey);
                    theFragment.iv_tmp_cancel.setVisibility(View.GONE);
                    theFragment.showToast("获取数据成功");
                    break;
                case 1:
                    theFragment.load_progress.setVisibility(View.GONE);
                    theFragment.showToast("获取数据失败");
                    break;
                case 2:
                    theFragment.load_progress.setVisibility(View.GONE);
                    theFragment.btn_is_change.setVisibility(View.GONE);
                    theFragment.tv_tmp.setTextColor(light_grey);
                    theFragment.iv_tmp_cancel.setVisibility(View.GONE);
                    theFragment.showToast("更新数据成功");
                    break;
                case 3:
                    theFragment.load_progress.setVisibility(View.GONE);
                    theFragment.showToast("更新数据失败");
                    break;
                case 4:
                    theFragment.load_progress.setVisibility(View.GONE);
                    theFragment.showToast("未联网请先联网");
                    break;
                case 5:
                    theFragment.load_progress.setVisibility(View.GONE);
                    theFragment.showToast("床垫还未连上服务器，请耐心等待");
                    break;
                case 6:
                    theFragment.load_progress.setVisibility(View.GONE);
                    WifiThread wifiThread=new WifiThread();
                    wifiThread.start();
                    theFragment.showToast("床垫还未连上服务器，请耐心等待");
                    break;
            }
        }
    }

    protected void lazyLoad() {//update interface
        if (!isPrepared || !isVisible) {
            Log.i("lazyLoad", "未加载");
            return;
        }
       /* if (mHasLoadedOnce ){
            getCurrentData();
            return;
        }*/
        Log.i("lazyLoad", "已加载");
        mHasLoadedOnce = true;
        initTitle();
        new Thread() {
            public void run() {
                getCurrentData();
            }
        }.start();
    }

   /* public void setUserVisibleHint(boolean isVisibleToUser) {
        //判断Fragment中的ListView时候存在，判断该Fragment时候已经正在前台显示  通过这两个判断，就可以知道什么时候去加载数据了
        if (isVisibleToUser && isVisible() && tv_open_time_info.getVisibility() != View.VISIBLE) {
            initTitle();
            getCurrentData(); //加载数据的方法
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (getUserVisibleHint() && tv_open_time_info.getVisibility() != View.VISIBLE) {
            initTitle();
            getCurrentData();
        }
        super.onActivityCreated(savedInstanceState);
    }*/

}
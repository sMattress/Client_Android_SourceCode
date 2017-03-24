package com.wtf.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.alibaba.fastjson.JSON;
import com.wtf.R;
import com.wtf.WTFApplication;
import com.wtf.model.AppMsg;
import com.wtf.model.CurrentState;
import com.wtf.model.Param;
import com.wtf.model.QueryData;
import com.wtf.model.Switch;
import com.wtf.ui.RefreshLayout;

import java.lang.ref.WeakReference;
import java.util.Date;

import wtf.socket.WTFSocketException;
import wtf.socket.WTFSocketHandler;
import wtf.socket.WTFSocketMsg;
import wtf.socket.WTFSocketSession;
import wtf.socket.WTFSocketSessionFactory;

/**
 * Created by liyan on 2016/10/12.
 * 远程控制
 */
public class CommonFragment extends BaseFragment implements OnClickListener, RefreshLayout.OnHeaderRefreshListener {
    private FragmentActivity mActivity;

    private ImageButton ib_switch;
    private TextView tv_mode_left;
    private ImageButton ib_heating_left;
    private TextView tv_tmp_left;
    private ImageButton ib_therapy_left;
    private ImageButton ib_tmp_plus_left;
    private ImageButton ib_tmp_minus_left;
    private TextView tv_mode_right;
    private ImageButton ib_heating_right;
    private TextView tv_tmp_right;
    private ImageButton ib_therapy_right;
    private ImageButton ib_tmp_plus_right;
    private ImageButton ib_tmp_minus_right;
    private TextView tv_title;
    private TextView tv_side;
    private ToggleButton tb_switch;
    private Button btn_is_change;
    private LinearLayout load_progress;
    private ImageView iv_off;

    private CurrentState[] currentState = new CurrentState[2];//左右当前状态
    private int[] TargetTmp = {35, 35};//左右目标温度
    //private int leftOriginalTargetTmp = 35;//左原来温度
    private int[] CurrentTmp = {25, 25};//右左当前温度
    private int[] Mode = {0, 0};//左右模式
    private Switch swicthState;

    private int powerOn = 0; //开关机标志位

    private QueryData queryData;

    private RefreshLayout mPullToRefreshView;
    private RefreshDataAsynTask mRefreshAsynTask;

    /**
     * 是否已被加载过一次，第二次就不再去请求数据了
     */
    private boolean mHasLoadedOnce;

    private CommonHandler commonHandler;//处理异步事件

    private static final int light_grey = Color.rgb(0x8c, 0x8c, 0x8c);
    private static final int LEFT_SIDE = 0x01;
    private static final int RIGHT_SIDE = 0x00;
    private static final int DOUBLE_SIDE = 0x02;

    private static final int MODE_STANDBY = 0;
    private static final int MODE_HEATING = 1;
    private static final int MODE_THERAPY = 2;
    private static final int MODE_HEATING_PRE = 3;
    private static final int MODE_THERAPY_PRE = 4;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_common, container, false);
        currentState[LEFT_SIDE] = new CurrentState();
        currentState[RIGHT_SIDE] = new CurrentState();
        commonHandler = new CommonHandler(this);
        swicthState = new Switch();
        initViews(view);
        isPrepared = true;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        lazyLoad();
    }

    private void initTitle() {
        SettingFragment settingFragment = (SettingFragment) CommonFragment.this.getParentFragment();
        View settingView = settingFragment.getView();
        tv_side = (TextView) settingView.findViewById(R.id.tv_side);
        tb_switch = (ToggleButton) settingView.findViewById(R.id.tb_switch);
        btn_is_change = (Button) settingView.findViewById(R.id.btn_is_change);
        review(1);
    }

    /**
     * 初始化视图
     *
     * @param view
     */
    private void initViews(View view) {
        mActivity = this.getActivity();

        load_progress = (LinearLayout) view.findViewById(R.id.common_load_progress);

        ib_heating_left = (ImageButton) view.findViewById(R.id.ib_heating_left);
        tv_mode_left = (TextView) view.findViewById(R.id.tv_mode_left);
        tv_tmp_left = (TextView) view.findViewById(R.id.tv_tmp_left);
        ib_therapy_left = (ImageButton) view.findViewById(R.id.ib_therapy_left);
        ib_tmp_plus_left = (ImageButton) view.findViewById(R.id.ib_tmp_plus_left);
        ib_tmp_minus_left = (ImageButton) view.findViewById(R.id.ib_tmp_minus_left);
        ib_switch = (ImageButton) view.findViewById(R.id.ib_switch);

        ib_heating_right = (ImageButton) view.findViewById(R.id.ib_heating_right);
        tv_mode_right = (TextView) view.findViewById(R.id.tv_mode_right);
        tv_tmp_right = (TextView) view.findViewById(R.id.tv_tmp_right);
        ib_therapy_right = (ImageButton) view.findViewById(R.id.ib_therapy_right);
        ib_tmp_plus_right = (ImageButton) view.findViewById(R.id.ib_tmp_plus_right);
        ib_tmp_minus_right = (ImageButton) view.findViewById(R.id.ib_tmp_minus_right);
        iv_off = (ImageView) view.findViewById(R.id.iv_off);

        ib_switch.setOnClickListener(this);

        ib_tmp_plus_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);

                if (Mode[LEFT_SIDE] == 1) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        //更改为按下时的背景图片
                        v.setBackgroundResource(R.mipmap.left_plus_touched);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        //改为抬起时的图片
                        v.setBackgroundResource(R.mipmap.left_plus_untouch);
                    }
                }
                return false;
            }
        });

        ib_tmp_plus_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if (Mode[RIGHT_SIDE] == 1) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        //更改为按下时的背景图片
                        v.setBackgroundResource(R.mipmap.right_plus_touched);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        //改为抬起时的图片
                        v.setBackgroundResource(R.mipmap.right_plus_untouch);
                    }
                }
                return false;

            }
        });

        ib_tmp_minus_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if (Mode[LEFT_SIDE] == 1) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        //更改为按下时的背景图片
                        v.setBackgroundResource(R.mipmap.left_minus_touched);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        //改为抬起时的图片
                        v.setBackgroundResource(R.mipmap.left_minus_untouch);
                    }
                }
                return false;
            }
        });
        ib_tmp_minus_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if (Mode[RIGHT_SIDE] == 1) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        //更改为按下时的背景图片
                        v.setBackgroundResource(R.mipmap.right_minus_touched);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        //改为抬起时的图片
                        v.setBackgroundResource(R.mipmap.right_minus_untouch);
                    }
                }
                return false;
            }
        });

        ib_heating_left.setOnClickListener(this);
        ib_therapy_left.setOnClickListener(this);
        ib_tmp_plus_left.setOnClickListener(this);
        ib_tmp_minus_left.setOnClickListener(this);

        ib_heating_right.setOnClickListener(this);
        ib_therapy_right.setOnClickListener(this);
        ib_tmp_plus_right.setOnClickListener(this);
        ib_tmp_minus_right.setOnClickListener(this);

        mPullToRefreshView = (RefreshLayout) view
                .findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnHeaderRefreshListener(this);
        mPullToRefreshView.setEnablePullLoadMoreDataStatus(false);
        mPullToRefreshView.showFooterView(false);
        mPullToRefreshView.setLastUpdated(new Date().toLocaleString());
    }

    /**
     * 获取最新数据
     */
    private void getCurrentData() {
        if (getActivity() == null) return;
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                tv_side.setVisibility(View.GONE);
                tb_switch.setVisibility(View.GONE);
                btn_is_change.setVisibility(View.GONE);
            }
        });

        if (!WTFApplication.isConnectingToInternet()) {
            commonHandler.sendEmptyMessage(4);
        } else {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    load_progress.setVisibility(View.VISIBLE);
                }
            });

            queryData = new QueryData();
            queryData.setSide(DOUBLE_SIDE);

            new Thread(){
                @Override
                public void run()
                {
                    WTFSocketMsg currentMsg = new WTFSocketMsg().setBody(new AppMsg().addParam(queryData).setCmd(32));

                    Log.d("Common","registerSocket");
                    //SocketConnection.registerSocket();

                    WTFSocketSession session1 = WTFSocketSessionFactory.getSession(WTFApplication.userData.getSelDeviceName());
                    session1.sendMsg(currentMsg, new WTFSocketHandler() {
                        @Override
                        public boolean onReceive(WTFSocketSession session, WTFSocketMsg msg) {
                            if (msg.getState() != 1) {
                                commonHandler.sendEmptyMessage(5);
                            } else {
                                AppMsg appMsg = msg.getBody(AppMsg.class);

                                if (appMsg.getFlag() == 1) {
                                    //System.out.println("获取数据成功");
                                    currentState[LEFT_SIDE] = JSON.parseObject(appMsg.getParams().getString(LEFT_SIDE),
                                            CurrentState.class);
                                    currentState[RIGHT_SIDE] = JSON.parseObject(appMsg.getParams().getString(RIGHT_SIDE),
                                            CurrentState.class);
                                    swicthState = JSON.parseObject(appMsg.getParams().getString(2), Switch.class);
                                    commonHandler.sendEmptyMessage(0);
                                } else {
                                    commonHandler.sendEmptyMessage(5);
                                }
                            }
                            return true;
                        }

                        @Override
                        public boolean onException(WTFSocketSession session, WTFSocketMsg msg, WTFSocketException e) {
                            commonHandler.sendEmptyMessage(1);
                            return true;
                        }
                    }, Param.TCP_TIMEOUT);
                }
            }.start();

        }
    }

    /**
     * 更新数据
     */
    private void updateCurrentData() {
        if (!WTFApplication.isConnectingToInternet()) {
            commonHandler.sendEmptyMessage(4);
        } else {
            load_progress.setVisibility(View.VISIBLE);

            currentState[LEFT_SIDE].setTargetTemperature(TargetTmp[LEFT_SIDE]);
            currentState[LEFT_SIDE].setCurrentTemperature(CurrentTmp[LEFT_SIDE]);
            currentState[LEFT_SIDE].setMode(Mode[LEFT_SIDE]);
            currentState[LEFT_SIDE].setSide(LEFT_SIDE);

            currentState[RIGHT_SIDE].setTargetTemperature(TargetTmp[RIGHT_SIDE]);
            currentState[RIGHT_SIDE].setCurrentTemperature(CurrentTmp[RIGHT_SIDE]);
            currentState[RIGHT_SIDE].setMode(Mode[RIGHT_SIDE]);
            currentState[RIGHT_SIDE].setSide(RIGHT_SIDE);

            new Thread(){
                @Override
                public void run()
                {
                    WTFSocketMsg currentMsg = new WTFSocketMsg().setBody(new AppMsg().setCmd(16).addParam(currentState[RIGHT_SIDE]).addParam(currentState[LEFT_SIDE]));
                    WTFSocketSession session1 = WTFSocketSessionFactory.getSession(WTFApplication.userData.getSelDeviceName());
                    session1.sendMsg(currentMsg, new WTFSocketHandler() {
                        @Override
                        public boolean onReceive(WTFSocketSession session, WTFSocketMsg msg) {
                            if (msg.getState() != 1) {
                                commonHandler.sendEmptyMessage(5);
                            } else {
                                AppMsg appMsg = msg.getBody(AppMsg.class);

                                if (appMsg.getFlag() == 1) {
                                    System.out.println("更新数据成功");
                                    commonHandler.sendEmptyMessage(2);
                                } else {
                                    commonHandler.sendEmptyMessage(5);
                                }
                            }
                            return true;
                        }

                        @Override
                        public boolean onException(WTFSocketSession session, WTFSocketMsg msg, WTFSocketException e) {
                            commonHandler.sendEmptyMessage(3);
                            return true;
                        }
                    }, Param.TCP_TIMEOUT);
                }
            }.start();

        }
    }

    /**
     * 开关机
     */
    private void power() {
        powerchange();
        if (!WTFApplication.isConnectingToInternet()) {
            commonHandler.sendEmptyMessage(4);
        } else {

            load_progress.setVisibility(View.VISIBLE);
            //WTFSocketMsg currentMsg = new WTFSocketMsg().setBody(new AppMsg().setCmd(20).addParam(powerOn).addParam(leftCurrentState));
            swicthState.setPowerOn(powerOn);

            new Thread(){
                @Override
                public void run(){
                    WTFSocketMsg currentMsg = new WTFSocketMsg().setBody(new AppMsg().setCmd(20).addParam(swicthState));

                   // SocketConnection.registerSocket();

                    WTFSocketSession session1 = WTFSocketSessionFactory.getSession(WTFApplication.userData.getSelDeviceName());

                    session1.sendMsg(currentMsg, new WTFSocketHandler() {
                        @Override
                        public boolean onReceive(WTFSocketSession session, WTFSocketMsg msg) {
                            if (msg.getState() != 1) {
                                commonHandler.sendEmptyMessage(5);
                            } else {
                                AppMsg appMsg = msg.getBody(AppMsg.class);

                                if (appMsg.getFlag() == 1) {
                                    commonHandler.sendEmptyMessage(2);
                                } else {
                                    commonHandler.sendEmptyMessage(5);
                                }
                            }
                            return true;
                        }

                        @Override
                        public boolean onException(WTFSocketSession session, WTFSocketMsg msg, WTFSocketException e) {
                            commonHandler.sendEmptyMessage(3);
                            return true;
                        }
                    }, Param.TCP_TIMEOUT);
                }
            }.start();
        }
    }


    /**
     * 点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_switch: //开关
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setCancelable(false);
                dialog.setTitle("提醒");
                if (powerOn == 0)
                    dialog.setMessage("请确认,你是否要打开床垫总开关\n将打开加热与理疗预约功能");
                else
                    dialog.setMessage("请确认,你是否要关闭床垫总开关\n将关闭加热与理疗预约功能");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (powerOn == 1) {
                            standby(LEFT_SIDE);
                            standby(RIGHT_SIDE);
                            ib_switch.setImageResource(R.mipmap.power_off);
                            iv_off.setVisibility(View.VISIBLE);
                            powerOn = 0;
                            power();
                        } else {
                            ib_switch.setImageResource(R.mipmap.power_on);
                            ib_heating_left.setClickable(true);
                            ib_heating_right.setClickable(true);
                            ib_therapy_left.setClickable(true);
                            ib_therapy_right.setClickable(true);
                            iv_off.setVisibility(View.GONE);
                            powerOn = 1;
                            power();
                        }
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                dialog.show();
                break;
            //左理疗按钮
            case R.id.ib_therapy_left:
                ib_therapy(LEFT_SIDE);
                break;
            //右理疗按钮
            case R.id.ib_therapy_right:
                ib_therapy(RIGHT_SIDE);
                break;
            //左加热按钮
            case R.id.ib_heating_left:
                ib_heating(LEFT_SIDE);
                break;
            //右加热按钮
            case R.id.ib_heating_right:
                ib_heating(RIGHT_SIDE);
                break;
            //左升温按钮
            case R.id.ib_tmp_plus_left:
                ib_tmp_plus(LEFT_SIDE);
                break;
            /** 右升温按钮 **/
            case R.id.ib_tmp_plus_right:
                ib_tmp_plus(RIGHT_SIDE);
                break;
            //左降温按钮
            case R.id.ib_tmp_minus_left:
                ib_tmp_minus(LEFT_SIDE);
                break;
            /** 右降温按钮 **/
            case R.id.ib_tmp_minus_right:
                ib_tmp_minus(RIGHT_SIDE);
                break;
        }
    }

    private void ib_therapy(int w) {////改变理疗模式
        if (Mode[w] == MODE_THERAPY) {
            standby(w);
            updateCurrentData();
        } else if (Mode[w] == MODE_THERAPY_PRE) {
            final int ww = w;
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setCancelable(false);
            dialog.setTitle("提醒");
            dialog.setMessage("正在执行预约的理疗功能\n请确认是否要取消");
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    standby(ww);
                    updateCurrentData();
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                }
            });
            dialog.show();
        } else if (Mode[w] == MODE_HEATING_PRE) {
            final int ww = w;
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setCancelable(false);
            dialog.setTitle("提醒");
            dialog.setMessage("正在执行预约的加热功能\n请确认是否要转变到理疗模式");
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    therapy(ww);
                    updateCurrentData();
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                }
            });
            dialog.show();
        } else {
            therapy(w);
            updateCurrentData();
        }
    }

    private void ib_heating(int w) {////改变加热模式
        if (Mode[w] == MODE_HEATING) {
            standby(w);
            updateCurrentData();
        } else if (Mode[w] == MODE_HEATING_PRE) {
            final int ww = w;
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setCancelable(false);
            dialog.setTitle("提醒");
            dialog.setMessage("正在执行预约的加热功能\n请确认是否要取消");
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    standby(ww);
                    updateCurrentData();
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                }
            });
            dialog.show();
        } else if (Mode[w] == MODE_THERAPY_PRE) {
            final int ww = w;
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setCancelable(false);
            dialog.setTitle("提醒");
            dialog.setMessage("正在执行预约的理疗功能\n请确认是否要转变到加热模式");
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    heating(ww);
                    updateCurrentData();
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                }
            });
            dialog.show();
        } else {
            heating(w);
            updateCurrentData();
        }
    }

    private void ib_tmp_plus(int w) {//改变温度
        if (Mode[w] == MODE_HEATING) {
            if (TargetTmp[w] < 45) {
                TargetTmp[w]++;
                if (w == LEFT_SIDE) {
                    tv_tmp_left.setTextColor(Color.WHITE);
                    tv_tmp_left.setText(TargetTmp[LEFT_SIDE] + "℃");
                } else {
                    tv_tmp_right.setTextColor(Color.WHITE);
                    tv_tmp_right.setText(TargetTmp[RIGHT_SIDE] + "℃");
                }
                updateCurrentData();
            } else {
                showToast("温度不能超过45摄氏度");
            }
        } else if (Mode[w] == MODE_HEATING_PRE) {
            showToast("在预约加热模式,请到加热预约调整温度");
        } else {
            showToast("请打开加热按钮");
        }
    }

    private void ib_tmp_minus(int w) {//改变温度
        if (Mode[w] == MODE_HEATING) {
            if (TargetTmp[w] > 25) {
                TargetTmp[w]--;
                if (w == LEFT_SIDE) {
                    tv_tmp_left.setTextColor(Color.WHITE);
                    tv_tmp_left.setText(TargetTmp[LEFT_SIDE] + "℃");
                } else {
                    tv_tmp_right.setTextColor(Color.WHITE);
                    tv_tmp_right.setText(TargetTmp[RIGHT_SIDE] + "℃");
                }
                updateCurrentData();
            } else {
                showToast("温度不能低于25摄氏度");
            }
        } else if (Mode[w] == MODE_HEATING_PRE) {
            showToast("在预约加热模式,请到加热预约调整温度");
        } else {
            showToast("请打开加热按钮");
        }
    }

    /*延迟加载*/
    protected void lazyLoad() {
        Log.i("lazyLoad","function");
        if (!isPrepared || !isVisible) {
            return;
        }
        mHasLoadedOnce = true;
        initTitle();
        new Thread() {
            public void run() {
                getCurrentData();

            }
        }.start();

    }

    private void reviewSuccess(int w) {
        TargetTmp[w] = currentState[w].getTargetTemperature();
        CurrentTmp[w] = currentState[w].getCurrentTemperature();
        Mode[w] = currentState[w].getMode();
        //判断初始温度是否正确，若不正确则，赋初始值，但是硬件没有反馈
        if (TargetTmp[w] < 25 && TargetTmp[w] > 45) {
            TargetTmp[w] = 35;
        }
        //0:：待机1：加热2：理疗3:智能加热4:智能理疗
        switch (currentState[w].getMode()) {
            case MODE_HEATING:
                heating(w);
                break;
            case MODE_THERAPY:
                therapy(w);
                break;
            case MODE_HEATING_PRE:
                heatingpre(w);
                break;
            case MODE_THERAPY_PRE:
                therapypre(w);
                break;
            case MODE_STANDBY:
            default:
                standby(w);
        }
    }

    private void powerchange() {
        /*更改开关图标*/
        if (powerOn == 1) {//if (leftMode != 0 || rightMode != 0) {
            ib_switch.setImageResource(R.mipmap.power_on);
            ib_heating_left.setClickable(true);
            ib_heating_right.setClickable(true);
            ib_therapy_left.setClickable(true);
            ib_therapy_right.setClickable(true);
            iv_off.setVisibility(View.GONE);
        } else {
            ib_switch.setImageResource(R.mipmap.power_off);
            ib_heating_left.setClickable(false);
            ib_heating_right.setClickable(false);
            ib_therapy_left.setClickable(false);
            ib_therapy_right.setClickable(false);
            iv_off.setVisibility(View.VISIBLE);
        }
    }

    private void review(int event) {
        if (event == 0) {//获取数据成功,更新界面
            powerOn = swicthState.getPowerOn();

            reviewSuccess(LEFT_SIDE);
            reviewSuccess(RIGHT_SIDE);
        }
        /*左侧控制视图*/
        tv_tmp_left.setText(TargetTmp[LEFT_SIDE] + "℃");
        tv_tmp_left.setTextColor(light_grey);

       /*右侧控制视图*/
        tv_tmp_right.setText(TargetTmp[RIGHT_SIDE] + "℃");
        tv_tmp_right.setTextColor(light_grey);

        powerchange();

        if (/*Mode[LEFT_SIDE] == MODE_HEATING_PRE || */Mode[LEFT_SIDE] == MODE_THERAPY_PRE ||
                Mode[LEFT_SIDE] == MODE_THERAPY || Mode[LEFT_SIDE] == MODE_STANDBY) {
            ib_tmp_plus_left.setEnabled(false);
            ib_tmp_plus_left.setBackgroundResource(R.mipmap.left_plus_touched);
            ib_tmp_minus_left.setEnabled(false);
            ib_tmp_minus_left.setBackgroundResource(R.mipmap.left_minus_touched);
        } else {
            ib_tmp_plus_left.setEnabled(true);
            ib_tmp_plus_left.setBackgroundResource(R.mipmap.left_plus_untouch);
            ib_tmp_minus_left.setEnabled(true);
            ib_tmp_minus_left.setBackgroundResource(R.mipmap.left_minus_untouch);
        }

        if (/*Mode[RIGHT_SIDE] == MODE_HEATING_PRE || */Mode[RIGHT_SIDE] == MODE_THERAPY_PRE ||
                Mode[RIGHT_SIDE] == MODE_THERAPY || Mode[RIGHT_SIDE] == MODE_STANDBY) {
            ib_tmp_plus_right.setEnabled(false);
            ib_tmp_plus_right.setBackgroundResource(R.mipmap.right_plus_touched);
            ib_tmp_minus_right.setEnabled(false);
            ib_tmp_minus_right.setBackgroundResource(R.mipmap.right_minus_touched);
        } else {
            ib_tmp_plus_right.setEnabled(true);
            ib_tmp_plus_right.setBackgroundResource(R.mipmap.right_plus_untouch);
            ib_tmp_minus_right.setEnabled(true);
            ib_tmp_minus_right.setBackgroundResource(R.mipmap.right_minus_untouch);
        }
    }

    // Handler
    static class CommonHandler extends Handler {
        WeakReference<CommonFragment> mFragment;

        CommonHandler(CommonFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            CommonFragment theFragment = mFragment.get();
            switch (msg.what) {
                case 0:
                    theFragment.load_progress.setVisibility(View.GONE);
                    theFragment.review(0);
                    theFragment.showToast("获取数据成功");
                    break;
                case 1:
                    theFragment.load_progress.setVisibility(View.GONE);
                    theFragment.review(1);
                    theFragment.showToast("获取数据失败");
                    break;
                case 2:
                    theFragment.load_progress.setVisibility(View.GONE);
                    theFragment.review(1);
                    theFragment.showToast("更新数据成功");
                    break;
                case 3:
                    theFragment.load_progress.setVisibility(View.GONE);
                    theFragment.review(1);
                    theFragment.showToast("更新数据失败");
                    break;
                case 4:
                    theFragment.load_progress.setVisibility(View.GONE);
                    theFragment.review(1);
                    theFragment.showToast("未联网请先联网");
                    break;
                case 5:
                    theFragment.load_progress.setVisibility(View.GONE);
                    theFragment.review(1);
                    theFragment.showToast("床垫还未连上服务器，请耐心等待");
                    break;
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

    private void heating(int w) {
        Mode[w] = MODE_HEATING;
        if (w == LEFT_SIDE) {//左加热
            tv_mode_left.setText("加热");
            ib_heating_left.setImageResource(R.mipmap.heat_left_selected);
            ib_therapy_left.setImageResource(R.mipmap.treat_left_unselect);
        } else {//右加热
            tv_mode_right.setText("加热");
            ib_heating_right.setImageResource(R.mipmap.heat_right_selected);
            ib_therapy_right.setImageResource(R.mipmap.treat_right_unselect);
        }
    }

    private void heatingpre(int w) {
        Mode[w] = MODE_HEATING_PRE;
        if (w == LEFT_SIDE) {//左加热
            tv_mode_left.setText("预约加热");
            ib_heating_left.setImageResource(R.mipmap.heat_left_selected);
            ib_therapy_left.setImageResource(R.mipmap.treat_left_unselect);
        } else {//右加热
            tv_mode_right.setText("预约加热");
            ib_heating_right.setImageResource(R.mipmap.heat_right_selected);
            ib_therapy_right.setImageResource(R.mipmap.treat_right_unselect);
        }
    }

    private void therapy(int w) {
        Mode[w] = MODE_THERAPY;
        if (w == LEFT_SIDE) {//左理疗
            tv_mode_left.setText("理疗");
            ib_heating_left.setImageResource(R.mipmap.heat_left_unselect);
            ib_therapy_left.setImageResource(R.mipmap.treat_left_selected);
        } else {//右理疗
            tv_mode_right.setText("理疗");
            ib_heating_right.setImageResource(R.mipmap.heat_right_unselect);
            ib_therapy_right.setImageResource(R.mipmap.treat_right_selected);
        }
    }

    private void therapypre(int w) {
        Mode[w] = MODE_THERAPY_PRE;
        if (w == LEFT_SIDE) {//左理疗
            tv_mode_left.setText("预约理疗");
            ib_heating_left.setImageResource(R.mipmap.heat_left_unselect);
            ib_therapy_left.setImageResource(R.mipmap.treat_left_selected);
        } else {//右理疗
            tv_mode_right.setText("预约理疗");
            ib_heating_right.setImageResource(R.mipmap.heat_right_unselect);
            ib_therapy_right.setImageResource(R.mipmap.treat_right_selected);
        }
    }

    private void standby(int w) {
        Mode[w] = MODE_STANDBY;
        if (w == LEFT_SIDE) {//左待机
            tv_mode_left.setText("待机");
            ib_heating_left.setImageResource(R.mipmap.heat_left_unselect);
            ib_therapy_left.setImageResource(R.mipmap.treat_left_unselect);
        } else {//右待机
            tv_mode_right.setText("待机");
            ib_heating_right.setImageResource(R.mipmap.heat_right_unselect);
            ib_therapy_right.setImageResource(R.mipmap.treat_right_unselect);
        }
    }

/*
    private void state(int state){
        switch (state){
            case 0:
                leftMode=1;
                isImgOffVisible = false;
                tv_mode_left.setText("加热");
                ib_heating_left.setImageResource(R.mipmap.heat_left_selected);
                ib_therapy_left.setImageResource(R.mipmap.treat_left_unselect);
                break;
            case 1:
                leftMode=2;
                isImgOffVisible = false;
                tv_mode_left.setText("理疗");
                ib_heating_left.setImageResource(R.mipmap.heat_left_unselect);
                ib_therapy_left.setImageResource(R.mipmap.treat_left_selected);
                break;
            case 2:
                leftMode=0;
                isImgOffVisible = false;
                tv_mode_left.setText("待机");
                ib_heating_left.setImageResource(R.mipmap.heat_left_unselect);
                ib_therapy_left.setImageResource(R.mipmap.treat_left_unselect);
                break;
            case 3:
                rightMode=1;
                isImgOffVisible = false;
                tv_mode_right.setText("加热");
                ib_heating_right.setImageResource(R.mipmap.heat_right_selected);
                ib_therapy_right.setImageResource(R.mipmap.treat_right_unselect);
                break;
            case 4:
                rightMode=2;
                isImgOffVisible = false;
                tv_mode_right.setText("理疗");
                ib_heating_right.setImageResource(R.mipmap.heat_right_unselect);
                ib_therapy_right.setImageResource(R.mipmap.treat_right_selected);
                break;
            case 5:
                rightMode=0;
                isImgOffVisible = false;
                tv_mode_right.setText("待机");
                ib_heating_right.setImageResource(R.mipmap.heat_right_unselect);
                ib_therapy_right.setImageResource(R.mipmap.treat_right_unselect);
                break;
        }
    }*/

}

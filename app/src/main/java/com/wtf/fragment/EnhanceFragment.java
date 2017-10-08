package com.wtf.fragment;

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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.alibaba.fastjson.JSON;
import com.wtf.R;
import com.wtf.WTFApplication;
import com.wtf.model.AppMsg;
import com.wtf.model.Param;
import com.wtf.model.PhysiotherapyReservation;
import com.wtf.model.QueryData;
import com.wtf.ui.RefreshLayout;
import com.wtf.ui.select_time.RangeSeekBar;
import com.wtf.ui.select_time.ThumbView;
import com.wtf.utils.WifiThread;

import java.lang.ref.WeakReference;
import java.util.Date;

import wtf.socket.WTFSocketException;
import wtf.socket.WTFSocketHandler;
import wtf.socket.WTFSocketMsg;
import wtf.socket.WTFSocketSession;
import wtf.socket.WTFSocketSessionFactory;

import static com.wtf.fragment.ConstantFragment.side;

/**
 * Created by liyan on 2016/10/12.
 * 理疗预约
 */
public class EnhanceFragment extends BaseFragment implements View.OnClickListener, RefreshLayout.OnHeaderRefreshListener {
    private FragmentActivity mActivity;
    private TextView tv_time;
    private Button btn_is_change;
    private ImageView iv_time_minus;
    private ImageView iv_time_cancel;
    private ImageView iv_time_plus;
    //    private ThumbView mThumbLeft;
//    private ThumbView mThumbRight;
    TextView tv_side;
    ToggleButton tb_switch;
    private LinearLayout load_progress;
    private ImageView iv_therapy_off;
    private PopupWindow mPopWindow;
    private TextView tv_pop;
    private TextView tv_time_info;
    private TextView tv_allow_time_info;
    private View contentView;
    private LinearLayout ll_pop;
    private static int LILIAO_MIN = 15;
    private static int LILIAO_MAX = 120;

    /**
     * 是否已被加载过一次，第二次就不再去请求数据了
     */
    private boolean mHasLoadedOnce;

    private int popLocation;

    private Integer _switch = 0;
    private Integer workTime = LILIAO_MIN * 60;
    private Integer startTime = 8 * 60 * 60;
    private Integer overTime = 17 * 60 * 60;
    //private Integer side = 1;
    private Integer originalWorkTime = LILIAO_MIN * 60;

    private RangeSeekBar bar;
    private PhysiotherapyReservation physiotherapy;
    private QueryData queryData;
    private String packet = null;
    private EnhanceHandler enhanceHandler;

    private RefreshLayout mPullToRefreshView;
    private RefreshDataAsynTask mRefreshAsynTask;

    private static final int light_grey = Color.rgb(0x8c, 0x8c, 0x8c);

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_enhance, container, false);
        enhanceHandler = new EnhanceHandler(this);
        physiotherapy = new PhysiotherapyReservation();

        initViews(view);
        isPrepared = true;
        lazyLoad();
        return view;
    }

    private void initTitle() {
        mActivity = this.getActivity();

        SettingFragment settingFragment = (SettingFragment) EnhanceFragment.this.getParentFragment();
        settingFragment.toChangeVISIBLE();
        Log.i("settingFragment", "" + String.valueOf(settingFragment == null));
        View settingView = settingFragment.getView();

        tv_side = (TextView) settingView.findViewById(R.id.tv_side);
        tb_switch = (ToggleButton) settingView.findViewById(R.id.tb_switch);
        btn_is_change = (Button) settingView.findViewById(R.id.btn_is_change);

        tb_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_switch != 0) {
                    iv_therapy_off.setVisibility(View.GONE);
                    btn_is_change.setVisibility(View.VISIBLE);
                } else {
                    workTime = originalWorkTime;
                    updateCurrentData();
                    iv_therapy_off.setVisibility(View.VISIBLE);
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
                } else {
                    _switch = 0;
                }

            }
        });

        tv_side.setOnClickListener(this);
        btn_is_change.setOnClickListener(this);
    }

    private void initViews(View view) {
        mActivity = this.getActivity();

        load_progress = (LinearLayout) view.findViewById(R.id.enhance_load);

        tv_time = (TextView) view.findViewById(R.id.tv_time);

        iv_time_minus = (ImageView) view.findViewById(R.id.iv_time_minus);
        iv_time_cancel = (ImageView) view.findViewById(R.id.iv_time_cancel);
        iv_time_plus = (ImageView) view.findViewById(R.id.iv_time_plus);
        bar = (RangeSeekBar) view.findViewById(R.id.select_time);
        iv_therapy_off = (ImageView) view.findViewById(R.id.iv_therapy_off);

        tv_time_info = (TextView) view.findViewById(R.id.tv_time_info);
        tv_allow_time_info = (TextView) view.findViewById(R.id.tv_allow_time_info);

        iv_time_cancel.setOnClickListener(this);
        iv_time_minus.setOnClickListener(this);
        iv_time_plus.setOnClickListener(this);
        tv_time_info.setOnClickListener(this);
        tv_allow_time_info.setOnClickListener(this);

        mPullToRefreshView = (RefreshLayout) view
                .findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnHeaderRefreshListener(this);
        mPullToRefreshView.setEnablePullLoadMoreDataStatus(false);
        mPullToRefreshView.showFooterView(false);
        mPullToRefreshView.setLastUpdated(new Date().toLocaleString());

        mPullToRefreshView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                // Log.v(TAG, "PARENT TOUCH");
                v.findViewById(R.id.select_time).getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });

        bar.getThumbLeft().setOnThumbListener(new ThumbView.OnThumbListener() {
            @Override
            public void onThumbChange(int i) {
                bar.setLeftValue(i);

            }
        });
        bar.getThumbRight().setOnThumbListener(new ThumbView.OnThumbListener() {
            @Override
            public void onThumbChange(int i) {
                bar.setRightValue(i);

            }
        });

        bar.getThumbLeft().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                if (btn_is_change != null && btn_is_change.getVisibility() == View.GONE) {
                    btn_is_change.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
        bar.getThumbRight().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                if (btn_is_change != null && btn_is_change.getVisibility() == View.GONE) {
                    btn_is_change.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
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
            enhanceHandler.sendEmptyMessage(4);
        } else {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    load_progress.setVisibility(View.VISIBLE);
                }
            });
            queryData = new QueryData();
            queryData.setSide(side);
            if (WTFSocketSessionFactory.isAvailable()) {
                new Thread() {
                    @Override
                    public void run() {
                        WTFSocketMsg currentMsg = new WTFSocketMsg().setBody(new AppMsg().setCmd(34).addParam(queryData));

                        WTFSocketSession session1 = WTFSocketSessionFactory.getSession(WTFApplication.userData.getSelDeviceName());
                        session1.sendMsg(currentMsg, new WTFSocketHandler() {
                            @Override
                            public boolean onReceive(WTFSocketSession session, WTFSocketMsg msg) {
                                if (msg.getState() != 1) {
                                    enhanceHandler.sendEmptyMessage(5);
                                } else {
                                    AppMsg appMsg = msg.getBody(AppMsg.class);

                                    if (appMsg.getFlag() == 1) {
                                        System.out.println("获取数据成功");
                                        packet = appMsg.getParams().getString(0);
                                        enhanceHandler.sendEmptyMessage(0);
                                    } else {
                                        enhanceHandler.sendEmptyMessage(5);
                                    }
                                }
                                return true;
                            }

                            public boolean onException(WTFSocketSession session, WTFSocketMsg msg, WTFSocketException e) {
                                enhanceHandler.sendEmptyMessage(1);
                                return true;
                            }
                        }, Param.TCP_TIMEOUT);
                    }
                }.start();
            } else {
                enhanceHandler.sendEmptyMessage(6);
            }

        }
    }

    private void updateCurrentData() {
        if (!WTFApplication.isConnectingToInternet()) {
            enhanceHandler.sendEmptyMessage(4);
        } else {
            if (bar.hourValue(bar.getThumbLeft()) < bar.hourValue(bar.getThumbRight())) {
                load_progress.setVisibility(View.VISIBLE);

                int startHour = bar.hourValue(bar.getThumbLeft());
                int startMin = bar.minValue(bar.getThumbLeft());
                int overHour = bar.hourValue(bar.getThumbRight());
                int overMin = bar.minValue(bar.getThumbRight());
       /* if (startHour>overHour) {*/
                startTime = startHour * 60 * 60 + startMin * 60;
                overTime = overHour * 60 * 60 + overMin * 60;
       /* }else if (overHour>startHour){
            startTime=overHour*60*60;
            overTime=startHour*60*60;
        }*/

                physiotherapy.setSide(side);
                physiotherapy.setOverTime(overTime);
                physiotherapy.setStartTime(startTime);
                physiotherapy.setWorkTime(workTime);
                physiotherapy.setModeSwitch(_switch);
                if(WTFSocketSessionFactory.isAvailable()) {
                    new Thread() {
                        @Override
                        public void run() {
                            WTFSocketMsg updateMsg = new WTFSocketMsg().setBody(new AppMsg().setCmd(18).addParam(physiotherapy));

                            WTFSocketSession session1 = WTFSocketSessionFactory.getSession(WTFApplication.userData.getSelDeviceName());
                            session1.sendMsg(updateMsg, new WTFSocketHandler() {
                                @Override
                                public boolean onReceive(WTFSocketSession session, WTFSocketMsg msg) {
                                    if (msg.getState() != 1) {
                                        enhanceHandler.sendEmptyMessage(5);
                                    } else {
                                        //  currentTemperature = Integer.valueOf(session.getMsg().getParams().getJSONObject(0).getString("currentTemperature"));
                                        AppMsg appMsg = msg.getBody(AppMsg.class);

                                        if (appMsg.getFlag() == 1) {
                                            System.out.println("更新数据成功");
                                            originalWorkTime = workTime;
                                            enhanceHandler.sendEmptyMessage(2);
                                        } else {
                                            enhanceHandler.sendEmptyMessage(5);
                                        }
                                    }
                                    return true;
                                }

                                public boolean onException(WTFSocketSession session, WTFSocketMsg msg, WTFSocketException e) {
                                    enhanceHandler.sendEmptyMessage(3);
                                    return true;
                                }
                            }, Param.TCP_TIMEOUT);
                        }
                    }.start();
                }else {
                    enhanceHandler.sendEmptyMessage(6);
                }

            } else {
                showToast("开始时间不能等于结束时间");
            }
        }
    }

    private void showPopupWindow() {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.pop_up, null);
        mPopWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv_pop = (TextView) contentView.findViewById(R.id.tv_pop);
        ll_pop = (LinearLayout) contentView.findViewById(R.id.ll_pop);
        mPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopWindow.setOutsideTouchable(true);
        mPopWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popWidth = mPopWindow.getContentView().getMeasuredWidth();
        popLocation = popWidth / 2 - tv_time_info.getWidth() / 2;
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
                load_progress.setVisibility(View.VISIBLE);
                getCurrentData();
                break;
            case R.id.btn_is_change:
                if (btn_is_change.getVisibility() == View.VISIBLE) {

                    updateCurrentData();
                } else {
                    btn_is_change.setVisibility(View.GONE);
                }
                break;
            case R.id.iv_time_cancel:
                if (workTime != originalWorkTime) {
                    workTime = originalWorkTime;
                    tv_time.setText(workTime / 60 + "min");
                    tv_time.setTextColor(light_grey);
                    iv_time_cancel.setVisibility(View.GONE);
                }
                break;
            case R.id.tv_time_info:
                showPopupWindow();
                tv_pop.setText(R.string.therapy_time_info);
                mPopWindow.showAsDropDown(tv_time_info, -popLocation, 0);
                break;
            case R.id.tv_allow_time_info:
                showPopupWindow();
                tv_pop.setText(R.string.therapy_allow_time_info);
                mPopWindow.showAsDropDown(tv_allow_time_info, -popLocation, 0);
                break;
            case R.id.iv_time_plus:
                if (workTime < LILIAO_MAX * 60) {
                    workTime = workTime + 5 * 60;
                    if (btn_is_change.getVisibility() == View.GONE) {
                        btn_is_change.setVisibility(View.VISIBLE);
                    }
                    tv_time.setTextColor(Color.WHITE);
                    tv_time.setText(workTime / 60 + "min");
                    if (!workTime.equals(originalWorkTime)) {
                        if (iv_time_cancel.getVisibility() == View.GONE) {
                            iv_time_cancel.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (iv_time_cancel.getVisibility() == View.VISIBLE) {
                            iv_time_cancel.setVisibility(View.GONE);
                        }
                    }
                }
                break;
            case R.id.iv_time_minus:
                if (workTime > LILIAO_MIN * 60) {
                    workTime = workTime - 5 * 60;
                    if (btn_is_change.getVisibility() == View.GONE) {
                        btn_is_change.setVisibility(View.VISIBLE);
                    }
                    tv_time.setTextColor(Color.WHITE);
                    tv_time.setText((workTime / 60) + "min");
                    if (!workTime.equals(originalWorkTime)) {
                        if (iv_time_cancel.getVisibility() == View.GONE) {
                            iv_time_cancel.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (iv_time_cancel.getVisibility() == View.VISIBLE) {
                            iv_time_cancel.setVisibility(View.GONE);
                        }
                    }
                }
                break;
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

    protected void lazyLoad() {
        if (!isPrepared || !isVisible) {
            Log.i("lazyLoad", "未加载");
            return;
        }
        /*if (mHasLoadedOnce ){
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


    static class EnhanceHandler extends Handler {
        WeakReference<EnhanceFragment> mFragment;

        EnhanceHandler(EnhanceFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            EnhanceFragment theFragment = mFragment.get();
            switch (msg.what) {
                case 0:
                    theFragment.load_progress.setVisibility(View.GONE);

                    theFragment.physiotherapy = JSON.parseObject(theFragment.packet, PhysiotherapyReservation.class);
                    System.out.println("currentState" + theFragment.physiotherapy);
                    theFragment.originalWorkTime = theFragment.physiotherapy.getWorkTime();
                    theFragment.startTime = theFragment.physiotherapy.getStartTime();
                    theFragment.overTime = theFragment.physiotherapy.getOverTime();
                    theFragment.workTime = theFragment.originalWorkTime;
                    theFragment._switch=theFragment.physiotherapy.getModeSwitch();
                    if (theFragment.workTime <= LILIAO_MIN * 60) {
                        theFragment.workTime = LILIAO_MIN * 60;
                    } else if (theFragment.workTime >= LILIAO_MAX * 60) {
                        theFragment.workTime = LILIAO_MAX * 60;
                    }

                    theFragment.tv_time.setText(theFragment.workTime / 60 + "min");

                    if (theFragment.physiotherapy.getModeSwitch() == 1) {
                        theFragment.tb_switch.setChecked(true);
                        theFragment.iv_therapy_off.setVisibility(View.GONE);
                    } else {
                        theFragment.tb_switch.setChecked(false);
                        theFragment.iv_therapy_off.setVisibility(View.VISIBLE);
                    }

                    if (theFragment.startTime < 6 * 60 * 60 || theFragment.startTime > 24 * 60 * 60) {
                        theFragment.startTime = 18 * 60 * 60;
                    }
                    if (theFragment.overTime < 6 * 60 * 60 || theFragment.overTime > 24 * 60 * 60) {
                        theFragment.overTime = 24 * 60 * 60;
                    }

                    theFragment.bar.setMLeft((float) theFragment.startTime / 60 / 60);
                    theFragment.bar.setMRight((float) theFragment.overTime / 60 / 60);
                    if (theFragment.bar.getMeasuredHeight() != 0) {
                        //theFragment.bar.onLayoutPrepared();
                        theFragment.bar.requestLayout();
                        theFragment.bar.invalidate();
                    }

                    theFragment.btn_is_change.setVisibility(View.GONE);
                    theFragment.tv_time.setTextColor(light_grey);
                    theFragment.iv_time_cancel.setVisibility(View.GONE);
                    theFragment.showToast("获取数据成功");
                    break;
                case 1:
                    theFragment.load_progress.setVisibility(View.GONE);
                    theFragment.showToast("获取数据失败");
                    break;
                case 2:
                    theFragment.load_progress.setVisibility(View.GONE);
                    theFragment.btn_is_change.setVisibility(View.GONE);
                    theFragment.tv_time.setTextColor(light_grey);
                    theFragment.iv_time_cancel.setVisibility(View.GONE);
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

}
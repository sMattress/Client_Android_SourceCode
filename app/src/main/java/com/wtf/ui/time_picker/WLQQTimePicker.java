package com.wtf.ui.time_picker;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;


import com.wtf.R;

import java.util.ArrayList;


public class WLQQTimePicker extends LinearLayout {
    public static final String PICKED_TIME_EXT = "picked_time";
    private static final int UPDATE_TITLE_MSG = 0x111;
    private static final int UPDATE_WHEEL = 0x112;
    private static final int UPDATE_UpdateDay_MSG = 0x113;

    // private TextView mPickerTitle;
    private WheelView mWheelHour;
    private WheelView mWheelMin;

    private int mHour;
    private int mMin;

    private int mDefaultDayWhellIndex = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_WHEEL: {
                    updateWheel();
                }
                break;

            }

        }
    };

    public int getHour(){
        return mHour;
    }
    public int getMin(){
        return mMin;
    }

    private WheelView.OnSelectListener mHourListener = new WheelView.OnSelectListener() {
        @Override
        public void endSelect(int hour, String text) {
            mHour = hour;
        }

        @Override
        public void selecting(int day, String text) {
        }
    };

    private WheelView.OnSelectListener mMinListener = new WheelView.OnSelectListener() {
        @Override
        public void endSelect(int min, String text) {
            mMin = min;
        }

        @Override
        public void selecting(int day, String text) {
        }
    };

    private Activity mContext;

    public WLQQTimePicker(Context context) {
        this(context, null);
    }

    public WLQQTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContext = (Activity) getContext();
        LayoutInflater.from(mContext).inflate(R.layout.time_picker, this);
        //mPickerTitle = (TextView) findViewById(R.id.picker_title);
        mWheelHour = (WheelView) findViewById(R.id.hour);
        mWheelMin=(WheelView)findViewById(R.id.minute);

        mWheelHour.setOnSelectListener(mHourListener);
        mWheelMin.setOnSelectListener(mMinListener);

    }


    /**
     * set WLQQTimePicker date
     * @param date
     */
    public void setDate(int date) {

        System.out.println("date:"+date);

        mHour = (date/60)/60;
        mMin = (date/60)%60;
        System.out.println("mHour:"+mHour);
        System.out.println("mMin:"+mMin);


        mWheelHour.setData(getHourData());
        mWheelMin.setData(getMinData());

        mHandler.sendEmptyMessage(UPDATE_WHEEL);
    }



    private void updateWheel() {

        mWheelHour.setDefault(mHour);
        mWheelMin.setDefault(mMin);

    }


    private ArrayList<String> getHourData() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 24; i++) {
            if(i<10){
                list.add("0"+i+"时");

            }else{
                list.add(i + "时");
            }
        }
        return list;
    }

    private ArrayList<String> getMinData() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 60; i++) {
            if(i<10){
                list.add("0"+i+"分");

            }else{
                list.add(i + "分");
            }
        }
        return list;
    }
}
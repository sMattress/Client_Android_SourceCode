package com.wtf.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements
        DatePickerDialog.OnDateSetListener {

    TheListener listener;
    private int year;
    private int month;
    private int day;
    boolean isfired = false;

    public static DatePickerFragment getInstance(int year, int month, int day) {

        DatePickerFragment instance = new DatePickerFragment();

        Bundle args = new Bundle();

        args.putInt("year", year);
        args.putInt("month", month);

        args.putInt("day", day);

        instance.setArguments(args);

        return instance;

    }

    /**
     * 监听是否返回日期
     */
    public interface TheListener {
        public void returnDate(String date);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Calendar 是一个抽象类，是通过getInstance()来获得实例,设置成系统默认时间
        /*final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		Log.i("year",String.valueOf(year));
		Log.i("month",String.valueOf(month));
		Log.i("day",String.valueOf(day));*/

        Bundle args = getArguments();
        if (args != null) {
            year=args.getInt("year");
            month=args.getInt("month");
            day=args.getInt("day");
        }

        listener = (TheListener) getActivity();
        return new DatePickerDialog(getActivity(), this, year, month-1, day);
    }

    /**
     * 获取fragment中的日期
     *
     * @param view
     * @param year
     * @param month
     * @param day
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(c.getTime());
        if(isfired == true) return;//二次触发问题
        if (listener != null) {
            isfired = true;
            listener.returnDate(formattedDate);
        }
    }
}

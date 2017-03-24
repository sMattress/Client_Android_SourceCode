package com.wtf.utils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TransformDate {

	public static Date Str2Date(String str) throws ParseException{
		SimpleDateFormat sdf=new SimpleDateFormat( "yyyy-MM-dd" );
		Date date = sdf.parse(str);
		return date;
	}

	public static Date Str2Datetime(String strDate) throws ParseException{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	public static String Date2Str(Date date) {
		SimpleDateFormat sdf=new SimpleDateFormat( "yyyy-MM-dd kk:mm:ss" );
		String dateStr=sdf.format(date);
		return dateStr;
	}

	public static String Month2Str(Date date) {
		SimpleDateFormat sdf=new SimpleDateFormat( "MM-dd" );
		String dateStr=sdf.format(date);
		return dateStr;
	}

	public static boolean isSameDate(Date date1, Date date2){
		String str1=Date2Str(date1);
		String str2=Date2Str(date2);
		if(date1==null||date2==null){
			return false;
		}
		else if(str1.equals(str2)){
			return true;
		}
		else{
			return false;
		}
	}
}

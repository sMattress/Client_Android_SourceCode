package com.wtf.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.wtf.model.Param;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {
    /**
     * 检查是否存在SDCard
     *
     * @return
     */
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static void setPicToStore(Bitmap mBitmap) {
        // 检测sd是否可用
        if (hasSdcard()) {
            FileOutputStream fOut = null;
            // 图片名字
            String fileName = Environment.getExternalStorageDirectory()
                    + Param.FILE_NAME;
            File f = new File(fileName);
            try {
                Log.i("start store", "true");
                f.createNewFile();
                fOut = new FileOutputStream(f);
                if (fOut != null) {
                    mBitmap.compress(CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    Log.i("savepng", "sucess");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String convertIconToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(CompressFormat.PNG, 100, baos);
        byte[] icon = baos.toByteArray();// 转为byte数组
        return Base64.encodeToString(icon, Base64.DEFAULT);
    }

    public static <E> StringBuffer array2String(List<E> list) {
        StringBuffer sb = null;
        if (list != null && !list.isEmpty()) {
            sb = new StringBuffer();
            for (E element : list) {
                sb.append(element.toString()).append(" ");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb;
    }

    public static int[] StringtoInt(String str) {
        String arrayStr[] = str.split(" ");
        int array[] = new int[arrayStr.length];
        for (int i = 0; i < arrayStr.length; i++) {
            array[i] = Integer.parseInt(arrayStr[i]);
        }
        return array;
    }

    public static float[] StringtoFloat(String str) {
        String arrayStr[] = str.split(" ");
        float array[] = new float[arrayStr.length];
        for (int i = 0; i < arrayStr.length; i++) {
            array[i] = Float.parseFloat(arrayStr[i]);
        }
        return array;
    }

    public static int[] StateStringtoInt(String str) {
        String arrayStr[] = str.split(" ");
        int array[] = new int[arrayStr.length];
        for (int i = 0; i < arrayStr.length; i++) {
            int temp = Integer.parseInt(arrayStr[i]);
            if (temp == 4)
                array[i] = 4;
            if (temp == 3)
                array[i] = 3;
            if (temp == 2)
                array[i] = 2;
            if (temp == 1)
                array[i] = 1;
        }
        return array;
    }

    // 使用系统当前日期加以调整作为照片的名称
    public static String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    public static boolean validateCellphone(String cellphone) {
        // 正则表达式验证手机
        Pattern p = Pattern
                .compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(cellphone);
        return m.matches();
    }

    public static boolean validateDate(String strDate) {
        if (null == strDate || strDate.isEmpty()) {
            return false;
        } else {
            Date date = null;
            try {
                date = TransformDate.Str2Date(strDate);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Date current = new Date();
            if (date.getTime() > current.getTime()) {
                return false;
            } else {
                return true;
            }
        }
    }
}

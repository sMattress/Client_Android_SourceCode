package com.wtf.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Xml;


import com.wtf.model.UpdateData;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;

/**
 * Created by Hailey on 2016/4/25.
 */
public class VersionUtil {


    public static int getVersionCode(Context context) {
        int verCode = -1;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            verCode = context.getPackageManager().getPackageInfo("com.wtf", 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verCode;
    }

    public static String getVersionName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(
                    "com.wtf", 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }
}

package com.wtf.utils;

import android.content.Context;

import com.litesuits.http.LiteHttp;
import com.litesuits.http.data.FastJson;
import com.litesuits.http.impl.huc.HttpUrlClient;
import com.wtf.WTFApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by liyan on 2016/12/14.
 */

public class HttpUtil {
    public static LiteHttp liteHttp;
    public void initLiteHttp(Context context){
        liteHttp = LiteHttp.build(context)
                .setHttpClient(new HttpUrlClient())       // http client
                .setJsonConvertor(new FastJson())
    //            .setDebugged(true)                     // log output when debugged
                .setDetectNetwork(true)              // detect network before connect
                .setConnectTimeout(4_000)
                .setDefaultMaxRetryTimes(1)
                .create();
    }

    public static String getIP(String url) throws UnknownHostException {
        if (url.contains("http://")) {
            url = url.substring(7);
        }
        if (url.contains("https://")) {
            url = url.substring(8);
        }
        InetAddress ip = InetAddress.getByName(url);
        return ip.getHostAddress();
    }
}

package com.wtf.api.net;

import android.util.Log;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Http处理类
 * Created by Hailey on 2016/3/24.
 */
public class HttpUtil {
    private final static String TAG = "HttpUtil";
    private final static String REQUEST_GET = "GET";
    private final static String REQUEST_POST = "POST";
    private final static String ENCODE_TYPE = "UTF-8";
    private final static int TIME_OUT = 10000;

    //    private final static String SERVER_URL = "http://121.42.151.185:8080/SmartSleep_V1.0/Mobile/";
    private String method;
    private int which = 0;
    private static HttpUtil instance = null;

    private HttpUtil() {
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new HttpUtil();
        }
    }

    public static HttpUtil getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    public static String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset="
                + ENCODE_TYPE;
    }

    /**
     * GET方法
     *
     * @param method
     * @param paramsMap
     * @param typeOfT
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> T getHandle(String method, Map<String, String> paramsMap, Type typeOfT) throws IOException {
        this.method = method;
        String connectUrl;
        if (paramsMap == null) {
            connectUrl = method;
        } else {
            String data = joinParams(paramsMap);
            // 打印出请求
            Log.i(TAG, "request: " + data);
            connectUrl = method + "?" + data;
            Log.i(TAG, "connectUrl: " + connectUrl);
        }
        //String connectUrl = SERVER_URL + method + "?" + data;
        HttpURLConnection connection = getConnection(connectUrl, REQUEST_GET);
        connection.connect();
        if (connection.getResponseCode() == 200) {
            // 获取响应的输入流对象
            InputStream is = connection.getInputStream();
            // 返回字符串
            String result = getResultString(is,
                    ENCODE_TYPE);
            connection.disconnect();
            Gson gson = new Gson();
            try {
                return gson.fromJson(result, typeOfT);
            } catch (Exception e) {
               e.printStackTrace();
                return null;
            }
        } else {
            Log.i(TAG, "ResponseCode........: " + connection.getResponseCode());
            connection.disconnect();
            return null;
        }
    }

    /**
     * POST方法
     *
     * @param method
     * @param paramsMap
     * @param typeOfT
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> T postHandle(int which, String method, Map<String, String> paramsMap, Type typeOfT) throws IOException {
        this.which = which;
        this.method = method;
        String data;
        if (which==1) {
            data = getRequestData(paramsMap, ENCODE_TYPE).toString();//获得请求体
            /*data = "{\"account\":123,\"password\":1111}";*/
            /*Gson gson = new Gson();
            data = gson.toJson(paramsMap);*/
            Log.i("data.....", data);
        } else {
            data = joinParams(paramsMap);
        }
        //String connectUrl = SERVER_URL + method;
        String connectUrl = method;
        HttpURLConnection connection = getConnection(connectUrl, REQUEST_POST);
//        connection.setRequestProperty("Content-Type","application/json;charset=utf-8");
        //connection.setRequestProperty("content-type","application/x-www-form-urlencoded;charset=utf-8");
        //connection.setRequestProperty("content-length", String.valueOf(data.getBytes().length));
        connection.connect();
        OutputStream os = connection.getOutputStream();
        os.write(data.getBytes(ENCODE_TYPE));
        os.flush();
        Log.i(TAG, "send........:" + data);
        if (connection.getResponseCode() == 200) {
            // 获取响应的输入流对象
            InputStream is = connection.getInputStream();
            // 返回字符串
            String result = getResultString(is,
                    ENCODE_TYPE);
            // 打印出结果
            Log.i(TAG, "response........: " + result);
            connection.disconnect();
            Gson gson = new Gson();
            try {
                return gson.fromJson(result, typeOfT);
            } catch (Exception e) {
                return null;
            }
        } else {
            Log.i(TAG, "ResponseCode........: " + connection.getResponseCode());
            connection.disconnect();
            return null;
        }
    }
    /*
     * Function  :   封装请求体信息
     * Param     :   params请求体内容，encode编码格式
     */
    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
        try {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }


    /**
     * 得到返回值
     *
     * @param inputStream
     * @param encode
     * @return
     */
    private static String getResultString(InputStream inputStream, String encode) {
        // 创建字节输出流对象
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 定义缓冲区
        byte[] data = new byte[1024];
        // 定义读取的长度
        int len = 0;
        String result = "";
        if (inputStream != null) {
            try {
                // 按照缓冲区的大小，循环读取
                while ((len = inputStream.read(data)) != -1) {
                    // 根据读取的长度写入到os对象中
                    outputStream.write(data, 0, len);
                }
                // 释放资源
                inputStream.close();
                outputStream.close();
                result = new String(outputStream.toByteArray(), encode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取connection对象
     *
     * @param connectUrl
     * @param requestMethod
     * @return
     */
    private HttpURLConnection getConnection(String connectUrl, String requestMethod) {
        HttpURLConnection connection = null;
        // 初始化connection
        try {
            // 根据地址创建URL对象
            URL url = new URL(connectUrl);
            // 根据URL对象打开链接
            connection = (HttpURLConnection) url.openConnection();
            // 设置请求的方式
            connection.setRequestMethod(requestMethod);
            // 发送POST请求必须设置允许输入，默认为true
            connection.setDoInput(true);
            if (requestMethod.equals(REQUEST_POST)) {
                // 发送POST请求必须设置允许输出
                connection.setDoOutput(true);//创建输出流
                // Post请求不能使用缓存
                connection.setUseCaches(false);
                if (which == 1) {
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                } else {
                    connection.setRequestProperty("Content-Type", getBodyContentType());
                }
                connection.setRequestProperty("Connection", "keep-alive");
                connection.setRequestProperty("Response-Type", "json");
                connection.setChunkedStreamingMode(0);
            }
            // 设置请求的超时时间
            connection.setReadTimeout(TIME_OUT);
            connection.setConnectTimeout(TIME_OUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * 拼接参数列表
     *
     * @param paramsMap
     * @return
     */
    private String joinParams(Map<String, String> paramsMap) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : paramsMap.keySet()) {
            stringBuilder.append(key);
            stringBuilder.append("=");
            try {
                stringBuilder.append(URLEncoder.encode(paramsMap.get(key), ENCODE_TYPE));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            stringBuilder.append("&");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

}

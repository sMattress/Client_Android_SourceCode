package com.wtf.utils.wifi;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * Created by liyan on 2016/9/19.
 */
public class SocketTcp {

    public static final int timeOut = 0 * 1000;


    public static void main(String[] args) {
        InputStream is = null;
        OutputStream os = null;
        JSONObject sendJson = new JSONObject();
        JSONObject receiveJson = new JSONObject();
        Socket socket = null;
        BufferedReader in = null;
        String resultData = "";
        StringBuffer jsonBuffer = new StringBuffer();
        try {
            socket.setSoTimeout(timeOut);
            socket.setKeepAlive(false);
            // 链接本机
            InetAddress address = InetAddress.getLocalHost();
            // 端口号8080
            int port = 8080;
            // 创建socket链接
            // Socket socket = new Socket("127.0.0.1", port);
            socket = new Socket("10.10.100.254", port);
            System.out.println("客户机已启动。。。");
            // 获取输出流，向服务器发送数据
            os = socket.getOutputStream();


            try {
                sendJson.put("from", address);
                sendJson.put("cmd", 201);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            os.write(sendJson.toString().getBytes());
            System.out.println("客户端的发送信息：" + sendJson.toString().getBytes());

            // 接收服务器反馈的数据

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            if (in.ready()) {
                while ((resultData = in.readLine()) != null) {
                    jsonBuffer.append(resultData);
                }
                System.out.println("StringBuffer:" + jsonBuffer.toString());


                System.out.println("resultData:" + resultData);
                if (resultData != null && !"".equals(resultData) && resultData.charAt(0) == '{') {
                    jsonBuffer.append(resultData);
                }
                try {
                    receiveJson = new JSONObject(jsonBuffer.toString());
                    String from = receiveJson.getString("from");
                    String to = receiveJson.getString("to");
                    String deviceId = receiveJson.getJSONObject("params").getString("deviceId");
                    String softVersion = receiveJson.getJSONObject("params").getString("softVersion");
                    String hardVersion = receiveJson.getJSONObject("params").getString("hardVersion");

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                System.out.println("服务器的反馈信息：" + receiveJson);
            }
        } catch (UnknownHostException e) {
            // do nothing
        } catch (IOException e) {

        } finally {
            // 关闭链接
            try {
                is.close();
                os.close();
                socket.close();
            } catch (IOException e) {
                // do nothing
            }

        }

    }

}



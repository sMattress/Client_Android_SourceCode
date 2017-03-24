package com.wtf.utils;

import android.content.IntentFilter;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.wtf.WTFApplication;
import com.wtf.activity.MainActivity;
import com.wtf.model.AppMsg;
import com.wtf.model.Param;

import wtf.socket.WTFSocketConfig;
import wtf.socket.WTFSocketEventListener;
import wtf.socket.WTFSocketHandler;
import wtf.socket.WTFSocketMsg;
import wtf.socket.WTFSocketSession;
import wtf.socket.WTFSocketSessionFactory;

/**
 * Created by liyan on 2017/3/9.
 */

public class SocketConnection {

    public static void registerSocket(){
        String localName = WTFApplication.userData.getAccount();
        WTFSocketSessionFactory.reInit();
        if (!WTFSocketSessionFactory.isAvailable()) {
            try {
                String ip= HttpUtil.getIP("smartmattress.lesmarthome.com");
                Log.i("IP",ip);
                WTFSocketSessionFactory.init(
                        new WTFSocketConfig()
                                .setIp(ip)
                                .setPort(Param.SERVER_PORT)
                                .setLocalName(localName)
                                .setUseHeartbeat(Param.SERVER_HEART_CHECK)
                                .setHeartbeatBreakTime(3)
                                .setHeartbeatPeriod(180_000)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }


            WTFSocketSessionFactory.clearEventListeners(null);
            WTFSocketSessionFactory.addEventListener(new WTFSocketEventListener() {
                @Override
                public void onDisconnect() {
                    WTFSocketSessionFactory.reInit();
                }

                @Override
                public void onConnect() {
                    WTFSocketMsg registerMsg = new WTFSocketMsg();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("deviceType", "Android");
                    registerMsg.setBody(new AppMsg().setCmd(64).addParam(jsonObject));

                    WTFSocketSessionFactory.SERVER.sendMsg(registerMsg, new WTFSocketHandler() {
                        @Override
                        public boolean onReceive(WTFSocketSession session, WTFSocketMsg msg) {
                            AppMsg body = msg.getBody(AppMsg.class);
                            if (body.getFlag() != 1) {
                                System.out.println("注册失败");
                            } else {
                                System.out.println("注册成功");
                            }
                            return true;
                        }
                    });
                }
            });
        }
/*        intentFilter = new IntentFilter();
        //addAction
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceive = new NetworkChangeReceive();
        registerReceiver(networkChangeReceive, intentFilter);*/
    }
}

package com.wtf.utils;

import wtf.socket.WTFSocketSessionFactory;

/**
 * Created by liyan on 2017/10/8.
 */

public class WifiThread extends Thread{
        @Override
        public void run() {
            synchronized (this) {
                System.out.println("WifiThread");
                if(WTFSocketSessionFactory.isAvailable()) {
                    WTFSocketSessionFactory.reInit();
                }else {
                    SocketConnection.registerSocket();
                }
            }
    }
}

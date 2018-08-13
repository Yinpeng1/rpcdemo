package com.yp.rpcdemo.test;

import com.yp.rpcdemo.service.center.Server;
import com.yp.rpcdemo.service.center.ServerCenter;
import com.yp.rpcdemo.service.provider.ProvicerImpl;
import com.yp.rpcdemo.service.provider.Provider;
import com.yp.rpcdemo.service.provider.Provider2;
import com.yp.rpcdemo.service.provider.Provider2Impl;
import com.yp.rpcdemo.util.RPCClient;

import java.io.IOException;
import java.net.InetSocketAddress;

public class RPCTest {

    public static void main(String[] args) throws IOException {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Server serviceServer = new ServerCenter(8088);
                    serviceServer.register(Provider.class, ProvicerImpl.class);
                    serviceServer.register(Provider2.class, Provider2Impl.class);
                    serviceServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Provider service = RPCClient.getRemoteProxyObj(Provider.class, new InetSocketAddress("localhost", 8088));
        System.out.println(service.sayHi("test"));
    }
}

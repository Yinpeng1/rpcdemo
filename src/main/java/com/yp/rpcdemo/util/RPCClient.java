package com.yp.rpcdemo.util;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RPCClient<T> {

    public static <T> T getRemoteProxyObj(final Class<?> serviceInterface, final InetSocketAddress addr) {
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Socket socket = null;
                ObjectOutputStream output = null;
                ObjectInputStream input = null;
                BufferedWriter bw = null;
                OutputStreamWriter outputStreamWriter = null;
                try {
                    // 2.创建Socket客户端，根据指定地址连接远程服务提供者
                    socket = new Socket();
                    socket.connect(addr);

                    // 3.将远程服务调用所需的接口类、方法名、参数列表等编码后发送给服务提供者
//                    JSONObject jsonObject = new JSONObject();
//                    jsonObject.put("serviceName", serviceInterface.getName());
//                    jsonObject.put("methodName", method.getName());
//                    jsonObject.put("parameterTypes", method.getParameterTypes());
//                    jsonObject.put("arguments", args);
//                    outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
//                    bw = new BufferedWriter(outputStreamWriter);
//                    bw.write(jsonObject.toJSONString());
                    output = new ObjectOutputStream(socket.getOutputStream());
                    output.writeUTF(serviceInterface.getName());
                    output.writeUTF(method.getName());
                    output.writeObject(method.getParameterTypes());
                    output.writeObject(args);

                    // 4.同步阻塞等待服务器返回应答，获取应答后返回
                    input = new ObjectInputStream(socket.getInputStream());
                    return input.readObject();
                } finally {
                    if (socket != null) socket.close();
                    if (outputStreamWriter != null) outputStreamWriter.close();
                    if (input != null) input.close();
                    if (bw != null) bw.close();
                }
            }
        });
    }
}

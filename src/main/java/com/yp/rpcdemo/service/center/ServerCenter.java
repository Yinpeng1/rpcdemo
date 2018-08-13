package com.yp.rpcdemo.service.center;

import com.alibaba.fastjson.JSONObject;
import com.yp.rpcdemo.entity.Entity;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerCenter implements Server {

    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static Lock lock = new ReentrantLock();

    private static final HashMap<String, Class> serviceRegistry = new HashMap<String, Class>();

    private static boolean isRunning = false;

    private static int port;

    public ServerCenter(int port1) {
        port = port1;
    }

    @Override
    public void stop() {
        isRunning = false;
        executor.shutdown();
    }

    @Override
    public void start() throws IOException {
        ServerSocket server = new ServerSocket();
        server.bind(new InetSocketAddress(port));
        System.out.println("start server");
        try {
            while (true){
                executor.execute(new ServiceTask(server.accept()));
//                System.out.println(serviceRegistry.size());
//                serviceRegistry.entrySet().forEach( t ->{
//                    System.out.println(t.getKey() + "-----" + t.getValue());
//                });
            }
        } finally {
            server.close();
        }
    }

    @Override
    public void register(Class serviceInterface, Class impl) {
        lock.lock();
        serviceRegistry.put(serviceInterface.getName(), impl);
        lock.unlock();
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPort() {
        return port;
    }

    private static class ServiceTask implements Runnable{
        Socket client = null;

        public ServiceTask(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            ObjectInputStream input = null;
            ObjectOutputStream output = null;

            try {
//                System.out.println(Inputstr2Str_Reader(client.getInputStream(), null));
//                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
//                String message = br.readLine();
//                System.out.println(message);
//                Entity entity = JSONObject.parseObject(Inputstr2Str_Reader(client.getInputStream(), "utf-8"), Entity.class);
//                Entity entity = JSONObject.parseObject(message, Entity.class);
//                System.out.println(entity);

                input = new ObjectInputStream(client.getInputStream());
                String serviceName = input.readUTF();
//                String serviceName = entity.getServiceName();
                String methodName = input.readUTF();
//                String methodName = entity.getMethodName();

                Class<?>[] parameterTypes = (Class<?>[])input.readObject();
//                Class<?>[] parameterTypes = entity.getParameterTypes();
                Object[] arguments = (Object[]) input.readObject();
//                Object[] arguments = entity.getArguments();
                Class serviceClass = serviceRegistry.get(serviceName);
                if (serviceClass == null) {
                    throw new ClassNotFoundException(serviceName + " not found");
                }
                Method method = serviceClass.getMethod(methodName, parameterTypes);
                Object result = method.invoke(serviceClass.newInstance(), arguments);

                // 3.将执行结果反序列化，通过socket发送给客户端
                output = new ObjectOutputStream(client.getOutputStream());
                output.writeObject(result);
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                if (output != null){
                    try {
                        output.close();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}

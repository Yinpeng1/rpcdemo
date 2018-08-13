package com.yp.rpcdemo.service.provider;

public class ProvicerImpl implements Provider{

    @Override
    public String sayHi(String name) {
        return "Hi, " + name;
    }
}

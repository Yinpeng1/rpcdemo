package com.yp.rpcdemo.service.provider;

public class Provider2Impl implements Provider2 {

    @Override
    public String sayHi2(String name) {
        return "sorry," + name;
    }
}

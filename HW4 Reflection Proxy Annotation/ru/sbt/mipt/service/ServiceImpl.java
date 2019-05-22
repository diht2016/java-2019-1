package ru.sbt.mipt.service;

import java.util.Date;
import java.util.List;

import ru.sbt.mipt.cache.CacheProxy;

public class ServiceImpl implements Service {
    @Override
    public List<String> run(String item, double value, Date date) {
        return null;
    }

    @Override
    public List<String> work(String item) {
        return null;
    }
    public static void main(String[] args) {
        CacheProxy cacheProxy = new CacheProxy("./cached-results/");
        Service service = cacheProxy.cache(new ServiceImpl());
        List<String> result1 = service.run("asd", 2.5, new Date());
        System.out.println(result1);
        List<String> result2 = service.work("asd");
        System.out.println(result2);
    }
}

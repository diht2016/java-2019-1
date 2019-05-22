package ru.sbt.mipt.cache;

import java.lang.reflect.Proxy;

public class CacheProxy {
    private final String cachePath;

    public CacheProxy(String cachePath) {
        this.cachePath = cachePath;
    }

    public <T> T cache(T object) {
        Class objectClass = object.getClass();
        @SuppressWarnings("unchecked")
        T wrapped = (T) Proxy.newProxyInstance(
                objectClass.getClassLoader(),
                new Class[] { objectClass },
                new CacheHandler<>(object, cachePath));
        return wrapped;
    }
}
